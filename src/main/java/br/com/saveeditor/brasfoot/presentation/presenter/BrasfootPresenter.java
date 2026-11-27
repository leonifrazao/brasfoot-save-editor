package br.com.saveeditor.brasfoot.presentation.presenter;

import br.com.saveeditor.brasfoot.application.ports.in.record.PlayerBatchUpdateCommand;
import br.com.saveeditor.brasfoot.application.ports.in.record.TeamBatchUpdateCommand;
import br.com.saveeditor.brasfoot.application.ports.out.BrasfootGameLibraryPort;
import br.com.saveeditor.brasfoot.application.services.CountryManagementService;
import br.com.saveeditor.brasfoot.application.services.CalendarManagementService;
import br.com.saveeditor.brasfoot.application.services.LeagueManagementService;
import br.com.saveeditor.brasfoot.application.services.ManagerManagementService;
import br.com.saveeditor.brasfoot.application.services.PlayerManagementService;
import br.com.saveeditor.brasfoot.application.services.SessionService;
import br.com.saveeditor.brasfoot.application.services.TeamManagementService;
import br.com.saveeditor.brasfoot.application.shared.SaveEdit;
import br.com.saveeditor.brasfoot.domain.CountryState;
import br.com.saveeditor.brasfoot.domain.League;
import br.com.saveeditor.brasfoot.domain.LeagueTableEntry;
import br.com.saveeditor.brasfoot.domain.Manager;
import br.com.saveeditor.brasfoot.domain.ManagerTrophy;
import br.com.saveeditor.brasfoot.domain.Player;
import br.com.saveeditor.brasfoot.domain.Team;
import br.com.saveeditor.brasfoot.domain.enums.TeamReputation;
import br.com.saveeditor.brasfoot.presentation.model.CountryRow;
import br.com.saveeditor.brasfoot.presentation.model.CalendarRoundRow;
import br.com.saveeditor.brasfoot.presentation.model.LeagueRow;
import br.com.saveeditor.brasfoot.presentation.model.LeagueTableRow;
import br.com.saveeditor.brasfoot.presentation.model.ManagerRow;
import br.com.saveeditor.brasfoot.presentation.model.PlayerRow;
import br.com.saveeditor.brasfoot.presentation.model.TeamRow;
import br.com.saveeditor.brasfoot.presentation.view.BrasfootDesktopView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Component
public class BrasfootPresenter {

    private static final Logger log = LoggerFactory.getLogger(BrasfootPresenter.class);
    private static final long DESTROYED_TEAM_MONEY = 0L;
    private static final int DESTROYED_TEAM_LEVEL = 0;
    private static final int DESTROYED_PLAYER_OVERALL = 1;
    private static final int DESTROYED_PLAYER_ENERGY = 0;
    private static final int DESTROYED_PLAYER_SKILL = 0;
    private static final List<Integer> DESTROYED_STADIUM_SECTORS = List.of(1, 0, 0, 0);

    private final SessionService sessionService;
    private final TeamManagementService teamManagementService;
    private final PlayerManagementService playerManagementService;
    private final ManagerManagementService managerManagementService;
    private final LeagueManagementService leagueManagementService;
    private final CountryManagementService countryManagementService;
    private final CalendarManagementService calendarManagementService;
    private final BrasfootGameLibraryPort gameLibraryPort;

    private BrasfootDesktopView view;
    private UUID currentSessionId;
    private Path installationDirectory;
    private Path loadedLibrary;

    public BrasfootPresenter(SessionService sessionService,
                               TeamManagementService teamManagementService,
                                PlayerManagementService playerManagementService,
                                ManagerManagementService managerManagementService,
                                LeagueManagementService leagueManagementService,
                                CountryManagementService countryManagementService,
                               CalendarManagementService calendarManagementService,
                                BrasfootGameLibraryPort gameLibraryPort) {
        this.sessionService = sessionService;
        this.teamManagementService = teamManagementService;
        this.playerManagementService = playerManagementService;
        this.managerManagementService = managerManagementService;
        this.leagueManagementService = leagueManagementService;
        this.countryManagementService = countryManagementService;
        this.calendarManagementService = calendarManagementService;
        this.gameLibraryPort = gameLibraryPort;
    }

