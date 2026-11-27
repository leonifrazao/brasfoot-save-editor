package br.com.saveeditor.brasfoot.infrastructure.adapters.in.swing;

import br.com.saveeditor.brasfoot.domain.enums.*;
import br.com.saveeditor.brasfoot.presentation.model.TeamRow;
import br.com.saveeditor.brasfoot.presentation.presenter.BrasfootPresenter;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

final class TeamEditorPanel extends RecordEditor<Integer, TeamRow> {
    private final BrasfootPresenter presenter;
    private final JTextField name;
    private final JTextField alias;
    private final JComboBox<Country> country;
    private final NumericSpinner division;
    private final NumericSpinner level;
    private final NumericSpinner money;
    private final JComboBox<TeamReputation> reputation;
    private final JTextField stadium;
    private final List<NumericSpinner> sectors;
    private final JComboBox<TeamPlayStyle> style;
    private final JComboBox<TeamMarking> marking;
    private final JComboBox<TeamAttackFocus> focus;

    TeamEditorPanel(BrasfootPresenter presenter) {
        super("Selecione um time na tabela.");
        this.presenter = presenter;
        JPanel identity = group("Identificação");
        name = field(identity, "Nome", new JTextField(16));
        alias = field(identity, "Apelido", new JTextField(16));
        country = field(identity, "País", new JComboBox<>(Country.values()));
        country.setPrototypeDisplayValue(Country.BRASIL);
        country.setRenderer(SwingMainWindow.enumRenderer(Country::getName));

        JPanel club = group("Clube");
        division = field(club, "Divisão", new NumericSpinner("Divisão", 0, Integer.MIN_VALUE, Integer.MAX_VALUE));
        level = field(club, "Nível", new NumericSpinner("Nível", 0, Integer.MIN_VALUE, Integer.MAX_VALUE));
        money = field(club, "Dinheiro", new NumericSpinner("Dinheiro", 0, 0, Long.MAX_VALUE));
        reputation = field(club, "Reputação", new JComboBox<>(TeamReputation.values()));
        reputation.setRenderer(SwingMainWindow.enumRenderer(TeamReputation::getDisplayName));

        JPanel tactics = group("Táticas");
        style = field(tactics, "Estilo de jogo", new JComboBox<>());
        marking = field(tactics, "Marcação", new JComboBox<>(TeamMarking.values()));
        focus = field(tactics, "Concentrar ataques", new JComboBox<>());
        style.setRenderer(SwingMainWindow.enumRenderer(TeamPlayStyle::getDisplayName));
        marking.setRenderer(SwingMainWindow.enumRenderer(TeamMarking::getDisplayName));
        focus.setRenderer(SwingMainWindow.enumRenderer(TeamAttackFocus::getDisplayName));

        JPanel ground = group("Estádio");
        stadium = field(ground, "Nome do estádio", new JTextField(16));
        sectors = java.util.stream.IntStream.rangeClosed(1, 4).mapToObj(i -> {
            String label = "Setor " + i;
            return field(ground, label, new NumericSpinner(label, 0, 0, Integer.MAX_VALUE));
        }).toList();
        showRecord(null, null);
    }

    @Override String recordName(TeamRow row) { return row.name(); }

    @Override
    void load(TeamRow row) {
        enableFields();
        name.setText(row.name());
        alias.setText(row.alias());
        country.setSelectedItem(row.country() == null ? null : Country.fromId(row.country()));
        division.display(row.division());
        level.display(row.level());
        money.display(row.money());
        reputation.setSelectedItem(TeamReputation.valueOf(row.reputation()));
        stadium.setText(row.stadiumName());
        for (int i = 0; i < sectors.size(); i++) {
            sectors.get(i).display(row.stadiumSectors() == null ? 0 : row.stadiumSectors().get(i));
        }

        boolean realBrasfoot = presenter.supportsRealBrasfootTactics();
        TeamPlayStyle currentStyle = TeamPlayStyle.fromCode(row.tacticStyle());
        style.setModel(new DefaultComboBoxModel<>(Arrays.stream(TeamPlayStyle.values())
                .filter(value -> realBrasfoot || value.getCode() <= 2 || value == currentStyle)
                .toArray(TeamPlayStyle[]::new)));
        style.setSelectedItem(currentStyle);
        style.setEnabled(realBrasfoot || currentStyle.getCode() <= 2);
        marking.setSelectedItem(TeamMarking.fromCode(row.tacticMarking()));
        TeamAttackFocus currentFocus = TeamAttackFocus.fromCode(row.tacticFocus());
        focus.setModel(new DefaultComboBoxModel<>(Arrays.stream(TeamAttackFocus.values())
                .filter(value -> realBrasfoot || value.getCode() <= 1 || value == currentFocus)
                .toArray(TeamAttackFocus[]::new)));
        focus.setSelectedItem(currentFocus);
        focus.setEnabled(realBrasfoot || currentFocus.getCode() <= 1);
    }

    @Override
    boolean save(Integer id, TeamRow row) {
        List<Integer> stadiumSizes = sectors.stream().anyMatch(this::changed)
                ? sectors.stream().map(spinner -> spinner.readInt(false)).toList() : null;
        return presenter.updateTeam(id, changedText(name), changedText(alias), changed(money) ? money.read(false) : null,
                changed(reputation) ? (TeamReputation) reputation.getSelectedItem() : null,
                changedText(stadium), stadiumSizes, changedChoice(country, Country::getId),
                changedNumber(division, true), changedNumber(level, true),
                changedChoice(style, TeamPlayStyle::getCode), changedChoice(marking, TeamMarking::getCode),
                changedChoice(focus, TeamAttackFocus::getCode));
    }
}
