package br.com.saveeditor.brasfoot.infrastructure.adapters.in.swing;

import br.com.saveeditor.brasfoot.application.ports.in.record.PlayerTransferTerms;
import br.com.saveeditor.brasfoot.presentation.model.ManagerRow;
import br.com.saveeditor.brasfoot.presentation.model.PlayerRow;
import br.com.saveeditor.brasfoot.presentation.model.TeamRow;
import br.com.saveeditor.brasfoot.presentation.presenter.BrasfootPresenter;
import br.com.saveeditor.brasfoot.util.StringUtils;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

final class TransferPanel extends JTabbedPane {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private final BrasfootPresenter presenter;
    private final SearchableChoice origin = new SearchableChoice("Pesquisar time de origem");
    private final SearchableChoice playerDestination = new SearchableChoice("Pesquisar time de destino");
    private final SearchableChoice manager = new SearchableChoice("Pesquisar técnico ou time atual");
    private final SearchableChoice managerDestination = new SearchableChoice("Pesquisar time de destino");
    private final RecordTable<PlayerRow> originRoster = rosterTable();
    private final RecordTable<PlayerRow> destinationRoster = rosterTable();
    private final JTextField playerSearch = new JTextField(22);
    private final JTextField transferFee = new JTextField(12);
    private final TransferDay transferDate;
    private final JTextField transferSalary = new JTextField(12);
    private final TransferDay transferContractEnd;
    private final JButton movePlayer = new JButton("Transferir jogador");
    private final JLabel transferSummary = new JLabel();
    private final JButton moveManager = new JButton("Transferir técnico");
    private final Map<Integer, String> teamNames = new HashMap<>();
    private List<ManagerRow> managers = List.of();

    TransferPanel(BrasfootPresenter presenter) {
        this.presenter = presenter;
        transferDate = new TransferDay("Data no histórico", "Data do jogo", presenter::currentGameDate);
        transferContractEnd = new TransferDay("Fim do contrato", "Manter atual", this::contractEndOrGameDate);
        addTab("Jogadores", playerForm());
        addTab("Técnicos", managerForm());
        origin.addActionListener(e -> loadRoster(origin, originRoster, true));
        origin.onFilter(() -> {
            if (selection(origin) == null) loadRoster(origin, originRoster, true);
        });
        playerDestination.addActionListener(e -> loadRoster(playerDestination, destinationRoster, true));
        playerDestination.onFilter(() -> {
            if (selection(playerDestination) == null) loadRoster(playerDestination, destinationRoster, true);
        });
        for (SearchableChoice combo : List.of(manager, managerDestination)) {
            combo.onFilter(this::updateButtons);
        }
        originRoster.onSelection(row -> updateButtons());
        manager.addActionListener(e -> updateButtons());
        managerDestination.addActionListener(e -> updateButtons());
        movePlayer.addActionListener(e -> transferSelectedPlayer());
        moveManager.addActionListener(e -> presenter.transferManager(selection(manager).id(), selection(managerDestination).id()));
        updateButtons();
    }

    void setTeams(List<TeamRow> rows) {
        teamNames.clear();
        rows.forEach(team -> teamNames.put(team.id(), team.name()));
        List<Choice> clubs = rows.stream().map(team -> new Choice(team.id(), team.name() + " · " + team.id(),
                StringUtils.normalize(team.name() + " " + Objects.toString(team.alias(), "") + " " + team.id()))).toList();
        playerDestination.setChoices(clubs, selection(playerDestination));
        managerDestination.setChoices(clubs, selection(managerDestination));
        origin.setChoices(clubs, selection(origin));
        refreshRosters();
        updateManagerChoices();
    }

    void setManagers(List<ManagerRow> rows) {
        managers = rows;
        updateManagerChoices();
    }

    void clear() {
        setTeams(List.of());
        setManagers(List.of());
        for (SearchableChoice combo : List.of(origin, playerDestination, manager, managerDestination)) {
            combo.search.setText("");
        }
        playerSearch.setText("");
        clearTransferTerms();
    }

