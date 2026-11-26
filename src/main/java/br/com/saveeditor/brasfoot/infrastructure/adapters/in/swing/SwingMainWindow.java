package br.com.saveeditor.brasfoot.infrastructure.adapters.in.swing;

import br.com.saveeditor.brasfoot.presentation.model.*;
import br.com.saveeditor.brasfoot.presentation.presenter.BrasfootPresenter;
import br.com.saveeditor.brasfoot.presentation.view.BrasfootDesktopView;
import br.com.saveeditor.brasfoot.domain.enums.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import javax.swing.BoxLayout;

/** Swing desktop interface: native Java widgets, with no platform-specific UI libraries. */
public final class SwingMainWindow extends JFrame implements BrasfootDesktopView {
    private final BrasfootPresenter presenter;
    private final JLabel session = new JLabel("Nenhum save aberto");
    private final JLabel status = new JLabel("Abra um save .s22 para comecar.");
    private final List<TeamRow> teams = new ArrayList<>();
    private final List<PlayerRow> players = new ArrayList<>();
    private final List<ManagerRow> managers = new ArrayList<>();
    private final List<LeagueRow> leagues = new ArrayList<>();
    private final List<LeagueTableRow> leagueRows = new ArrayList<>();
    private final List<CountryRow> countries = new ArrayList<>();
    private final List<CalendarRoundRow> calendarRounds = new ArrayList<>();
    private final List<CalendarRoundRow> visibleCalendarRounds = new ArrayList<>();
    private final JTable teamTable = table("ID", "Time", "Apelido", "País", "Div", "Nivel", "Dinheiro", "Reputacao", "Estadio");
    private final JTable playerTable = table("ID", "Jogador", "Idade", "Forca", "Posicao", "Lado", "Energia", "País", "Estrelas");
    private final JTable managerTable = table("ID", "Tecnico", "Time", "Humano", "Diretoria", "Torcida", "Trofeus");
    private final JTable leagueTable = table("Pos", "ID", "Time", "Pts", "J", "V", "E", "D", "GP", "GC", "SG");
    private final JTable countryTable = table("ID", "Pais", "Grupo", "Nivel", "Divisoes");
    private final JTable standingTable = table("Pos", "Time ID", "Time", "Pts", "J", "V", "E", "D", "GP", "GC", "SG");
    private final JTable calendarTable = table("Data atual", "Competicao", "Rodada", "Jogos");
    private final JLabel calendarHint = new JLabel("Selecione uma rodada futura para alterar a data.");
    private final JComboBox<CalendarCompetitionFilter> calendarCompetition = new JComboBox<>();
    private final JComboBox<TeamRow> playerTeam = new JComboBox<>();
    private final JTextField playerTeamSearch = new JTextField(20);
    private final JComboBox<LeagueRow> leagueChoice = new JComboBox<>();
    private String selectedLeague;
    private final Map<String,JTextField> teamFields = fields("Nome", "Apelido", "Divisao", "Nivel", "Dinheiro", "Estadio", "Setor 1", "Setor 2", "Setor 3", "Setor 4");
    private final JComboBox<Country> teamCountry = new JComboBox<>(Country.values());
    private final JComboBox<String> reputation = new JComboBox<>();
    private final JComboBox<TeamPlayStyle> style = new JComboBox<>(TeamPlayStyle.values());
    private final JComboBox<TeamMarking> marking = new JComboBox<>(TeamMarking.values());
    private final JComboBox<TeamAttackFocus> attack = new JComboBox<>(TeamAttackFocus.values());
    private final Map<String,JTextField> playerFields = fields("Nome", "Idade", "Forca", "Energia", "Salario", "Goleiro", "Velocidade", "Tecnica", "Passe", "Desarme", "Armacao", "Finalizacao");
    private final JComboBox<Country> playerCountry = new JComboBox<>(Country.values());
    private final JComboBox<PlayerSide> playerSide = new JComboBox<>(PlayerSide.values());
    private final JComboBox<PlayerPosition> playerPosition = new JComboBox<>(PlayerPosition.values());
    private final JComboBox<PlayerCharacteristic> characteristic1 = new JComboBox<>(PlayerCharacteristic.values());
    private final JComboBox<PlayerCharacteristic> characteristic2 = new JComboBox<>(PlayerCharacteristic.values());
    private final JSpinner contractEndDate = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
    private final JCheckBox starLocal = new JCheckBox("Estrela local"), starGlobal = new JCheckBox("Estrela mundial");
    private final Map<String,JTextField> managerFields = fields("Nome", "Confianca diretoria", "Confianca torcida");
    private final JComboBox<TeamRow> managerTeam = new JComboBox<>();
    private final JCheckBox human = new JCheckBox("Tecnico humano");
    private final Map<String,JTextField> standingFields = fields("Pontos", "Vitorias", "Empates", "Derrotas", "Gols pro", "Gols contra");
    private final JTextField countryLevel = new JTextField(8);
    private final JSpinner calendarTargetDate = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
    private boolean updatingPlayerTeams;
    private JButton editPlayerButton;
    private JButton batchPlayersButton;
    private JButton editTeamButton;
    private JButton batchTeamsButton;

