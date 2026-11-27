package br.com.saveeditor.brasfoot.presentation.presenter;

import br.com.saveeditor.brasfoot.application.shared.SaveEdit;
import br.com.saveeditor.brasfoot.domain.enums.Country;
import br.com.saveeditor.brasfoot.domain.enums.PlayerCharacteristic;
import br.com.saveeditor.brasfoot.domain.enums.PlayerPosition;
import br.com.saveeditor.brasfoot.domain.enums.PlayerSide;
import br.com.saveeditor.brasfoot.domain.enums.TeamAttackFocus;
import br.com.saveeditor.brasfoot.domain.enums.TeamMarking;
import br.com.saveeditor.brasfoot.domain.enums.TeamPlayStyle;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class EditDescriptions {
    private static final Map<String, String> LABELS = Map.ofEntries(
            Map.entry("name", "Nome"), Map.entry("alias", "Apelido"), Map.entry("money", "Dinheiro"),
            Map.entry("reputation", "Reputação"), Map.entry("country", "País"), Map.entry("division", "Divisão"),
            Map.entry("level", "Nível"), Map.entry("stadiumName", "Estádio"), Map.entry("stadiumCapacity", "Capacidade"),
            Map.entry("stadiumSectors", "Setores"), Map.entry("tacticStyle", "Estilo"), Map.entry("tacticMarking", "Marcação"),
            Map.entry("tacticFocus", "Foco"), Map.entry("age", "Idade"), Map.entry("overall", "Força"),
            Map.entry("position", "Posição"), Map.entry("energy", "Energia"), Map.entry("salary", "Salário"),
            Map.entry("side", "Lado"), Map.entry("contractEnd", "Fim do contrato"), Map.entry("characteristic1", "Característica 1"),
            Map.entry("characteristic2", "Característica 2"), Map.entry("skillGoalkeeping", "Goleiro"),
            Map.entry("skillSpeed", "Velocidade"), Map.entry("skillTechnique", "Técnica"), Map.entry("skillPassing", "Passe"),
            Map.entry("skillTackling", "Desarme"), Map.entry("skillPlaymaking", "Armação"), Map.entry("skillFinishing", "Finalização"),
            Map.entry("starLocal", "Estrela local"), Map.entry("starGlobal", "Estrela mundial"), Map.entry("human", "Humano"),
            Map.entry("teamId", "ID do time"), Map.entry("confidenceBoard", "Confiança da diretoria"),
            Map.entry("confidenceFans", "Confiança da torcida"), Map.entry("trophies", "Títulos"),
            Map.entry("points", "Pontos"), Map.entry("played", "Jogos"), Map.entry("wins", "Vitórias"),
            Map.entry("draws", "Empates"), Map.entry("losses", "Derrotas"), Map.entry("goalsFor", "Gols pró"),
            Map.entry("goalsAgainst", "Gols contra"), Map.entry("goalDifference", "Saldo de gols"),
            Map.entry("divisionCount", "Divisões"));

    private EditDescriptions() {}

    static SaveEdit compare(String title, Record before, Record after) {
        List<SaveEdit.Detail> details = new ArrayList<>();
        for (var component : after.getClass().getRecordComponents()) {
            String label = LABELS.get(component.getName());
            if (label == null) continue;
            try {
                Object oldValue = before == null ? null : component.getAccessor().invoke(before);
                Object newValue = component.getAccessor().invoke(after);
                if (!Objects.equals(oldValue, newValue)) {
                    details.add(new SaveEdit.Detail(label, format(component.getName(), oldValue),
                            format(component.getName(), newValue)));
                }
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Não foi possível preparar a revisão da edição.", e);
            }
        }
        return new SaveEdit(title, details);
    }

    static SaveEdit describe(String title, Object... fields) {
        List<SaveEdit.Detail> details = new ArrayList<>();
        for (int i = 0; i < fields.length; i += 2) {
            if (fields[i + 1] != null) details.add(new SaveEdit.Detail((String) fields[i], null, display(fields[i + 1])));
        }
        return new SaveEdit(title, details);
    }

    private static String format(String field, Object value) {
        if (value == null) return "—";
        if (value instanceof Integer number) {
            return switch (field) {
                case "country" -> Country.fromId(number) == null ? number.toString() : Country.fromId(number).getName();
                case "position" -> PlayerPosition.fromCode(number).getLabel();
                case "side" -> PlayerSide.fromCode(number).getLabel();
                case "characteristic1", "characteristic2" -> PlayerCharacteristic.fromCode(number).getDisplayName();
                case "tacticStyle" -> TeamPlayStyle.fromCode(number).getDisplayName();
                case "tacticMarking" -> TeamMarking.fromCode(number).getDisplayName();
                case "tacticFocus" -> TeamAttackFocus.fromCode(number).getDisplayName();
                default -> number.toString();
            };
        }
        if ("contractEnd".equals(field) && value instanceof Long time && time > 0) {
            return Instant.ofEpochMilli(time < 100_000_000_000L ? time * 1000 : time)
                    .atZone(ZoneId.systemDefault()).toLocalDate().toString();
        }
        return display(value);
    }

    private static String display(Object value) {
        if (value instanceof Boolean selected) return selected ? "Sim" : "Não";
        return String.valueOf(value);
    }
}