    public void attach(BrasfootDesktopView view) {
        this.view = view;
    }

    public boolean supportsRealBrasfootTactics() {
        return gameLibraryPort.supportsRealBrasfootTactics();
    }

    public void selectBrasfootLibrary(Path libraryPath) {
        try {
            if (currentSessionId != null && libraryPath.toAbsolutePath().normalize().equals(loadedLibrary)) return;
            Path loadedPath = gameLibraryPort.load(libraryPath);
            loadedLibrary = loadedPath;
            installationDirectory = loadedPath.getParent();
            if (currentSessionId != null) {
                sessionService.close(currentSessionId);
                currentSessionId = null;
                view.showSessionClosed();
                publishHistory();
            }
            view.showStatus("Biblioteca do Brasfoot carregada: " + loadedPath);
        } catch (RuntimeException e) {
            showError("Falha ao carregar Brasfoot", e);
        }
    }

    public Path getInstallationDirectory() {
        return installationDirectory;
    }

    public void openSave(Path savePath) {
        try {
            String sessionId = sessionService.open(savePath);
            currentSessionId = UUID.fromString(sessionId);
            view.showSessionOpened(sessionId, savePath.toAbsolutePath().normalize());
            refreshSession();
            publishHistory();
        } catch (RuntimeException e) {
            showError("Falha ao abrir save", e);
        }
    }

    public void saveCopy() {
        try {
            Path outputPath = sessionService.saveCopy(requireSession().toString());
            view.showSaveWritten(outputPath);
        } catch (RuntimeException e) {
            showError("Falha ao salvar copia", e);
        }
    }

    public void saveOverCurrent() {
        try {
            Path outputPath = sessionService.saveOverCurrent(requireSession().toString());
            publishHistory();
            view.showSaveWritten(outputPath);
        } catch (RuntimeException e) {
            showError("Falha ao substituir save", e);
        }
    }

    public SaveEdit.Preview savePreview() {
        try {
            return sessionService.savePreview(requireSession());
        } catch (RuntimeException e) {
            showError("Falha ao preparar a revisão do save", e);
            return null;
        }
    }

    public void undo() { restoreHistory(true); }

    public void redo() { restoreHistory(false); }

    private void restoreHistory(boolean undo) {
        try {
            UUID id = requireSession();
            boolean changed = undo ? sessionService.undo(id) : sessionService.redo(id);
            if (!changed) return;
            refreshSession();
            publishHistory();
            view.showStatus(undo ? "Alteração desfeita." : "Alteração refeita.");
        } catch (RuntimeException e) {
            showError(undo ? "Falha ao desfazer alteração" : "Falha ao refazer alteração", e);
        }
    }

    private void refreshSession() {
        refreshTeams();
        refreshManagers();
        refreshLeagues();
        refreshCountries();
        refreshCalendar();
    }

    private SaveEdit applyEdit(UUID id, Supplier<SaveEdit> operation) {
        SaveEdit edit = sessionService.applyEdit(id, operation);
        publishHistory();
        return edit;
    }

    private void publishHistory() {
        SaveEdit.HistoryStatus state = currentSessionId == null ? null : sessionService.historyStatus(currentSessionId);
        if (state != null) view.showEditHistory(state);
        else if (currentSessionId == null) view.showEditHistory(new SaveEdit.HistoryStatus(false, false, false));
    }

    public void refreshTeams() {
        try {
            List<TeamRow> teams = teamManagementService.getAllTeams(requireSession()).stream()
                    .map(this::toTeamRow)
                    .toList();
            view.showTeams(teams);
            view.showStatus(teams.size() + " times carregados.");
        } catch (RuntimeException e) {
            showError("Falha ao carregar times", e);
        }
    }

    public void loadPlayers(int teamId) {
        try {
            List<PlayerRow> players = playerManagementService.getTeamPlayers(requireSession(), teamId).stream()
                    .map(this::toPlayerRow)
                    .toList();
            view.showPlayers(teamId, players);
            view.showStatus(players.size() + " jogadores carregados.");
        } catch (RuntimeException e) {
            showError("Falha ao carregar jogadores", e);
        }
    }

