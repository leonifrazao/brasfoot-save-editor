package br.com.saveeditor.brasfoot.infrastructure.adapters.in.swing;

import br.com.saveeditor.brasfoot.application.shared.SaveEdit;
import br.com.saveeditor.brasfoot.presentation.model.*;
import br.com.saveeditor.brasfoot.presentation.presenter.BrasfootPresenter;
import br.com.saveeditor.brasfoot.presentation.view.BrasfootDesktopView;
import br.com.saveeditor.brasfoot.domain.enums.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
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
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import javax.swing.BoxLayout;

/** Swing desktop interface: native Java widgets, with no platform-specific UI libraries. */
public final class SwingMainWindow extends JFrame implements BrasfootDesktopView {
    private final BrasfootPresenter presenter;
    private final JTabbedPane tabs = new JTabbedPane();
    private final TransferPanel transfers;
    private JComponent playerTab;
    private final JLabel session = new JLabel("Nenhum save aberto");
    private final JButton reloadButton = new JButton("Recarregar save");
    private final JButton saveButton = new JButton("Salvar…");
    private final JButton undoButton = new JButton("Desfazer");
    private final JButton redoButton = new JButton("Refazer");
    private boolean pendingAppliedEdits;
    private final JLabel status = new JLabel("Abra um save .s22 para comecar.");
    private final List<TeamRow> teams = new ArrayList<>();
    private final List<PlayerRow> players = new ArrayList<>();
    private final List<LeagueRow> leagues = new ArrayList<>();
    private final List<LeagueTableRow> leagueRows = new ArrayList<>();
    private final List<CountryRow> countries = new ArrayList<>();
    private final List<CalendarRoundRow> calendarRounds = new ArrayList<>();
    private final List<CalendarRoundRow> visibleCalendarRounds = new ArrayList<>();
    private final RecordTable<TeamRow> teamTable = new RecordTable<>(TeamRow::id,
            team -> new Object[]{team.id(), team.name(), team.alias(), countryName(team.country()), team.division(),
                    team.level(), team.money(), team.reputation(), team.stadiumName()},
            "ID", "Time", "Apelido", "País", "Div.", "Nível", "Dinheiro", "Reputação", "Estádio");
    private final RecordTable<PlayerRow> playerTable = new RecordTable<>(PlayerRow::draftKey,
            player -> new Object[]{player.id(), player.name(), player.age(), player.overall(), player.extraAttributes().potential(),
                    PlayerPosition.fromCode(player.position()).getLabel(), PlayerSide.fromCode(player.side()).getLabel(),
                    player.energy(), countryName(player.country()),
                    (player.starLocal() ? "Local " : "") + (player.starGlobal() ? "Mundial" : "")},
            "ID", "Jogador", "Idade", "Força", "Potencial", "Posição", "Lado", "Energia", "País", "Estrelas");
    private final TeamEditorPanel teamEditor;
    private final PlayerEditorPanel playerEditor;
    private final ManagerTablePanel managerTable = new ManagerTablePanel();
    private final JTable leagueTable = table("Pos", "ID", "Time", "Pts", "J", "V", "E", "D", "GP", "GC", "SG");
    private final JTable countryTable = table("ID", "Pais", "Grupo", "Nivel", "Divisoes");
    private final JTable standingTable = table("Pos", "Time ID", "Time", "Pts", "J", "V", "E", "D", "GP", "GC", "SG");
    private final JTable calendarTable = table("Data atual", "Competição", "País", "Rodada / fase", "Jogos");
    private final JLabel calendarHint = new JLabel("Selecione uma rodada futura para alterar a data.");
    private final JComboBox<CalendarCompetitionFilter> calendarCompetition = new JComboBox<>();
    private final JComboBox<TeamRow> playerTeam = new JComboBox<>();
    private final JTextField playerTeamSearch = new JTextField(20);
    private final JComboBox<LeagueRow> leagueChoice = new JComboBox<>();
    private String selectedLeague;
    private final Map<String,JTextField> managerFields = fields("Nome", "Confianca diretoria", "Confianca torcida");
    private final JComboBox<TeamRow> managerTeam = new JComboBox<>();
    private final JCheckBox human = new JCheckBox("Técnico humano");
    private final JButton saveManagerButton = new JButton("Salvar técnico");
    private final Map<String,JTextField> standingFields = fields("Pontos", "Vitorias", "Empates", "Derrotas", "Gols pro", "Gols contra");
    private final JTextField countryLevel = new JTextField(8);
    private final JSpinner calendarTargetDate = dayPicker();
    private boolean updatingPlayerTeams;
    private Integer loadedRosterId;
    private JButton batchPlayersButton;
    private JButton batchTeamsButton;

