package br.com.saveeditor.brasfoot.presentation.presenter;

import br.com.saveeditor.brasfoot.application.ports.in.record.PlayerBatchUpdateCommand;
import br.com.saveeditor.brasfoot.application.ports.in.record.TeamBatchUpdateCommand;
import br.com.saveeditor.brasfoot.application.services.CountryManagementService;
import br.com.saveeditor.brasfoot.application.services.LeagueManagementService;
import br.com.saveeditor.brasfoot.application.services.ManagerManagementService;
import br.com.saveeditor.brasfoot.application.services.PlayerManagementService;
import br.com.saveeditor.brasfoot.application.services.SessionService;
import br.com.saveeditor.brasfoot.application.services.TeamManagementService;
import br.com.saveeditor.brasfoot.domain.CountryState;
import br.com.saveeditor.brasfoot.domain.League;
import br.com.saveeditor.brasfoot.domain.LeagueTableEntry;
import br.com.saveeditor.brasfoot.domain.Manager;
import br.com.saveeditor.brasfoot.domain.Player;
import br.com.saveeditor.brasfoot.domain.Team;
import br.com.saveeditor.brasfoot.domain.enums.TeamReputation;
import br.com.saveeditor.brasfoot.presentation.model.CountryRow;
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
import java.util.List;
import java.util.UUID;

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

    private BrasfootDesktopView view;
    private UUID currentSessionId;

    public BrasfootPresenter(SessionService sessionService,
                               TeamManagementService teamManagementService,
                               PlayerManagementService playerManagementService,
                               ManagerManagementService managerManagementService,
                               LeagueManagementService leagueManagementService,
                               CountryManagementService countryManagementService) {
        this.sessionService = sessionService;
        this.teamManagementService = teamManagementService;
        this.playerManagementService = playerManagementService;
        this.managerManagementService = managerManagementService;
        this.leagueManagementService = leagueManagementService;
        this.countryManagementService = countryManagementService;
    }

    public void attach(BrasfootDesktopView view) {
        this.view = view;
    }

    public void openSave(Path savePath) {
        try {
            String sessionId = sessionService.open(savePath);
            currentSessionId = UUID.fromString(sessionId);
            view.showSessionOpened(sessionId, savePath.toAbsolutePath().normalize());
            refreshTeams();
            refreshManagers();
            refreshLeagues();
            refreshCountries();
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

            teamManagementService.updateTeam(requireSession(), teamId, money, reputation, normalizedStadiumName, sectors);
            refreshTeams();
            view.showStatus("Time atualizado.");
        } catch (RuntimeException e) {
            showError("Falha ao atualizar time", e);
        }
    }

    public void updateTeam(int teamId, String name, String alias, String moneyText, String reputationText,
                           String stadiumName, List<String> stadiumSectorTexts, String countryText,
                           String divisionText, String levelText, int tacticStyle, int tacticMarking, int tacticFocus) {
        try {
            Long money = parseLong(moneyText, "dinheiro");
            TeamReputation reputation = TeamReputation.valueOf(reputationText);
            List<Integer> sectors = parseSectors(stadiumSectorTexts);
            teamManagementService.updateTeam(requireSession(), teamId, normalizeOptionalText(name), normalizeOptionalText(alias),
                    money, reputation, normalizeOptionalText(stadiumName), sectors, parseOptionalInteger(countryText, "pais"),
                    parseOptionalInteger(divisionText, "divisao"), parseOptionalInteger(levelText, "nivel"),
                    tacticStyle, tacticMarking, tacticFocus);
            refreshTeams();
            view.showStatus("Time atualizado.");
        } catch (RuntimeException e) {
            showError("Falha ao atualizar time", e);
        }
    }

    public void updatePlayer(int teamId, int playerId, int age, int overall, int position, int energy,
                             boolean starLocal, boolean starGlobal) {
        try {
            playerManagementService.updatePlayer(requireSession(), teamId, playerId, age, overall, position, energy,
                    null, starLocal, starGlobal);
            loadPlayers(teamId);
            view.showStatus("Jogador atualizado.");
        } catch (RuntimeException e) {
            showError("Falha ao atualizar jogador", e);
        }
    }

    public void updatePlayer(int teamId, int playerId, String name, int age, int overall, int position, int energy,
                             String salaryText, String sideText, String contractEndText, int characteristic1,
                             int characteristic2, int skillGoalkeeping, int skillSpeed, int skillTechnique,
                             int skillPassing, int skillTackling, int skillPlaymaking, int skillFinishing,
                             String countryText, boolean starLocal, boolean starGlobal) {
        try {
            playerManagementService.updatePlayer(requireSession(), teamId, playerId, normalizeOptionalText(name), age,
                    overall, position, energy, parseOptionalInteger(salaryText, "salario"),
                    parseOptionalInteger(sideText, "lado"), parseOptionalLong(contractEndText, "fim de contrato"),
                    characteristic1, characteristic2, skillGoalkeeping, skillSpeed, skillTechnique, skillPassing,
                    skillTackling, skillPlaymaking, skillFinishing, parseOptionalInteger(countryText, "pais"),
                    starLocal, starGlobal);
            loadPlayers(teamId);
            view.showStatus("Jogador atualizado.");
        } catch (RuntimeException e) {
            showError("Falha ao atualizar jogador", e);
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
            managerManagementService.updateManager(requireSession(), managerId, updateData);
            refreshManagers();
            view.showStatus("Tecnico atualizado.");
        } catch (RuntimeException e) {
            showError("Falha ao atualizar tecnico", e);
        }
    }

    public void updateManager(int managerId, String name, Boolean human, String teamIdText, int confidenceBoard,
                              int confidenceFans) {
        try {
            Manager updateData = Manager.builder()
                    .name(normalizeRequiredText(name, "nome"))
                    .isHuman(human)
                    .teamId(parseOptionalInteger(teamIdText, "time"))
                    .confidenceBoard(confidenceBoard)
                    .confidenceFans(confidenceFans)
                    .build();
            managerManagementService.updateManager(requireSession(), managerId, updateData);
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
            var response = playerManagementService.batchUpdatePlayers(sessionId, teamId, commands);
            long succeeded = response.getResults().stream().filter(r -> r.isSuccess()).count();
            loadPlayers(teamId);
            view.showStatus(succeeded + "/" + commands.size() + " jogadores atualizados em lote.");
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
            var response = teamManagementService.batchUpdateTeams(sessionId, commands);
            long succeeded = response.getResults().stream().filter(r -> r.isSuccess()).count();
            refreshTeams();
            view.showStatus(succeeded + "/" + commands.size() + " times atualizados em lote.");
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

            List<TeamBatchUpdateCommand> teamCommands = targetTeams.stream()
                    .map(team -> new TeamBatchUpdateCommand(team.getId(), DESTROYED_TEAM_MONEY,
                            TeamReputation.MUNICIPAL, null, DESTROYED_STADIUM_SECTORS, DESTROYED_TEAM_LEVEL))
                    .toList();
            var teamResponse = teamManagementService.batchUpdateTeams(sessionId, teamCommands);

            int playerCommandCount = 0;
            long playerSucceeded = 0;
            for (Team team : targetTeams) {
                List<PlayerBatchUpdateCommand> playerCommands = playerManagementService.getTeamPlayers(sessionId, team.getId()).stream()
                        .map(player -> destroyedPlayerCommand(player.getId()))
                        .toList();
                playerCommandCount += playerCommands.size();
                var playerResponse = playerManagementService.batchUpdatePlayers(sessionId, team.getId(), playerCommands);
                playerSucceeded += playerResponse.getResults().stream().filter(result -> result.isSuccess()).count();
            }

            long teamSucceeded = teamResponse.getResults().stream().filter(result -> result.isSuccess()).count();
            refreshTeams();
            view.showStatus(teamSucceeded + "/" + teamCommands.size() + " times destruidos; "
                    + playerSucceeded + "/" + playerCommandCount + " jogadores enfraquecidos. Time protegido: "
                    + protectedTeamId + ".");
        } catch (RuntimeException e) {
            showError("Falha ao destruir times do pais", e);
        }
    }

    private PlayerBatchUpdateCommand destroyedPlayerCommand(int playerId) {
        return new PlayerBatchUpdateCommand(playerId, null, DESTROYED_PLAYER_OVERALL, null, DESTROYED_PLAYER_ENERGY,
                null, false, false, null, DESTROYED_PLAYER_SKILL, DESTROYED_PLAYER_SKILL, DESTROYED_PLAYER_SKILL,
                DESTROYED_PLAYER_SKILL, DESTROYED_PLAYER_SKILL, DESTROYED_PLAYER_SKILL, DESTROYED_PLAYER_SKILL);
    }

    public void updateLeagueTableEntry(String leagueId, int teamId, int points, int wins, int draws, int losses,
                                        int goalsFor, int goalsAgainst) {
        try {
            leagueManagementService.updateTableEntry(requireSession(), leagueId, teamId, points, wins, draws, losses,
                    goalsFor, goalsAgainst);
            loadLeagueTable(leagueId);
            view.showStatus("Tabela atualizada. Jogos = V + E + D para preservar empates.");
        } catch (RuntimeException e) {
            showError("Falha ao atualizar tabela", e);
        }
    }

    public void updateCountryLevel(String countryId, int level) {
        try {
            countryManagementService.updateCountryLevel(requireSession(), countryId, level);
            refreshCountries();
            refreshLeagues();
            view.showStatus("Pais atualizado.");
        } catch (RuntimeException e) {
            showError("Falha ao atualizar pais", e);
        }
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
                manager.getConfidenceBoard(), manager.getConfidenceFans());
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

    private Long parseOptionalLong(String text, String fieldName) {
        String normalized = normalizeOptionalText(text);
        if (normalized == null) {
            return null;
        }
        try {
            return Long.parseLong(normalized);
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