    public void refreshManagers() {
        try {
            List<ManagerRow> managers = managerManagementService.getManagers(requireSession()).stream()
                    .map(this::toManagerRow)
                    .toList();
            view.showManagers(managers);
        } catch (RuntimeException e) {
            showError("Falha ao carregar tecnicos", e);
        }
    }

    public void refreshLeagues() {
        try {
            List<LeagueRow> leagues = leagueManagementService.getLeagues(requireSession()).stream()
                    .map(this::toLeagueRow)
                    .toList();
            view.showLeagues(leagues);
        } catch (RuntimeException e) {
            showError("Falha ao carregar ligas", e);
        }
    }

    public void refreshCountries() {
        try {
            List<CountryRow> countries = countryManagementService.getCountries(requireSession()).stream()
                    .map(this::toCountryRow)
                    .toList();
            view.showCountries(countries);
        } catch (RuntimeException e) {
            showError("Falha ao carregar paises", e);
        }
    }

    public void refreshCalendar() {
        try {
            view.showCalendar(calendarManagementService.getUpcomingRounds(requireSession()));
        } catch (RuntimeException e) {
            showError("Falha ao carregar calendario", e);
        }
    }

    public void moveCalendarRound(String competitionKey, java.time.LocalDate sourceDate,
                                  java.time.LocalDate targetDate) {
        try {
            UUID id = requireSession();
            SaveEdit edit = applyEdit(id, () -> {
                var round = calendarManagementService.getUpcomingRounds(id).stream()
                        .filter(row -> row.date().equals(sourceDate) && (row.eventKey().equals(competitionKey)
                                || row.competitionKey().equals(competitionKey))).findFirst().orElse(null);
                int matches = calendarManagementService.moveRound(id, competitionKey, sourceDate, targetDate);
                String title = round == null ? "Calendário" : round.competition() + " · " + round.phase();
                return new SaveEdit(title, List.of(new SaveEdit.Detail("Data de " + matches + " jogos",
                        sourceDate.toString(), targetDate.toString())));
            });
            refreshCalendar();
            view.showStatus(edit.details().get(0).field() + " alterada para " + targetDate + ".");
        } catch (RuntimeException e) {
            showError("Nao foi possivel mover a rodada", e);
        }
    }

    public boolean calendarMoveHasHumanNationalTournamentConflict(String competitionKey,
                                                                 java.time.LocalDate sourceDate,
                                                                 java.time.LocalDate targetDate) {
        return calendarManagementService.hasHumanNationalTournamentConflict(
                requireSession(), competitionKey, sourceDate, targetDate);
    }

    public void loadLeagueTable(String leagueId) {
        try {
            List<LeagueTableRow> table = leagueManagementService.getTable(requireSession(), leagueId).stream()
                    .map(this::toLeagueTableRow)
                    .toList();
            view.showLeagueTable(leagueId, table);
            view.showStatus(table.size() + " linhas de tabela carregadas.");
        } catch (RuntimeException e) {
            showError("Falha ao carregar tabela", e);
        }
    }

    public void updateTeam(int teamId, String moneyText, String reputationText, String stadiumName,
                           List<String> stadiumSectorTexts) {
        try {
            Long money = parseLong(moneyText, "dinheiro");
            TeamReputation reputation = TeamReputation.valueOf(reputationText);
            List<Integer> sectors = parseSectors(stadiumSectorTexts);
            String normalizedStadiumName = normalizeOptionalText(stadiumName);

            UUID id = requireSession();
            applyEdit(id, () -> {
                Team before = teamManagementService.getTeam(id, teamId);
                Team after = teamManagementService.updateTeam(id, teamId, money, reputation, normalizedStadiumName, sectors);
                return EditDescriptions.compare("Time: " + before.getName(), toTeamRow(before), toTeamRow(after));
            });
            refreshTeams();
            view.showStatus("Time atualizado.");
        } catch (RuntimeException e) {
            showError("Falha ao atualizar time", e);
        }
    }

