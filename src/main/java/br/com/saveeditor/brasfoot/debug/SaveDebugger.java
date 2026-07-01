package br.com.saveeditor.brasfoot.debug;

import br.com.saveeditor.brasfoot.domain.NavegacaoState;
import br.com.saveeditor.brasfoot.service.BrasfootGameLibraryService;
import br.com.saveeditor.brasfoot.service.SaveFileService;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Explorador/debugger para saves do Brasfoot.
 *
 * Objetivo: carregar um .s22 via Kryo, navegar o grafo de objetos serializado e
 * cruzar strings/numeros com os arquivos originais extraidos em brasfoot_files/.
 *
 * Uso rapido:
 *   mvn -q -DskipTests package
 *   mvn -q exec:java -Dexec.mainClass=br.com.saveeditor.brasfoot.debug.SaveDebugger \
 *     -Dexec.args="~/Documentos/Brasfoot22-23/sav/not.s22 ~/Documentos/brasfoot-save-editor/brasfoot_files"
 */
public final class SaveDebugger {
    private static final Path DEFAULT_SAVE = Path.of(System.getProperty("user.home"), "Documentos/Brasfoot22-23/sav/not.s22");
    private static final Path DEFAULT_BRASFOOT_FILES = Path.of(System.getProperty("user.home"), "Documentos/brasfoot-save-editor/brasfoot_files");
    private static final int MAX_LIST_PREVIEW = 30;
    private static final int MAX_SCAN_NODES = 500_000;
    private static final int MAX_REF_FILES = 2_000;
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[\\p{L}\\p{N}][\\p{L}\\p{N} ._'-]{2,}");

    private final NavegacaoState state;
    private final Path savePath;
    private final Path brasfootFilesPath;
    private final ReferenceIndex references;
    private Object current;
    private final ArrayDeque<Crumb> stack = new ArrayDeque<>();

    private SaveDebugger(NavegacaoState state, Path savePath, Path brasfootFilesPath, ReferenceIndex references) {
        this.state = state;
        this.savePath = savePath;
        this.brasfootFilesPath = brasfootFilesPath;
        this.references = references;
        this.current = state.getObjetoRaiz();
        this.stack.push(new Crumb("root", current));
    }

    public static void main(String[] args) throws Exception {
        Path save = args.length >= 1 ? expandPath(args[0]) : DEFAULT_SAVE;
        Path refs = args.length >= 2 ? expandPath(args[1]) : DEFAULT_BRASFOOT_FILES;

        byte[] payload = Files.readAllBytes(save);
        SaveFileService service = new SaveFileService(new BrasfootGameLibraryService());
        NavegacaoState state = service.restoreFromSnapshot(payload, save.toString());
        ReferenceIndex index = ReferenceIndex.load(refs);

        SaveDebugger debugger = new SaveDebugger(state, save, refs, index);
        debugger.printBanner();
        debugger.repl();
    }

    private static Path expandPath(String raw) {
        if (raw.equals("~")) {
            return Path.of(System.getProperty("user.home"));
        }
        if (raw.startsWith("~/")) {
            return Path.of(System.getProperty("user.home"), raw.substring(2));
        }
        return Path.of(raw).toAbsolutePath().normalize();
    }

    private void printBanner() {
        System.out.println("Brasfoot Save Debugger");
        System.out.println("save: " + savePath);
        System.out.println("brasfoot_files: " + brasfootFilesPath);
        System.out.println("root: " + describeOneLine(state.getObjetoRaiz()));
        System.out.println("dataAfQ: " + describeOneLine(state.getDataAfQ()));
        System.out.println("referencias indexadas: " + references.fileCount + " arquivos texto, " + references.lineCount + " linhas");
        System.out.println("Digite 'help' para comandos. Comece com: ls, scan, plausible, search <texto>, cd aj, cd nd[0]");
    }

    private void repl() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print(pathPrompt() + "> ");
                if (!scanner.hasNextLine()) {
                    break;
                }
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }
                try {
                    if (!handleCommand(line)) {
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("ERRO: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                }
            }
        }
    }

    private boolean handleCommand(String line) throws Exception {
        String[] parts = line.split("\\s+", 2);
        String cmd = parts[0].toLowerCase(Locale.ROOT);
        String arg = parts.length > 1 ? parts[1].trim() : "";
        switch (cmd) {
            case "help" -> printHelp();
            case "pwd" -> System.out.println(pathPrompt());
            case "ls" -> listCurrent(arg);
            case "fields" -> listFields(current, true);
            case "cd" -> cd(arg);
            case "up" -> up();
            case "root" -> goRoot();
            case "show" -> showCurrent();
            case "dump" -> dumpCurrent(arg);
            case "scan" -> scan(arg);
            case "engine", "motor" -> engine();
            case "team", "time" -> team(arg);
            case "league", "liga", "table", "tabela" -> league(arg);
            case "plausible" -> plausible(arg);
            case "search" -> search(arg);
            case "refs" -> refs(arg);
            case "quit", "exit", "q" -> { return false; }
            default -> System.out.println("Comando desconhecido. Use 'help'.");
        }
        return true;
    }