    public SwingMainWindow(BrasfootPresenter presenter) {
        super("Brasfoot Save Editor");
        this.presenter = presenter;
        presenter.attach(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));
        setSize(1320, 850);
        setLocationRelativeTo(null);
        buildUi();
    }

    private void buildUi() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton libraryButton = new JButton("Selecionar Brasfoot"), openButton = new JButton("Abrir save"), saveButton = new JButton("Salvar…");
        libraryButton.addActionListener(e -> selectLibrary()); openButton.addActionListener(e -> openSave()); saveButton.addActionListener(e -> chooseSaveMode());
        toolbar.add(libraryButton); toolbar.add(openButton); toolbar.add(saveButton); toolbar.add(session);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Times", teamTab()); tabs.addTab("Jogadores", playerTab()); tabs.addTab("Tecnicos", managerTab()); tabs.addTab("Ligas", leaguesTab()); tabs.addTab("Paises", countryTab()); tabs.addTab("Calendario", calendarTab());
        JPanel root = new JPanel(new BorderLayout(6,6)); root.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        root.add(toolbar, BorderLayout.NORTH); root.add(tabs, BorderLayout.CENTER); root.add(status, BorderLayout.SOUTH); setContentPane(root);
    }

    private JPanel teamTab() {
        style.setRenderer(enumRenderer(TeamPlayStyle::getDisplayName));
        marking.setRenderer(enumRenderer(TeamMarking::getDisplayName));
        attack.setRenderer(enumRenderer(TeamAttackFocus::getDisplayName));
        teamCountry.setRenderer(enumRenderer(Country::getName));
        JTextField filter = new JTextField(); filter.setToolTipText("Pesquisar time por nome, ID, apelido, país ou estádio");
        filter.getDocument().addDocumentListener(change(() -> fillTeams(filter.getText())));
        for (TeamReputation r : TeamReputation.values()) reputation.addItem(r.name());
        editTeamButton = new JButton("Editar time");
        editTeamButton.setEnabled(false);
        editTeamButton.addActionListener(e -> editSelectedTeam());
        batchTeamsButton = new JButton("Edição em lote…");
        batchTeamsButton.setEnabled(false);
        batchTeamsButton.addActionListener(e -> showBatchTeamEditor());
        teamTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean selected = selected(teamTable, teams) != null;
                editTeamButton.setEnabled(selected);
                batchTeamsButton.setEnabled(!teams.isEmpty());
            }
        });
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(editTeamButton); actions.add(batchTeamsButton);
        JPanel list = new JPanel(new BorderLayout(5, 5));
        list.add(filter, BorderLayout.NORTH); list.add(new JScrollPane(teamTable), BorderLayout.CENTER); list.add(actions, BorderLayout.SOUTH);
        return list;
    }

    private void editSelectedTeam() {
        TeamRow team = selected(teamTable, teams);
        if (team == null) return;
        configureTeamStyleChoices();
        loadTeamForm();
        JPanel editor = form("Dados do time", teamFields);
        addRow(editor, "País", teamCountry);
        addRow(editor, "Reputação", reputation);
        addRow(editor, "Estilo de jogo", style);
        addRow(editor, "Marcação", marking);
        addRow(editor, "Concentrar ataques", attack);
        int result = JOptionPane.showConfirmDialog(this, new JScrollPane(editor), "Editar time — " + team.name(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) saveTeam();
    }

    private void showBatchTeamEditor() {
        if (teams.isEmpty()) return;
        JTextField money = new JTextField();
        JTextField stadium = new JTextField();
        JSpinner level = new JSpinner(new SpinnerNumberModel(-1, -1, 100, 1));
        JComboBox<String> rep = new JComboBox<>(); rep.addItem("Manter reputação");
        for (TeamReputation value : TeamReputation.values()) rep.addItem(value.name());
        JComboBox<TeamPlayStyle> batchStyle = new JComboBox<>();
        batchStyle.addItem(null);
        Arrays.stream(TeamPlayStyle.values()).filter(value -> presenter.supportsRealBrasfootTactics() || value.getCode() <= 2)
                .forEach(batchStyle::addItem);
        batchStyle.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list,Object value,int index,boolean selected,boolean focus) {
                super.getListCellRendererComponent(list,value,index,selected,focus);
                setText(value instanceof TeamPlayStyle tactic ? tactic.getDisplayName() : "Manter estilo atual"); return this;
            }
        });
        JPanel editor = new JPanel(new GridLayout(0, 2, 6, 6));
        editor.add(new JLabel("Dinheiro (vazio mantém)")); editor.add(money);
        editor.add(new JLabel("Reputação")); editor.add(rep);
        editor.add(new JLabel("Estádio (vazio mantém)")); editor.add(stadium);
        editor.add(new JLabel("Nível (-1 mantém)")); editor.add(level);
        editor.add(new JLabel("Estilo de jogo")); editor.add(batchStyle);
        Object[] options = {"Aplicar alterações", "Destruir exceto meu time", "Destruir todos", "Cancelar"};
        int result = JOptionPane.showOptionDialog(this, editor, "Edição em lote — todos os times",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (result == 0) {
            try {
                presenter.batchUpdateAllTeams(money.getText().isBlank() ? null : Long.valueOf(money.getText()),
                        "Manter reputação".equals(rep.getSelectedItem()) ? null : TeamReputation.valueOf((String) rep.getSelectedItem()),
                        stadium.getText().isBlank() ? null : stadium.getText(), (Integer) level.getValue() < 0 ? null : (Integer) level.getValue(),
                        batchStyle.getSelectedItem() instanceof TeamPlayStyle selected ? selected.getCode() : null);
            } catch (Exception ex) { showError("Dados inválidos", ex.getMessage()); }
        } else if (result == 1) confirmDestroyTeams(true);
        else if (result == 2) confirmDestroyTeams(false);
    }

    private void confirmDestroyTeams(boolean keepHumanTeam) {
        String action = keepHumanTeam ? "Destruir todos os times exceto os controlados por técnico humano?"
                : "Destruir TODOS os times, inclusive o seu? Esta ação não pode ser desfeita no save atual.";
        if (JOptionPane.showConfirmDialog(this, action, "Confirmar destruição", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) presenter.destroyTeams(keepHumanTeam);
    }
    private JPanel playerTab() {
        configurePlayerControls();
        playerTeam.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof TeamRow team) setText(team.id() + " - " + team.name());
                return this;
            }
        });
        playerTeam.setMaximumRowCount(20);
        playerTeam.setToolTipText("Escolha um time para carregar ou trocar de elenco.");
        playerCountry.setRenderer(enumRenderer(Country::getName));
        playerTeamSearch.setToolTipText("Filtre times por nome, apelido, ID ou país.");
        playerTeamSearch.getDocument().addDocumentListener(change(this::filterPlayerTeams));
        playerTeam.addActionListener(e->{
            if(!updatingPlayerTeams){TeamRow t=(TeamRow)playerTeam.getSelectedItem();if(t!=null)presenter.loadPlayers(t.id());}
        });
        editPlayerButton = new JButton("Editar jogador");
        editPlayerButton.addActionListener(e -> editSelectedPlayer());
        batchPlayersButton = new JButton("Edição em lote…");
        batchPlayersButton.addActionListener(e -> showBatchPlayerEditor());
        editPlayerButton.setEnabled(false);
        batchPlayersButton.setEnabled(false);
        playerTable.getSelectionModel().addListSelectionListener(e->{
            if(!e.getValueIsAdjusting()){
                boolean hasSelection=selected(playerTable,players)!=null;
                editPlayerButton.setEnabled(hasSelection);
                batchPlayersButton.setEnabled(playerTeam.getSelectedItem()!=null&&!players.isEmpty());
            }
        });
        JPanel roster = new JPanel(new BorderLayout(6, 6));
        JPanel teamSelector = new JPanel(new FlowLayout(FlowLayout.LEFT));
        teamSelector.add(new JLabel("Pesquisar time"));
        teamSelector.add(playerTeamSearch);
        teamSelector.add(new JLabel("Time do elenco"));
        teamSelector.add(playerTeam);
        roster.add(teamSelector, BorderLayout.NORTH);
        roster.add(new JScrollPane(playerTable), BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(editPlayerButton);
        actions.add(batchPlayersButton);
        roster.add(actions, BorderLayout.SOUTH);
        return roster;
    }

    private void filterPlayerTeams() {
        TeamRow selectedTeam = (TeamRow) playerTeam.getSelectedItem();
        String query = playerTeamSearch.getText().trim().toLowerCase(Locale.ROOT);
        List<TeamRow> matches = teams.stream()
                .filter(team -> (team.id() + " " + team.name() + " " + team.alias() + " " + countryName(team.country()))
                        .toLowerCase(Locale.ROOT).contains(query))
                .sorted(Comparator.comparing(TeamRow::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
        updatingPlayerTeams = true;
        try {
            playerTeam.removeAllItems();
            matches.forEach(playerTeam::addItem);
            if (selectedTeam != null && matches.stream().anyMatch(team -> team.id() == selectedTeam.id())) {
                playerTeam.setSelectedItem(selectedTeam);
            } else {
                playerTeam.setSelectedIndex(-1);
            }
        } finally {
            updatingPlayerTeams = false;
        }
        if (playerTeam.getSelectedItem() == null) {
            players.clear();
            fill(playerTable, List.of());
            editPlayerButton.setEnabled(false);
            batchPlayersButton.setEnabled(false);
        }
        if (!query.isEmpty() && !matches.isEmpty() && playerTeamSearch.hasFocus()) {
            SwingUtilities.invokeLater(() -> {
                if (playerTeamSearch.hasFocus() && playerTeam.getItemCount() > 0 && !playerTeam.isPopupVisible()) {
                    playerTeam.showPopup();
                }
            });
        } else if (matches.isEmpty() && playerTeam.isPopupVisible()) {
            playerTeam.hidePopup();
        }
    }

    private void editSelectedPlayer() {
        PlayerRow player = selected(playerTable, players);
        if (player == null) return;
        loadPlayerForm();
        JPanel editor = form("Dados do jogador", playerFields);
        addRow(editor, "País", playerCountry);
        addRow(editor, "Posição", playerPosition);
        addRow(editor, "Lado", playerSide);
        addRow(editor, "Fim do contrato", contractEndDate);
        addRow(editor, "Característica 1", characteristic1);
        addRow(editor, "Característica 2", characteristic2);
        JPanel stars = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        stars.add(starLocal);
        stars.add(starGlobal);
        addFormFooter(editor, stars);
        int result = JOptionPane.showConfirmDialog(this, new JScrollPane(editor),
                "Editar jogador — " + player.name(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) savePlayer();
    }

    private void showBatchPlayerEditor() {
        TeamRow team = (TeamRow) playerTeam.getSelectedItem();
        if (team == null || players.isEmpty()) {
            showError("Edição em lote", "Escolha um time com jogadores carregados.");
            return;
        }
        Map<String, JSpinner> values = new LinkedHashMap<>();
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Deixe -1 para manter o valor atual"));
        addBatchSpinner(form, values, "Idade", -1, 50);
        addBatchSpinner(form, values, "Força", -1, 100);
        addBatchSpinner(form, values, "Energia", -1, 100);
        addBatchSpinner(form, values, "Goleiro", -1, 100);
        addBatchSpinner(form, values, "Velocidade", -1, 100);
        addBatchSpinner(form, values, "Técnica", -1, 100);
        addBatchSpinner(form, values, "Passe", -1, 100);
        addBatchSpinner(form, values, "Desarme", -1, 100);
        addBatchSpinner(form, values, "Armação", -1, 100);
        addBatchSpinner(form, values, "Finalização", -1, 100);

        JComboBox<String> position = new JComboBox<>();
        position.addItem("Manter posição");
        Arrays.stream(PlayerPosition.values()).map(PlayerPosition::getLabel).forEach(position::addItem);
        JComboBox<String> localStar = triStateChoice();
        JComboBox<String> globalStar = triStateChoice();
        JComboBox<Country> batchCountry = new JComboBox<>();
        batchCountry.addItem(null);
        Arrays.stream(Country.values()).forEach(batchCountry::addItem);
        batchCountry.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list,Object value,int index,boolean selected,boolean focus) {
                super.getListCellRendererComponent(list,value,index,selected,focus);
                setText(value instanceof Country c ? c.getName() : "Manter país"); return this;
            }
        });
        addRow(form, "País", batchCountry);
        addRow(form, "Posição", position);
        addRow(form, "Estrela local", localStar);
        addRow(form, "Estrela mundial", globalStar);

        int result = JOptionPane.showConfirmDialog(this, new JScrollPane(form),
                "Edição em lote — " + team.name() + " (" + players.size() + " jogadores)",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        int age = batchValue(values, "Idade");
        if (age >= 0 && age < 15) {
            showError("Edição em lote", "A idade precisa ser -1 (manter) ou estar entre 15 e 50.");
            return;
        }
        Integer positionCode = position.getSelectedIndex() == 0 ? null : position.getSelectedIndex() - 1;
        presenter.batchUpdatePlayers(team.id(), nullableBatchValue(values, "Idade"),
                nullableBatchValue(values, "Força"), positionCode,
                nullableBatchValue(values, "Energia"), batchCountry.getSelectedItem() instanceof Country c ? c.getId() : null,
                nullableBatchValue(values, "Goleiro"), nullableBatchValue(values, "Velocidade"),
                nullableBatchValue(values, "Técnica"), nullableBatchValue(values, "Passe"),
                nullableBatchValue(values, "Desarme"), nullableBatchValue(values, "Armação"),
                nullableBatchValue(values, "Finalização"), triStateValue(localStar), triStateValue(globalStar));
    }

    private void addBatchSpinner(JPanel panel, Map<String, JSpinner> values, String name, int minimum, int maximum) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(-1, minimum, maximum, 1));
        values.put(name, spinner);
        addRow(panel, name, spinner);
    }

    private JComboBox<String> triStateChoice() {
        return new JComboBox<>(new String[]{"Manter", "Sim", "Não"});
    }

    private static Boolean triStateValue(JComboBox<String> choice) {
        return switch (choice.getSelectedIndex()) {
            case 1 -> true;
            case 2 -> false;
            default -> null;
        };
    }

    private static int batchValue(Map<String, JSpinner> values, String name) {
        return ((Number) values.get(name).getValue()).intValue();
    }

    private static Integer nullableBatchValue(Map<String, JSpinner> values, String name) {
        int value = batchValue(values, name);
        return value < 0 ? null : value;
    }
    private void configurePlayerControls() {
        playerSide.setRenderer(enumRenderer(PlayerSide::getDisplayName));
        playerPosition.setRenderer(enumRenderer(PlayerPosition::getDisplayName));
        characteristic1.setRenderer(enumRenderer(PlayerCharacteristic::getDisplayName));
        characteristic2.setRenderer(enumRenderer(PlayerCharacteristic::getDisplayName));
        contractEndDate.setEditor(new JSpinner.DateEditor(contractEndDate, "dd/MM/yyyy"));
    }
    private void configureTeamStyleChoices() {
        boolean realBrasfoot = presenter.supportsRealBrasfootTactics();
        style.removeAllItems();
        Arrays.stream(TeamPlayStyle.values()).filter(value -> realBrasfoot || value.getCode() <= 2).forEach(style::addItem);
        style.setEnabled(true);
        attack.removeAllItems();
        Arrays.stream(TeamAttackFocus.values()).filter(value -> realBrasfoot || value.getCode() <= 1).forEach(attack::addItem);
        attack.setEnabled(true);
    }
    private JPanel managerTab(){
        managerTeam.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list,Object value,int index,boolean selected,boolean focus) {
                super.getListCellRendererComponent(list,value,index,selected,focus);
                if(value instanceof TeamRow team)setText(team.name());
                return this;
            }
        });
        JPanel editor=form("Editor do técnico",managerFields);addRow(editor,"Time",managerTeam);addRow(editor,"",human); JButton save=new JButton("Salvar técnico");save.addActionListener(e->saveManager());addFormFooter(editor,save);
        managerTable.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting())loadManagerForm();});
        return sideBySide(new JScrollPane(managerTable),editor);
    }
    private JPanel leaguesTab(){
        leagueChoice.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof LeagueRow league) setText(league.name() + " (" + league.teamCount() + " times)");
                return this;
            }
        });
        JPanel top=new JPanel(new FlowLayout(FlowLayout.LEFT));top.add(new JLabel("Liga"));top.add(leagueChoice);
        leagueChoice.addActionListener(e->{LeagueRow l=(LeagueRow)leagueChoice.getSelectedItem();if(l!=null)presenter.loadLeagueTable(l.id());});
        JPanel editor=form("Editar classificacao",standingFields); JButton save=new JButton("Salvar linha");save.addActionListener(e->saveStanding());addFormFooter(editor,save);
        standingTable.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting())loadStandingForm();});
        JPanel left=new JPanel(new BorderLayout());left.add(top,BorderLayout.NORTH);left.add(new JScrollPane(standingTable));
        return sideBySide(left,editor);
    }
    private JPanel calendarTab() {
        calendarTargetDate.setEditor(new JSpinner.DateEditor(calendarTargetDate, "dd/MM/yyyy"));
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(new JLabel("Competicao")); controls.add(calendarCompetition);
        controls.add(new JLabel("Nova data")); controls.add(calendarTargetDate);
        JButton move = new JButton("Mover rodada selecionada");
        move.addActionListener(e -> {
            int selected = calendarTable.getSelectedRow();
            if (selected < 0) { showError("Calendario", "Selecione uma rodada."); return; }
            CalendarRoundRow row = visibleCalendarRounds.get(calendarTable.convertRowIndexToModel(selected));
            LocalDate target = ((Date) calendarTargetDate.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            try {
                if (presenter.calendarMoveHasHumanNationalTournamentConflict(
                        row.competitionKey(), row.date(), target)) {
                    int answer = JOptionPane.showConfirmDialog(this,
                            "O técnico humano comanda um clube e uma seleção, e já há uma partida da seleção "
                                    + "nesse dia por um torneio internacional (Copa, Eliminatórias, Euro, Nations League etc.).\n\n"
                                    + "O Brasfoot pode apresentar um comportamento inesperado e permitir controlar "
                                    + "apenas uma das partidas. Deseja mesmo colocar a rodada do clube nessa data?",
                            "Possível conflito com jogo da seleção",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if (answer != JOptionPane.YES_OPTION) return;
                }
            } catch (RuntimeException ex) {
                showError("Calendario", ex.getMessage());
                return;
            }
            presenter.moveCalendarRound(row.competitionKey(), row.date(), target);
        });
        controls.add(move);
        JPanel top = new JPanel(new BorderLayout()); top.add(calendarHint, BorderLayout.NORTH); top.add(controls, BorderLayout.SOUTH);
        calendarTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int index = calendarTable.getSelectedRow();
                if (index >= 0) calendarTargetDate.setValue(Date.from(visibleCalendarRounds.get(calendarTable.convertRowIndexToModel(index)).date().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }
        });
        calendarCompetition.setRenderer(enumRenderer(CalendarCompetitionFilter::label));
        calendarCompetition.addActionListener(e -> renderCalendarRows());
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.add(top, BorderLayout.NORTH); panel.add(new JScrollPane(calendarTable), BorderLayout.CENTER);
        return panel;
    }
    private JPanel countryTab(){JPanel editor=new JPanel(new FlowLayout(FlowLayout.LEFT));editor.setBorder(BorderFactory.createTitledBorder("Editor do pais"));editor.add(new JLabel("Nivel"));editor.add(countryLevel);JButton save=new JButton("Salvar pais");save.addActionListener(e->saveCountry());editor.add(save);countryTable.getSelectionModel().addListSelectionListener(e->{CountryRow c=selected(countryTable,countries);if(c!=null)countryLevel.setText(""+c.level());});return sideBySide(new JScrollPane(countryTable),editor);}

    @Override public void showSessionOpened(String id,Path path){session.setText("Sessao "+id+" - "+path);showStatus("Save aberto.");}
    @Override public void showSaveWritten(Path path){showStatus("Save salvo em "+path);JOptionPane.showMessageDialog(this,"Save salvo em:\n"+path,"Save salvo",JOptionPane.INFORMATION_MESSAGE);}
    @Override public void showTeams(List<TeamRow> rows){
        teams.clear();teams.addAll(rows);fillTeams("");
        TeamRow selectedManagerTeam=(TeamRow)managerTeam.getSelectedItem();
        managerTeam.removeAllItems();
        rows.stream().sorted(Comparator.comparing(TeamRow::name,String.CASE_INSENSITIVE_ORDER)).forEach(managerTeam::addItem);
        if(selectedManagerTeam!=null) rows.stream().filter(t->t.id()==selectedManagerTeam.id()).findFirst().ifPresent(managerTeam::setSelectedItem);
        updatingPlayerTeams=true;
        try {
            playerTeam.removeAllItems();
            playerTeam.setSelectedIndex(-1);
        } finally { updatingPlayerTeams=false; }
        playerTeamSearch.setText("");
        filterPlayerTeams();
        players.clear();fill(playerTable,List.of());
        editPlayerButton.setEnabled(false);
        batchPlayersButton.setEnabled(false);
        if(!teams.isEmpty())teamTable.setRowSelectionInterval(0,0);
    }
    @Override public void showPlayers(int teamId,List<PlayerRow> rows){
        PlayerRow previousSelection = selected(playerTable, players);
        players.clear();players.addAll(rows);
        fill(playerTable,players.stream().map(p->new Object[]{p.id(),p.name(),p.age(),p.overall(),PlayerPosition.fromCode(p.position()).getLabel(),PlayerSide.fromCode(p.side()).getLabel(),p.energy(),countryName(p.country()),(p.starLocal()?"Local ":"")+(p.starGlobal()?"Mundial":"")}).toList());
        if(!players.isEmpty()) {
            int selectedIndex = previousSelection == null ? -1
                    : java.util.stream.IntStream.range(0, players.size())
                    .filter(index -> players.get(index).id() == previousSelection.id()).findFirst().orElse(-1);
            playerTable.setRowSelectionInterval(selectedIndex >= 0 ? selectedIndex : 0,
                    selectedIndex >= 0 ? selectedIndex : 0);
        }
        editPlayerButton.setEnabled(!players.isEmpty());
        batchPlayersButton.setEnabled(!players.isEmpty() && playerTeam.getSelectedItem() != null);
    }
    @Override public void showManagers(List<ManagerRow> rows){managers.clear();managers.addAll(rows);fill(managerTable,managers.stream().map(m->new Object[]{m.id(),m.name(),teamName(m.teamId()),m.human(),m.confidenceBoard(),m.confidenceFans(),m.trophies()==null?0:m.trophies().size()}).toList());if(!managers.isEmpty())managerTable.setRowSelectionInterval(0,0);}
    @Override public void showLeagues(List<LeagueRow> rows){leagues.clear();leagues.addAll(rows);leagueChoice.removeAllItems();rows.forEach(leagueChoice::addItem);if(!rows.isEmpty())presenter.loadLeagueTable(rows.get(0).id());}
    @Override public void showLeagueTable(String id,List<LeagueTableRow> rows){selectedLeague=id;leagueRows.clear();leagueRows.addAll(rows);fill(standingTable,rows.stream().map(r->new Object[]{r.position(),r.teamId(),r.teamName(),r.points(),r.played(),r.wins(),r.draws(),r.losses(),r.goalsFor(),r.goalsAgainst(),r.goalDifference()}).toList());if(!rows.isEmpty())standingTable.setRowSelectionInterval(0,0);}
    @Override public void showCountries(List<CountryRow> rows){countries.clear();countries.addAll(rows);fill(countryTable,rows.stream().map(c->new Object[]{c.id(),c.name(),c.group(),c.level(),c.divisionCount()}).toList());if(!rows.isEmpty())countryTable.setRowSelectionInterval(0,0);}
    @Override public void showCalendar(List<CalendarRoundRow> rows){calendarRounds.clear();calendarRounds.addAll(rows);CalendarCompetitionFilter previous=(CalendarCompetitionFilter)calendarCompetition.getSelectedItem();calendarCompetition.removeAllItems();calendarCompetition.addItem(new CalendarCompetitionFilter(null,"Todas as competicoes"));Map<String,CalendarCompetitionFilter> choices=new LinkedHashMap<>();rows.forEach(r->choices.putIfAbsent(r.competitionKey(),new CalendarCompetitionFilter(r.competitionKey(),r.competition())));choices.values().forEach(calendarCompetition::addItem);if(previous!=null&&previous.competitionKey()!=null){for(int i=0;i<calendarCompetition.getItemCount();i++)if(Objects.equals(calendarCompetition.getItemAt(i).competitionKey(),previous.competitionKey()))calendarCompetition.setSelectedIndex(i);}renderCalendarRows();}
    private void renderCalendarRows(){CalendarCompetitionFilter filter=(CalendarCompetitionFilter)calendarCompetition.getSelectedItem();visibleCalendarRounds.clear();calendarRounds.stream().filter(r->filter==null||filter.competitionKey()==null||filter.competitionKey().equals(r.competitionKey())).forEach(visibleCalendarRounds::add);fill(calendarTable,visibleCalendarRounds.stream().map(r->new Object[]{r.date(),r.competition(),r.phase(),r.matchCount()}).toList());calendarHint.setText(calendarRounds.isEmpty()?"Nenhum jogo futuro em campeonatos por pontos foi encontrado.":calendarRounds.size()+" rodadas futuras em "+Math.max(0,calendarCompetition.getItemCount()-1)+" competicoes. Rodadas numeradas pela ordem das datas no save.");if(!visibleCalendarRounds.isEmpty())calendarTable.setRowSelectionInterval(0,0);}
    @Override public void showStatus(String message){status.setText(message);}
    @Override public void showError(String title,String message){status.setText(message);JOptionPane.showMessageDialog(this,message,title,JOptionPane.ERROR_MESSAGE);}

    private void fillTeams(String query){String q=query.toLowerCase(Locale.ROOT);fill(teamTable,teams.stream().filter(t->(t.id()+" "+t.name()+" "+t.alias()+" "+countryName(t.country())+" "+t.division()+" "+t.stadiumName()).toLowerCase(Locale.ROOT).contains(q)).map(t->new Object[]{t.id(),t.name(),t.alias(),countryName(t.country()),t.division(),t.level(),t.money(),t.reputation(),t.stadiumName()}).toList());}
    private void loadTeamForm(){TeamRow t=selected(teamTable,teams);if(t==null)return;put(teamFields,"Nome",t.name());put(teamFields,"Apelido",t.alias());teamCountry.setSelectedItem(Country.fromId(t.country()==null?-1:t.country()));put(teamFields,"Divisao",t.division());put(teamFields,"Nivel",t.level());put(teamFields,"Dinheiro",t.money());put(teamFields,"Estadio",t.stadiumName());for(int i=0;i<4;i++)put(teamFields,"Setor "+(i+1),t.stadiumSectors()!=null&&i<t.stadiumSectors().size()?t.stadiumSectors().get(i):0);reputation.setSelectedItem(t.reputation());TeamPlayStyle currentStyle=TeamPlayStyle.fromCode(t.tacticStyle());if(style.getItemCount()>0&&currentStyle.getCode()>2&&!presenter.supportsRealBrasfootTactics()){style.setSelectedIndex(0);style.setEnabled(false);}else{style.setEnabled(true);style.setSelectedItem(currentStyle);}marking.setSelectedItem(TeamMarking.fromCode(t.tacticMarking()));TeamAttackFocus currentFocus=TeamAttackFocus.fromCode(t.tacticFocus());if(attack.getItemCount()>0&&currentFocus.getCode()>1&&!presenter.supportsRealBrasfootTactics()){attack.setSelectedIndex(0);attack.setEnabled(false);}else{attack.setEnabled(true);attack.setSelectedItem(currentFocus);}}
    private void loadPlayerForm(){PlayerRow p=selected(playerTable,players);if(p==null)return;put(playerFields,"Nome",p.name());put(playerFields,"Idade",p.age());put(playerFields,"Forca",p.overall());put(playerFields,"Energia",p.energy());put(playerFields,"Salario",p.salary());playerCountry.setSelectedItem(Country.fromId(p.country()==null?-1:p.country()));put(playerFields,"Goleiro",p.skillGoalkeeping());put(playerFields,"Velocidade",p.skillSpeed());put(playerFields,"Tecnica",p.skillTechnique());put(playerFields,"Passe",p.skillPassing());put(playerFields,"Desarme",p.skillTackling());put(playerFields,"Armacao",p.skillPlaymaking());put(playerFields,"Finalizacao",p.skillFinishing());playerPosition.setSelectedItem(PlayerPosition.fromCode(p.position()));playerSide.setSelectedItem(PlayerSide.fromCode(p.side()));characteristic1.setSelectedItem(PlayerCharacteristic.fromCode(p.characteristic1()));characteristic2.setSelectedItem(PlayerCharacteristic.fromCode(p.characteristic2()));contractEndDate.setValue(dateFromTimestamp(p.contractEnd()));starLocal.setSelected(p.starLocal());starGlobal.setSelected(p.starGlobal());}
    private void loadManagerForm(){ManagerRow m=selected(managerTable,managers);if(m==null)return;put(managerFields,"Nome",m.name());teams.stream().filter(t->Objects.equals(t.id(),m.teamId())).findFirst().ifPresent(managerTeam::setSelectedItem);put(managerFields,"Confianca diretoria",m.confidenceBoard());put(managerFields,"Confianca torcida",m.confidenceFans());human.setSelected(Boolean.TRUE.equals(m.human()));}
    private void loadStandingForm(){LeagueTableRow r=selected(standingTable,leagueRows);if(r==null)return;put(standingFields,"Pontos",r.points());put(standingFields,"Vitorias",r.wins());put(standingFields,"Empates",r.draws());put(standingFields,"Derrotas",r.losses());put(standingFields,"Gols pro",r.goalsFor());put(standingFields,"Gols contra",r.goalsAgainst());}
    private void saveTeam(){TeamRow t=selected(teamTable,teams);if(t==null)return;try{List<String> sectors=new ArrayList<>();for(int i=1;i<=4;i++)sectors.add(val(teamFields,"Setor "+i));Country country=(Country)teamCountry.getSelectedItem();TeamPlayStyle selectedStyle=(TeamPlayStyle)style.getSelectedItem();TeamAttackFocus selectedFocus=(TeamAttackFocus)attack.getSelectedItem();presenter.updateTeam(t.id(),val(teamFields,"Nome"),val(teamFields,"Apelido"),val(teamFields,"Dinheiro"),String.valueOf(reputation.getSelectedItem()),val(teamFields,"Estadio"),sectors,country==null?"":Integer.toString(country.getId()),val(teamFields,"Divisao"),val(teamFields,"Nivel"),style.isEnabled()&&selectedStyle!=null?selectedStyle.getCode():null,((TeamMarking)marking.getSelectedItem()).getCode(),attack.isEnabled()&&selectedFocus!=null?selectedFocus.getCode():null);}catch(Exception e){showError("Dados inválidos",e.getMessage());}}
    private void savePlayer(){PlayerRow p=selected(playerTable,players);TeamRow t=(TeamRow)playerTeam.getSelectedItem();if(p==null||t==null)return;try{PlayerSide side=(PlayerSide)playerSide.getSelectedItem();PlayerPosition position=(PlayerPosition)playerPosition.getSelectedItem();PlayerCharacteristic cr1=(PlayerCharacteristic)characteristic1.getSelectedItem();PlayerCharacteristic cr2=(PlayerCharacteristic)characteristic2.getSelectedItem();Country country=(Country)playerCountry.getSelectedItem();presenter.updatePlayer(t.id(),p.id(),val(playerFields,"Nome"),num(playerFields,"Idade"),num(playerFields,"Forca"),position.getCode(),num(playerFields,"Energia"),val(playerFields,"Salario"),Integer.toString(side.getCode()),Long.toString(timestampFromDate((Date)contractEndDate.getValue())),cr1.getCode(),cr2.getCode(),num(playerFields,"Goleiro"),num(playerFields,"Velocidade"),num(playerFields,"Tecnica"),num(playerFields,"Passe"),num(playerFields,"Desarme"),num(playerFields,"Armacao"),num(playerFields,"Finalizacao"),country==null?"":Integer.toString(country.getId()),starLocal.isSelected(),starGlobal.isSelected());}catch(Exception e){showError("Dados inválidos",e.getMessage());}}
    private void saveManager(){ManagerRow m=selected(managerTable,managers);TeamRow team=(TeamRow)managerTeam.getSelectedItem();if(m!=null)presenter.updateManager(m.id(),val(managerFields,"Nome"),human.isSelected(),team==null?"":Integer.toString(team.id()),num(managerFields,"Confianca diretoria"),num(managerFields,"Confianca torcida"),m.trophies());}
    private void saveStanding(){LeagueTableRow r=selected(standingTable,leagueRows);if(r!=null)try{presenter.updateLeagueTableEntry(selectedLeague,r.teamId(),num(standingFields,"Pontos"),num(standingFields,"Vitorias"),num(standingFields,"Empates"),num(standingFields,"Derrotas"),num(standingFields,"Gols pro"),num(standingFields,"Gols contra"));}catch(Exception e){showError("Dados invalidos",e.getMessage());}}
    private void saveCountry(){CountryRow c=selected(countryTable,countries);if(c!=null)try{presenter.updateCountryLevel(c.id(),Integer.parseInt(countryLevel.getText()));}catch(Exception e){showError("Dados invalidos",e.getMessage());}}
    private void openSave(){JFileChooser fc=new JFileChooser();fc.setDialogTitle("Abrir save do Brasfoot");if(fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION)presenter.openSave(fc.getSelectedFile().toPath());}
    private void chooseSaveMode(){
        Object[] choices={"Substituir o arquivo atual","Salvar com outro nome","Cancelar"};
        int choice=JOptionPane.showOptionDialog(this,"Como deseja salvar as alterações?","Salvar save",
                JOptionPane.DEFAULT_OPTION,JOptionPane.QUESTION_MESSAGE,null,choices,choices[0]);
        if(choice==0){
            int confirm=JOptionPane.showConfirmDialog(this,"O arquivo atual será substituído. Deseja continuar?",
                    "Confirmar substituição",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
            if(confirm==JOptionPane.YES_OPTION)presenter.saveOverCurrent();
        }else if(choice==1)presenter.saveCopy();
    }
    private void selectLibrary(){JFileChooser fc=new JFileChooser();fc.setDialogTitle("Selecionar biblioteca do Brasfoot");if(fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION)presenter.selectBrasfootLibrary(fc.getSelectedFile().toPath());}
    private static JTable table(String... cols){JTable t=new JTable(new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}});t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);t.setAutoCreateRowSorter(true);t.setFillsViewportHeight(true);return t;}
    private static void fill(JTable t,List<Object[]> rows){DefaultTableModel m=(DefaultTableModel)t.getModel();m.setRowCount(0);rows.forEach(m::addRow);}
    private static <T>T selected(JTable table,List<T> rows){
        int viewRow=table.getSelectedRow(); if(viewRow<0)return null;
        Object id=table.getModel().getValueAt(table.convertRowIndexToModel(viewRow),0);
        for(T row:rows){
            Object rowId = row instanceof TeamRow value ? value.id()
                    : row instanceof PlayerRow value ? value.id()
                    : row instanceof ManagerRow value ? value.id()
                    : row instanceof LeagueTableRow value ? value.position()
                    : row instanceof CountryRow value ? value.id()
                    : null;
            if(Objects.equals(String.valueOf(rowId),String.valueOf(id)))return row;
        }
        return null;
    }
    private static Map<String,JTextField> fields(String...names){Map<String,JTextField> m=new LinkedHashMap<>();for(String n:names)m.put(n,new JTextField(12));return m;}
    private static JPanel form(String title,Map<String,JTextField> fields){JPanel p=new JPanel(new GridBagLayout());p.setBorder(BorderFactory.createTitledBorder(title));for(var e:fields.entrySet())addRow(p,e.getKey(),e.getValue());return p;}
    private static void addRow(JPanel p,String label,Component c){GridBagConstraints g=new GridBagConstraints();g.gridx=0;g.gridy=p.getComponentCount()/2;g.anchor=GridBagConstraints.WEST;g.insets=new Insets(2,4,2,4);p.add(new JLabel(label),g);g.gridx=1;g.weightx=1;g.fill=GridBagConstraints.HORIZONTAL;p.add(c,g);}
    private static void addFormFooter(JPanel p,Component c){GridBagConstraints g=new GridBagConstraints();g.gridx=0;g.gridy=(p.getComponentCount()+1)/2;g.gridwidth=2;g.anchor=GridBagConstraints.WEST;g.insets=new Insets(8,4,2,4);p.add(c,g);}
    private static JPanel wrap(Component... components){JPanel p=new JPanel();p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));for(Component c:components)p.add(c);return p;}
    private static JPanel sideBySide(Component left,Component right){JSplitPane p=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,new JScrollPane(left),new JScrollPane(right));p.setResizeWeight(.58);JPanel out=new JPanel(new BorderLayout());out.add(p);return out;}
    private static String val(Map<String,JTextField> f,String k){return f.get(k).getText().trim();}
    private static int num(Map<String,JTextField> f,String k){return Integer.parseInt(val(f,k));}
    private static void put(Map<String,JTextField> f,String k,Object v){f.get(k).setText(v==null?"":String.valueOf(v));}
    private static int integer(JSpinner s,int empty){return ((Number)s.getValue()).intValue()==empty?empty:((Number)s.getValue()).intValue();}
    private static JComboBox<String> enumNames(Enum<?>[] e){JComboBox<String> c=new JComboBox<>();for(Enum<?> x:e)c.addItem(x.name());return c;}
    private static <T> ListCellRenderer<T> enumRenderer(Function<T,String> label){return (list,value,index,selected,focus)->{JLabel cell=(JLabel)new DefaultListCellRenderer().getListCellRendererComponent(list,value,index,selected,focus);if(value!=null)cell.setText(label.apply(value));return cell;};}
    private static Date dateFromTimestamp(Long timestamp){long value=timestamp==null||timestamp<=0?System.currentTimeMillis():timestamp;if(value<100_000_000_000L)value*=1000;return Date.from(Instant.ofEpochMilli(value));}
    private static long timestampFromDate(Date value){LocalDate date=value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();}
    private static int countryId(Country c){return c==null?0:c.getId();}
    private static String countryName(Integer id){Country country=id==null?null:Country.fromId(id);return country==null?"País desconhecido":country.getName();}
    private String teamName(Integer id){return teams.stream().filter(team->Objects.equals(team.id(),id)).map(TeamRow::name).findFirst().orElse("Sem time");}
    private static DocumentListener change(Runnable r){return new DocumentListener(){public void insertUpdate(DocumentEvent e){r.run();}public void removeUpdate(DocumentEvent e){r.run();}public void changedUpdate(DocumentEvent e){r.run();}};}
    private record CalendarCompetitionFilter(String competitionKey,String label) { @Override public String toString(){return label;} }
}
