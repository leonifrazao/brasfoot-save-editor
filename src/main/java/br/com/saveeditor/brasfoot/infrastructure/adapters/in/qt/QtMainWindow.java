package br.com.saveeditor.brasfoot.infrastructure.adapters.in.qt;

import br.com.saveeditor.brasfoot.domain.enums.PlayerCharacteristic;
import br.com.saveeditor.brasfoot.domain.enums.PlayerPosition;
import br.com.saveeditor.brasfoot.domain.enums.PlayerSide;
import br.com.saveeditor.brasfoot.domain.enums.TeamAttackFocus;
import br.com.saveeditor.brasfoot.domain.enums.TeamMarking;
import br.com.saveeditor.brasfoot.domain.enums.TeamPlayStyle;
import br.com.saveeditor.brasfoot.domain.enums.TeamReputation;
import br.com.saveeditor.brasfoot.presentation.model.LeagueRow;
import br.com.saveeditor.brasfoot.presentation.model.LeagueTableRow;
import br.com.saveeditor.brasfoot.presentation.model.ManagerRow;
import br.com.saveeditor.brasfoot.presentation.model.PlayerRow;
import br.com.saveeditor.brasfoot.presentation.model.TeamRow;
import br.com.saveeditor.brasfoot.presentation.presenter.BrasfootPresenter;
import br.com.saveeditor.brasfoot.presentation.view.BrasfootDesktopView;
import io.qt.core.QDate;
import io.qt.widgets.QAbstractItemView;
import io.qt.widgets.QCheckBox;
import io.qt.widgets.QComboBox;
import io.qt.widgets.QDateEdit;
import io.qt.widgets.QFileDialog;
import io.qt.widgets.QFormLayout;
import io.qt.widgets.QGroupBox;
import io.qt.widgets.QHBoxLayout;
import io.qt.widgets.QHeaderView;
import io.qt.widgets.QLineEdit;
import io.qt.widgets.QMainWindow;
import io.qt.widgets.QMessageBox;
import io.qt.widgets.QPushButton;
import io.qt.widgets.QLabel;
import io.qt.widgets.QSpinBox;
import io.qt.widgets.QTabWidget;
import io.qt.widgets.QTableWidget;
import io.qt.widgets.QTableWidgetItem;
import io.qt.widgets.QVBoxLayout;
import io.qt.widgets.QWidget;

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class QtMainWindow extends QMainWindow implements BrasfootDesktopView {

    private final BrasfootPresenter presenter;

    private final QLabel sessionLabel = new QLabel("Nenhum save aberto");
    private final QLabel statusLabel = new QLabel("Abra um save .s22 para comecar.");

    private final QLineEdit teamsFilterEdit = new QLineEdit();
    private final QTableWidget teamsTable = table("ID", "Time", "Apelido", "Pais", "Div", "Nivel", "Dinheiro", "Reputacao", "Estilo", "Marcacao", "Ataques", "Estadio", "Capacidade");
    private final QLineEdit teamNameEdit = new QLineEdit();
    private final QLineEdit teamAliasEdit = new QLineEdit();
    private final QLineEdit teamCountryEdit = new QLineEdit();
    private final QLineEdit teamDivisionEdit = new QLineEdit();
    private final QLineEdit teamLevelEdit = new QLineEdit();
    private final QLineEdit teamMoneyEdit = new QLineEdit();
    private final QComboBox teamReputationCombo = new QComboBox();
    private final QLineEdit stadiumNameEdit = new QLineEdit();
    private final List<QLineEdit> stadiumSectorEdits = List.of(new QLineEdit(), new QLineEdit(), new QLineEdit(), new QLineEdit());
    private final QComboBox teamPlayStyleCombo = new QComboBox();
    private final QComboBox teamMarkingCombo = new QComboBox();
    private final QComboBox teamAttackFocusCombo = new QComboBox();

    private final QLineEdit playerTeamFilterEdit = new QLineEdit();
    private final QComboBox playerTeamCombo = new QComboBox();
    private final QTableWidget playersTable = table("ID", "Jogador", "Idade", "Forca", "Posicao", "Lado", "Energia", "Salario", "Cr1", "Cr2", "Estrelas");
    private final QLineEdit playerNameEdit = new QLineEdit();
    private final QSpinBox playerAgeSpin = spin(15, 50);
    private final QSpinBox playerOverallSpin = spin(1, 100);
    private final QComboBox playerPositionCombo = new QComboBox();
    private final QSpinBox playerEnergySpin = spin(-1, 100);
    private final QLineEdit playerSalaryEdit = new QLineEdit();
    private final QComboBox playerSideCombo = new QComboBox();
    private final QDateEdit playerContractEndDateEdit = new QDateEdit();
    private final QComboBox playerCharacteristic1Combo = new QComboBox();
    private final QComboBox playerCharacteristic2Combo = new QComboBox();
    private final QSpinBox playerSkillGoalkeepingSpin = spin(0, 100);
    private final QSpinBox playerSkillSpeedSpin = spin(0, 100);
    private final QSpinBox playerSkillTechniqueSpin = spin(0, 100);
    private final QSpinBox playerSkillPassingSpin = spin(0, 100);
    private final QSpinBox playerSkillTacklingSpin = spin(0, 100);
    private final QSpinBox playerSkillPlaymakingSpin = spin(0, 100);
    private final QSpinBox playerSkillFinishingSpin = spin(0, 100);
    private final QCheckBox playerStarLocalCheck = new QCheckBox("Estrela local");
    private final QCheckBox playerStarGlobalCheck = new QCheckBox("Estrela mundial");

    private final QTableWidget managersTable = table("ID", "Tecnico", "Time", "Humano", "Diretoria", "Torcida");
    private final QLineEdit managerNameEdit = new QLineEdit();
    private final QLineEdit managerTeamIdEdit = new QLineEdit();
    private final QCheckBox managerHumanCheck = new QCheckBox("Tecnico humano");
    private final QSpinBox managerBoardSpin = spin(0, 100);
    private final QSpinBox managerFansSpin = spin(0, 100);

    private final QComboBox leagueCombo = new QComboBox();
    private final QTableWidget leagueTable = table("Pos", "Team ID", "Time", "Pts", "J", "V", "E", "D", "GP", "GC", "SG");
    private final QSpinBox leaguePointsSpin = spin(-999, 999);
    private final QSpinBox leagueWinsSpin = spin(0, 999);
    private final QSpinBox leagueDrawsSpin = spin(0, 999);
    private final QSpinBox leagueLossesSpin = spin(0, 999);
    private final QSpinBox leagueGoalsForSpin = spin(0, 9999);
    private final QSpinBox leagueGoalsAgainstSpin = spin(0, 9999);

    private final List<TeamRow> teams = new ArrayList<>();
    private final List<PlayerRow> players = new ArrayList<>();
    private final List<ManagerRow> managers = new ArrayList<>();
    private final List<LeagueRow> leagues = new ArrayList<>();
    private final List<LeagueTableRow> leagueEntries = new ArrayList<>();

    private String currentLeagueId;

    private boolean updatingUi;

    public QtMainWindow(BrasfootPresenter presenter) {
        this.presenter = presenter;
        this.presenter.attach(this);
        setWindowTitle("Brasfoot Save Editor");
        resize(1180, 760);
        buildUi();
    }

    @Override
    public void showSessionOpened(String sessionId, Path savePath) {
        sessionLabel.setText("Sessao " + sessionId + " - " + savePath);
        statusLabel.setText("Save aberto.");
    }

    @Override
    public void showSaveWritten(Path outputPath) {
        statusLabel.setText("Copia salva em " + outputPath);
        QMessageBox.information(this, "Save salvo", "Copia salva em:\n" + outputPath);
    }

    @Override
    public void showTeams(List<TeamRow> rows) {
        updatingUi = true;
        teams.clear();
        teams.addAll(rows);
        fillTeamsTable();
        fillPlayerTeamCombo();
        updatingUi = false;

        if (!teams.isEmpty()) {
            teamsTable.selectRow(0);
            updateTeamEditor();
            if (playerTeamCombo.count() > 0) {
                playerTeamCombo.setCurrentIndex(0);
            }
        }
    }

    @Override
    public void showPlayers(int teamId, List<PlayerRow> rows) {
        updatingUi = true;
        players.clear();
        players.addAll(rows);
        fillPlayersTable();
        updatingUi = false;

        if (!players.isEmpty()) {
            playersTable.selectRow(0);
            updatePlayerEditor();
        }
    }

    @Override
    public void showManagers(List<ManagerRow> rows) {
        updatingUi = true;
        managers.clear();
        managers.addAll(rows);
        fillManagersTable();
        updatingUi = false;

        if (!managers.isEmpty()) {
            managersTable.selectRow(0);
            updateManagerEditor();
        }
    }

    @Override
    public void showLeagues(List<LeagueRow> rows) {
        updatingUi = true;
        leagues.clear();
        leagues.addAll(rows);
        leagueCombo.clear();
        for (LeagueRow league : leagues) {
            leagueCombo.addItem(league.id() + " - " + league.name() + " (" + league.teamCount() + ")");
        }
        updatingUi = false;
        if (!leagues.isEmpty()) {
            leagueCombo.setCurrentIndex(0);
            loadSelectedLeagueTable();
        }
    }

    @Override
    public void showLeagueTable(String leagueId, List<LeagueTableRow> rows) {
        updatingUi = true;
        currentLeagueId = leagueId;
        leagueEntries.clear();
        leagueEntries.addAll(rows);
        fillLeagueTable();
        updatingUi = false;
        if (!leagueEntries.isEmpty()) {
            leagueTable.selectRow(0);
            updateLeagueEditor();
        }
    }

    @Override
    public void showStatus(String message) {
        statusLabel.setText(message);
    }

    @Override
    public void showError(String title, String message) {
        statusLabel.setText(message);
        QMessageBox.critical(this, title, message);
    }

    private void buildUi() {
        QWidget central = new QWidget();
        QVBoxLayout rootLayout = new QVBoxLayout(central);
        rootLayout.addLayout(buildToolbar());

        QTabWidget tabs = new QTabWidget();
        tabs.addTab(buildTeamsTab(), "Times");
        tabs.addTab(buildPlayersTab(), "Jogadores");
        tabs.addTab(buildManagersTab(), "Tecnicos");
        tabs.addTab(buildLeaguesTab(), "Ligas");
        rootLayout.addWidget(tabs);
        rootLayout.addWidget(statusLabel);

        setCentralWidget(central);
    }

    private QHBoxLayout buildToolbar() {
        QPushButton openButton = new QPushButton("Abrir save");
        QPushButton saveButton = new QPushButton("Salvar copia");
        openButton.clicked.connect(this::openSave);
        saveButton.clicked.connect(presenter::saveCopy);

        QHBoxLayout toolbar = new QHBoxLayout();
        toolbar.addWidget(openButton);
        toolbar.addWidget(saveButton);
        toolbar.addWidget(sessionLabel);
        toolbar.addStretch();
        return toolbar;
    }

    private QWidget buildTeamsTab() {
        teamsTable.itemSelectionChanged.connect(this::updateTeamEditor);
        teamsFilterEdit.setPlaceholderText("Pesquisar time por nome, ID, apelido, pais, divisao ou estadio");
        teamsFilterEdit.textChanged.connect(this::filterTeams);
        configureTeamEnumControls();

        for (TeamReputation reputation : TeamReputation.values()) {
            teamReputationCombo.addItem(reputation.name());
        }

        QPushButton saveTeamButton = new QPushButton("Salvar time");
        saveTeamButton.clicked.connect(this::saveSelectedTeam);

        QFormLayout form = new QFormLayout();
        form.addRow("Nome", teamNameEdit);
        form.addRow("Apelido", teamAliasEdit);
        form.addRow("Pais", teamCountryEdit);
        form.addRow("Divisao", teamDivisionEdit);
        form.addRow("Nivel", teamLevelEdit);
        form.addRow("Dinheiro", teamMoneyEdit);
        form.addRow("Reputacao", teamReputationCombo);
        form.addRow("Estadio", stadiumNameEdit);
        form.addRow("Setor 1", stadiumSectorEdits.get(0));
        form.addRow("Setor 2", stadiumSectorEdits.get(1));
        form.addRow("Setor 3", stadiumSectorEdits.get(2));
        form.addRow("Setor 4", stadiumSectorEdits.get(3));
        form.addRow("Estilo", teamPlayStyleCombo);
        form.addRow("Marcacao", teamMarkingCombo);
        form.addRow("Ataques", teamAttackFocusCombo);
        form.addRow(saveTeamButton);

        QGroupBox editor = new QGroupBox("Editor do time");
        editor.setLayout(form);

        QVBoxLayout tableLayout = new QVBoxLayout();
        tableLayout.addWidget(teamsFilterEdit);
        tableLayout.addWidget(teamsTable);

        QHBoxLayout layout = new QHBoxLayout();
        layout.addLayout(tableLayout, 3);
        layout.addWidget(editor, 1);

        QWidget tab = new QWidget();
        tab.setLayout(layout);
        return tab;
    }

    private void configureTeamEnumControls() {
        if (teamPlayStyleCombo.count() == 0) {
            for (TeamPlayStyle style : TeamPlayStyle.values()) {
                teamPlayStyleCombo.addItem(style.getDisplayName());
            }
        }
        if (teamMarkingCombo.count() == 0) {
            for (TeamMarking marking : TeamMarking.values()) {
                teamMarkingCombo.addItem(marking.getDisplayName());
            }
        }
        if (teamAttackFocusCombo.count() == 0) {
            for (TeamAttackFocus focus : TeamAttackFocus.values()) {
                teamAttackFocusCombo.addItem(focus.getDisplayName());
            }
        }
    }

    private QWidget buildPlayersTab() {
        QPushButton loadPlayersButton = new QPushButton("Carregar jogadores do time");
        loadPlayersButton.clicked.connect(this::loadPlayersForSelectedTeam);
        playersTable.itemSelectionChanged.connect(this::updatePlayerEditor);
        playerTeamFilterEdit.setPlaceholderText("Pesquisar time por nome, ID, apelido, pais, divisao ou estadio");
        playerTeamFilterEdit.textChanged.connect(this::filterPlayerTeams);
        configurePlayerEnumControls();

        QPushButton savePlayerButton = new QPushButton("Salvar jogador");
        savePlayerButton.clicked.connect(this::saveSelectedPlayer);

        QFormLayout form = new QFormLayout();
        form.addRow("Pesquisar time", playerTeamFilterEdit);
        form.addRow("Time", playerTeamCombo);
        form.addRow(loadPlayersButton);
        form.addRow("Nome", playerNameEdit);
        form.addRow("Idade", playerAgeSpin);
        form.addRow("Forca", playerOverallSpin);
        form.addRow("Posicao", playerPositionCombo);
        form.addRow("Energia", playerEnergySpin);
        form.addRow("Salario", playerSalaryEdit);
        form.addRow("Lado", playerSideCombo);
        form.addRow("Fim contrato", playerContractEndDateEdit);
        form.addRow("Caracteristica 1", playerCharacteristic1Combo);
        form.addRow("Caracteristica 2", playerCharacteristic2Combo);
        form.addRow("Hab. goleiro", playerSkillGoalkeepingSpin);
        form.addRow("Hab. velocidade", playerSkillSpeedSpin);
        form.addRow("Hab. tecnica", playerSkillTechniqueSpin);
        form.addRow("Hab. passe", playerSkillPassingSpin);
        form.addRow("Hab. desarme", playerSkillTacklingSpin);
        form.addRow("Hab. armacao", playerSkillPlaymakingSpin);
        form.addRow("Hab. finalizacao", playerSkillFinishingSpin);
        form.addRow(playerStarLocalCheck);
        form.addRow(playerStarGlobalCheck);
        form.addRow(savePlayerButton);

        QGroupBox editor = new QGroupBox("Editor do jogador");
        editor.setLayout(form);

        QHBoxLayout layout = new QHBoxLayout();
        layout.addWidget(playersTable, 3);
        layout.addWidget(editor, 1);

        QWidget tab = new QWidget();
        tab.setLayout(layout);
        return tab;
    }

    private void configurePlayerEnumControls() {
        if (playerPositionCombo.count() == 0) {
            for (PlayerPosition position : PlayerPosition.values()) {
                playerPositionCombo.addItem(position.getDisplayName());
            }
        }
        if (playerSideCombo.count() == 0) {
            for (PlayerSide side : PlayerSide.values()) {
                playerSideCombo.addItem(side.getDisplayName());
            }
        }
        if (playerCharacteristic1Combo.count() == 0) {
            for (PlayerCharacteristic characteristic : PlayerCharacteristic.values()) {
                playerCharacteristic1Combo.addItem(characteristic.getDisplayName());
                playerCharacteristic2Combo.addItem(characteristic.getDisplayName());
            }
        }
        playerContractEndDateEdit.setCalendarPopup(true);
        playerContractEndDateEdit.setDisplayFormat("dd/MM/yyyy");
        playerContractEndDateEdit.setDateRange(new QDate(1970, 1, 1), new QDate(2100, 12, 31));
        playerContractEndDateEdit.setDate(QDate.currentDate());
    }

    private QWidget buildManagersTab() {
        managersTable.itemSelectionChanged.connect(this::updateManagerEditor);

        QPushButton saveManagerButton = new QPushButton("Salvar tecnico");
        saveManagerButton.clicked.connect(this::saveSelectedManager);

        QFormLayout form = new QFormLayout();
        form.addRow("Nome", managerNameEdit);
        form.addRow("Time", managerTeamIdEdit);
        form.addRow(managerHumanCheck);
        form.addRow("Confianca diretoria", managerBoardSpin);
        form.addRow("Confianca torcida", managerFansSpin);
        form.addRow(saveManagerButton);

        QGroupBox editor = new QGroupBox("Editor do tecnico");
        editor.setLayout(form);

        QHBoxLayout layout = new QHBoxLayout();
        layout.addWidget(managersTable, 3);
        layout.addWidget(editor, 1);

        QWidget tab = new QWidget();
        tab.setLayout(layout);
        return tab;
    }

    private QWidget buildLeaguesTab() {
        QPushButton loadLeagueButton = new QPushButton("Carregar tabela");
        loadLeagueButton.clicked.connect(this::loadSelectedLeagueTable);
        leagueTable.itemSelectionChanged.connect(this::updateLeagueEditor);

        QPushButton saveLeagueRowButton = new QPushButton("Salvar linha da tabela");
        saveLeagueRowButton.clicked.connect(this::saveSelectedLeagueEntry);

        QFormLayout form = new QFormLayout();
        form.addRow("Liga", leagueCombo);
        form.addRow(loadLeagueButton);
        form.addRow("Pontos", leaguePointsSpin);
        form.addRow("Vitorias", leagueWinsSpin);
        form.addRow("Empates", leagueDrawsSpin);
        form.addRow("Derrotas", leagueLossesSpin);
        form.addRow("GP", leagueGoalsForSpin);
        form.addRow("GC", leagueGoalsAgainstSpin);
        form.addRow(saveLeagueRowButton);

        QGroupBox editor = new QGroupBox("Tabela da liga");
        editor.setLayout(form);

        QHBoxLayout layout = new QHBoxLayout();
        layout.addWidget(leagueTable, 3);
        layout.addWidget(editor, 1);

        QWidget tab = new QWidget();
        tab.setLayout(layout);
        return tab;
    }

    private void openSave() {
        String path = QFileDialog.getOpenFileName(this, "Abrir save Brasfoot", "", "Brasfoot save (*.s22)").result;
        if (path == null || path.isBlank()) {
            return;
        }
        presenter.openSave(Path.of(path));
    }

    private void saveSelectedTeam() {
        TeamRow team = selectedTeam();
        if (team == null) {
            showError("Time nao selecionado", "Selecione um time antes de salvar.");
            return;
        }

        presenter.updateTeam(team.id(), teamNameEdit.text(), teamAliasEdit.text(), teamMoneyEdit.text(),
                teamReputationCombo.currentText(), stadiumNameEdit.text(),
                stadiumSectorEdits.stream().map(QLineEdit::text).toList(), teamCountryEdit.text(),
                teamDivisionEdit.text(), teamLevelEdit.text(), selectedPlayStyleCode(),
                selectedMarkingCode(), selectedAttackFocusCode());
    }

    private void loadPlayersForSelectedTeam() {
        Integer teamId = selectedPlayerTeamId();
        if (teamId == null) {
            showError("Time nao selecionado", "Selecione um time para carregar jogadores.");
            return;
        }
        presenter.loadPlayers(teamId);
    }

    private void saveSelectedPlayer() {
        Integer teamId = selectedPlayerTeamId();
        PlayerRow player = selectedPlayer();
        if (teamId == null || player == null) {
            showError("Jogador nao selecionado", "Selecione um time e um jogador antes de salvar.");
            return;
        }

        presenter.updatePlayer(teamId, player.id(), playerNameEdit.text(), playerAgeSpin.value(),
                playerOverallSpin.value(), selectedPositionCode(), playerEnergySpin.value(),
                playerSalaryEdit.text(), String.valueOf(selectedSideCode()), String.valueOf(timestampFromDate(playerContractEndDateEdit.date())),
                selectedCharacteristicCode(playerCharacteristic1Combo), selectedCharacteristicCode(playerCharacteristic2Combo), playerSkillGoalkeepingSpin.value(),
                playerSkillSpeedSpin.value(), playerSkillTechniqueSpin.value(), playerSkillPassingSpin.value(),
                playerSkillTacklingSpin.value(), playerSkillPlaymakingSpin.value(), playerSkillFinishingSpin.value(),
                playerStarLocalCheck.isChecked(), playerStarGlobalCheck.isChecked());
    }

    private void saveSelectedManager() {
        ManagerRow manager = selectedManager();
        if (manager == null) {
            showError("Tecnico nao selecionado", "Selecione um tecnico antes de salvar.");
            return;
        }

        presenter.updateManager(manager.id(), managerNameEdit.text(), managerHumanCheck.isChecked(), managerTeamIdEdit.text(),
                managerBoardSpin.value(), managerFansSpin.value());
    }

    private void loadSelectedLeagueTable() {
        String leagueId = selectedLeagueId();
        if (leagueId == null) {
            showError("Liga nao selecionada", "Selecione uma liga antes de carregar a tabela.");
            return;
        }
        presenter.loadLeagueTable(leagueId);
    }

    private void saveSelectedLeagueEntry() {
        LeagueTableRow entry = selectedLeagueEntry();
        if (currentLeagueId == null || entry == null) {
            showError("Linha nao selecionada", "Selecione uma linha da tabela antes de salvar.");
            return;
        }
        presenter.updateLeagueTableEntry(currentLeagueId, entry.teamId(), leaguePointsSpin.value(),
                leagueWinsSpin.value(), leagueDrawsSpin.value(), leagueLossesSpin.value(), leagueGoalsForSpin.value(),
                leagueGoalsAgainstSpin.value());
    }

    private void updateTeamEditor() {
        if (updatingUi) {
            return;
        }
        TeamRow team = selectedTeam();
        if (team == null) {
            return;
        }

        teamNameEdit.setText(value(team.name()));
        teamAliasEdit.setText(value(team.alias()));
        teamCountryEdit.setText(value(team.country()));
        teamDivisionEdit.setText(value(team.division()));
        teamLevelEdit.setText(value(team.level()));
        teamMoneyEdit.setText(String.valueOf(team.money()));
        teamReputationCombo.setCurrentText(team.reputation());
        stadiumNameEdit.setText(team.stadiumName() == null ? "" : team.stadiumName());
        for (int i = 0; i < stadiumSectorEdits.size(); i++) {
            Integer value = team.stadiumSectors() != null && team.stadiumSectors().size() > i
                    ? team.stadiumSectors().get(i)
                    : null;
            stadiumSectorEdits.get(i).setText(value == null ? "" : String.valueOf(value));
        }
        teamPlayStyleCombo.setCurrentIndex(TeamPlayStyle.fromCode(team.tacticStyle()).getCode());
        teamMarkingCombo.setCurrentIndex(TeamMarking.fromCode(team.tacticMarking()).getCode());
        teamAttackFocusCombo.setCurrentIndex(TeamAttackFocus.fromCode(team.tacticFocus()).getCode());
    }

    private void updatePlayerEditor() {
        if (updatingUi) {
            return;
        }
        PlayerRow player = selectedPlayer();
        if (player == null) {
            return;
        }

        playerNameEdit.setText(value(player.name()));
        playerAgeSpin.setValue(player.age());
        playerOverallSpin.setValue(player.overall());
        playerPositionCombo.setCurrentIndex(PlayerPosition.fromCode(player.position()).getCode());
        playerEnergySpin.setValue(player.energy());
        playerSalaryEdit.setText(value(player.salary()));
        playerSideCombo.setCurrentIndex(PlayerSide.fromCode(player.side()).getCode());
        playerContractEndDateEdit.setDate(dateFromTimestamp(player.contractEnd()));
        playerCharacteristic1Combo.setCurrentIndex(PlayerCharacteristic.fromCode(player.characteristic1()).getCode());
        playerCharacteristic2Combo.setCurrentIndex(PlayerCharacteristic.fromCode(player.characteristic2()).getCode());
        playerSkillGoalkeepingSpin.setValue(player.skillGoalkeeping() == null ? 0 : player.skillGoalkeeping());
        playerSkillSpeedSpin.setValue(player.skillSpeed() == null ? 0 : player.skillSpeed());
        playerSkillTechniqueSpin.setValue(player.skillTechnique() == null ? 0 : player.skillTechnique());
        playerSkillPassingSpin.setValue(player.skillPassing() == null ? 0 : player.skillPassing());
        playerSkillTacklingSpin.setValue(player.skillTackling() == null ? 0 : player.skillTackling());
        playerSkillPlaymakingSpin.setValue(player.skillPlaymaking() == null ? 0 : player.skillPlaymaking());
        playerSkillFinishingSpin.setValue(player.skillFinishing() == null ? 0 : player.skillFinishing());
        playerStarLocalCheck.setChecked(player.starLocal());
        playerStarGlobalCheck.setChecked(player.starGlobal());
    }

    private void updateManagerEditor() {
        if (updatingUi) {
            return;
        }
        ManagerRow manager = selectedManager();
        if (manager == null) {
            return;
        }

        managerNameEdit.setText(manager.name() == null ? "" : manager.name());
        managerTeamIdEdit.setText(value(manager.teamId()));
        managerHumanCheck.setChecked(Boolean.TRUE.equals(manager.human()));
        managerBoardSpin.setValue(manager.confidenceBoard() == null ? 0 : manager.confidenceBoard());
        managerFansSpin.setValue(manager.confidenceFans() == null ? 0 : manager.confidenceFans());
    }

    private void updateLeagueEditor() {
        if (updatingUi) {
            return;
        }
        LeagueTableRow entry = selectedLeagueEntry();
        if (entry == null) {
            return;
        }
        leaguePointsSpin.setValue(entry.points());
        leagueWinsSpin.setValue(entry.wins());
        leagueDrawsSpin.setValue(entry.draws());
        leagueLossesSpin.setValue(entry.losses());
        leagueGoalsForSpin.setValue(entry.goalsFor());
        leagueGoalsAgainstSpin.setValue(entry.goalsAgainst());
    }

    private void filterTeams(String ignored) {
        fillTeamsTable();
        if (teamsTable.rowCount() > 0) {
            teamsTable.selectRow(0);
            updateTeamEditor();
        }
    }

    private void filterPlayerTeams(String ignored) {
        fillPlayerTeamCombo();
        if (playerTeamCombo.count() > 0) {
            playerTeamCombo.setCurrentIndex(0);
        }
    }

    private void fillTeamsTable() {
        List<TeamRow> filteredTeams = filteredTeams(teamsFilterEdit.text());
        teamsTable.setRowCount(filteredTeams.size());
        for (int row = 0; row < filteredTeams.size(); row++) {
            TeamRow team = filteredTeams.get(row);
            setRow(teamsTable, row, String.valueOf(team.id()), team.name(), value(team.alias()), value(team.country()),
                    value(team.division()), value(team.level()), String.valueOf(team.money()), team.reputation(),
                    playStyleLabel(team.tacticStyle()), markingLabel(team.tacticMarking()), attackFocusLabel(team.tacticFocus()),
                    value(team.stadiumName()), value(team.stadiumCapacity()));
        }
    }

    private void fillPlayersTable() {
        playersTable.setRowCount(players.size());
        for (int row = 0; row < players.size(); row++) {
            PlayerRow player = players.get(row);
            setRow(playersTable, row, String.valueOf(player.id()), player.name(), String.valueOf(player.age()),
                    String.valueOf(player.overall()), positionLabel(player.position()), sideLabel(player.side()), String.valueOf(player.energy()),
                    value(player.salary()), characteristicLabel(player.characteristic1()), characteristicLabel(player.characteristic2()), stars(player));
        }
    }

    private void fillLeagueTable() {
        leagueTable.setRowCount(leagueEntries.size());
        for (int row = 0; row < leagueEntries.size(); row++) {
            LeagueTableRow entry = leagueEntries.get(row);
            setRow(leagueTable, row, String.valueOf(entry.position()), String.valueOf(entry.teamId()), entry.teamName(),
                    String.valueOf(entry.points()), String.valueOf(entry.played()), String.valueOf(entry.wins()),
                    String.valueOf(entry.draws()), String.valueOf(entry.losses()), String.valueOf(entry.goalsFor()),
                    String.valueOf(entry.goalsAgainst()), String.valueOf(entry.goalDifference()));
        }
    }

    private void fillManagersTable() {
        managersTable.setRowCount(managers.size());
        for (int row = 0; row < managers.size(); row++) {
            ManagerRow manager = managers.get(row);
            setRow(managersTable, row, String.valueOf(manager.id()), value(manager.name()), value(manager.teamId()),
                    Boolean.TRUE.equals(manager.human()) ? "Sim" : "Nao", value(manager.confidenceBoard()),
                    value(manager.confidenceFans()));
        }
    }

    private void fillPlayerTeamCombo() {
        playerTeamCombo.clear();
        for (TeamRow team : filteredTeams(playerTeamFilterEdit.text())) {
            playerTeamCombo.addItem(team.id() + " - " + team.name());
        }
    }

    private List<TeamRow> filteredTeams(String filterText) {
        String filter = normalize(filterText);
        if (filter.isBlank()) {
            return teams;
        }
        return teams.stream()
                .filter(team -> normalize(searchText(team)).contains(filter))
                .toList();
    }

    private String searchText(TeamRow team) {
        return String.join(" ",
                String.valueOf(team.id()),
                value(team.name()),
                value(team.alias()),
                value(team.country()),
                value(team.division()),
                value(team.level()),
                value(team.reputation()),
                value(team.stadiumName()));
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.trim().toLowerCase(Locale.ROOT);
    }

    private TeamRow selectedTeam() {
        Integer id = selectedTableId(teamsTable);
        if (id == null) {
            return null;
        }
        return teams.stream().filter(team -> team.id() == id).findFirst().orElse(null);
    }

    private PlayerRow selectedPlayer() {
        Integer id = selectedTableId(playersTable);
        if (id == null) {
            return null;
        }
        return players.stream().filter(player -> player.id() == id).findFirst().orElse(null);
    }

    private ManagerRow selectedManager() {
        Integer id = selectedTableId(managersTable);
        if (id == null) {
            return null;
        }
        return managers.stream().filter(manager -> manager.id() == id).findFirst().orElse(null);
    }

    private LeagueTableRow selectedLeagueEntry() {
        Integer teamId = selectedTableId(leagueTable, 1);
        if (teamId == null) {
            return null;
        }
        return leagueEntries.stream().filter(entry -> entry.teamId() == teamId).findFirst().orElse(null);
    }

    private Integer selectedTableId(QTableWidget table) {
        return selectedTableId(table, 0);
    }

    private Integer selectedTableId(QTableWidget table, int column) {
        int row = table.currentRow();
        if (row < 0 || table.item(row, column) == null) {
            return null;
        }
        return Integer.parseInt(table.item(row, column).text());
    }

    private String selectedLeagueId() {
        String text = leagueCombo.currentText();
        if (text == null || text.isBlank()) {
            return null;
        }
        int separator = text.indexOf(" - ");
        return separator >= 0 ? text.substring(0, separator) : text;
    }

    private Integer selectedPlayerTeamId() {
        String text = playerTeamCombo.currentText();
        if (text == null || text.isBlank()) {
            return null;
        }
        int separator = text.indexOf(" - ");
        String idText = separator >= 0 ? text.substring(0, separator) : text;
        return Integer.parseInt(idText);
    }

    private QTableWidget table(String... headers) {
        QTableWidget table = new QTableWidget();
        table.setColumnCount(headers.length);
        table.setHorizontalHeaderLabels(Arrays.asList(headers));
        table.setSelectionBehavior(QAbstractItemView.SelectionBehavior.SelectRows);
        table.setSelectionMode(QAbstractItemView.SelectionMode.SingleSelection);
        table.setEditTriggers(QAbstractItemView.EditTrigger.NoEditTriggers);
        table.horizontalHeader().setSectionResizeMode(QHeaderView.ResizeMode.Stretch);
        return table;
    }

    private QSpinBox spin(int minimum, int maximum) {
        QSpinBox spinBox = new QSpinBox();
        spinBox.setRange(minimum, maximum);
        return spinBox;
    }

    private int selectedCharacteristicCode(QComboBox comboBox) {
        int index = comboBox.currentIndex();
        return index < 0 ? PlayerCharacteristic.POSITIONING.getCode() : index;
    }

    private int selectedPositionCode() {
        int index = playerPositionCombo.currentIndex();
        return index < 0 ? PlayerPosition.GOALKEEPER.getCode() : index;
    }

    private int selectedSideCode() {
        int index = playerSideCombo.currentIndex();
        return index < 0 ? PlayerSide.RIGHT.getCode() : index;
    }

    private int selectedPlayStyleCode() {
        int index = teamPlayStyleCombo.currentIndex();
        return index < 0 ? TeamPlayStyle.BALANCED.getCode() : index;
    }

    private int selectedMarkingCode() {
        int index = teamMarkingCombo.currentIndex();
        return index < 0 ? TeamMarking.LIGHT.getCode() : index;
    }

    private int selectedAttackFocusCode() {
        int index = teamAttackFocusCombo.currentIndex();
        return index < 0 ? TeamAttackFocus.CENTER.getCode() : index;
    }

    private QDate dateFromTimestamp(Long timestamp) {
        if (timestamp == null || timestamp <= 0) {
            return QDate.currentDate();
        }

        long epochMillis = timestamp < 100_000_000_000L ? timestamp * 1000 : timestamp;
        LocalDate date = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate();
        return new QDate(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    }

    private long timestampFromDate(QDate date) {
        LocalDate localDate = LocalDate.of(date.year(), date.month(), date.day());
        return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private void setRow(QTableWidget table, int row, String... values) {
        for (int column = 0; column < values.length; column++) {
            table.setItem(row, column, new QTableWidgetItem(values[column]));
        }
    }

    private String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String characteristicLabel(Integer code) {
        return PlayerCharacteristic.fromCode(code).getAbbreviation();
    }

    private String positionLabel(Integer code) {
        return PlayerPosition.fromCode(code).getAbbreviation();
    }

    private String sideLabel(Integer code) {
        return PlayerSide.fromCode(code).getAbbreviation();
    }

    private String playStyleLabel(Integer code) {
        return TeamPlayStyle.fromCode(code).getLabel();
    }

    private String markingLabel(Integer code) {
        return TeamMarking.fromCode(code).getLabel();
    }

    private String attackFocusLabel(Integer code) {
        return TeamAttackFocus.fromCode(code).getLabel();
    }

    private String stars(PlayerRow player) {
        List<String> values = new ArrayList<>();
        if (player.starLocal()) {
            values.add("Local");
        }
        if (player.starGlobal()) {
            values.add("Mundial");
        }
        return values.isEmpty() ? "" : String.join(" / ", values);
    }
}
