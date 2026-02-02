package br.com.saveeditor.brasfoot.service;

import br.com.saveeditor.brasfoot.model.NavegacaoState;
import br.com.saveeditor.brasfoot.util.BrasfootConstants;
import br.com.saveeditor.brasfoot.util.ConsoleHelper;
import br.com.saveeditor.brasfoot.util.ReflectionUtils;
import br.com.saveeditor.brasfoot.util.StringUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Serviço de edição e navegação na estrutura de dados.
 */
import org.springframework.stereotype.Service;

@Service
public class EditorService {

    private final GameDataService gameDataService;

    public EditorService(GameDataService gameDataService) {
        this.gameDataService = gameDataService;
    }

    public void entrarEmCampo(NavegacaoState estado, String nomeCampo) throws ReflectiveOperationException {
        if (nomeCampo == null) {
            throw new IllegalArgumentException("Especifique um campo para entrar");
        }
        Object obj = estado.getObjetoAtual();
        Field campo;
        try {
            campo = obj.getClass().getDeclaredField(nomeCampo);
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldException("O campo '" + nomeCampo + "' não existe");
        }
        campo.setAccessible(true);
        Object valorCampo = campo.get(obj);
        if (valorCampo != null) {
            estado.entrar(valorCampo, nomeCampo);
        } else {
            throw new IllegalStateException("O campo '" + nomeCampo + "' é nulo");
        }
    }

    public void entrarEmItemDeLista(NavegacaoState estado, String indiceStr) {
        if (indiceStr == null) {
            throw new IllegalArgumentException("Especifique um índice");
        }
        Object obj = estado.getObjetoAtual();
        try {
            int index = Integer.parseInt(indiceStr);
            Object item;
            if (obj instanceof List) {
                item = ((List<?>) obj).get(index);
            } else if (obj.getClass().isArray()) {
                item = Array.get(obj, index);
            } else {
                throw new IllegalStateException("O objeto atual não é uma lista ou array");
            }
            if (item != null) {
                estado.entrar(item, "[" + index + "]");
            } else {
                throw new IllegalStateException("O item no índice " + index + " é nulo");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("O índice deve ser um número inteiro");
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException("Índice fora dos limites");
        }
    }

    public void modificarValor(Object obj, String arg) throws Exception {
        if (obj instanceof Collection || obj.getClass().isArray()) {
            throw new IllegalStateException("Não pode usar modificação direta numa lista");
        }
        if (arg == null || !arg.contains("=")) {
            throw new IllegalArgumentException("Use o formato '<campo> = <valor>'");
        }
        String[] partes = arg.split("=", 2);
        String nomeCampo = partes[0].trim();
        String valorStr = partes[1].trim();
        Field campo;
        try {
            campo = obj.getClass().getDeclaredField(nomeCampo);
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldException("O campo '" + nomeCampo + "' não existe");
        }
        try {
            Object valorConvertido = ReflectionUtils.converterStringParaTipoDoCampo(valorStr, campo.getType());
            ReflectionUtils.setFieldValue(obj, nomeCampo, valorConvertido);
            System.out.println("✔ Campo '" + nomeCampo + "' atualizado para '" + valorStr + "'");
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Erro de conversão: O valor '" + valorStr + "' não é válido para o tipo "
                    + campo.getType().getSimpleName());
        }
    }

    public void editarTodosOsItens(Object colecaoOuArray, String arg) {
        if (!(colecaoOuArray instanceof Collection || colecaoOuArray.getClass().isArray())) {
            throw new IllegalStateException("Só pode ser usado em lista ou array");
        }
        if (arg == null || !arg.contains("=")) {
            throw new IllegalArgumentException("Use o formato '<campo> = <valor>'");
        }

        String[] partes = arg.split("=", 2);
        String nomeCampo = partes[0].trim();
        String valorStr = partes[1].trim();

        int totalItens = (colecaoOuArray instanceof Collection) ? ((Collection<?>) colecaoOuArray).size()
                : Array.getLength(colecaoOuArray);
        if (totalItens == 0)
            return;

        int sucessos = 0;
        Iterator<?> iterator = (colecaoOuArray instanceof Collection)
                ? ((Collection<?>) colecaoOuArray).iterator()
                : null;

        for (int i = 0; i < totalItens; i++) {
            Object item = (iterator != null) ? iterator.next() : Array.get(colecaoOuArray, i);
            if (item == null)
                continue;

            try {
                Field campo = item.getClass().getDeclaredField(nomeCampo);
                Object valorConvertido = ReflectionUtils.converterStringParaTipoDoCampo(valorStr, campo.getType());
                ReflectionUtils.setFieldValue(item, nomeCampo, valorConvertido);
                sucessos++;
            } catch (Exception e) {
                // Ignore missing fields
            }
        }
        System.out.println(ConsoleHelper
                .success("Campo '" + nomeCampo + "' modificado em " + sucessos + " de " + totalItens + " itens"));
    }

    // Reuse GameDataService for specific object finding logic where possible
    // Note: editarJogador and editarTime have specific recursive logic.
    // We can verify if GameDataService can simplify.
    // For now, delegating team lookup in editarTime

