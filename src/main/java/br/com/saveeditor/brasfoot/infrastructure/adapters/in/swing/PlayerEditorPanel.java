package br.com.saveeditor.brasfoot.infrastructure.adapters.in.swing;

import br.com.saveeditor.brasfoot.domain.Player;
import br.com.saveeditor.brasfoot.domain.PlayerExtraAttributes;
import br.com.saveeditor.brasfoot.domain.enums.*;
import br.com.saveeditor.brasfoot.presentation.model.PlayerRow;
import br.com.saveeditor.brasfoot.presentation.presenter.BrasfootPresenter;

import javax.swing.*;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;

final class PlayerEditorPanel extends RecordEditor<PlayerEditorPanel.Key, PlayerRow> {
    record Key(int teamId, int draftKey) {}

    @Override
    Key draftKey(Key key) {
        return key.draftKey() < 0 ? new Key(-1, key.draftKey()) : key;
    }

    private final BrasfootPresenter presenter;
    private final JTextField name;
    private final JComboBox<Country> country;
    private final NumericSpinner age;
    private final JComboBox<PlayerPosition> position;
    private final JComboBox<PlayerSide> side;
    private final NumericSpinner overall;
    private final NumericSpinner energy;
    private final NumericSpinner potential;
    private final NumericSpinner estimatedPotential;
    private final NumericSpinner development;
    private final NumericSpinner shirtNumber;
    private final JComboBox<PlayerCharacteristic> firstTrait;
    private final JComboBox<PlayerCharacteristic> secondTrait;
    private final JCheckBox localStar;
    private final JCheckBox worldStar;
    private final NumericSpinner goalkeeping;
    private final NumericSpinner speed;
    private final NumericSpinner technique;
    private final NumericSpinner passing;
    private final NumericSpinner tackling;
    private final NumericSpinner playmaking;
    private final NumericSpinner finishing;
    private final NumericSpinner salary;
    private final JSpinner contract;
    private final NumericSpinner marketValue;
    private final NumericSpinner askingPrice;
    private final JCheckBox listedForSale;
    private final JCheckBox listedForLoan;
    private final JSpinner injuredUntil;

    PlayerEditorPanel(BrasfootPresenter presenter) {
        super("Selecione um jogador no elenco.");
        this.presenter = presenter;
        JPanel identity = group("Identificação");
        name = field(identity, "Nome", new JTextField(16));
        country = field(identity, "País", new JComboBox<>(Country.values()));
        country.setPrototypeDisplayValue(Country.BRASIL);
        country.setRenderer(SwingMainWindow.enumRenderer(Country::getName));
        age = field(identity, "Idade", new NumericSpinner("Idade", Player.MIN_AGE, Player.MIN_AGE, Player.MAX_AGE));

        JPanel profile = group("Perfil em campo");
        position = field(profile, "Posição", new JComboBox<>(PlayerPosition.values()));
        position.setRenderer(SwingMainWindow.enumRenderer(PlayerPosition::getDisplayName));
        side = field(profile, "Lado", new JComboBox<>(PlayerSide.values()));
        side.setRenderer(SwingMainWindow.enumRenderer(PlayerSide::getDisplayName));
        overall = field(profile, "Força", new NumericSpinner("Força", Player.MIN_OVERALL, Player.MIN_OVERALL, Player.MAX_ATTRIBUTE));
        energy = field(profile, "Energia", new NumericSpinner("Energia", 0, Player.MIN_ENERGY, Player.MAX_ATTRIBUTE));
        shirtNumber = number(profile, "Número da camisa", 0, PlayerExtraAttributes.MAX_SHIRT_NUMBER);
        firstTrait = trait(profile, "Característica 1");
        secondTrait = trait(profile, "Característica 2");
        localStar = field(profile, "", new JCheckBox("Estrela local"));
        worldStar = field(profile, "", new JCheckBox("Estrela mundial"));

        JPanel growth = group("Evolução (experimental)");
        potential = number(growth, "Potencial", 1, PlayerExtraAttributes.MAX_POTENTIAL);
        estimatedPotential = number(growth, "Potencial estimado", 1, PlayerExtraAttributes.MAX_POTENTIAL);
        development = number(growth, "Desenvolvimento", 0, Player.MAX_ATTRIBUTE);

        JPanel skills = group("Habilidades individuais");
        goalkeeping = skill(skills, "Goleiro");
        speed = skill(skills, "Velocidade");
        technique = skill(skills, "Técnica");
        passing = skill(skills, "Passe");
        tackling = skill(skills, "Desarme");
        playmaking = skill(skills, "Armação");
        finishing = skill(skills, "Finalização");

        JPanel employment = group("Contrato");
        salary = field(employment, "Salário", new NumericSpinner("Salário", 0, 0, Integer.MAX_VALUE));
        contract = field(employment, "Fim do contrato", SwingMainWindow.dayPicker());
        JPanel market = group("Mercado");
        marketValue = number(market, "Valor de mercado", 0, Integer.MAX_VALUE);
        askingPrice = number(market, "Preço pedido", 0, Integer.MAX_VALUE);
        listedForSale = field(market, "", new JCheckBox("Listado para venda"));
        listedForLoan = field(market, "", new JCheckBox("Listado para empréstimo"));
        JPanel health = group("Lesão");
        injuredUntil = field(health, "Lesionado até", SwingMainWindow.dayPicker());
        var injuryDate = dateInput(injuredUntil);
        JButton clearInjury = new JButton("Remover lesão");
        Runnable updateClearInjury = () -> clearInjury.setEnabled(injuredUntil.isEnabled() && !injuryDate.getText().isBlank());
        injuryDate.getDocument().addDocumentListener(SwingMainWindow.change(updateClearInjury));
        injuredUntil.addPropertyChangeListener("enabled", e -> updateClearInjury.run());
        clearInjury.addActionListener(e -> injuryDate.setText(""));
        SwingMainWindow.addRow(health, "", clearInjury);
        showRecord(null, null);
    }