    public boolean updateTeam(int teamId, String name, String alias, Long money, TeamReputation reputation,
                              String stadiumName, List<Integer> sectors, Integer country, Integer division,
                              Integer level, Integer tacticStyle, Integer tacticMarking, Integer tacticFocus) {
        try {
            UUID id = requireSession();
            applyEdit(id, () -> {
                Team before = teamManagementService.getTeam(id, teamId);
                Team after = teamManagementService.updateTeam(id, teamId, normalizeOptionalText(name), normalizeOptionalText(alias),
                        money, reputation, normalizeOptionalText(stadiumName), sectors, country, division, level,
                        tacticStyle, tacticMarking, tacticFocus);
                return EditDescriptions.compare("Time: " + before.getName(), toTeamRow(before), toTeamRow(after));
            });
            refreshTeams();
            view.showStatus("Time atualizado.");
            return true;
        } catch (RuntimeException e) {
            showError("Falha ao atualizar time", e);
            return false;
        }
    }

    public void updatePlayer(int teamId, int playerId, int age, int overall, int position, int energy,
                             boolean starLocal, boolean starGlobal) {
        try {
            UUID id = requireSession();
            applyEdit(id, () -> {
                Player before = playerManagementService.getPlayer(id, teamId, playerId);
                Player after = playerManagementService.updatePlayer(id, teamId, playerId, age, overall, position, energy,
                        null, starLocal, starGlobal);
                return describePlayer(id, teamId, before, after);
            });
            loadPlayers(teamId);
            view.showStatus("Jogador atualizado.");
        } catch (RuntimeException e) {
            showError("Falha ao atualizar jogador", e);
        }
    }

    public boolean updatePlayer(int teamId, int playerId, String name, Integer age, Integer overall,
                                Integer position, Integer energy, Integer salary, Integer side, Long contractEnd,
                                Integer characteristic1, Integer characteristic2, Integer skillGoalkeeping,
                                Integer skillSpeed, Integer skillTechnique, Integer skillPassing, Integer skillTackling,
                                Integer skillPlaymaking, Integer skillFinishing, Integer country,
                                Boolean starLocal, Boolean starGlobal) {
        try {
            UUID id = requireSession();
            applyEdit(id, () -> {
                Player before = playerManagementService.getPlayer(id, teamId, playerId);
                Player after = playerManagementService.updatePlayer(id, teamId, playerId, normalizeOptionalText(name), age,
                        overall, position, energy, salary, side, contractEnd, characteristic1, characteristic2,
                        skillGoalkeeping, skillSpeed, skillTechnique, skillPassing, skillTackling, skillPlaymaking,
                        skillFinishing, country, starLocal, starGlobal);
                return describePlayer(id, teamId, before, after);
            });
            loadPlayers(teamId);
            view.showStatus("Jogador atualizado.");
            return true;
        } catch (RuntimeException e) {
            showError("Falha ao atualizar jogador", e);
            return false;
        }
    }

    public void updateManager(int managerId, String name, int confidenceBoard, int confidenceFans) {
        try {
            String normalizedName = normalizeRequiredText(name, "nome");
            Manager updateData = Manager.builder()
                    .name(normalizedName)
                    .confidenceBoard(confidenceBoard)
                    .confidenceFans(confidenceFans)
                    .build();
            UUID id = requireSession();
            applyEdit(id, () -> describeManagerUpdate(id, managerId, updateData));
            refreshManagers();
            view.showStatus("Tecnico atualizado.");
        } catch (RuntimeException e) {
            showError("Falha ao atualizar tecnico", e);
        }
    }

    public void updateManager(int managerId, String name, Boolean human, String teamIdText, int confidenceBoard,
                               int confidenceFans) {
        updateManager(managerId, name, human, teamIdText, confidenceBoard, confidenceFans, null);
    }

    public void updateManager(int managerId, String name, Boolean human, String teamIdText, int confidenceBoard,
                               int confidenceFans, List<ManagerTrophy> trophies) {
        try {
            Manager updateData = Manager.builder()
                    .name(normalizeRequiredText(name, "nome"))
                    .isHuman(human)
                    .teamId(parseOptionalInteger(teamIdText, "time"))
                    .confidenceBoard(confidenceBoard)
                    .confidenceFans(confidenceFans)
                    .trophies(trophies)
                    .build();
            UUID id = requireSession();
            applyEdit(id, () -> describeManagerUpdate(id, managerId, updateData));
            refreshManagers();
            view.showStatus("Tecnico atualizado.");
        } catch (RuntimeException e) {
            showError("Falha ao atualizar tecnico", e);
        }
    }