    public void editarTime(Object objetoRaiz, String arg) {
        if (arg == null || !arg.contains(";")) {
            throw new IllegalArgumentException("Use: <time>; <atributo>; <valor>");
        }
        try {
            String[] params = arg.split(";", 3);
            if (params.length != 3) {
                throw new IllegalArgumentException("Faltam parâmetros");
            }
            String nomeTime = params[0].trim();
            String atributo = params[1].trim();
            String valorStr = params[2].trim();

            System.out.println(ConsoleHelper.info("A procurar pelo time: '" + nomeTime + "'..."));

            int teamIndex = gameDataService.findTeamIndex(objetoRaiz, nomeTime);
            if (teamIndex == -1) {
                throw new IllegalStateException("Time '" + nomeTime + "' não encontrado");
            }
            Object timeEncontrado = gameDataService.getTeams(objetoRaiz).get(teamIndex);

            System.out.println(ConsoleHelper.success("Time encontrado! A modificar jogadores..."));
            List<Object> listaJogadores = gameDataService.getPlayers(timeEncontrado);

            if (listaJogadores == null || listaJogadores.isEmpty()) {
                System.out.println(ConsoleHelper.warning("Time não possui jogadores"));
                return;
            }

            int contador = 0;
            // Validate field exists on first player
            try {
                listaJogadores.get(0).getClass().getDeclaredField(atributo);
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException("Atributo '" + atributo + "' não existe nos jogadores.");
            }

            // Assume type from first player field (simplification)
            Class<?> type = listaJogadores.get(0).getClass().getDeclaredField(atributo).getType();
            Object valorConvertido = ReflectionUtils.converterStringParaTipoDoCampo(valorStr, type);

            for (Object jogador : listaJogadores) {
                try {
                    ReflectionUtils.setFieldValue(jogador, atributo, valorConvertido);
                    contador++;
                } catch (Exception e) {
                    /* Ignora */ }
            }
            System.out.println("✔ " + contador + " jogadores do time '" + nomeTime + "' modificados");
        } catch (Exception e) {
            throw new RuntimeException("Erro ao editar time: " + e.getMessage(), e);
        }
    }

    // Kept recursive player edit because it searches GLOBAL players, not just
    // inside teams (sometimes free agents?)
    // But logic could be simplified. Keeping as is for safety but cleaned up
    // slightly.
    public void editarJogador(Object objetoRaiz, String arg) {
        // ... (keep finding logic, it's specific)
        if (arg == null || !arg.contains(";")) {
            throw new IllegalArgumentException("Use: <nome>; <idade>; <over>");
        }
        try {
            String[] params = arg.split(";", 3);
            if (params.length != 3)
                throw new IllegalArgumentException("Faltam parâmetros");

            String nomeJogador = params[0].trim();
            int novaIdade = Integer.parseInt(params[1].trim());
            int novoOver = Integer.parseInt(params[2].trim());

            System.out.println("A procurar por '" + nomeJogador + "'...");
            boolean encontrado = encontrarEModificarJogadorRecursivo(objetoRaiz, nomeJogador, novaIdade, novoOver,
                    new HashSet<>());

            if (!encontrado) {
                throw new IllegalStateException("Jogador '" + nomeJogador + "' não encontrado");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Idade e over devem ser números válidos");
        }
    }

    private boolean encontrarEModificarJogadorRecursivo(Object currentObject, String nomeJogadorAlvo, int novaIdade,
            int novoOver, Set<Object> visited) {
        if (currentObject == null || visited.contains(currentObject))
            return false;

        // Optim: Check if it looks like a Player object (has name, age, overall)
        // Or if it's the specific obfuscated class

        visited.add(currentObject);

        if ("best.F".equals(currentObject.getClass().getName())) {
            try {
                String nomeAtual = (String) ReflectionUtils.getFieldValue(currentObject, BrasfootConstants.PLAYER_NAME);
                if (StringUtils.normalize(nomeJogadorAlvo).equals(StringUtils.normalize(nomeAtual))) {
                    ReflectionUtils.setFieldValue(currentObject, BrasfootConstants.PLAYER_AGE, novaIdade);
                    ReflectionUtils.setFieldValue(currentObject, BrasfootConstants.PLAYER_OVERALL, novoOver);
                    System.out.println(ConsoleHelper.success("Jogador modificado!"));
                    return true;
                }
            } catch (Exception e) {
            }
        }

        // Recurse
        if (currentObject instanceof Collection) {
            for (Object item : (Collection<?>) currentObject) {
                if (encontrarEModificarJogadorRecursivo(item, nomeJogadorAlvo, novaIdade, novoOver, visited))
                    return true;
            }
        } else if (currentObject.getClass().isArray()) {
            // Arrays
            int len = Array.getLength(currentObject);
            for (int i = 0; i < len; i++) {
                if (encontrarEModificarJogadorRecursivo(Array.get(currentObject, i), nomeJogadorAlvo, novaIdade,
                        novoOver, visited))
                    return true;
            }
        } else if (ReflectionUtils.isComplexObject(currentObject)) {
            for (Field field : currentObject.getClass().getDeclaredFields()) {
                if (field.isSynthetic())
                    continue;
                try {
                    field.setAccessible(true);
                    Object val = field.get(currentObject);
                    if (encontrarEModificarJogadorRecursivo(val, nomeJogadorAlvo, novaIdade, novoOver, visited))
                        return true;
                } catch (Exception e) {
                }
            }
        }
        return false;
    }
}