    private void printHelp() {
        System.out.println("Comandos:");
        System.out.println("  ls [limite]             lista campos/itens do objeto atual");
        System.out.println("  fields                  lista campos declarados + tipo + valor curto");
        System.out.println("  cd <campo|[i]|path>     navega: cd aj, cd aj[0].nd[3], cd [12]");
        System.out.println("  up | root | pwd         navegação");
        System.out.println("  show                    mostra tipo, valor curto e plausibilidade do atual");
        System.out.println("  dump [profundidade]     imprime árvore pequena do atual");
        System.out.println("  scan [termo]            varre grafo e mostra classes/campos/strings; com termo filtra");
        System.out.println("  engine                  mostra mapa atual do motor de partida encontrado no bytecode");
        System.out.println("  team <nome|path>        lista jogadores/campos-chave de um time: team Cruzeiro ou team root.aj[0]");
        System.out.println("  league <termo>          acha uma liga por nome/pais e imprime tabela: pts, jogos, V/E/D, GP/GC/SG");
        System.out.println("  plausible [limite]      candidatos plausíveis no subgrafo atual (strings em brasfoot_files e numeros em faixas)");
        System.out.println("  search <texto>          procura texto no save e cruza com brasfoot_files");
        System.out.println("  refs <texto>            procura só em brasfoot_files");
        System.out.println("  quit                    sai");
    }