    private JPanel playerForm() {
        JPanel selectors = new JPanel(new GridLayout(1, 2, 12, 0));
        selectors.add(rosterSelector("Time de origem", origin));
        selectors.add(rosterSelector("Time de destino", playerDestination));

        playerSearch.putClientProperty("JTextField.placeholderText", "Pesquisar jogador na origem");
        playerSearch.putClientProperty("JTextField.showClearButton", true);
        playerSearch.getAccessibleContext().setAccessibleName("Pesquisar jogador no elenco de origem");
        playerSearch.getDocument().addDocumentListener(SwingMainWindow.change(() -> {
            String query = StringUtils.normalize(playerSearch.getText()).strip();
            originRoster.filter(row -> StringUtils.normalize(row.name() + " " + row.id()).contains(query));
        }));
        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.add(selectors, BorderLayout.CENTER);
        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        search.add(playerSearch);
        header.add(search, BorderLayout.SOUTH);

        JPanel rosters = new JPanel(new GridLayout(1, 2, 12, 0));
        rosters.add(originRoster.scrollPane());
        rosters.add(destinationRoster.scrollPane());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        actions.add(transferSummary);
        actions.add(movePlayer);
        JPanel footer = new JPanel(new BorderLayout(0, 8));
        footer.add(transferTermsForm(), BorderLayout.CENTER);
        footer.add(actions, BorderLayout.SOUTH);

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.add(header, BorderLayout.NORTH);
        panel.add(rosters, BorderLayout.CENTER);
        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel transferTermsForm() {
        JPanel form = new JPanel(new GridLayout(1, 4, 10, 0));
        form.setBorder(BorderFactory.createTitledBorder("Detalhes opcionais"));
        form.add(transferField("Valor da transferência", transferFee, "0 (gratuita)"));
        form.add(transferDate);
        form.add(transferField("Novo salário", transferSalary, "Manter atual"));
        form.add(transferContractEnd);
        transferDate.setToolTipText("DD/MM/AAAA. O jogador muda de time imediatamente.");
        return form;
    }

    private JPanel transferField(String label, JTextField field, String placeholder) {
        var group = new JPanel(new BorderLayout(0, 4));
        var caption = new JLabel(label);
        caption.setLabelFor(field);
        field.putClientProperty("JTextField.placeholderText", placeholder);
        group.add(caption, BorderLayout.NORTH);
        group.add(field, BorderLayout.CENTER);
        return group;
    }

    private void transferSelectedPlayer() {
        PlayerRow selected = selectedPlayer();
        PlayerTransferTerms terms;
        try {
            Integer fee = readAmount(transferFee, "Valor da transferência");
            terms = new PlayerTransferTerms(fee == null ? 0 : fee, transferDate.selectedDate(),
                    readAmount(transferSalary, "Novo salário"), transferContractEnd.selectedDate());
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Dados da transferência", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (presenter.transferPlayer(selection(origin).id(), selected.id(), selection(playerDestination).id(), terms)) {
            clearTransferTerms();
            if (selected.draftKey() < 0) destinationRoster.revealRecord(selected.draftKey());
        }
    }

    private Integer readAmount(JTextField field, String label) {
        String text = field.getText().strip();
        if (text.isEmpty()) return null;
        try {
            if (text.matches("[0-9]{1,3}(\\.[0-9]{3})+")) text = text.replace(".", "");
            return Integer.valueOf(text);
        } catch (NumberFormatException e) {
            field.requestFocusInWindow();
            throw new IllegalArgumentException(label + " deve ser um número inteiro de até " + Integer.MAX_VALUE + ".");
        }
    }

    private void clearTransferTerms() {
        transferFee.setText("");
        transferSalary.setText("");
        transferDate.clear();
        transferContractEnd.clear();
    }

    private LocalDate contractEndOrGameDate() {
        Long timestamp = selectedPlayer().contractEnd();
        if (timestamp == null || timestamp <= 0) return presenter.currentGameDate();
        long millis = timestamp < 100_000_000_000L ? timestamp * 1000 : timestamp;
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private JPanel rosterSelector(String label, SearchableChoice club) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        JLabel title = new JLabel(label);
        title.setLabelFor(club);
        panel.add(title, BorderLayout.NORTH);
        panel.add(club.withSearch(), BorderLayout.CENTER);
        return panel;
    }

    private static RecordTable<PlayerRow> rosterTable() {
        var table = new RecordTable<PlayerRow>(false, PlayerRow::draftKey, row -> new Object[]{row.id(), row.name()},
                "ID", "Jogador");
        var id = table.getColumnModel().getColumn(0);
        id.setPreferredWidth(60);
        id.setMaxWidth(90);
        return table;
    }

    private JPanel managerForm() {
        JPanel form = new JPanel(new GridBagLayout());
        SwingMainWindow.addRow(form, "Técnico", manager.withSearch());
        SwingMainWindow.addRow(form, "Time de destino", managerDestination.withSearch());
        SwingMainWindow.addRow(form, "", moveManager);
        return wrap(form, new JLabel("Se o destino tiver um técnico de computador, ele assume o clube de origem ou fica sem clube."));
    }

    private JPanel wrap(JPanel form, JLabel hint) {
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        content.add(form, BorderLayout.CENTER);
        content.add(hint, BorderLayout.SOUTH);
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.add(content);
        return panel;
    }

    void refreshRosters() {
        loadRoster(origin, originRoster, false);
        loadRoster(playerDestination, destinationRoster, false);
    }

    private void loadRoster(SearchableChoice club, RecordTable<PlayerRow> table, boolean newClub) {
        Choice team = selection(club);
        table.setRows(team == null ? List.of() : presenter.rosterForTransfer(team.id()), newClub);
        updateButtons();
    }

    private PlayerRow selectedPlayer() {
        return originRoster.recordAt(originRoster.getSelectedRow());
    }

    private void updateManagerChoices() {
        List<Choice> choices = managers.stream().map(row -> {
            String club = teamNames.getOrDefault(row.teamId(), "Sem clube");
            return new Choice(row.id(), row.name() + " · " + club + " · " + row.id());
        }).toList();
        manager.setChoices(choices, selection(manager));
        updateButtons();
    }

    private void updateButtons() {
        Choice from = selection(origin);
        Choice to = selection(playerDestination);
        PlayerRow player = selectedPlayer();
        movePlayer.setEnabled(from != null && player != null && to != null && from.id() != to.id());
        transferDate.setAvailable(!teamNames.isEmpty());
        transferContractEnd.setAvailable(player != null);
        transferSummary.setText(player == null && to == null ? "Selecione um jogador e o time de destino."
                : "Jogador: " + (player == null ? "—" : player.name())
                + "   ·   Destino: " + (to == null ? "—" : teamNames.getOrDefault(to.id(), to.label())));

        Choice coach = selection(manager);
        Choice club = selection(managerDestination);
        moveManager.setEnabled(coach != null && club != null && managers.stream()
                .anyMatch(row -> row.id() == coach.id() && !Objects.equals(row.teamId(), club.id())));
    }

    private static Choice selection(JComboBox<Choice> combo) {
        return (Choice) combo.getSelectedItem();
    }

    private record Choice(int id, String label, String searchText) {
        Choice(int id, String label) {
            this(id, label, StringUtils.normalize(label));
        }

        @Override public String toString() { return label; }
    }

    private static final class TransferDay extends JPanel {
        private final JCheckBox useCustomDate;
        private final JSpinner picker = SwingMainWindow.dayPicker();
        private final JFormattedTextField input = ((JSpinner.DateEditor) picker.getEditor()).getTextField();

        TransferDay(String label, String placeholder, Supplier<LocalDate> initialDate) {
            super(new BorderLayout(0, 4));
            useCustomDate = new JCheckBox(label);
            input.getAccessibleContext().setAccessibleName(label);
            input.putClientProperty("JTextField.placeholderText", placeholder);
            add(useCustomDate, BorderLayout.NORTH);
            add(picker, BorderLayout.CENTER);
            useCustomDate.addActionListener(e -> {
                if (useCustomDate.isSelected()) {
                    try {
                        LocalDate date = initialDate.get();
                        picker.setValue(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                        input.setValue(picker.getValue());
                        picker.setEnabled(true);
                    } catch (RuntimeException ex) {
                        clear();
                        JOptionPane.showMessageDialog(this, ex.getMessage(), label, JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    clear();
                }
            });
            clear();
        }

        void setAvailable(boolean available) {
            useCustomDate.setEnabled(available);
            picker.setEnabled(available && useCustomDate.isSelected());
        }

        LocalDate selectedDate() {
            if (!useCustomDate.isSelected()) return null;
            try {
                return LocalDate.parse(input.getText().strip(), DATE_FORMAT);
            } catch (DateTimeParseException e) {
                input.requestFocusInWindow();
                throw new IllegalArgumentException(useCustomDate.getText() + " deve ser uma data no formato DD/MM/AAAA.");
            }
        }

        void clear() {
            useCustomDate.setSelected(false);
            picker.setEnabled(false);
            input.setText("");
        }
    }

    private static final class SearchableChoice extends JComboBox<Choice> {
        private final JTextField search = new JTextField(24);
        private List<Choice> choices = List.of();

        SearchableChoice(String placeholder) {
            search.putClientProperty("JTextField.placeholderText", placeholder);
            search.putClientProperty("JTextField.showClearButton", true);
            search.getAccessibleContext().setAccessibleName(placeholder);
            search.setEnabled(false);
            setPrototypeDisplayValue(new Choice(0, "Nome para seleção no save                "));
            setMaximumRowCount(18);
            setEnabled(false);
            search.addActionListener(e -> {
                if (getItemCount() == 1) setSelectedIndex(0);
                else if (getItemCount() > 1 && isShowing()) {
                    requestFocusInWindow();
                    showPopup();
                }
            });
        }

        JPanel withSearch() {
            JPanel field = new JPanel(new BorderLayout(6, 0));
            field.add(search, BorderLayout.WEST);
            field.add(this, BorderLayout.CENTER);
            return field;
        }

        void onFilter(Runnable changed) {
            search.getDocument().addDocumentListener(SwingMainWindow.change(() -> {
                filter(selection(this));
                changed.run();
                updateSearchPopup();
            }));
        }

        private void updateSearchPopup() {
            if (search.getText().isBlank() || getItemCount() == 0) {
                hidePopup();
            } else if (search.hasFocus()) {
                SwingUtilities.invokeLater(() -> {
                    if (search.hasFocus() && isShowing() && !search.getText().isBlank()
                            && getItemCount() > 0 && !isPopupVisible()) {
                        showPopup();
                    }
                });
            }
        }

        void setChoices(List<Choice> choices, Choice previous) {
            this.choices = choices.stream()
                    .sorted(Comparator.comparing(Choice::label, String.CASE_INSENSITIVE_ORDER))
                    .toList();
            search.setEnabled(!choices.isEmpty());
            filter(previous);
        }

        private void filter(Choice previous) {
            String[] terms = StringUtils.normalize(search.getText()).strip().split("\\s+");
            Choice[] matches = choices.stream()
                    .filter(choice -> Arrays.stream(terms).allMatch(choice.searchText()::contains))
                    .toArray(Choice[]::new);
            var model = new DefaultComboBoxModel<>(matches);
            model.setSelectedItem(previous == null ? null : Arrays.stream(matches)
                    .filter(choice -> choice.id() == previous.id()).findFirst().orElse(null));
            setModel(model);
            setEnabled(matches.length > 0);
            search.putClientProperty("JComponent.outline", matches.length == 0 && !search.getText().isBlank()
                    ? "error" : null);
        }
    }
}