    private JComboBox<PlayerCharacteristic> trait(JPanel section, String label) {
        JComboBox<PlayerCharacteristic> choice = field(section, label, new JComboBox<>(PlayerCharacteristic.values()));
        choice.setRenderer(SwingMainWindow.enumRenderer(PlayerCharacteristic::getDisplayName));
        return choice;
    }

    private NumericSpinner skill(JPanel section, String label) {
        return number(section, label, Player.MIN_SKILL, Player.MAX_ATTRIBUTE);
    }

    private NumericSpinner number(JPanel section, String label, int minimum, int maximum) {
        return field(section, label, new NumericSpinner(label, minimum, minimum, maximum));
    }

    @Override String recordName(PlayerRow row) { return row.name(); }

    @Override
    void load(PlayerRow row) {
        enableFields();
        name.setText(row.name());
        country.setSelectedItem(row.country() == null ? null : Country.fromId(row.country()));
        age.display(row.age());
        position.setSelectedItem(PlayerPosition.fromCode(row.position()));
        side.setSelectedItem(PlayerSide.fromCode(row.side()));
        overall.display(row.overall());
        energy.display(row.energy());
        firstTrait.setSelectedItem(PlayerCharacteristic.fromCode(row.characteristic1()));
        secondTrait.setSelectedItem(PlayerCharacteristic.fromCode(row.characteristic2()));
        localStar.setSelected(row.starLocal());
        worldStar.setSelected(row.starGlobal());
        goalkeeping.display(row.skillGoalkeeping());
        speed.display(row.skillSpeed());
        technique.display(row.skillTechnique());
        passing.display(row.skillPassing());
        tackling.display(row.skillTackling());
        playmaking.display(row.skillPlaymaking());
        finishing.display(row.skillFinishing());
        salary.display(row.salary());
        displayDate(contract, row.contractEnd());

        PlayerExtraAttributes extra = row.extraAttributes();
        displayAttribute(potential, extra.potential());
        displayAttribute(estimatedPotential, extra.estimatedPotential());
        displayAttribute(development, extra.development());
        displayAttribute(shirtNumber, extra.shirtNumber());
        displayAttribute(marketValue, extra.marketValue());
        displayAttribute(askingPrice, extra.askingPrice());
        displayFlag(listedForSale, extra.listedForSale());
        displayFlag(listedForLoan, extra.listedForLoan());
        displayDate(injuredUntil, extra.injuredUntil());
        injuredUntil.setEnabled(extra.injuredUntil() != null);
    }

    private void displayAttribute(NumericSpinner control, Integer value) {
        control.display(value);
        control.setEnabled(value != null);
    }

    private void displayFlag(JCheckBox control, Boolean value) {
        control.setSelected(Boolean.TRUE.equals(value));
        control.setEnabled(value != null);
    }

    private static JFormattedTextField dateInput(JSpinner control) {
        return ((JSpinner.DateEditor) control.getEditor()).getTextField();
    }

    private void displayDate(JSpinner control, Long timestamp) {
        if (timestamp == null || timestamp <= 0) {
            control.setValue(new Date());
            dateInput(control).setText("");
        } else {
            control.setValue(Date.from(Instant.ofEpochMilli(timestamp < 100_000_000_000L ? timestamp * 1000 : timestamp)));
            dateInput(control).setValue(control.getValue());
        }
    }

    @Override
    boolean save(Key key, PlayerRow row) throws ParseException {
        return presenter.updatePlayer(key.teamId(), row.id(), changedText(name),
                changedNumber(age, false), changedNumber(overall, false), changedChoice(position, PlayerPosition::getCode),
                changedNumber(energy, false), changedNumber(salary, true), changedChoice(side, PlayerSide::getCode),
                changedDate(contract, "Fim do contrato", null), changedChoice(firstTrait, PlayerCharacteristic::getCode),
                changedChoice(secondTrait, PlayerCharacteristic::getCode), changedNumber(goalkeeping, true),
                changedNumber(speed, true), changedNumber(technique, true), changedNumber(passing, true),
                changedNumber(tackling, true), changedNumber(playmaking, true), changedNumber(finishing, true),
                changedChoice(country, Country::getId), changed(localStar) ? localStar.isSelected() : null,
                changed(worldStar) ? worldStar.isSelected() : null,
                new PlayerExtraAttributes(changedNumber(potential, true), changedNumber(estimatedPotential, true),
                        changedNumber(development, true), changedNumber(shirtNumber, true),
                        changedNumber(marketValue, true), changedNumber(askingPrice, true),
                        changed(listedForSale) ? listedForSale.isSelected() : null,
                        changed(listedForLoan) ? listedForLoan.isSelected() : null,
                        changedDate(injuredUntil, "Lesionado até", 0L)));
    }

    private Long changedDate(JSpinner control, String label, Long clearValue) throws ParseException {
        if (!changed(control)) return null;
        var dateEditor = (JSpinner.DateEditor) control.getEditor();
        String raw = dateEditor.getTextField().getText().strip();
        if (raw.isEmpty()) return clearValue;
        var parsed = new java.text.ParsePosition(0);
        Date date = dateEditor.getFormat().parse(raw, parsed);
        if (date == null || parsed.getIndex() != raw.length()) {
            dateEditor.getTextField().requestFocusInWindow();
            throw new ParseException(label + ": informe uma data no formato dia/mês/ano.", 0);
        }
        var zone = ZoneId.systemDefault();
        return date.toInstant().atZone(zone).toLocalDate().atStartOfDay(zone).toInstant().toEpochMilli();
    }
}