    private String pathPrompt() {
        return stack.descendingIterator().next().name.equals("root") && stack.size() == 1
                ? "root"
                : stack.stream().map(c -> c.name).collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
                    java.util.Collections.reverse(list);
                    return String.join(".", list).replace(".[", "[");
                }));
    }

    private void listCurrent(String arg) throws IllegalAccessException {
        int limit = parseIntOr(arg, MAX_LIST_PREVIEW);
        if (current == null) {
            System.out.println("null");
            return;
        }
        Class<?> type = current.getClass();
        System.out.println(describeOneLine(current));
        if (current instanceof List<?> list) {
            for (int i = 0; i < Math.min(limit, list.size()); i++) {
                System.out.printf("  [%d] %s%n", i, describeOneLine(list.get(i)));
            }
            if (list.size() > limit) System.out.println("  ... +" + (list.size() - limit) + " itens");
            return;
        }
        if (type.isArray()) {
            int len = Array.getLength(current);
            for (int i = 0; i < Math.min(limit, len); i++) {
                System.out.printf("  [%d] %s%n", i, describeOneLine(Array.get(current, i)));
            }
            if (len > limit) System.out.println("  ... +" + (len - limit) + " itens");
            return;
        }
        listFields(current, false);
    }

    private void listFields(Object obj, boolean verbose) throws IllegalAccessException {
        if (obj == null || isLeaf(obj)) {
            System.out.println(describeOneLine(obj));
            return;
        }
        for (Field field : allFields(obj.getClass())) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            field.setAccessible(true);
            Object value = field.get(obj);
            String plausible = verbose ? " | " + plausibleFor(value) : "";
            System.out.printf("  %-12s %-24s = %s%s%n", field.getName(), simpleType(field.getType()), describeOneLine(value), plausible);
        }
    }

    private void cd(String arg) throws Exception {
        if (arg.isBlank()) {
            System.out.println("Uso: cd <campo|[i]|path>");
            return;
        }
        Object target = resolvePath(current, arg);
        current = target;
        stack.push(new Crumb(arg, current));
        showCurrent();
    }

    private void up() {
        if (stack.size() <= 1) {
            System.out.println("Ja esta no root.");
            return;
        }
        stack.pop();
        current = stack.peek().value;
        showCurrent();
    }

    private void goRoot() {
        while (stack.size() > 1) stack.pop();
        current = stack.peek().value;
        showCurrent();
    }

    private void showCurrent() {
        System.out.println(describeOneLine(current));
        System.out.println("plausibilidade: " + plausibleFor(current));
    }

    private void dumpCurrent(String arg) {
        int depth = parseIntOr(arg, 2);
        dump(current, "", depth, new IdentityHashMap<>());
    }

    private void scan(String arg) throws IllegalAccessException {
        String filter = normalize(arg);
        ScanStats stats = new ScanStats();
        walk(current, pathPrompt(), 0, stats, (path, value) -> {
            stats.observe(path, value);
            if (!filter.isBlank() && normalize(describeOneLine(value)).contains(filter)) {
                System.out.println(path + " = " + describeOneLine(value) + " | " + plausibleFor(value));
            }
        });
        stats.print();
    }

    private void plausible(String arg) throws IllegalAccessException {
        int limit = parseIntOr(arg, 120);
        List<Candidate> candidates = new ArrayList<>();
        ScanStats ignored = new ScanStats();
        walk(current, pathPrompt(), 0, ignored, (path, value) -> {
            String plaus = plausibleFor(value);
            if (!plaus.equals("-") && !plaus.startsWith("colecao") && !plaus.startsWith("objeto")) {
                candidates.add(new Candidate(path, describeOneLine(value), plaus));
            }
        });
        candidates.stream().limit(limit).forEach(c -> System.out.printf("%s = %s | %s%n", c.path, c.value, c.reason));
        if (candidates.size() > limit) System.out.println("... +" + (candidates.size() - limit) + " candidatos");
    }

    private void search(String arg) throws IllegalAccessException {
        if (arg.isBlank()) {
            System.out.println("Uso: search <texto>");
            return;
        }
        String needle = normalize(arg);
        List<Candidate> hits = new ArrayList<>();
        ScanStats ignored = new ScanStats();
        walk(state.getObjetoRaiz(), "root", 0, ignored, (path, value) -> {
            if (value instanceof String s && normalize(s).contains(needle)) {
                hits.add(new Candidate(path, quote(s), references.explain(s)));
            }
        });
        hits.stream().limit(200).forEach(c -> System.out.printf("%s = %s | %s%n", c.path, c.value, c.reason));
        if (hits.isEmpty()) System.out.println("Nada encontrado no save.");
        System.out.println("-- brasfoot_files --");
        refs(arg);
    }

    private void refs(String arg) {
        if (arg.isBlank()) {
            System.out.println("Uso: refs <texto>");
            return;
        }
        List<ReferenceHit> hits = references.search(arg, 80);
        if (hits.isEmpty()) {
            System.out.println("Nada encontrado em brasfoot_files.");
            return;
        }
        for (ReferenceHit hit : hits) {
            System.out.printf("%s:%d: %s%n", hit.file, hit.lineNumber, hit.line);
        }
    }


    private void engine() {
        System.out.println("Mapa do motor de partida (derivado de javap em brasfoot_files/*.class):");
        System.out.println("  Classe principal: best.I");
        System.out.println("    fz/hc = mandante, fA/hd = visitante, fB/hu = gols mandante, fC/hw = gols visitante");
        System.out.println("    fN/hE = eventos da partida (best.A)");
        System.out.println("    fF/hl e fG/hm = listas do mandante/visitante; fJ/hp e fK/hq = jogadores em campo/ofensivos");
        System.out.println("    fW/hz = força/meio/campo por lado usada em eventos 10..17");
        System.out.println("    gb/hB = força/pressão ofensiva por lado usada em eventos 1..13");
        System.out.println("    fY/hA e fZ/hZ aparecem no cálculo de nota do goleiro");
        System.out.println("  Geração de eventos: best.I.a(best.I,int minuto,int fase)");
        System.out.println("    escolhe lado por Random.nextInt(100): >55 mandante, <=55 visitante");
        System.out.println("    evento 3/2 = gol/chance convertida por best.I.b(...) / best.I.a(...)");
        System.out.println("    evento 5 = lance/ocorrência escolhendo best.I.I(lista)");
        System.out.println("  Evento: best.A");
        System.out.println("    dy/cu = time do evento, w/b = lado, dz/el = tipo do evento, dA/em = minuto, dB/en = detalhe");
        System.out.println("    dC/eo = jogador principal/autor, dD/ep = jogador secundário");
        System.out.println("  Jogador: best.F");
        System.out.println("    dm/getNome = nome, en/getPosicao = posição, eq/fi = força/base usada na nota");
        System.out.println("    eR/fT = faixa/slot do jogador em campo; se <1 recebe default por posição [1,2,7,15,23]");
        System.out.println("    eN/gk = nota/avaliação pós-jogo calculada em best.F.a(best.at,best.I,int,int,best.ah)");
        System.out.println("    et/fj = salário confirmado; eu/fk = valor/passe provável; eA..eG/gK..gP = atributos/contadores úteis a investigar");
        System.out.println("  Time: best.ah");
        System.out.println("    dm/getNome = nome, mV/jY = slug, nd/kc = jogadores, nb/kb = dinheiro, nc/getReputacao = reputação");
        System.out.println();
        System.out.println("Estratégia mais promissora pelo save/editor: aumentar força/base dos jogadores em campo (best.F.eq/fi), reputação/nível do time e manter elenco sem suspensões/lesões, em vez de mexer só na tabela já jogada.");
        System.out.println("Use: team <nome> para ver candidatos e paths; fields em jogadores para editar/confirmar campos.");
    }

    private void team(String arg) throws Exception {
        if (arg.isBlank()) {
            System.out.println("Uso: team <nome|path>. Ex: team Cruzeiro | team root.aj[0] | team root.ao[1].ds[0].YL.Zb[0]");
            return;
        }
        Object team;
        String path;
        if (arg.startsWith("root")) {
            path = arg;
            team = resolvePath(state.getObjetoRaiz(), arg.replaceFirst("^root\\.?", ""));
        } else {
            Candidate found = findTeamByName(arg);
            if (found == null) {
                System.out.println("Time não encontrado no save: " + arg);
                return;
            }
            path = found.path;
            team = resolvePath(state.getObjetoRaiz(), path.replaceFirst("^root\\.?", ""));
        }
        if (team == null || !team.getClass().getName().equals("best.ah")) {
            System.out.println("Path não aponta para best.ah/time: " + describeOneLine(team));
            return;
        }
        System.out.printf("Time: %s | path=%s | dinheiro(nb/kb)=%s | reputacao(nc)=%s | nivel(hA)=%s%n",
                teamName(team), path, fieldValue(team, "nb"), fieldValue(team, "nc"), fieldValue(team, "hA"));
        Object playersValue = fieldValue(team, "nd");
        if (!(playersValue instanceof List<?> players)) {
            System.out.println("Campo nd não é lista: " + describeOneLine(playersValue));
            return;
        }
        System.out.printf("%3s  %-28s %3s %5s %6s %8s %8s %5s %5s %5s%n", "#", "Jogador", "pos", "idade", "forca", "salario", "valor?", "slot", "nota", "status");
        for (int i = 0; i < players.size(); i++) {
            Object player = players.get(i);
            System.out.printf("%3d  %-28s %3s %5s %6s %8s %8s %5s %5s %5s  %s.nd[%d]%n",
                    i,
                    trim(String.valueOf(fieldValue(player, "dm")), 28),
                    fieldValue(player, "en"),
                    fieldValue(player, "em"),
                    fieldValue(player, "eq"),
                    fieldValue(player, "et"),
                    fieldValue(player, "eu"),
                    fieldValue(player, "eR"),
                    fieldValue(player, "eN"),
                    fieldValue(player, "status"),
                    path,
                    i);
        }
    }

    private Candidate findTeamByName(String arg) throws Exception {
        String needle = normalize(arg);
        Object root = state.getObjetoRaiz();
        Object teamsValue;
        try {
            teamsValue = fieldValue(root, "aj");
        } catch (ReflectiveOperationException e) {
            return null;
        }
        if (!(teamsValue instanceof List<?> teams)) return null;
        Candidate partial = null;
        for (int i = 0; i < teams.size(); i++) {
            Object team = teams.get(i);
            if (team == null || !team.getClass().getName().equals("best.ah")) continue;
            String name = teamName(team);
            String slug = stringFieldOr(team, "mV", "");
            String hay = normalize(name + " " + slug);
            Candidate c = new Candidate("root.aj[" + i + "]", describeOneLine(team), name + " / " + slug);
            if (normalize(name).equals(needle) || normalize(slug).equals(needle)) return c;
            if (partial == null && hay.contains(needle)) partial = c;
        }
        return partial;
    }

    private void league(String arg) throws Exception {
        String needle = normalize(arg.isBlank() ? "bundesliga alemanha" : arg);
        List<Candidate> candidates = findLeagueCandidates(needle);

        if (candidates.isEmpty()) {
            System.out.println("Nenhuma liga f.s encontrada para: " + arg);
            System.out.println("Dica: tente 'league alemanha', 'league bundesliga' ou navegue até root.ao[0].ds[0].YL");
            return;
        }

        Candidate chosen = candidates.get(0);
        if (candidates.size() > 1) {
            System.out.println("Ligas candidatas (usando a primeira):");
            candidates.stream().limit(12).forEach(c -> System.out.printf("  %s -> %s%n", c.path, c.reason));
        }

        Object league = resolvePath(state.getObjetoRaiz(), chosen.path.replaceFirst("^root\\.?", ""));
        printLeagueTable(chosen.path, league);
    }

    private List<Candidate> findLeagueCandidates(String needle) throws Exception {
        List<Candidate> candidates = new ArrayList<>();
        addLeagueCandidate(candidates, pathPrompt(), current, needle);

        Object root = state.getObjetoRaiz();
        for (String listName : List.of("ao", "ap")) {
            Object listValue;
            try {
                listValue = fieldValue(root, listName);
            } catch (ReflectiveOperationException e) {
                continue;
            }
            if (!(listValue instanceof List<?> countries)) continue;
            for (int countryIndex = 0; countryIndex < countries.size(); countryIndex++) {
                Object country = countries.get(countryIndex);
                Object divisionsValue;
                try {
                    divisionsValue = fieldValue(country, "ds");
                } catch (ReflectiveOperationException e) {
                    continue;
                }
                if (!(divisionsValue instanceof List<?> divisions)) continue;
                for (int divisionIndex = 0; divisionIndex < divisions.size(); divisionIndex++) {
                    Object division = divisions.get(divisionIndex);
                    try {
                        addLeagueCandidate(candidates, String.format("root.%s[%d].ds[%d].YL", listName, countryIndex, divisionIndex), fieldValue(division, "YL"), needle);
                        addLeagueCandidate(candidates, String.format("root.%s[%d].ds[%d].ZU", listName, countryIndex, divisionIndex), fieldValue(division, "ZU"), needle);
                    } catch (ReflectiveOperationException ignored) {
                        // divisao sem essas fases
                    }
                }
            }
        }
        return candidates;
    }

    private void addLeagueCandidate(List<Candidate> candidates, String path, Object value, String needle) {
        if (value == null || !value.getClass().getName().equals("f.s")) return;
        String label = leagueLabel(value);
        if (needle.isBlank() || normalize(label).contains(needle) || containsAllTerms(label, needle)) {
            boolean alreadyAdded = candidates.stream().anyMatch(c -> c.path.equals(path));
            if (!alreadyAdded) candidates.add(new Candidate(path, describeOneLine(value), label));
        }
    }

    private void printLeagueTable(String path, Object league) throws Exception {
        Object teamsValue = fieldValue(league, "Zb");
        if (!(teamsValue instanceof List<?> teams)) {
            System.out.println("Liga sem campo Zb/List de times: " + describeOneLine(teamsValue));
            return;
        }

        System.out.println("Liga: " + leagueLabel(league));
        System.out.println("Path: " + path);
        System.out.println("Mapeamento confirmado neste save:");
        System.out.println("  best.ah.c(f.s) -> best.ak estatisticas da equipe nesta liga");
        System.out.println("  best.ah.d(f.s) -> int[] [pts,jogos,vitorias,empates,derrotas,gp,gc,sg]");
        System.out.println("  best.ak: nT/lD=pontos, T/w=jogos, bX/cm=vitorias, d/co=derrotas, nK/ls=GP, nL/lt=GC");
        System.out.println();
        System.out.printf("%3s  %-28s %4s %3s %3s %3s %3s %4s %4s %4s  %-18s%n", "#", "Time", "Pts", "J", "V", "E", "D", "GP", "GC", "SG", "team path");
        for (int i = 0; i < teams.size(); i++) {
            Object team = teams.get(i);
            int[] stats = teamLeagueStats(team, league);
            String teamPath = String.format("%s.Zb[%d]", path, i);
            System.out.printf("%3d  %-28s %4d %3d %3d %3d %3d %4d %4d %4d  %-18s%n",
                    i + 1, trim(teamName(team), 28), stats[0], stats[1], stats[2], stats[3], stats[4], stats[5], stats[6], stats[7], teamPath);
        }
    }

    private int[] teamLeagueStats(Object team, Object league) throws Exception {
        try {
            Method d = team.getClass().getDeclaredMethod("d", league.getClass());
            d.setAccessible(true);
            Object raw = d.invoke(team, league);
            if (raw != null && raw.getClass().isArray() && Array.getLength(raw) >= 8) {
                int[] out = new int[8];
                for (int i = 0; i < out.length; i++) out[i] = ((Number) Array.get(raw, i)).intValue();
                return out;
            }
        } catch (ReflectiveOperationException ignored) {
            // cai para best.ak
        }

        Method c = team.getClass().getDeclaredMethod("c", league.getClass());
        c.setAccessible(true);
        Object stat = c.invoke(team, league);
        int pts = intField(stat, "nT");
        int jogos = intField(stat, "T");
        int vitorias = intField(stat, "bX");
        int derrotas = intField(stat, "d");
        int gp = intField(stat, "nK");
        int gc = intField(stat, "nL");
        int empates = jogos - vitorias - derrotas;
        int sg = gp - gc;
        return new int[] {pts, jogos, vitorias, empates, derrotas, gp, gc, sg};
    }

    private String leagueLabel(Object league) {
        String nomeLiga = stringFieldOr(league, "nomeLiga", "");
        String nomeDivisao = stringFieldOr(league, "nomeDivisao", "");
        String nome = stringFieldOr(league, "nome", "");
        String label = (nomeLiga + " - " + nomeDivisao).trim();
        if (label.equals("-") || label.isBlank()) label = nome;
        return label.replaceAll("\\s+", " ").trim();
    }

    private static boolean containsAllTerms(String value, String normalizedTerms) {
        String norm = normalize(value);
        for (String term : normalizedTerms.split("\\s+")) {
            if (!term.isBlank() && !norm.contains(term)) return false;
        }
        return true;
    }

    private static String teamName(Object team) {
        try {
            Object name = fieldValue(team, "dm");
            return String.valueOf(name);
        } catch (Exception ignored) {
            return describeOneLine(team);
        }
    }

    private static String trim(String value, int max) {
        if (value == null) return "";
        return value.length() <= max ? value : value.substring(0, Math.max(0, max - 1)) + "…";
    }

    private static int intField(Object obj, String name) throws ReflectiveOperationException {
        Object value = fieldValue(obj, name);
        return value instanceof Number n ? n.intValue() : 0;
    }

    private static String stringFieldOr(Object obj, String name, String fallback) {
        try {
            Object value = fieldValue(obj, name);
            return value == null ? fallback : String.valueOf(value);
        } catch (ReflectiveOperationException e) {
            return fallback;
        }
    }

    private static Object fieldValue(Object obj, String name) throws ReflectiveOperationException {
        Field field = findField(obj.getClass(), name);
        field.setAccessible(true);
        return field.get(obj);
    }

    private Object resolvePath(Object base, String path) throws Exception {
        Object cursor = base;
        if (path == null || path.isBlank()) return cursor;
        for (String segment : splitPath(path)) {
            cursor = resolveSegment(cursor, segment);
        }
        return cursor;
    }

    private static List<String> splitPath(String path) {
        List<String> segments = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int bracket = 0;
        for (char c : path.toCharArray()) {
            if (c == '[') bracket++;
            if (c == ']') bracket--;
            if (c == '.' && bracket == 0) {
                if (!current.isEmpty()) segments.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        if (!current.isEmpty()) segments.add(current.toString());
        return segments;
    }

    private Object resolveSegment(Object obj, String segment) throws Exception {
        if (obj == null) throw new IllegalArgumentException("objeto atual é null");
        Matcher matcher = Pattern.compile("([^\\[]*)?(?:\\[(\\d+)])?").matcher(segment);
        if (!matcher.matches()) throw new IllegalArgumentException("segmento invalido: " + segment);
        String fieldName = matcher.group(1);
        String indexText = matcher.group(2);
        Object value = obj;
        if (fieldName != null && !fieldName.isBlank()) {
            Field f = findField(obj.getClass(), fieldName);
            f.setAccessible(true);
            value = f.get(obj);
        }
        if (indexText != null) {
            int index = Integer.parseInt(indexText);
            if (value instanceof List<?> list) return list.get(index);
            if (value != null && value.getClass().isArray()) return Array.get(value, index);
            throw new IllegalArgumentException("valor não é lista/array: " + describeOneLine(value));
        }
        return value;
    }

    private void dump(Object value, String indent, int depth, IdentityHashMap<Object, Boolean> seen) {
        System.out.println(indent + describeOneLine(value) + " | " + plausibleFor(value));
        if (depth <= 0 || value == null || isLeaf(value)) return;
        if (seen.put(value, Boolean.TRUE) != null) {
            System.out.println(indent + "  <ciclo>");
            return;
        }
        try {
            if (value instanceof List<?> list) {
                for (int i = 0; i < Math.min(MAX_LIST_PREVIEW, list.size()); i++) {
                    System.out.print(indent + "  [" + i + "] ");
                    dump(list.get(i), indent + "    ", depth - 1, seen);
                }
            } else if (value.getClass().isArray()) {
                int len = Array.getLength(value);
                for (int i = 0; i < Math.min(MAX_LIST_PREVIEW, len); i++) {
                    System.out.print(indent + "  [" + i + "] ");
                    dump(Array.get(value, i), indent + "    ", depth - 1, seen);
                }
            } else {
                for (Field field : allFields(value.getClass())) {
                    if (Modifier.isStatic(field.getModifiers())) continue;
                    field.setAccessible(true);
                    System.out.print(indent + "  ." + field.getName() + " ");
                    dump(field.get(value), indent + "    ", depth - 1, seen);
                }
            }
        } catch (Exception e) {
            System.out.println(indent + "  <erro: " + e.getMessage() + ">");
        }
    }

    private void walk(Object root, String rootPath, int depth, ScanStats stats, Visitor visitor) throws IllegalAccessException {
        IdentityHashMap<Object, Boolean> seen = new IdentityHashMap<>();
        ArrayDeque<Node> queue = new ArrayDeque<>();
        queue.add(new Node(rootPath, root, 0));
        while (!queue.isEmpty() && stats.visited < MAX_SCAN_NODES) {
            Node node = queue.removeFirst();
            stats.visited++;
            visitor.visit(node.path, node.value);
            Object value = node.value;
            if (value == null || isLeaf(value)) continue;
            if (seen.put(value, Boolean.TRUE) != null) continue;
            if (value instanceof List<?> list) {
                for (int i = 0; i < list.size(); i++) queue.add(new Node(node.path + "[" + i + "]", list.get(i), node.depth + 1));
            } else if (value.getClass().isArray()) {
                int len = Array.getLength(value);
                for (int i = 0; i < len; i++) queue.add(new Node(node.path + "[" + i + "]", Array.get(value, i), node.depth + 1));
            } else if (!isJavaInternal(value.getClass())) {
                for (Field field : allFields(value.getClass())) {
                    if (Modifier.isStatic(field.getModifiers())) continue;
                    field.setAccessible(true);
                    queue.add(new Node(node.path + "." + field.getName(), field.get(value), node.depth + 1));
                }
            }
        }
        if (stats.visited >= MAX_SCAN_NODES) {
            System.out.println("AVISO: scan interrompido em " + MAX_SCAN_NODES + " nós para evitar loop gigantesco.");
        }
    }

    private String plausibleFor(Object value) {
        if (value == null) return "-";
        if (value instanceof String s) {
            String ref = references.explain(s);
            if (!ref.equals("-")) return ref;
            if (s.length() >= 3 && looksLikeName(s)) return "string com cara de nome/texto";
            return "-";
        }
        if (value instanceof Boolean) return "boolean/flag";
        if (value instanceof Number n) {
            long x = n.longValue();
            List<String> reasons = new ArrayList<>();
            if (x >= 0 && x <= 4) reasons.add("enum pequeno 0-4 (posição/lado/divisão?)");
            if (x >= 0 && x <= 20) reasons.add("enum/id pequeno 0-20");
            if (x >= 14 && x <= 45) reasons.add("idade plausível");
            if (x >= 0 && x <= 100) reasons.add("percentual/força/energia/confiança 0-100");
            if (x >= 1_000 && x <= 1_000_000_000L) reasons.add("dinheiro/id/capacidade plausível");
            if (x > 946684800000L && x < 4102444800000L) reasons.add("timestamp ms plausível");
            return reasons.isEmpty() ? "-" : String.join(", ", reasons);
        }
        if (value instanceof List<?> list) return "colecao List size=" + list.size();
        if (value.getClass().isArray()) return "array len=" + Array.getLength(value);
        if (!isJavaInternal(value.getClass())) return "objeto " + value.getClass().getName();
        return "-";
    }

    private static boolean looksLikeName(String s) {
        return TOKEN_PATTERN.matcher(s).matches() && s.chars().anyMatch(Character::isLetter);
    }

    private static String describeOneLine(Object value) {
        if (value == null) return "null";
        Class<?> type = value.getClass();
        if (value instanceof String s) return "String " + quote(s);
        if (value instanceof Number || value instanceof Boolean || value instanceof Character) return type.getSimpleName() + " " + value;
        if (value instanceof List<?> list) return type.getName() + " size=" + list.size();
        if (type.isArray()) return type.getComponentType().getSimpleName() + "[] len=" + Array.getLength(value);
        return type.getName() + "@" + Integer.toHexString(System.identityHashCode(value));
    }

    private static String quote(String s) {
        String compact = s.replace("\r", "\\r").replace("\n", "\\n");
        if (compact.length() > 100) compact = compact.substring(0, 97) + "...";
        return '"' + compact + '"';
    }

    private static boolean isLeaf(Object value) {
        if (value == null) return true;
        Class<?> type = value.getClass();
        return type.isPrimitive()
                || value instanceof String
                || value instanceof Number
                || value instanceof Boolean
                || value instanceof Character
                || type.isEnum();
    }

    private static boolean isJavaInternal(Class<?> type) {
        Package pkg = type.getPackage();
        return pkg != null && (pkg.getName().startsWith("java.") || pkg.getName().startsWith("javax."));
    }

    private static List<Field> allFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            fields.addAll(List.of(c.getDeclaredFields()));
        }
        fields.sort(Comparator.comparing(Field::getName));
        return fields;
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                // tenta superclass
            }
        }
        throw new NoSuchFieldException(name + " em " + type.getName());
    }

    private static String simpleType(Class<?> type) {
        if (type.isArray()) return type.getComponentType().getSimpleName() + "[]";
        return type.getSimpleName();
    }

    private static int parseIntOr(String text, int fallback) {
        if (text == null || text.isBlank()) return fallback;
        try { return Integer.parseInt(text.trim()); } catch (NumberFormatException ignored) { return fallback; }
    }

    private static String normalize(String text) {
        if (text == null) return "";
        String n = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return n.toLowerCase(Locale.ROOT).trim();
    }

    private record Crumb(String name, Object value) {}
    private record Node(String path, Object value, int depth) {}
    private record Candidate(String path, String value, String reason) {}
    private record ReferenceHit(String file, int lineNumber, String line) {}

    @FunctionalInterface
    private interface Visitor {
        void visit(String path, Object value) throws IllegalAccessException;
    }

    private static final class ScanStats {
        int visited;
        final Map<String, Integer> classes = new HashMap<>();
        int strings;
        int numbers;
        int booleans;

        void observe(String path, Object value) {
            if (value == null) return;
            classes.merge(value.getClass().getName(), 1, Integer::sum);
            if (value instanceof String) strings++;
            if (value instanceof Number) numbers++;
            if (value instanceof Boolean) booleans++;
        }

        void print() {
            System.out.println("-- resumo scan --");
            System.out.println("nos visitados: " + visited + ", strings: " + strings + ", numeros: " + numbers + ", booleans: " + booleans);
            System.out.println("classes mais frequentes:");
            classes.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(30)
                    .forEach(e -> System.out.printf("  %-45s %d%n", e.getKey(), e.getValue()));
        }
    }

    private static final class ReferenceIndex {
        final List<ReferenceLine> lines;
        final int fileCount;
        final int lineCount;

        private ReferenceIndex(List<ReferenceLine> lines, int fileCount) {
            this.lines = lines;
            this.fileCount = fileCount;
            this.lineCount = lines.size();
        }

        static ReferenceIndex load(Path root) throws IOException {
            if (!Files.exists(root)) {
                return new ReferenceIndex(List.of(), 0);
            }
            List<Path> files;
            try (Stream<Path> stream = Files.walk(root)) {
                files = stream.filter(Files::isRegularFile)
                        .filter(ReferenceIndex::looksTextual)
                        .limit(MAX_REF_FILES)
                        .collect(Collectors.toList());
            }
            List<ReferenceLine> lines = new ArrayList<>();
            Set<Path> loaded = new HashSet<>();
            for (Path file : files) {
                List<String> fileLines = readTextLines(file);
                loaded.add(file);
                for (int i = 0; i < fileLines.size(); i++) {
                    String line = fileLines.get(i).trim();
                    if (!line.isBlank()) lines.add(new ReferenceLine(root.relativize(file).toString(), i + 1, line, normalize(line)));
                }
            }
            return new ReferenceIndex(lines, loaded.size());
        }

        private static boolean looksTextual(Path path) {
            String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
            if (name.endsWith(".class") || name.endsWith(".jar") || name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".gif") || name.endsWith(".ico")) return false;
            return name.endsWith(".txt") || name.endsWith(".xml") || name.endsWith(".csv") || name.endsWith(".properties") || !name.contains(".");
        }

        private static List<String> readTextLines(Path file) {
            for (Charset charset : List.of(StandardCharsets.UTF_8, Charset.forName("ISO-8859-1"))) {
                try {
                    return Files.readAllLines(file, charset);
                } catch (Exception ignored) {
                    // tenta outro charset
                }
            }
            return List.of();
        }

        String explain(String value) {
            String norm = normalize(value);
            if (norm.length() < 3) return "-";
            for (ReferenceLine line : lines) {
                if (Objects.equals(line.norm, norm)) return "match exato em " + line.file + ":" + line.lineNumber;
            }
            for (ReferenceLine line : lines) {
                if (line.norm.contains(norm) || norm.contains(line.norm)) return "match parcial em " + line.file + ":" + line.lineNumber;
            }
            return "-";
        }

        List<ReferenceHit> search(String query, int limit) {
            String norm = normalize(query);
            return lines.stream()
                    .filter(l -> l.norm.contains(norm))
                    .limit(limit)
                    .map(l -> new ReferenceHit(l.file, l.lineNumber, l.text))
                    .collect(Collectors.toList());
        }
    }

    private record ReferenceLine(String file, int lineNumber, String text, String norm) {}
}