    public void batchUpdatePlayers(int teamId, Integer age, Integer overall, Integer position, Integer energy,
                                   Integer country, Integer skillGoalkeeping, Integer skillSpeed,
                                   Integer skillTechnique, Integer skillPassing, Integer skillTackling,
                                   Integer skillPlaymaking, Integer skillFinishing,
                                   Boolean starLocal, Boolean starGlobal) {
        try {
            UUID sessionId = requireSession();
            List<PlayerRow> currentPlayers = playerManagementService.getTeamPlayers(sessionId, teamId).stream()
                    .map(this::toPlayerRow)
                    .toList();
            List<PlayerBatchUpdateCommand> commands = currentPlayers.stream()
                    .map(p -> new PlayerBatchUpdateCommand(p.id(), age, overall, position, energy, null,
                            starLocal, starGlobal, country, skillGoalkeeping, skillSpeed, skillTechnique,
                            skillPassing, skillTackling, skillPlaymaking, skillFinishing))
                    .toList();
            SaveEdit edit = applyEdit(sessionId, () -> {
                var response = playerManagementService.batchUpdatePlayers(sessionId, teamId, commands);
                long succeeded = response.getResults().stream().filter(r -> r.isSuccess()).count();
                return EditDescriptions.describe(succeeded + "/" + commands.size() + " jogadores · " + teamName(sessionId, teamId),
                        "Idade", age, "Força", overall, "Posição", position, "Energia", energy, "País", country,
                        "Goleiro", skillGoalkeeping, "Velocidade", skillSpeed, "Técnica", skillTechnique,
                        "Passe", skillPassing, "Desarme", skillTackling, "Armação", skillPlaymaking,
                        "Finalização", skillFinishing, "Estrela local", starLocal, "Estrela mundial", starGlobal);
            });
            loadPlayers(teamId);
            view.showStatus("Edição em lote: " + edit.title() + ".");
        } catch (RuntimeException e) {
            showError("Falha ao atualizar jogadores em lote", e);
        }
    }

    public void batchUpdateTeamsByCountry(int countryId, Long money) {
        try {
            UUID sessionId = requireSession();
            List<Team> allTeams = teamManagementService.getAllTeams(sessionId);
            List<TeamBatchUpdateCommand> commands = allTeams.stream()
                    .filter(t -> t.getCountry() != null && t.getCountry() == countryId)
                    .map(t -> new TeamBatchUpdateCommand(t.getId(), money, null))
                    .toList();
            if (commands.isEmpty()) {
                view.showStatus("Nenhum time encontrado para este pais.");
                return;
            }
            SaveEdit edit = applyEdit(sessionId, () -> {
                var response = teamManagementService.batchUpdateTeams(sessionId, commands);
                long succeeded = response.getResults().stream().filter(r -> r.isSuccess()).count();
                return EditDescriptions.describe(succeeded + "/" + commands.size() + " times · " + countryName(countryId),
                        "Dinheiro", money);
            });
            refreshTeams();
            view.showStatus("Edição em lote: " + edit.title() + ".");
        } catch (RuntimeException e) {
            showError("Falha ao atualizar times em lote", e);
        }
    }

    public void destroyOtherTeamsByCountry(int countryId, int protectedTeamId) {
        try {
            UUID sessionId = requireSession();
            List<Team> targetTeams = teamManagementService.getAllTeams(sessionId).stream()
                    .filter(team -> team.getCountry() != null && team.getCountry() == countryId)
                    .filter(team -> team.getId() != protectedTeamId)
                    .toList();
            if (targetTeams.isEmpty()) {
                view.showStatus("Nenhum outro time encontrado para este pais.");
                return;
            }

            SaveEdit edit = applyEdit(sessionId, () -> destroyTargets(sessionId, targetTeams,
                    "Outros clubes de " + countryName(countryId), "Time protegido", teamName(sessionId, protectedTeamId)));
            refreshTeams();
            view.showStatus(edit.title());
        } catch (RuntimeException e) {
            showError("Falha ao destruir times do pais", e);
        }
    }

