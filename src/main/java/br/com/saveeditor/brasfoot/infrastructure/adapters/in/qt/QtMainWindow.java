package br.com.saveeditor.brasfoot.infrastructure.adapters.in.qt;

import br.com.saveeditor.brasfoot.domain.enums.Country;
import br.com.saveeditor.brasfoot.domain.enums.PlayerCharacteristic;
import br.com.saveeditor.brasfoot.domain.enums.PlayerPosition;
import br.com.saveeditor.brasfoot.domain.enums.PlayerSide;
import br.com.saveeditor.brasfoot.domain.enums.TeamAttackFocus;
import br.com.saveeditor.brasfoot.domain.enums.TeamMarking;
import br.com.saveeditor.brasfoot.domain.enums.TeamPlayStyle;
import br.com.saveeditor.brasfoot.domain.ManagerTrophy;
import br.com.saveeditor.brasfoot.domain.ManagerTrophyCompetition;
import br.com.saveeditor.brasfoot.domain.enums.TeamReputation;
import br.com.saveeditor.brasfoot.presentation.model.CountryRow;
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
    private final QComboBox teamCountryCombo = new QComboBox();
    private final QLineEdit teamDivisionEdit = new QLineEdit();
    private final QLineEdit teamLevelEdit = new QLineEdit();
    private final QLineEdit teamMoneyEdit = new QLineEdit();
    private final QComboBox teamReputationCombo = new QComboBox();
    private final QLineEdit stadiumNameEdit = new QLineEdit();
    private final List<QLineEdit> stadiumSectorEdits = List.of(new QLineEdit(), new QLineEdit(), new QLineEdit(), new QLineEdit());
    private final QComboBox teamPlayStyleCombo = new QComboBox();
    private final QComboBox teamMarkingCombo = new QComboBox();
    private final QComboBox teamAttackFocusCombo = new QComboBox();

    private final QComboBox batchTeamCountryCombo = new QComboBox();
    private final QLineEdit batchTeamMoneyEdit = new QLineEdit();
    private final QPushButton batchTeamApplyButton = new QPushButton("Aplicar em lote para times do pais");
    private final QPushButton destroyCountryTeamsButton = new QPushButton("Destruir outros times do pais");

    private final QComboBox batchPlayerCountryCombo = new QComboBox();
    private final QSpinBox batchPlayerAgeSpin = spin(0, 99);
    private final QSpinBox batchPlayerOverallSpin = spin(0, 100);
    private final QSpinBox batchPlayerEnergySpin = spin(-1, 100, -1);
    private final QSpinBox batchPlayerSkillGoalkeepingSpin = spin(-1, 100, -1);
    private final QSpinBox batchPlayerSkillSpeedSpin = spin(-1, 100, -1);
    private final QSpinBox batchPlayerSkillTechniqueSpin = spin(-1, 100, -1);
    private final QSpinBox batchPlayerSkillPassingSpin = spin(-1, 100, -1);
    private final QSpinBox batchPlayerSkillTacklingSpin = spin(-1, 100, -1);
    private final QSpinBox batchPlayerSkillPlaymakingSpin = spin(-1, 100, -1);
    private final QSpinBox batchPlayerSkillFinishingSpin = spin(-1, 100, -1);
    private final QCheckBox batchPlayerStarLocalCheck = new QCheckBox("Estrela local");
    private final QCheckBox batchPlayerStarGlobalCheck = new QCheckBox("Estrela mundial");
    private final QPushButton batchPlayerApplyButton = new QPushButton("Aplicar em lote para todos jogadores do time");

    private final QLineEdit playerTeamFilterEdit = new QLineEdit();
    private final QComboBox playerTeamCombo = new QComboBox();
    private final QTableWidget playersTable = table("ID", "Jogador", "Idade", "Forca", "Posicao", "Lado", "Pais", "Energia", "Salario", "Cr1", "Cr2", "Estrelas");
    private final QLineEdit playerNameEdit = new QLineEdit();
    private final QSpinBox playerAgeSpin = spin(15, 50);
    private final QSpinBox playerOverallSpin = spin(1, 100);
    private final QComboBox playerPositionCombo = new QComboBox();
    private final QSpinBox playerEnergySpin = spin(-1, 100);
    private final QLineEdit playerSalaryEdit = new QLineEdit();
    private final QComboBox playerSideCombo = new QComboBox();
    private final QDateEdit playerContractEndDateEdit = new QDateEdit();
    private final QComboBox playerCountryCombo = new QComboBox();
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

    private final QTableWidget managersTable = table("ID", "Tecnico", "Time", "Humano", "Diretoria", "Torcida", "Trofeus");
    private final QTableWidget managerTrophiesTable = table("Idx", "Temporada", "Tipo", "Variante", "Time", "Competicao");
    private final QLineEdit managerFilterEdit = new QLineEdit();
    private final QCheckBox managerHumansOnlyCheck = new QCheckBox("Somente humanos");
    private final QSpinBox managerMaxTeamIdFilterSpin = spin(-1, 999999, 999999);
    private final QLineEdit managerNameEdit = new QLineEdit();
    private final QLineEdit managerTeamFilterEdit = new QLineEdit();
    private final QComboBox managerTeamCombo = new QComboBox();
    private final QCheckBox managerHumanCheck = new QCheckBox("Tecnico humano");
    private final QSpinBox managerBoardSpin = spin(0, 100);
    private final QSpinBox managerFansSpin = spin(0, 100);
    private final QSpinBox managerTrophyYearSpin = spin(0, 9999);
    private final QComboBox managerTrophyCompetitionCombo = new QComboBox();
    private final QSpinBox managerTrophyTypeSpin = spin(0, 99);
    private final QSpinBox managerTrophyVariantSpin = spin(0, 9999);
    private final QSpinBox managerTrophyTeamSpin = spin(-1, 999999);

    private final QComboBox leagueCombo = new QComboBox();
    private final QTableWidget leagueTable = table("Pos", "Team ID", "Time", "Pts", "J", "V", "E", "D", "GP", "GC", "SG");
    private final QSpinBox leaguePointsSpin = spin(-999, 999);
    private final QSpinBox leagueWinsSpin = spin(0, 999);
    private final QSpinBox leagueDrawsSpin = spin(0, 999);
    private final QSpinBox leagueLossesSpin = spin(0, 999);
    private final QSpinBox leagueGoalsForSpin = spin(0, 9999);
    private final QSpinBox leagueGoalsAgainstSpin = spin(0, 9999);

    private final QLineEdit countriesFilterEdit = new QLineEdit();
    private final QTableWidget countriesTable = table("ID", "Pais", "Grupo", "Nivel", "Divisoes");
    private final QSpinBox countryLevelSpin = spin(0, 100);

    private final List<TeamRow> teams = new ArrayList<>();
    private final List<PlayerRow> players = new ArrayList<>();
    private final List<ManagerRow> managers = new ArrayList<>();
    private final List<LeagueRow> leagues = new ArrayList<>();
    private final List<LeagueTableRow> leagueEntries = new ArrayList<>();
    private final List<CountryRow> countries = new ArrayList<>();

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
        fillManagerTeamCombo();
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
        fillManagerTrophyCompetitionCombo();
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
    public void showCountries(List<CountryRow> rows) {
        updatingUi = true;
        countries.clear();
        countries.addAll(rows);
        fillCountriesTable();
        updatingUi = false;

        if (!countries.isEmpty()) {
            countriesTable.selectRow(0);
            updateCountryEditor();
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
        tabs.addTab(buildCountriesTab(), "Paises");
        rootLayout.addWidget(tabs);
        rootLayout.addWidget(statusLabel);

        setCentralWidget(central);
    }

    private QHBoxLayout buildToolbar() {
        QPushButton brasfootButton = new QPushButton("Selecionar Brasfoot");
        QPushButton openButton = new QPushButton("Abrir save");
        QPushButton saveButton = new QPushButton("Salvar copia");
        brasfootButton.clicked.connect(this::selectBrasfootLibrary);
        openButton.clicked.connect(this::openSave);
        saveButton.clicked.connect(presenter::saveCopy);

        QHBoxLayout toolbar = new QHBoxLayout();
        toolbar.addWidget(brasfootButton);
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
        form.addRow("Pais", teamCountryCombo);
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

        QGroupBox batchEditor = buildTeamBatchEditor();

        QVBoxLayout tableLayout = new QVBoxLayout();
        tableLayout.addWidget(teamsFilterEdit);
        tableLayout.addWidget(teamsTable);

        QHBoxLayout layout = new QHBoxLayout();
        layout.addLayout(tableLayout, 3);
        layout.addWidget(editor, 1);
        layout.addWidget(batchEditor, 1);

        QWidget tab = new QWidget();
        tab.setLayout(layout);
        return tab;
    }

    private void configureTeamEnumControls() {
        if (teamCountryCombo.count() == 0) {
            teamCountryCombo.addItem("", -1);
            for (Country country : Country.values()) {
                teamCountryCombo.addItem(country.getName(), country.getId());
            }
        }
        if (batchTeamCountryCombo.count() == 0) {
            for (Country country : Country.values()) {
                batchTeamCountryCombo.addItem(country.getName(), country.getId());
            }
        }
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

    private QGroupBox buildTeamBatchEditor() {
        batchTeamApplyButton.clicked.connect(this::batchUpdateTeamsByCountry);
        destroyCountryTeamsButton.clicked.connect(this::destroyOtherTeamsByCountry);

        QFormLayout form = new QFormLayout();
        form.addRow("Pais", batchTeamCountryCombo);
        form.addRow("Novo dinheiro", batchTeamMoneyEdit);
        form.addRow(batchTeamApplyButton);
        form.addRow("Nao destruir", new QLabel("time selecionado na tabela"));
        form.addRow(destroyCountryTeamsButton);

        QGroupBox box = new QGroupBox("Edicao em lote de times por pais");
        box.setLayout(form);
        return box;
    }

    private QGroupBox buildPlayerBatchEditor() {
        batchPlayerApplyButton.clicked.connect(this::batchUpdatePlayers);

        QFormLayout form = new QFormLayout();
        form.addRow("Nova idade (0=ignorar)", batchPlayerAgeSpin);
        form.addRow("Nova forca geral (0=ignorar)", batchPlayerOverallSpin);
        form.addRow("Forca goleiro (-1=ignorar)", batchPlayerSkillGoalkeepingSpin);
        form.addRow("Forca velocidade (-1=ignorar)", batchPlayerSkillSpeedSpin);
        form.addRow("Forca tecnica (-1=ignorar)", batchPlayerSkillTechniqueSpin);
        form.addRow("Forca passe (-1=ignorar)", batchPlayerSkillPassingSpin);
        form.addRow("Forca desarme (-1=ignorar)", batchPlayerSkillTacklingSpin);
        form.addRow("Forca armacao (-1=ignorar)", batchPlayerSkillPlaymakingSpin);
        form.addRow("Forca finalizacao (-1=ignorar)", batchPlayerSkillFinishingSpin);
        form.addRow("Nova energia (-1=ignorar)", batchPlayerEnergySpin);
        form.addRow("Pais", batchPlayerCountryCombo);
        form.addRow(batchPlayerStarLocalCheck);
        form.addRow(batchPlayerStarGlobalCheck);
        form.addRow(batchPlayerApplyButton);

        QGroupBox box = new QGroupBox("Edicao em lote de jogadores do time");
        box.setLayout(form);
        return box;
    }

    private void configureBatchPlayerEnumControls() {
        if (batchPlayerCountryCombo.count() == 0) {
            batchPlayerCountryCombo.addItem("", -1);
            for (Country country : Country.values()) {
                batchPlayerCountryCombo.addItem(country.getName(), country.getId());
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
        configureBatchPlayerEnumControls();

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
        form.addRow("Pais", playerCountryCombo);
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

        QGroupBox batchEditor = buildPlayerBatchEditor();

        QHBoxLayout layout = new QHBoxLayout();
        layout.addWidget(playersTable, 3);
        layout.addWidget(editor, 1);
        layout.addWidget(batchEditor, 1);

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
        if (playerCountryCombo.count() == 0) {
            playerCountryCombo.addItem("", -1);
            for (Country country : Country.values()) {
                playerCountryCombo.addItem(country.getName(), country.getId());
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
        managerTrophiesTable.itemSelectionChanged.connect(this::updateTrophyEditor);
        managerTrophyCompetitionCombo.currentIndexChanged.connect(ignored -> updateTrophyCompetitionFields());
        managerFilterEdit.setPlaceholderText("Pesquisar tecnico por nome, ID, time ou humano");
        managerFilterEdit.textChanged.connect(ignored -> filterManagers());
        managerHumansOnlyCheck.toggled.connect(ignored -> filterManagers());
        managerMaxTeamIdFilterSpin.valueChanged.connect(ignored -> filterManagers());
        managerTeamFilterEdit.setPlaceholderText("Pesquisar time por nome, ID, apelido, pais, divisao ou estadio");
        managerTeamFilterEdit.textChanged.connect(ignored -> filterManagerTeams());

        QPushButton saveManagerButton = new QPushButton("Salvar tecnico");
        saveManagerButton.clicked.connect(this::saveSelectedManager);
        QPushButton applyTrophyButton = new QPushButton("Aplicar linha do trofeu");
        applyTrophyButton.clicked.connect(() -> applySelectedTrophyFieldsToTable());
        QPushButton addTrophyButton = new QPushButton("Adicionar trofeu");
        addTrophyButton.clicked.connect(this::addTrophyRow);
        QPushButton removeTrophyButton = new QPushButton("Remover trofeu");
        removeTrophyButton.clicked.connect(this::removeSelectedTrophyRow);

        QFormLayout form = new QFormLayout();
        form.addRow("Nome", managerNameEdit);
        form.addRow("Pesquisar time", managerTeamFilterEdit);
        form.addRow("Time", managerTeamCombo);
        form.addRow(managerHumanCheck);
        form.addRow("Confianca diretoria", managerBoardSpin);
        form.addRow("Confianca torcida", managerFansSpin);
        form.addRow(saveManagerButton);

        QGroupBox editor = new QGroupBox("Editor do tecnico");
        editor.setLayout(form);

        QFormLayout trophyForm = new QFormLayout();
        trophyForm.addRow("Temporada", managerTrophyYearSpin);
        trophyForm.addRow("Competicao", managerTrophyCompetitionCombo);
        trophyForm.addRow("Tipo", managerTrophyTypeSpin);
        trophyForm.addRow("Variante", managerTrophyVariantSpin);
        trophyForm.addRow("Time ID", managerTrophyTeamSpin);
        trophyForm.addRow(applyTrophyButton);
        trophyForm.addRow(addTrophyButton);
        trophyForm.addRow(removeTrophyButton);

        QGroupBox trophyEditor = new QGroupBox("Trofeus do tecnico");
        trophyEditor.setLayout(trophyForm);

        QVBoxLayout trophyLayout = new QVBoxLayout();
        trophyLayout.addWidget(managerTrophiesTable, 2);
        trophyLayout.addWidget(trophyEditor, 1);

        QFormLayout filtersLayout = new QFormLayout();
        filtersLayout.addRow("Pesquisar", managerFilterEdit);
        filtersLayout.addRow(managerHumansOnlyCheck);
        filtersLayout.addRow("ID max. do time", managerMaxTeamIdFilterSpin);

        QVBoxLayout tableLayout = new QVBoxLayout();
        tableLayout.addLayout(filtersLayout);
        tableLayout.addWidget(managersTable);

        QHBoxLayout layout = new QHBoxLayout();
        layout.addLayout(tableLayout, 3);
        layout.addWidget(editor, 1);
        layout.addLayout(trophyLayout, 2);

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

    private QWidget buildCountriesTab() {
        countriesTable.itemSelectionChanged.connect(this::updateCountryEditor);
        countriesFilterEdit.setPlaceholderText("Pesquisar pais por nome, grupo ou ID");
        countriesFilterEdit.textChanged.connect(this::filterCountries);

        QPushButton saveCountryButton = new QPushButton("Salvar pais");
        saveCountryButton.clicked.connect(this::saveSelectedCountry);

        QFormLayout form = new QFormLayout();
        form.addRow("Nivel", countryLevelSpin);
        form.addRow(saveCountryButton);

        QGroupBox editor = new QGroupBox("Editor do pais");
        editor.setLayout(form);

        QVBoxLayout tableLayout = new QVBoxLayout();
        tableLayout.addWidget(countriesFilterEdit);
        tableLayout.addWidget(countriesTable);

        QHBoxLayout layout = new QHBoxLayout();
        layout.addLayout(tableLayout, 3);
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

    private void selectBrasfootLibrary() {
        String path = QFileDialog.getOpenFileName(this, "Selecionar Brasfoot", "",
                "Brasfoot (*.exe *.jar);;Executavel (*.exe);;Jar (*.jar)").result;
        if (path == null || path.isBlank()) {
            return;
        }
        presenter.selectBrasfootLibrary(Path.of(path));
    }

    private void saveSelectedTeam() {
        TeamRow team = selectedTeam();
        if (team == null) {
            showError("Time nao selecionado", "Selecione um time antes de salvar.");
            return;
        }

        presenter.updateTeam(team.id(), teamNameEdit.text(), teamAliasEdit.text(), teamMoneyEdit.text(),
                teamReputationCombo.currentText(), stadiumNameEdit.text(),
                stadiumSectorEdits.stream().map(QLineEdit::text).toList(), selectedTeamCountryCode(),
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
                selectedPlayerCountryCode(), playerStarLocalCheck.isChecked(), playerStarGlobalCheck.isChecked());
    }

    private void saveSelectedManager() {
        ManagerRow manager = selectedManager();
        if (manager == null) {
            showError("Tecnico nao selecionado", "Selecione um tecnico antes de salvar.");
            return;
        }

        applySelectedTrophyFieldsToTable(false);
        presenter.updateManager(manager.id(), managerNameEdit.text(), managerHumanCheck.isChecked(), selectedManagerTeamIdText(),
                managerBoardSpin.value(), managerFansSpin.value(), managerTrophiesFromTable());
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

    private void saveSelectedCountry() {
        CountryRow country = selectedCountry();
        if (country == null) {
            showError("Pais nao selecionado", "Selecione um pais antes de salvar.");
            return;
        }
        presenter.updateCountryLevel(country.id(), countryLevelSpin.value());
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
        selectComboByData(teamCountryCombo, team.country());
        teamDivisionEdit.setText(value(team.division()));
        teamLevelEdit.setText(value(team.level()));
        selectComboByData(batchTeamCountryCombo, team.country());
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
        selectComboByData(playerCountryCombo, player.country());
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
        selectManagerTeam(manager.teamId());
        managerHumanCheck.setChecked(Boolean.TRUE.equals(manager.human()));
        managerBoardSpin.setValue(manager.confidenceBoard() == null ? 0 : manager.confidenceBoard());
        managerFansSpin.setValue(manager.confidenceFans() == null ? 0 : manager.confidenceFans());
        fillManagerTrophiesTable(manager);
    }

    private void updateTrophyEditor() {
        if (updatingUi) {
            return;
        }
        int row = managerTrophiesTable.currentRow();
        if (row < 0 || managerTrophiesTable.item(row, 0) == null) {
            return;
        }

        managerTrophyYearSpin.setValue(parseTableInteger(managerTrophiesTable, row, 1, 0));
        managerTrophyTypeSpin.setValue(parseTableInteger(managerTrophiesTable, row, 2, 0));
        managerTrophyVariantSpin.setValue(parseTableInteger(managerTrophiesTable, row, 3, 0));
        managerTrophyTeamSpin.setValue(parseTableInteger(managerTrophiesTable, row, 4, -1));
        selectTrophyCompetition(managerTrophyTypeSpin.value(), managerTrophyVariantSpin.value(), tableText(managerTrophiesTable, row, 5));
    }

    private void updateTrophyCompetitionFields() {
        if (updatingUi) {
            return;
        }
        TrophyCompetitionOption competition = selectedTrophyCompetitionOption();
        if (competition == null) {
            return;
        }

        managerTrophyTypeSpin.setValue(competition.type());
        managerTrophyVariantSpin.setValue(competition.variant());
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

    private void updateCountryEditor() {
        if (updatingUi) {
            return;
        }
        CountryRow country = selectedCountry();
        if (country == null) {
            return;
        }
        countryLevelSpin.setValue(country.level());
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

    private void filterCountries(String ignored) {
        fillCountriesTable();
        if (countriesTable.rowCount() > 0) {
            countriesTable.selectRow(0);
            updateCountryEditor();
        }
    }

    private void filterManagers() {
        fillManagersTable();
        if (managersTable.rowCount() > 0) {
            managersTable.selectRow(0);
            updateManagerEditor();
        }
    }

    private void filterManagerTeams() {
        fillManagerTeamCombo();
    }

    private void fillTeamsTable() {
        List<TeamRow> filteredTeams = filteredTeams(teamsFilterEdit.text());
        teamsTable.setRowCount(filteredTeams.size());
        for (int row = 0; row < filteredTeams.size(); row++) {
            TeamRow team = filteredTeams.get(row);
            setRow(teamsTable, row, String.valueOf(team.id()), team.name(), value(team.alias()), countryLabel(team.country()),
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
                    String.valueOf(player.overall()), positionLabel(player.position()), sideLabel(player.side()),
                    countryLabel(player.country()), String.valueOf(player.energy()),
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
        List<ManagerRow> filteredManagers = filteredManagers();
        managersTable.setRowCount(filteredManagers.size());
        for (int row = 0; row < filteredManagers.size(); row++) {
            ManagerRow manager = filteredManagers.get(row);
            setRow(managersTable, row, String.valueOf(manager.id()), value(manager.name()), managerTeamLabel(manager.teamId()),
                    Boolean.TRUE.equals(manager.human()) ? "Sim" : "Nao", value(manager.confidenceBoard()),
                    value(manager.confidenceFans()), String.valueOf(manager.trophies() == null ? 0 : manager.trophies().size()));
        }
    }

    private void fillManagerTrophiesTable(ManagerRow manager) {
        List<ManagerTrophy> trophies = manager.trophies() == null ? List.of() : manager.trophies();
        managerTrophiesTable.setRowCount(trophies.size());
        for (int row = 0; row < trophies.size(); row++) {
            ManagerTrophy trophy = trophies.get(row);
            setRow(managerTrophiesTable, row, value(trophy.index()), value(trophy.year()),
                    value(trophy.competitionType()), value(trophy.variant()), value(trophy.teamId()),
                    value(trophy.competitionName()));
        }
        if (!trophies.isEmpty()) {
            managerTrophiesTable.selectRow(0);
            updateTrophyEditor();
        }
    }

    private void fillManagerTrophyCompetitionCombo() {
        Object selectedData = currentComboData(managerTrophyCompetitionCombo);
        managerTrophyCompetitionCombo.clear();
        managerTrophyCompetitionCombo.addItem("Selecione uma competicao", "");
        for (TrophyCompetitionOption competition : managerTrophyCompetitionOptions()) {
            managerTrophyCompetitionCombo.addItem(trophyCompetitionLabel(competition), trophyCompetitionKey(competition));
        }
        selectComboByData(managerTrophyCompetitionCombo, selectedData);
    }

    private List<TrophyCompetitionOption> managerTrophyCompetitionOptions() {
        List<TrophyCompetitionOption> options = new ArrayList<>();
        List<String> keys = new ArrayList<>();
        for (ManagerRow manager : managers) {
            List<ManagerTrophyCompetition> competitions = manager.trophyCompetitions() == null ? List.of() : manager.trophyCompetitions();
            for (ManagerTrophyCompetition competition : competitions) {
                addTrophyCompetitionOption(options, keys, competition.competitionType(), competition.variant(), competition.name());
            }
            List<ManagerTrophy> trophies = manager.trophies() == null ? List.of() : manager.trophies();
            for (ManagerTrophy trophy : trophies) {
                addTrophyCompetitionOption(options, keys, trophy.competitionType(), trophy.variant(), trophy.competitionName());
            }
        }
        return options;
    }

    private void addTrophyCompetitionOption(List<TrophyCompetitionOption> options, List<String> keys,
                                            Integer type, Integer variant, String name) {
        if (type == null || variant == null || name == null || name.isBlank()) {
            return;
        }
        String key = trophyCompetitionKey(type, variant, name);
        if (keys.contains(key)) {
            return;
        }
        keys.add(key);
        options.add(new TrophyCompetitionOption(type, variant, name));
    }

    private void fillCountriesTable() {
        List<CountryRow> filteredCountries = filteredCountries(countriesFilterEdit.text());
        countriesTable.setRowCount(filteredCountries.size());
        for (int row = 0; row < filteredCountries.size(); row++) {
            CountryRow country = filteredCountries.get(row);
            setRow(countriesTable, row, country.id(), country.name(), country.group(),
                    String.valueOf(country.level()), String.valueOf(country.divisionCount()));
        }
    }

    private void fillPlayerTeamCombo() {
        playerTeamCombo.clear();
        for (TeamRow team : filteredTeams(playerTeamFilterEdit.text())) {
            playerTeamCombo.addItem(team.id() + " - " + team.name());
        }
    }

    private void fillManagerTeamCombo() {
        Integer selectedTeamId = selectedManagerTeamId();
        managerTeamCombo.clear();
        managerTeamCombo.addItem("Desempregado", -1);
        for (TeamRow team : filteredTeams(managerTeamFilterEdit.text())) {
            managerTeamCombo.addItem(team.id() + " - " + team.name(), team.id());
        }
        selectManagerTeam(selectedTeamId);
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

    private List<CountryRow> filteredCountries(String filterText) {
        String filter = normalize(filterText);
        if (filter.isBlank()) {
            return countries;
        }
        return countries.stream()
                .filter(country -> normalize(searchText(country)).contains(filter))
                .toList();
    }

    private List<ManagerRow> filteredManagers() {
        String filter = normalize(managerFilterEdit.text());
        int maxTeamId = managerMaxTeamIdFilterSpin.value();
        return managers.stream()
                .filter(manager -> !managerHumansOnlyCheck.isChecked() || Boolean.TRUE.equals(manager.human()))
                .filter(manager -> normalizedManagerTeamId(manager.teamId()) <= maxTeamId)
                .filter(manager -> filter.isBlank() || normalize(searchText(manager)).contains(filter))
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

    private String searchText(CountryRow country) {
        return String.join(" ", country.id(), value(country.name()), value(country.group()), value(country.level()));
    }

    private String searchText(ManagerRow manager) {
        return String.join(" ",
                String.valueOf(manager.id()),
                value(manager.name()),
                String.valueOf(normalizedManagerTeamId(manager.teamId())),
                managerTeamLabel(manager.teamId()),
                Boolean.TRUE.equals(manager.human()) ? "sim humano" : "nao bot maquina");
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

    private CountryRow selectedCountry() {
        int row = countriesTable.currentRow();
        if (row < 0 || countriesTable.item(row, 0) == null) {
            return null;
        }
        String id = countriesTable.item(row, 0).text();
        return countries.stream().filter(country -> country.id().equals(id)).findFirst().orElse(null);
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

    private String selectedManagerTeamIdText() {
        Integer teamId = selectedManagerTeamId();
        return teamId == null ? null : String.valueOf(teamId);
    }

    private Integer selectedManagerTeamId() {
        int index = managerTeamCombo.currentIndex();
        if (index < 0) {
            return null;
        }
        Object data = managerTeamCombo.itemData(index);
        if (data instanceof Integer teamId) {
            return teamId;
        }
        String text = managerTeamCombo.currentText();
        if (text == null || text.isBlank()) {
            return null;
        }
        int separator = text.indexOf(" - ");
        String idText = separator >= 0 ? text.substring(0, separator) : text;
        return Integer.parseInt(idText);
    }

    private void selectManagerTeam(Integer teamId) {
        if (teamId == null) {
            managerTeamCombo.setCurrentIndex(-1);
            return;
        }
        for (int i = 0; i < managerTeamCombo.count(); i++) {
            if (teamId.equals(managerTeamCombo.itemData(i))) {
                managerTeamCombo.setCurrentIndex(i);
                return;
            }
        }
        managerTeamCombo.setCurrentIndex(-1);
    }

    private void applySelectedTrophyFieldsToTable() {
        applySelectedTrophyFieldsToTable(true);
    }

    private void applySelectedTrophyFieldsToTable(boolean showMissingSelection) {
        int row = managerTrophiesTable.currentRow();
        if (row < 0 || managerTrophiesTable.item(row, 0) == null) {
            if (showMissingSelection) {
                showError("Trofeu nao selecionado", "Selecione um trofeu antes de aplicar a linha.");
            }
            return;
        }

        setRow(managerTrophiesTable, row, managerTrophiesTable.item(row, 0).text(),
                String.valueOf(managerTrophyYearSpin.value()), String.valueOf(managerTrophyTypeSpin.value()),
                String.valueOf(managerTrophyVariantSpin.value()), String.valueOf(managerTrophyTeamSpin.value()),
                trophyCompetitionNameForCurrentFields(row));
    }

    private void addTrophyRow() {
        int row = managerTrophiesTable.rowCount();
        managerTrophiesTable.setRowCount(row + 1);
        setRow(managerTrophiesTable, row, "-1", String.valueOf(managerTrophyYearSpin.value()),
                String.valueOf(managerTrophyTypeSpin.value()), String.valueOf(managerTrophyVariantSpin.value()),
                String.valueOf(managerTrophyTeamSpin.value()), trophyCompetitionNameForCurrentFields(row));
        managerTrophiesTable.selectRow(row);
    }

    private String trophyCompetitionNameForCurrentFields(int row) {
        TrophyCompetitionOption selected = selectedTrophyCompetitionOption();
        if (selected != null && selected.type() == managerTrophyTypeSpin.value() && selected.variant() == managerTrophyVariantSpin.value()) {
            return selected.name();
        }

        String currentName = row < managerTrophiesTable.rowCount() ? tableText(managerTrophiesTable, row, 5) : "";
        TrophyCompetitionOption competition = trophyCompetitionOption(managerTrophyTypeSpin.value(), managerTrophyVariantSpin.value(), currentName);
        if (competition != null) {
            return competition.name();
        }
        competition = uniqueTrophyCompetitionOption(managerTrophyTypeSpin.value(), managerTrophyVariantSpin.value());
        return competition == null ? currentName : competition.name();
    }

    private TrophyCompetitionOption selectedTrophyCompetitionOption() {
        Object data = currentComboData(managerTrophyCompetitionCombo);
        if (!(data instanceof String key) || key.isBlank()) {
            return null;
        }
        return trophyCompetitionOption(key);
    }

    private TrophyCompetitionOption trophyCompetitionOption(String key) {
        for (TrophyCompetitionOption competition : managerTrophyCompetitionOptions()) {
            if (key.equals(trophyCompetitionKey(competition))) {
                return competition;
            }
        }
        return null;
    }

    private TrophyCompetitionOption trophyCompetitionOption(int type, int variant, String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String key = trophyCompetitionKey(type, variant, name);
        for (TrophyCompetitionOption competition : managerTrophyCompetitionOptions()) {
            if (key.equals(trophyCompetitionKey(competition))) {
                return competition;
            }
        }
        return null;
    }

    private TrophyCompetitionOption uniqueTrophyCompetitionOption(int type, int variant) {
        TrophyCompetitionOption match = null;
        for (TrophyCompetitionOption competition : managerTrophyCompetitionOptions()) {
            if (competition.type() != type || competition.variant() != variant) {
                continue;
            }
            if (match != null) {
                return null;
            }
            match = competition;
        }
        return match;
    }

    private void selectTrophyCompetition(int type, int variant, String name) {
        String exactKey = trophyCompetitionKey(type, variant, name);
        for (int i = 0; i < managerTrophyCompetitionCombo.count(); i++) {
            if (exactKey.equals(managerTrophyCompetitionCombo.itemData(i))) {
                managerTrophyCompetitionCombo.setCurrentIndex(i);
                return;
            }
        }
        TrophyCompetitionOption unique = uniqueTrophyCompetitionOption(type, variant);
        if (unique == null) {
            managerTrophyCompetitionCombo.setCurrentIndex(0);
            return;
        }
        selectComboByData(managerTrophyCompetitionCombo, trophyCompetitionKey(unique));
    }

    private Object currentComboData(QComboBox comboBox) {
        int index = comboBox.currentIndex();
        return index < 0 ? null : comboBox.itemData(index);
    }

    private String trophyCompetitionLabel(TrophyCompetitionOption competition) {
        return competition.name() + " (tipo " + competition.type() + ", variante " + competition.variant() + ")";
    }

    private String trophyCompetitionKey(TrophyCompetitionOption competition) {
        return trophyCompetitionKey(competition.type(), competition.variant(), competition.name());
    }

    private String trophyCompetitionKey(int type, int variant, String name) {
        return type + ":" + variant + ":" + normalize(name);
    }

    private void removeSelectedTrophyRow() {
        int row = managerTrophiesTable.currentRow();
        if (row < 0) {
            showError("Trofeu nao selecionado", "Selecione um trofeu antes de remover.");
            return;
        }
        managerTrophiesTable.removeRow(row);
    }

    private List<ManagerTrophy> managerTrophiesFromTable() {
        List<ManagerTrophy> trophies = new ArrayList<>();
        for (int row = 0; row < managerTrophiesTable.rowCount(); row++) {
            trophies.add(new ManagerTrophy(
                    parseTableInteger(managerTrophiesTable, row, 0, -1),
                    parseTableInteger(managerTrophiesTable, row, 1, 0),
                    parseTableInteger(managerTrophiesTable, row, 2, 0),
                    parseTableInteger(managerTrophiesTable, row, 3, 0),
                    parseTableInteger(managerTrophiesTable, row, 4, -1),
                    tableText(managerTrophiesTable, row, 5)
            ));
        }
        return trophies;
    }

    private int parseTableInteger(QTableWidget table, int row, int column, int fallback) {
        String text = tableText(table, row, column);
        if (text == null || text.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private String tableText(QTableWidget table, int row, int column) {
        QTableWidgetItem item = table.item(row, column);
        return item == null ? "" : item.text();
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

    private QSpinBox spin(int minimum, int maximum, int value) {
        QSpinBox spinBox = spin(minimum, maximum);
        spinBox.setValue(value);
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

    private String selectedTeamCountryCode() {
        int index = teamCountryCombo.currentIndex();
        if (index <= 0) return "";
        Object data = teamCountryCombo.itemData(index);
        return data == null ? "" : String.valueOf(data);
    }

    private String selectedPlayerCountryCode() {
        int index = playerCountryCombo.currentIndex();
        if (index <= 0) return "";
        Object data = playerCountryCombo.itemData(index);
        return data == null ? "" : String.valueOf(data);
    }

    private void selectComboByData(QComboBox combo, Object value) {
        if (value == null) {
            combo.setCurrentIndex(0);
            return;
        }
        for (int i = 0; i < combo.count(); i++) {
            if (value.equals(combo.itemData(i))) {
                combo.setCurrentIndex(i);
                return;
            }
        }
        combo.setCurrentIndex(0);
    }

    private String countryLabel(Integer countryId) {
        if (countryId == null) return "";
        Country c = Country.fromId(countryId);
        return c == null ? String.valueOf(countryId) : c.getName();
    }

    private String managerTeamLabel(Integer teamId) {
        int normalizedTeamId = normalizedManagerTeamId(teamId);
        if (normalizedTeamId < 0) {
            return "Desempregado";
        }
        TeamRow team = teams.stream()
                .filter(value -> value.id() == normalizedTeamId)
                .findFirst()
                .orElse(null);
        return team == null ? String.valueOf(normalizedTeamId) : normalizedTeamId + " - " + team.name();
    }

    private int normalizedManagerTeamId(Integer teamId) {
        return teamId == null ? -1 : teamId;
    }

    private void batchUpdateTeamsByCountry() {
        int idx = batchTeamCountryCombo.currentIndex();
        if (idx < 0) {
            showError("Pais nao selecionado", "Selecione um pais para o batch.");
            return;
        }
        Object data = batchTeamCountryCombo.itemData(idx);
        if (!(data instanceof Integer countryId)) {
            showError("Pais nao selecionado", "Selecione um pais valido para o batch.");
            return;
        }
        presenter.batchUpdateTeamsByCountry(countryId, parseOptionalLong(batchTeamMoneyEdit.text(), "dinheiro"));
    }

    private void destroyOtherTeamsByCountry() {
        TeamRow protectedTeam = selectedTeam();
        if (protectedTeam == null) {
            showError("Time nao selecionado", "Selecione o seu time na tabela antes de destruir os outros.");
            return;
        }

        int idx = batchTeamCountryCombo.currentIndex();
        if (idx < 0) {
            showError("Pais nao selecionado", "Selecione um pais para destruir os times.");
            return;
        }

        Object data = batchTeamCountryCombo.itemData(idx);
        if (!(data instanceof Integer countryId)) {
            showError("Pais nao selecionado", "Selecione um pais valido para destruir os times.");
            return;
        }
        if (protectedTeam.country() == null || protectedTeam.country() != countryId) {
            showError("Time protegido invalido", "O time selecionado precisa pertencer ao pais escolhido.");
            return;
        }

        presenter.destroyOtherTeamsByCountry(countryId, protectedTeam.id());
    }

    private Long parseOptionalLong(String text, String fieldName) {
        String normalized = normalizeOptionalText(text);
        if (normalized == null) return null;
        try {
            return Long.parseLong(normalized);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor invalido para " + fieldName + ".", e);
        }
    }

    private String normalizeOptionalText(String text) {
        if (text == null || text.isBlank()) return null;
        return text.trim();
    }

    private void batchUpdatePlayers() {
        Integer teamId = selectedPlayerTeamId();
        if (teamId == null) {
            showError("Time nao selecionado", "Selecione um time antes do batch.");
            return;
        }

        Integer age = batchPlayerAgeSpin.value() > 0 ? batchPlayerAgeSpin.value() : null;
        Integer overall = batchPlayerOverallSpin.value() > 0 ? batchPlayerOverallSpin.value() : null;
        Integer energy = optionalNonNegativeSpinValue(batchPlayerEnergySpin);
        Integer skillGoalkeeping = optionalNonNegativeSpinValue(batchPlayerSkillGoalkeepingSpin);
        Integer skillSpeed = optionalNonNegativeSpinValue(batchPlayerSkillSpeedSpin);
        Integer skillTechnique = optionalNonNegativeSpinValue(batchPlayerSkillTechniqueSpin);
        Integer skillPassing = optionalNonNegativeSpinValue(batchPlayerSkillPassingSpin);
        Integer skillTackling = optionalNonNegativeSpinValue(batchPlayerSkillTacklingSpin);
        Integer skillPlaymaking = optionalNonNegativeSpinValue(batchPlayerSkillPlaymakingSpin);
        Integer skillFinishing = optionalNonNegativeSpinValue(batchPlayerSkillFinishingSpin);
        Integer country = null;
        int countryIdx = batchPlayerCountryCombo.currentIndex();
        if (countryIdx > 0) {
            Object data = batchPlayerCountryCombo.itemData(countryIdx);
            if (data instanceof Integer c) country = c;
        }
        Boolean starLocal = batchPlayerStarLocalCheck.isChecked() ? Boolean.TRUE : null;
        Boolean starGlobal = batchPlayerStarGlobalCheck.isChecked() ? Boolean.TRUE : null;

        presenter.batchUpdatePlayers(teamId, age, overall, null, energy, country, skillGoalkeeping, skillSpeed,
                skillTechnique, skillPassing, skillTackling, skillPlaymaking, skillFinishing,
                starLocal, starGlobal);
    }

    private Integer optionalNonNegativeSpinValue(QSpinBox spinBox) {
        return spinBox.value() >= 0 ? spinBox.value() : null;
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

    private record TrophyCompetitionOption(int type, int variant, String name) {
    }
}