    public SwingMainWindow(BrasfootPresenter presenter) {
        super("Brasfoot Save Editor");
        this.presenter = presenter;
        transfers = new TransferPanel(presenter);
        teamEditor = new TeamEditorPanel(presenter);
        playerEditor = new PlayerEditorPanel(presenter);
        presenter.attach(this);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent event) {
                if (confirmLeavingChanges("Fechar o editor")) dispose();
            }
        });
        setMinimumSize(new Dimension(980, 560));
        setSize(1240, 680);
        setLocationRelativeTo(null);
        buildUi();
    }

    private void buildUi() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton libraryButton = new JButton("Selecionar Brasfoot"), openButton = new JButton("Abrir save");
        libraryButton.addActionListener(e -> selectLibrary());
        openButton.addActionListener(e -> openSave());
        reloadButton.addActionListener(e -> {
            if (confirmLeavingChanges("Recarregar save")) presenter.reloadSave();
        });
        saveButton.addActionListener(e -> chooseSaveMode());
        undoButton.addActionListener(e -> presenter.undo());
        redoButton.addActionListener(e -> presenter.redo());
        saveButton.setEnabled(false);
        reloadButton.setEnabled(false);
        undoButton.setEnabled(false);
        redoButton.setEnabled(false);
        undoButton.setToolTipText("Desfazer a última edição aplicada");
        redoButton.setToolTipText("Refazer a edição desfeita");
        toolbar.add(libraryButton);
        toolbar.add(openButton);
        toolbar.add(reloadButton);
        toolbar.add(saveButton);
        toolbar.add(undoButton);
        toolbar.add(redoButton);
        toolbar.add(session);
        int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        bindHistoryShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_Z, shortcut), "undoSaveEdit", undoButton);
        bindHistoryShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_Y, shortcut), "redoSaveEdit", redoButton);
        bindHistoryShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_Z, shortcut | InputEvent.SHIFT_DOWN_MASK),
                "redoSaveEdit", redoButton);
        playerTab = playerTab();
        tabs.addTab("Times", teamTab());
        tabs.addTab("Jogadores", playerTab);
        tabs.addTab("Técnicos", managerTab());
        tabs.addTab("Transferências", transfers);
        tabs.addTab("Ligas", leaguesTab());
        tabs.addTab("Paises", countryTab());
        tabs.addTab("Calendario", calendarTab());
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedComponent() == transfers) transfers.refreshRosters();
        });
        JPanel root = new JPanel(new BorderLayout(6,6)); root.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        root.add(toolbar, BorderLayout.NORTH); root.add(tabs, BorderLayout.CENTER); root.add(status, BorderLayout.SOUTH); setContentPane(root);
    }

    private void bindHistoryShortcut(KeyStroke key, String action, JButton button) {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(key, action);
        getRootPane().getActionMap().put(action, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent event) {
                if (button.isEnabled()) button.doClick(0);
            }
        });
    }

    private JComponent teamTab() {
        JTextField filter = new JTextField();
        filter.putClientProperty("JTextField.placeholderText", "Pesquisar time por nome, ID, apelido, país ou estádio");
        filter.putClientProperty("JTextField.showClearButton", true);
        filter.getDocument().addDocumentListener(change(() -> {
            String query = filter.getText().strip().toLowerCase(Locale.ROOT);
            teamTable.filter(team -> (team.id() + " " + team.name() + " " + team.alias() + " "
                    + countryName(team.country()) + " " + team.division() + " " + team.stadiumName())
                    .toLowerCase(Locale.ROOT).contains(query));
        }));
        batchTeamsButton = new JButton("Edição em lote…");
        batchTeamsButton.setEnabled(false);
        batchTeamsButton.addActionListener(e -> showBatchTeamEditor());
        JButton viewPlayers = new JButton("Ver jogadores também");
        viewPlayers.setEnabled(false);
        viewPlayers.addActionListener(e -> openRoster(teamTable.recordAt(teamTable.getSelectedRow())));
        teamTable.onSelection(team -> {
            viewPlayers.setEnabled(team != null);
            teamEditor.showRecord(team == null ? null : team.id(), team);
        });
        teamTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent event) {
                if (SwingUtilities.isLeftMouseButton(event) && event.getClickCount() == 1) {
                    openRoster(teamTable.recordAt(teamTable.rowAtPoint(event.getPoint())));
                }
            }
        });
        teamTable.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "openRoster");
        teamTable.getActionMap().put("openRoster", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent event) { viewPlayers.doClick(0); }
        });
        configureColumns(teamTable, 42, 145, 100, 84, 42, 52, 86, 83, 135);
        return editSplit(tablePanel(filter, teamTable, viewPlayers, batchTeamsButton), teamEditor);
    }

    private void openRoster(TeamRow team) {
        if (team == null) return;
        if (!Objects.equals(loadedRosterId, team.id())) presenter.loadPlayers(team.id());
        if (Objects.equals(loadedRosterId, team.id())) tabs.setSelectedComponent(playerTab);
    }

    private void showBatchTeamEditor() {
        if (teams.isEmpty()) return;
        JTextField money = new JTextField();
        JTextField stadium = new JTextField();
        NumericSpinner level = new NumericSpinner("Nível", -1, -1, 100);
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
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.add(editor, BorderLayout.NORTH);
        String apply = "Aplicar alterações";
        Object[] options = {apply, "Cancelar"};
        JOptionPane pane = new JOptionPane(content, JOptionPane.PLAIN_MESSAGE,
                JOptionPane.DEFAULT_OPTION, null, options, apply);
        JPanel presets = new JPanel(new GridLayout(1, 3, 6, 0));
        for (TeamBatchAction action : TeamBatchAction.values()) {
            JButton button = new JButton(action.label + "...");
            button.addActionListener(e -> pane.setValue(action));
            presets.add(button);
        }
        content.add(presets, BorderLayout.SOUTH);
        JDialog dialog = pane.createDialog(this, "Edição em lote — todos os times");
        dialog.setVisible(true);
        dialog.dispose();

        if (apply.equals(pane.getValue())) {
            try {
                int levelValue = level.readInt(false);
                presenter.batchUpdateAllTeams(money.getText().isBlank() ? null : Long.valueOf(money.getText()),
                        "Manter reputação".equals(rep.getSelectedItem()) ? null : TeamReputation.valueOf((String) rep.getSelectedItem()),
                        stadium.getText().isBlank() ? null : stadium.getText(), levelValue < 0 ? null : levelValue,
                        batchStyle.getSelectedItem() instanceof TeamPlayStyle selected ? selected.getCode() : null);
            } catch (Exception ex) { showError("Dados inválidos", ex.getMessage()); }
        } else if (pane.getValue() instanceof TeamBatchAction action) {
            confirmTeamBatchAction(action);
        }
    }

    private void confirmTeamBatchAction(TeamBatchAction action) {
        JPanel editor = new JPanel(new BorderLayout(0, 8));
        JComboBox<Integer> country = new JComboBox<>();
        if (action == TeamBatchAction.DESTROY_COUNTRY) {
            List<Integer> available = teams.stream().map(TeamRow::country).filter(Objects::nonNull)
                    .distinct().sorted(Comparator.comparing(SwingMainWindow::countryName)).toList();
            if (available.isEmpty()) {
                showError(action.label, "Nenhum país encontrado nos times deste save.");
                return;
            }
            country.setModel(new DefaultComboBoxModel<>(available.toArray(Integer[]::new)));
            country.setRenderer(enumRenderer(SwingMainWindow::countryName));
            Set<Integer> humanIds = presenter.humanManagedTeamIds();
            TeamRow selected = teamTable.recordAt(teamTable.getSelectedRow());
            Integer preferredCountry = selected == null ? null : selected.country();
            if (selected == null || !humanIds.contains(selected.id()) || preferredCountry == null) {
                preferredCountry = teams.stream().filter(team -> humanIds.contains(team.id()))
                        .map(TeamRow::country).filter(Objects::nonNull).findFirst().orElse(preferredCountry);
            }
            if (preferredCountry != null) country.setSelectedItem(preferredCountry);
            JPanel countryForm = new JPanel(new GridBagLayout());
            addRow(countryForm, "País", country);
            editor.add(countryForm, BorderLayout.NORTH);
        }
        editor.add(new JLabel("A ação deve afetar os times controlados por técnicos humanos?"), BorderLayout.CENTER);
        Object[] options = {"Preservar times humanos", "Incluir times humanos", "Cancelar"};
        int choice = JOptionPane.showOptionDialog(this, editor, action.label,
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
        if (choice != 0 && choice != 1) return;
        boolean preserveHumans = choice == 0;
        switch (action) {
            case DESTROY_ALL -> presenter.destroyTeams(preserveHumans);
            case DESTROY_COUNTRY -> presenter.destroyTeamsInCountry((Integer) country.getSelectedItem(), preserveHumans);
            case IMPROVE_ALL -> presenter.improveTeams(preserveHumans);
        }
    }

    private enum TeamBatchAction {
        DESTROY_ALL("Destruir todos"),
        DESTROY_COUNTRY("Destruir todos do país"),
        IMPROVE_ALL("Melhorar todos");

        private final String label;

        TeamBatchAction(String label) { this.label = label; }
    }
    private JComponent playerTab() {
        playerTeam.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof TeamRow team) setText(team.id() + " - " + team.name());
                return this;
            }
        });
        playerTeam.setMaximumRowCount(16);
        playerTeam.setPrototypeDisplayValue(new TeamRow(0, " Time do elenco             ", null, 0, null,
                null, null, null, null, null, null, null, null, null));
        playerTeamSearch.putClientProperty("JTextField.placeholderText", "Pesquisar time do elenco");
        playerTeamSearch.putClientProperty("JTextField.showClearButton", true);
        playerTeamSearch.getDocument().addDocumentListener(change(this::filterPlayerTeams));
        playerTeam.addActionListener(e -> {
            if (updatingPlayerTeams || !(playerTeam.getSelectedItem() instanceof TeamRow team)
                    || Objects.equals(loadedRosterId, team.id())) return;
            presenter.loadPlayers(team.id());
            if (!Objects.equals(loadedRosterId, team.id())) filterPlayerTeams();
        });
        batchPlayersButton = new JButton("Edição em lote…");
        batchPlayersButton.addActionListener(e -> showBatchPlayerEditor());
        batchPlayersButton.setEnabled(false);
        playerTable.onSelection(player -> playerEditor.showRecord(
                player == null ? null : new PlayerEditorPanel.Key(loadedRosterId, player.draftKey()), player));
        configureColumns(playerTable, 42, 175, 48, 48, 68, 74, 60, 63, 92, 75);

        JPanel selector = new JPanel(new GridLayout(1, 2, 6, 0));
        JLabel selectorLabel = new JLabel("Time do elenco");
        selectorLabel.setLabelFor(playerTeam);
        JPanel club = new JPanel(new BorderLayout(6, 0));
        club.add(selectorLabel, BorderLayout.WEST);
        club.add(playerTeam, BorderLayout.CENTER);
        selector.add(playerTeamSearch);
        selector.add(club);
        return editSplit(tablePanel(selector, playerTable, batchPlayersButton), playerEditor);
    }

    private void filterPlayerTeams() {
        String query = playerTeamSearch.getText().strip().toLowerCase(Locale.ROOT);
        List<TeamRow> matches = teams.stream()
                .filter(team -> (team.id() + " " + team.name() + " " + team.alias() + " " + countryName(team.country()))
                        .toLowerCase(Locale.ROOT).contains(query))
                .sorted(Comparator.comparing(TeamRow::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
        TeamRow loaded = teams.stream().filter(team -> Objects.equals(loadedRosterId, team.id())).findFirst().orElse(null);
        var model = new DefaultComboBoxModel<>(matches.toArray(TeamRow[]::new));
        model.setSelectedItem(loaded);
        updatingPlayerTeams = true;
        try {
            playerTeam.setModel(model);
        } finally {
            updatingPlayerTeams = false;
        }
        if (!query.isEmpty() && !matches.isEmpty() && playerTeamSearch.hasFocus()) {
            SwingUtilities.invokeLater(() -> {
                if (playerTeamSearch.hasFocus() && playerTeam.isShowing()
                        && playerTeam.getItemCount() > 0 && !playerTeam.isPopupVisible()) {
                    playerTeam.showPopup();
                }
            });
        } else if (matches.isEmpty() && playerTeam.isPopupVisible()) {
            playerTeam.hidePopup();
        }
    }

    private void showBatchPlayerEditor() {
        TeamRow team = (TeamRow) playerTeam.getSelectedItem();
        if (team == null || players.isEmpty()) {
            showError("Edição em lote", "Escolha um time com jogadores carregados.");
            return;
        }
        Map<String, NumericSpinner> values = new LinkedHashMap<>();
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

        try {
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
        } catch (IllegalArgumentException ex) {
            showError("Edição em lote", ex.getMessage());
        }
    }

    private void addBatchSpinner(JPanel panel, Map<String, NumericSpinner> values, String name, int minimum, int maximum) {
        NumericSpinner spinner = new NumericSpinner(name, -1, minimum, maximum);
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

    private static int batchValue(Map<String, NumericSpinner> values, String name) {
        return values.get(name).readInt(false);
    }

    private static Integer nullableBatchValue(Map<String, NumericSpinner> values, String name) {
        int value = batchValue(values, name);
        return value < 0 ? null : value;
    }
    private JPanel managerTab(){
        managerTeam.setToolTipText("Se os dois técnicos já têm clube, eles trocam de clube. O destino precisa estar livre ou sob controle do computador.");
        managerTeam.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list,Object value,int index,boolean selected,boolean focus) {
                super.getListCellRendererComponent(list,value,index,selected,focus);
                if(value instanceof TeamRow team)setText(team.name());
                return this;
            }
        });
        JPanel editor = form("Editor do técnico", managerFields);
        addRow(editor, "Time", managerTeam);
        addRow(editor, "", human);
        saveManagerButton.addActionListener(e -> saveManager());
        addFormFooter(editor, saveManagerButton);
        managerTable.addSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadManagerForm();
        });
        loadManagerForm();
        return sideBySide(managerTable, editor);
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
    static JSpinner dayPicker() {
        var picker = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        var dateEditor = new JSpinner.DateEditor(picker, "dd/MM/yyyy");
        dateEditor.getFormat().setLenient(false);
        dateEditor.getTextField().setFocusLostBehavior(JFormattedTextField.PERSIST);
        picker.setEditor(dateEditor);
        return picker;
    }

    private JPanel calendarTab() {
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(new JLabel("Competicao")); controls.add(calendarCompetition);
        controls.add(new JLabel("Nova data")); controls.add(calendarTargetDate);
        JButton move = new JButton("Mover rodada selecionada");
        move.addActionListener(e -> {
            int selected = calendarTable.getSelectedRow();
            if (selected < 0) { showError("Calendario", "Selecione uma rodada."); return; }
            CalendarRoundRow row = visibleCalendarRounds.get(calendarTable.convertRowIndexToModel(selected));
            try {
                calendarTargetDate.commitEdit();
                LocalDate target = ((Date) calendarTargetDate.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                if (presenter.calendarMoveHasHumanNationalTournamentConflict(
                    row.eventKey(), row.date(), target)) {
                    int answer = JOptionPane.showConfirmDialog(this,
                            "A nova data terá partidas de um clube e de uma seleção sob controle humano.\n\n"
                                    + "O Brasfoot pode apresentar um comportamento inesperado e permitir controlar "
                                    + "apenas uma das partidas. Deseja mover a rodada para essa data?",
                            "Possível conflito com jogo da seleção",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if (answer != JOptionPane.YES_OPTION) return;
                }
                presenter.moveCalendarRound(row.eventKey(), row.date(), target);
            } catch (java.text.ParseException ex) {
                showError("Calendário", "Informe uma data no formato dia/mês/ano.");
            } catch (RuntimeException ex) {
                showError("Calendario", ex.getMessage());
            }
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

    @Override public void showSessionOpened(String id, Path path) {
        session.setText("Sessão " + id + " - " + path);
        resetSessionViews();
        saveButton.setEnabled(true);
        reloadButton.setEnabled(true);
        showStatus("Save aberto.");
    }

    @Override public void showSessionClosed() {
        session.setText("Nenhum save aberto");
        resetSessionViews();
        saveButton.setEnabled(false);
        reloadButton.setEnabled(false);
    }

    @Override public void showEditHistory(SaveEdit.HistoryStatus history) {
        undoButton.setEnabled(history.canUndo());
        redoButton.setEnabled(history.canRedo());
        pendingAppliedEdits = history.pending();
    }

    private void resetSessionViews() {
        teamEditor.clear();
        playerEditor.clear();
        loadedRosterId = null;
        teams.clear();
        players.clear();
        teamTable.setRows(List.of(), true);
        playerTable.setRows(List.of(), true);
        filterPlayerTeams();
        batchTeamsButton.setEnabled(false);
        batchPlayersButton.setEnabled(false);
        managerTable.setManagers(List.of());
        managerTeam.removeAllItems();
        managerTable.setTeams(List.of());
        transfers.clear();
        showLeagues(List.of());
        selectedLeague = null;
        leagueRows.clear();
        fill(standingTable, List.of());
        showCountries(List.of());
        showCalendar(List.of());
    }

    @Override public void showSaveWritten(Path path){showStatus("Save salvo em "+path);JOptionPane.showMessageDialog(this,"Save salvo em:\n"+path,"Save salvo",JOptionPane.INFORMATION_MESSAGE);}

    @Override public void showTeams(List<TeamRow> rows) {
        teams.clear();
        teams.addAll(rows);
        teamTable.setRows(rows, false);
        batchTeamsButton.setEnabled(!teams.isEmpty());
        managerTeam.removeAllItems();
        rows.stream().sorted(Comparator.comparing(TeamRow::name, String.CASE_INSENSITIVE_ORDER)).forEach(managerTeam::addItem);
        managerTable.setTeams(rows);
        transfers.setTeams(rows);
        loadManagerForm();
        filterPlayerTeams();
        if (playerTeam.getSelectedItem() instanceof TeamRow roster) presenter.loadPlayers(roster.id());
    }

    @Override public void showPlayers(int teamId, List<PlayerRow> rows) {
        boolean newRoster = !Objects.equals(teamId, loadedRosterId);
        loadedRosterId = teamId;
        if (newRoster) filterPlayerTeams();
        players.clear();
        players.addAll(rows);
        playerTable.setRows(rows, newRoster);
        batchPlayersButton.setEnabled(!rows.isEmpty());
    }

    @Override public void showManagers(List<ManagerRow> rows) {
        managerTable.setManagers(rows);
        transfers.setManagers(rows);
    }
    @Override public void showLeagues(List<LeagueRow> rows){leagues.clear();leagues.addAll(rows);leagueChoice.removeAllItems();rows.forEach(leagueChoice::addItem);if(!rows.isEmpty())presenter.loadLeagueTable(rows.get(0).id());}
    @Override public void showLeagueTable(String id,List<LeagueTableRow> rows){selectedLeague=id;leagueRows.clear();leagueRows.addAll(rows);fill(standingTable,rows.stream().map(r->new Object[]{r.position(),r.teamId(),r.teamName(),r.points(),r.played(),r.wins(),r.draws(),r.losses(),r.goalsFor(),r.goalsAgainst(),r.goalDifference()}).toList());if(!rows.isEmpty())standingTable.setRowSelectionInterval(0,0);}
    @Override public void showCountries(List<CountryRow> rows){countries.clear();countries.addAll(rows);fill(countryTable,rows.stream().map(c->new Object[]{c.id(),c.name(),c.group(),c.level(),c.divisionCount()}).toList());if(!rows.isEmpty())countryTable.setRowSelectionInterval(0,0);}
    @Override public void showCalendar(List<CalendarRoundRow> rows) {
        calendarRounds.clear();
        calendarRounds.addAll(rows);
        CalendarCompetitionFilter previous = (CalendarCompetitionFilter) calendarCompetition.getSelectedItem();
        CalendarCompetitionFilter all = new CalendarCompetitionFilter(null, "Todas as competições");
        Map<String, CalendarCompetitionFilter> choices = new LinkedHashMap<>();
        for (CalendarRoundRow row : rows) {
            String label = row.country() == null ? row.competition() : row.competition() + " · " + row.country();
            choices.putIfAbsent(row.competitionKey(), new CalendarCompetitionFilter(row.competitionKey(), label));
        }
        var model = new DefaultComboBoxModel<CalendarCompetitionFilter>();
        model.addElement(all);
        choices.values().forEach(model::addElement);
        if (previous != null && previous.competitionKey() != null) {
            model.setSelectedItem(choices.getOrDefault(previous.competitionKey(), all));
        }
        calendarCompetition.setModel(model);
        renderCalendarRows();
    }

    private void renderCalendarRows() {
        CalendarCompetitionFilter filter = (CalendarCompetitionFilter) calendarCompetition.getSelectedItem();
        visibleCalendarRounds.clear();
        calendarRounds.stream().filter(row -> filter == null || filter.competitionKey() == null
                || filter.competitionKey().equals(row.competitionKey())).forEach(visibleCalendarRounds::add);
        fill(calendarTable, visibleCalendarRounds.stream().map(row ->
                new Object[]{row.date(), row.competition(), row.country(), row.phase(), row.matchCount()}).toList());
        int count = visibleCalendarRounds.size();
        calendarHint.setText(switch (count) {
            case 0 -> "Nenhuma rodada ou fase futura encontrada.";
            case 1 -> "1 data editável.";
            default -> count + " datas editáveis.";
        });
        if (count > 0) calendarTable.setRowSelectionInterval(0, 0);
    }
    @Override public void showStatus(String message){status.setText(message);}
    @Override public void showError(String title,String message){status.setText(message);JOptionPane.showMessageDialog(this,message,title,JOptionPane.ERROR_MESSAGE);}

    private void loadManagerForm() {
        ManagerRow manager = managerTable.selectedManager();
        boolean selected = manager != null;
        managerFields.values().forEach(field -> field.setEnabled(selected));
        managerTeam.setEnabled(selected);
        human.setEnabled(selected);
        saveManagerButton.setEnabled(selected);
        put(managerFields, "Nome", selected ? manager.name() : null);
        put(managerFields, "Confianca diretoria", selected ? manager.confidenceBoard() : null);
        put(managerFields, "Confianca torcida", selected ? manager.confidenceFans() : null);
        human.setSelected(selected && Boolean.TRUE.equals(manager.human()));
        TeamRow team = selected ? teams.stream().filter(t -> Objects.equals(t.id(), manager.teamId()))
                .findFirst().orElse(null) : null;
        managerTeam.setSelectedItem(team);
    }
    private void loadStandingForm(){LeagueTableRow r=selected(standingTable,leagueRows);if(r==null)return;put(standingFields,"Pontos",r.points());put(standingFields,"Vitorias",r.wins());put(standingFields,"Empates",r.draws());put(standingFields,"Derrotas",r.losses());put(standingFields,"Gols pro",r.goalsFor());put(standingFields,"Gols contra",r.goalsAgainst());}
    private void saveManager() {
        ManagerRow manager = managerTable.selectedManager();
        if (manager == null) return;
        TeamRow team = (TeamRow) managerTeam.getSelectedItem();
        String teamId = team == null || Objects.equals(team.id(), manager.teamId()) ? "" : Integer.toString(team.id());
        Boolean humanChange = human.isSelected() == Boolean.TRUE.equals(manager.human()) ? null : human.isSelected();
        try {
            presenter.updateManager(manager.id(), val(managerFields, "Nome"), humanChange, teamId,
                    num(managerFields, "Confianca diretoria"), num(managerFields, "Confianca torcida"), manager.trophies());
        } catch (NumberFormatException e) {
            showError("Dados inválidos", "Informe valores inteiros para a confiança da diretoria e da torcida.");
        }
    }
    private void saveStanding(){LeagueTableRow r=selected(standingTable,leagueRows);if(r!=null)try{presenter.updateLeagueTableEntry(selectedLeague,r.teamId(),num(standingFields,"Pontos"),num(standingFields,"Vitorias"),num(standingFields,"Empates"),num(standingFields,"Derrotas"),num(standingFields,"Gols pro"),num(standingFields,"Gols contra"));}catch(Exception e){showError("Dados invalidos",e.getMessage());}}
    private void saveCountry(){CountryRow c=selected(countryTable,countries);if(c!=null)try{presenter.updateCountryLevel(c.id(),Integer.parseInt(countryLevel.getText()));}catch(Exception e){showError("Dados invalidos",e.getMessage());}}
    private void openSave() {
        JFileChooser chooser = createSaveChooser(presenter.getInstallationDirectory());
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION && confirmLeavingChanges("Abrir outro save")) {
            presenter.openSave(chooser.getSelectedFile().toPath());
        }
    }

    static JFileChooser createSaveChooser(Path installationDirectory) {
        JFileChooser chooser = new JFileChooser();
        if (installationDirectory != null) {
            Path saveDirectory = installationDirectory.resolve("sav");
            if (isBrowsableDirectory(saveDirectory)) {
                chooser.setCurrentDirectory(saveDirectory.toFile());
            } else if (isBrowsableDirectory(installationDirectory)) {
                chooser.setCurrentDirectory(installationDirectory.toFile());
            }
        }
        chooser.setDialogTitle("Abrir save do Brasfoot");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("Saves do Brasfoot (*.s22)", "s22"));
        return chooser;
    }

    private static boolean isBrowsableDirectory(Path directory) {
        try {
            // Accessing the explicit dot also checks search permission on Unix.
            return Files.isDirectory(directory.resolve(".")) && Files.isReadable(directory);
        } catch (SecurityException e) {
            return false;
        }
    }

    private void chooseSaveMode() {
        SaveEdit.Preview preview = presenter.savePreview();
        if (preview == null) return;
        Object[] choices = {"Substituir arquivo atual", "Salvar cópia", "Cancelar"};
        int choice = JOptionPane.showOptionDialog(this, new SaveReviewPanel(preview, pendingDrafts() > 0),
                "Revisar e salvar", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, choices, choices[2]);
        if (choice == 0) presenter.saveOverCurrent();
        else if (choice == 1) presenter.saveCopy();
    }

    private void selectLibrary() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Selecionar biblioteca do Brasfoot");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION
                && confirmLeavingChanges("Trocar biblioteca do Brasfoot")) {
            presenter.selectBrasfootLibrary(chooser.getSelectedFile().toPath());
        }
    }

    private int pendingDrafts() {
        return teamEditor.draftCount() + playerEditor.draftCount();
    }

    private boolean confirmLeavingChanges(String title) {
        boolean drafts = pendingDrafts() > 0;
        if (!drafts && !pendingAppliedEdits) return true;
        String message = pendingAppliedEdits
                ? "As alterações ainda não gravadas no arquivo atual serão descartadas."
                : "Os rascunhos que ainda não foram aplicados serão descartados.";
        return JOptionPane.showConfirmDialog(this, message + "\nDeseja continuar?", title,
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private static void configureColumns(JTable table, int... widths) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int column = 0; column < widths.length; column++) {
            table.getColumnModel().getColumn(column).setPreferredWidth(widths[column]);
        }
    }

    private static JPanel tablePanel(Component filter, RecordTable<?> table, JButton... actions) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.add(filter, BorderLayout.NORTH);
        panel.add(table.scrollPane(), BorderLayout.CENTER);
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        for (JButton action : actions) footer.add(action);
        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    static JSplitPane editSplit(JComponent table, JComponent editor) {
        table.setMinimumSize(new Dimension(320, 100));
        editor.setMinimumSize(new Dimension(320, 100));
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, table, editor);
        split.setContinuousLayout(true);
        split.setResizeWeight(0.62);
        split.setBorder(BorderFactory.createEmptyBorder());
        SwingUtilities.invokeLater(() -> split.setDividerLocation(0.62));
        return split;
    }

    private static JTable table(String... cols){JTable t=new JTable(new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}});t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);t.setAutoCreateRowSorter(true);t.setFillsViewportHeight(true);return t;}
    private static void fill(JTable t,List<Object[]> rows){DefaultTableModel m=(DefaultTableModel)t.getModel();m.setRowCount(0);rows.forEach(m::addRow);}
    private static <T>T selected(JTable table,List<T> rows){
        int viewRow=table.getSelectedRow(); if(viewRow<0)return null;
        Object id=table.getModel().getValueAt(table.convertRowIndexToModel(viewRow),0);
        for(T row:rows){
            Object rowId = row instanceof LeagueTableRow value ? value.position()
                    : row instanceof CountryRow value ? value.id()
                    : null;
            if(Objects.equals(String.valueOf(rowId),String.valueOf(id)))return row;
        }
        return null;
    }
    private static Map<String,JTextField> fields(String...names){Map<String,JTextField> m=new LinkedHashMap<>();for(String n:names)m.put(n,new JTextField(12));return m;}
    private static JPanel form(String title,Map<String,JTextField> fields){JPanel p=new JPanel(new GridBagLayout());p.setBorder(BorderFactory.createTitledBorder(title));for(var e:fields.entrySet())addRow(p,e.getKey(),e.getValue());return p;}
    static void addRow(JPanel panel, String label, Component field) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = panel.getComponentCount() / 2;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(2, 4, 2, 4);
        JLabel caption = new JLabel(label);
        caption.setLabelFor(field instanceof JSpinner spinner
                ? ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField() : field);
        panel.add(caption, constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, constraints);
    }
    private static void addFormFooter(JPanel p,Component c){GridBagConstraints g=new GridBagConstraints();g.gridx=0;g.gridy=(p.getComponentCount()+1)/2;g.gridwidth=2;g.anchor=GridBagConstraints.WEST;g.insets=new Insets(8,4,2,4);p.add(c,g);}
    private static JPanel sideBySide(Component left,Component right){JSplitPane p=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,new JScrollPane(left),new JScrollPane(right));p.setResizeWeight(.58);JPanel out=new JPanel(new BorderLayout());out.add(p);return out;}
    private static String val(Map<String,JTextField> f,String k){return f.get(k).getText().trim();}
    private static int num(Map<String,JTextField> f,String k){return Integer.parseInt(val(f,k));}
    private static void put(Map<String,JTextField> f,String k,Object v){f.get(k).setText(v==null?"":String.valueOf(v));}
    private static int integer(JSpinner s,int empty){return ((Number)s.getValue()).intValue()==empty?empty:((Number)s.getValue()).intValue();}
    static <T> ListCellRenderer<T> enumRenderer(Function<T,String> label){return (list,value,index,selected,focus)->{JLabel cell=(JLabel)new DefaultListCellRenderer().getListCellRendererComponent(list,value,index,selected,focus);if(value!=null)cell.setText(label.apply(value));return cell;};}
    private static String countryName(Integer id){Country country=id==null?null:Country.fromId(id);return country==null?"País desconhecido":country.getName();}
    static DocumentListener change(Runnable r){return new DocumentListener(){public void insertUpdate(DocumentEvent e){r.run();}public void removeUpdate(DocumentEvent e){r.run();}public void changedUpdate(DocumentEvent e){r.run();}};}
    private record CalendarCompetitionFilter(String competitionKey,String label) { @Override public String toString(){return label;} }
}