    public void batchUpdateAllTeams(Long money, TeamReputation reputation, String stadiumName, Integer level,
                                    Integer tacticStyle) {
        try {
            UUID sessionId = requireSession();
            List<Team> all = teamManagementService.getAllTeams(sessionId);
            List<TeamBatchUpdateCommand> commands = all.stream()
                    .map(team -> new TeamBatchUpdateCommand(team.getId(), money, reputation, stadiumName, null, level,
                            tacticStyle, null, null))
                    .toList();
            SaveEdit edit = applyEdit(sessionId, () -> {
                var response = teamManagementService.batchUpdateTeams(sessionId, commands);
                long succeeded = response.getResults().stream().filter(result -> result.isSuccess()).count();
                return EditDescriptions.describe(succeeded + "/" + commands.size() + " times · todos os clubes",
                        "Dinheiro", money, "Reputação", reputation, "Estádio", stadiumName, "Nível", level,
                        "Estilo de jogo", tacticStyle);
            });
            refreshTeams();
            view.showStatus("Edição em lote: " + edit.title() + ".");
        } catch (RuntimeException e) {
            showError("Falha ao editar times em lote", e);
        }
    }

    public void destroyTeams(boolean keepHumanManaged) {
        try {
            UUID sessionId = requireSession();
            List<Team> teams = teamManagementService.getAllTeams(sessionId);
            var teamIds = teams.stream().map(Team::getId).collect(java.util.stream.Collectors.toSet());
            var protectedIds = keepHumanManaged
                    ? managerManagementService.getManagers(sessionId).stream()
                        .filter(manager -> Boolean.TRUE.equals(manager.getIsHuman()))
                        .map(Manager::getTeamId).filter(teamIds::contains).collect(java.util.stream.Collectors.toSet())
                    : java.util.Set.<Integer>of();
            if (keepHumanManaged && protectedIds.isEmpty()) {
                view.showError("Time humano não encontrado", "Não encontrei um time associado a técnico humano. Nenhum time foi alterado.");
                return;
            }
            List<Team> targets = teams.stream().filter(team -> !protectedIds.contains(team.getId())).toList();
            if (targets.isEmpty()) { view.showStatus("Não há times para destruir."); return; }
            SaveEdit edit = applyEdit(sessionId, () -> destroyTargets(sessionId, targets,
                    keepHumanManaged ? "Clubes de computador" : "Todos os clubes", "Times preservados", protectedIds.size()));
            refreshTeams();
            view.showStatus(edit.title());
        } catch (RuntimeException e) {
            showError("Falha ao destruir times", e);
        }
    }

    private SaveEdit destroyTargets(UUID id, List<Team> targets, String scope, String protectionLabel, Object protection) {
        var commands = targets.stream().map(team -> new TeamBatchUpdateCommand(team.getId(), DESTROYED_TEAM_MONEY,
                TeamReputation.MUNICIPAL, null, DESTROYED_STADIUM_SECTORS, DESTROYED_TEAM_LEVEL)).toList();
        var response = teamManagementService.batchUpdateTeams(id, commands);
        long teamSuccess = response.getResults().stream().filter(result -> result.isSuccess()).count();
        int playerCount = 0;
        long playerSuccess = 0;
        for (Team team : targets) {
            var players = playerManagementService.getTeamPlayers(id, team.getId());
            var playerResponse = playerManagementService.batchUpdatePlayers(id, team.getId(), players.stream()
                    .map(player -> destroyedPlayerCommand(player.getId())).toList());
            playerCount += players.size();
            playerSuccess += playerResponse.getResults().stream().filter(result -> result.isSuccess()).count();
        }
        return EditDescriptions.describe("Enfraquecer clubes · " + scope,
                "Times atualizados", teamSuccess + "/" + commands.size(),
                "Jogadores atualizados", playerSuccess + "/" + playerCount,
                protectionLabel, protection, "Dinheiro", DESTROYED_TEAM_MONEY, "Reputação", TeamReputation.MUNICIPAL,
                "Nível", DESTROYED_TEAM_LEVEL, "Setores", DESTROYED_STADIUM_SECTORS, "Força", DESTROYED_PLAYER_OVERALL,
                "Energia e habilidades", 0, "Estrelas", "removidas");
    }

