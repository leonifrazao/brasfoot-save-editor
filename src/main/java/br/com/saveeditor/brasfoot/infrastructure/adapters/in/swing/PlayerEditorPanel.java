package br.com.saveeditor.brasfoot.infrastructure.adapters.in.swing;

import br.com.saveeditor.brasfoot.domain.Player;
import br.com.saveeditor.brasfoot.domain.enums.*;
import br.com.saveeditor.brasfoot.presentation.model.PlayerRow;
import br.com.saveeditor.brasfoot.presentation.presenter.BrasfootPresenter;

import javax.swing.*;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

final class PlayerEditorPanel extends RecordEditor<PlayerEditorPanel.Key, PlayerRow> {
    record Key(int teamId, int playerId) {}

    private final BrasfootPresenter presenter;
    private final JTextField name;
    private final JComboBox<Country> country;
    private final NumericSpinner age;
    private final JComboBox<PlayerPosition> position;
    private final JComboBox<PlayerSide> side;
    private final NumericSpinner overall;
    private final NumericSpinner energy;
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
        firstTrait = trait(profile, "Característica 1");
        secondTrait = trait(profile, "Característica 2");
        localStar = field(profile, "", new JCheckBox("Estrela local"));
        worldStar = field(profile, "", new JCheckBox("Estrela mundial"));

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
        contract = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        var dateEditor = new JSpinner.DateEditor(contract, "dd/MM/yyyy");
        dateEditor.getFormat().setLenient(false);
        dateEditor.getTextField().setFocusLostBehavior(JFormattedTextField.PERSIST);
        contract.setEditor(dateEditor);
        field(employment, "Fim do contrato", contract);
        showRecord(null, null);
    }

    private JComboBox<PlayerCharacteristic> trait(JPanel section, String label) {
        JComboBox<PlayerCharacteristic> choice = field(section, label, new JComboBox<>(PlayerCharacteristic.values()));
        choice.setRenderer(SwingMainWindow.enumRenderer(PlayerCharacteristic::getDisplayName));
        return choice;
    }

    private NumericSpinner skill(JPanel section, String label) {
        return field(section, label, new NumericSpinner(label, Player.MIN_SKILL, Player.MIN_SKILL, Player.MAX_ATTRIBUTE));
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
        var dateInput = ((JSpinner.DateEditor) contract.getEditor()).getTextField();
        Long timestamp = row.contractEnd();
        if (timestamp == null || timestamp <= 0) {
            contract.setValue(new Date());
            dateInput.setText("");
        } else {
            contract.setValue(Date.from(Instant.ofEpochMilli(timestamp < 100_000_000_000L ? timestamp * 1000 : timestamp)));
            dateInput.setValue(contract.getValue());
        }
    }

    @Override
    boolean save(Key key, PlayerRow row) throws ParseException {
        return presenter.updatePlayer(key.teamId(), key.playerId(), changedText(name),
                changedNumber(age, false), changedNumber(overall, false), changedChoice(position, PlayerPosition::getCode),
                changedNumber(energy, false), changedNumber(salary, true), changedChoice(side, PlayerSide::getCode),
                changedContract(), changedChoice(firstTrait, PlayerCharacteristic::getCode),
                changedChoice(secondTrait, PlayerCharacteristic::getCode), changedNumber(goalkeeping, true),
                changedNumber(speed, true), changedNumber(technique, true), changedNumber(passing, true),
                changedNumber(tackling, true), changedNumber(playmaking, true), changedNumber(finishing, true),
                changedChoice(country, Country::getId), changed(localStar) ? localStar.isSelected() : null,
                changed(worldStar) ? worldStar.isSelected() : null);
    }

    private Long changedContract() throws ParseException {
        var dateEditor = (JSpinner.DateEditor) contract.getEditor();
        String raw = dateEditor.getTextField().getText().strip();
        if (!changed(contract) || raw.isEmpty()) return null;
        var parsed = new java.text.ParsePosition(0);
        Date date = dateEditor.getFormat().parse(raw, parsed);
        if (date == null || parsed.getIndex() != raw.length()) {
            dateEditor.getTextField().requestFocusInWindow();
            throw new ParseException("Fim do contrato: informe uma data no formato dia/mês/ano.", 0);
        }
        var zone = ZoneId.systemDefault();
        return date.toInstant().atZone(zone).toLocalDate().atStartOfDay(zone).toInstant().toEpochMilli();
    }
}