    private PlayerBatchUpdateCommand destroyedPlayerCommand(int playerId) {
        return new PlayerBatchUpdateCommand(playerId, null, DESTROYED_PLAYER_OVERALL, null, DESTROYED_PLAYER_ENERGY,
                null, false, false, null, DESTROYED_PLAYER_SKILL, DESTROYED_PLAYER_SKILL, DESTROYED_PLAYER_SKILL,
                DESTROYED_PLAYER_SKILL, DESTROYED_PLAYER_SKILL, DESTROYED_PLAYER_SKILL, DESTROYED_PLAYER_SKILL);
    }

    public void updateLeagueTableEntry(String leagueId, int teamId, int points, int wins, int draws, int losses,
                                        int goalsFor, int goalsAgainst) {
        try {
            UUID id = requireSession();
            applyEdit(id, () -> {
                LeagueTableEntry before = leagueManagementService.getTable(id, leagueId).stream()
                        .filter(row -> row.teamId() == teamId).findFirst().orElse(null);
                LeagueTableEntry after = leagueManagementService.updateTableEntry(id, leagueId, teamId, points, wins, draws,
                        losses, goalsFor, goalsAgainst);
                return EditDescriptions.compare("Classificação: " + after.teamName(), before, after);
            });
            loadLeagueTable(leagueId);
            view.showStatus("Tabela atualizada. Jogos = V + E + D para preservar empates.");
        } catch (RuntimeException e) {
            showError("Falha ao atualizar tabela", e);
        }
    }

    public void updateCountryLevel(String countryId, int level) {
        try {
            UUID id = requireSession();
            applyEdit(id, () -> {
                CountryState before = countryManagementService.getCountries(id).stream()
                        .filter(country -> country.id().equals(countryId)).findFirst().orElse(null);
                CountryState after = countryManagementService.updateCountryLevel(id, countryId, level);
                return EditDescriptions.compare("País: " + after.name(), before == null ? null : toCountryRow(before), toCountryRow(after));
            });
            refreshCountries();
            refreshLeagues();
            view.showStatus("Pais atualizado.");
        } catch (RuntimeException e) {
            showError("Falha ao atualizar pais", e);
        }
    }

    private SaveEdit describePlayer(UUID id, int teamId, Player before, Player after) {
        return EditDescriptions.compare("Jogador: " + before.getName() + " · " + teamName(id, teamId),
                toPlayerRow(before), toPlayerRow(after));
    }

    private SaveEdit describeManagerUpdate(UUID id, int managerId, Manager update) {
        Manager before = managerManagementService.getManager(id, managerId).orElseThrow();
        List<Manager> previousAssignments = update.getTeamId() == null ? List.of() : managerManagementService.getManagers(id);
        Manager after = managerManagementService.updateManager(id, managerId, update);
        SaveEdit edit = EditDescriptions.compare("Técnico: " + before.getName(), toManagerRow(before), toManagerRow(after));
        if (Objects.equals(before.getTeamId(), after.getTeamId())) return edit;
        var details = new ArrayList<>(edit.details());
        details.removeIf(detail -> detail.field().equals("ID do time"));
        details.add(new SaveEdit.Detail("Clube", managerClub(id, before), managerClub(id, after)));
        var managers = managerManagementService.getManagers(id);
        for (Manager manager : previousAssignments) {
            Manager current = managers.get(manager.getId());
            if (manager.getId() != managerId && !Objects.equals(manager.getTeamId(), current.getTeamId())) {
                details.add(new SaveEdit.Detail("Clube de " + manager.getName(), managerClub(id, manager), managerClub(id, current)));
            }
        }
        return new SaveEdit(edit.title(), details);
    }

    private String managerClub(UUID id, Manager manager) {
        Integer club = manager.getTeamId();
        return club == null || club < 0 ? "Sem clube" : teamName(id, club);
    }

    private String teamName(UUID id, int teamId) {
        try {
            Team found = teamManagementService.getTeam(id, teamId);
            if (found != null) return found.getName();
        } catch (IllegalArgumentException ignored) {
            // Some saves have old club assignments that no longer identify a club.
        }
        return "Time " + teamId;
    }

    private String countryName(int id) {
        var country = br.com.saveeditor.brasfoot.domain.enums.Country.fromId(id);
        return country == null ? "País " + id : country.getName();
    }

    private UUID requireSession() {
        if (currentSessionId == null) {
            throw new IllegalStateException("Abra um arquivo .s22 antes de editar.");
        }
        return currentSessionId;
    }

    private TeamRow toTeamRow(Team team) {
        return new TeamRow(team.getId(), team.getName(), team.getAlias(), team.getMoney(), team.getReputation().name(),
                team.getCountry(), team.getDivision(), team.getLevel(), team.getStadiumName(), team.getStadiumCapacity(),
                team.getStadiumSectors(), team.getTacticStyle(), team.getTacticMarking(), team.getTacticFocus());
    }

    private PlayerRow toPlayerRow(Player player) {
        return new PlayerRow(player.getId(), player.getName(), player.getAge(), player.getOverall(),
                player.getPosition(), player.getEnergy(), player.getSalary(), player.getSide(), player.getContractEnd(),
                player.getCountry(),
                player.getCharacteristic1(), player.getCharacteristic2(), player.getSkillGoalkeeping(),
                player.getSkillSpeed(), player.getSkillTechnique(), player.getSkillPassing(), player.getSkillTackling(),
                player.getSkillPlaymaking(), player.getSkillFinishing(), player.isStarLocal(), player.isStarGlobal());
    }

    private ManagerRow toManagerRow(Manager manager) {
        return new ManagerRow(manager.getId(), manager.getName(), manager.getIsHuman(), manager.getTeamId(),
                manager.getConfidenceBoard(), manager.getConfidenceFans(), manager.getTrophies(), manager.getTrophyCompetitions());
    }

    private LeagueRow toLeagueRow(League league) {
        return new LeagueRow(league.id(), league.name(), league.path(), league.teamCount());
    }

    private LeagueTableRow toLeagueTableRow(LeagueTableEntry entry) {
        return new LeagueTableRow(entry.position(), entry.teamId(), entry.teamName(), entry.points(), entry.played(),
                entry.wins(), entry.draws(), entry.losses(), entry.goalsFor(), entry.goalsAgainst(),
                entry.goalDifference());
    }

    private CountryRow toCountryRow(CountryState country) {
        return new CountryRow(country.id(), country.name(), country.group(), country.level(), country.divisionCount());
    }

    private Long parseLong(String text, String fieldName) {
        try {
            return Long.parseLong(normalizeRequiredText(text, fieldName));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor invalido para " + fieldName + ".", e);
        }
    }

    private Integer parseOptionalInteger(String text, String fieldName) {
        String normalized = normalizeOptionalText(text);
        if (normalized == null) {
            return null;
        }
        try {
            return Integer.parseInt(normalized);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor invalido para " + fieldName + ".", e);
        }
    }

    private List<Integer> parseSectors(List<String> sectorTexts) {
        if (sectorTexts == null || sectorTexts.stream().allMatch(text -> text == null || text.isBlank())) {
            return null;
        }
        return sectorTexts.stream()
                .map(text -> Math.toIntExact(parseLong(text, "setor do estadio")))
                .toList();
    }

    private String normalizeRequiredText(String text, String fieldName) {
        String normalized = normalizeOptionalText(text);
        if (normalized == null) {
            throw new IllegalArgumentException("Informe " + fieldName + ".");
        }
        return normalized;
    }

    private String normalizeOptionalText(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        return text.trim();
    }

    private void showError(String title, RuntimeException e) {
        String message = userFacingMessage(e);
        log.error("desktop_operation_failed {} {} {} {}", kv("title", title), kv("session_id", currentSessionId),
                kv("error", message), kv("exception_class", e.getClass().getName()), e);
        view.showError(title, message);
    }

    private String userFacingMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getMessage() != null
                && current.getMessage().startsWith("Failed to")) {
            current = current.getCause();
        }
        String message = current.getMessage();
        if (message == null || message.isBlank()) {
            message = current.getClass().getSimpleName();
        }
        return message;
    }
}
