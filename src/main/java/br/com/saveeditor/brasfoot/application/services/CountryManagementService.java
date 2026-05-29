package br.com.saveeditor.brasfoot.application.services;

import br.com.saveeditor.brasfoot.application.ports.out.SessionStatePort;
import br.com.saveeditor.brasfoot.domain.CountryState;
import br.com.saveeditor.brasfoot.domain.Session;
import br.com.saveeditor.brasfoot.util.BrasfootConstants;
import br.com.saveeditor.brasfoot.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CountryManagementService {

    private static final Logger log = LoggerFactory.getLogger(CountryManagementService.class);

    private final SessionStatePort sessionStatePort;
    private final SessionResolver sessionResolver;

    public CountryManagementService(SessionStatePort sessionStatePort, SessionResolver sessionResolver) {
        this.sessionStatePort = sessionStatePort;
        this.sessionResolver = sessionResolver;
    }

    public List<CountryState> getCountries(UUID sessionId) {
        Object root = sessionResolver.loadRequired(sessionId).getContext().getState().getObjetoRaiz();
        List<CountryState> countries = new ArrayList<>();

        collectCountries(root, countries, BrasfootConstants.ROOT_PRIMARY_COUNTRIES, "Principal");
        collectCountries(root, countries, BrasfootConstants.ROOT_SECONDARY_COUNTRIES, "Secundario");

        return countries;
    }

    public CountryState updateCountryLevel(UUID sessionId, String countryId, int level) {
        Session session = sessionResolver.loadRequired(sessionId);
        Object country = resolveCountry(session.getContext().getState().getObjetoRaiz(), countryId);
        try {
            ReflectionUtils.setFieldValue(country, BrasfootConstants.COUNTRY_LEVEL, level);
            sessionStatePort.save(session);
            return toCountryState(country, countryId, groupLabel(countryId));
        } catch (ReflectiveOperationException e) {
            log.warn("Failed to update country level", e);
            throw new IllegalStateException("Nao foi possivel atualizar o nivel do pais.", e);
        }
    }

    private void collectCountries(Object root, List<CountryState> countries, String countryListField, String group) {
        Object countriesValue;
        try {
            countriesValue = ReflectionUtils.getFieldValue(root, countryListField);
        } catch (ReflectiveOperationException e) {
            return;
        }

        if (!(countriesValue instanceof List<?> countryObjects)) {
            return;
        }

        for (int i = 0; i < countryObjects.size(); i++) {
            countries.add(toCountryState(countryObjects.get(i), countryListField + "[" + i + "]", group));
        }
    }

    private Object resolveCountry(Object root, String countryId) {
        int bracket = countryId.indexOf('[');
        int closeBracket = countryId.indexOf(']');
        if (bracket <= 0 || closeBracket <= bracket) {
            throw new IllegalArgumentException("Pais invalido: " + countryId);
        }

        String listField = countryId.substring(0, bracket);
        int index = Integer.parseInt(countryId.substring(bracket + 1, closeBracket));
        try {
            Object countriesValue = ReflectionUtils.getFieldValue(root, listField);
            if (countriesValue instanceof List<?> countries && index >= 0 && index < countries.size()) {
                return countries.get(index);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Pais invalido: " + countryId, e);
        }
        throw new IllegalArgumentException("Pais invalido: " + countryId);
    }

    private CountryState toCountryState(Object country, String id, String group) {
        return new CountryState(id, stringField(country, BrasfootConstants.COUNTRY_NAME), group,
                intField(country, BrasfootConstants.COUNTRY_LEVEL), divisionCount(country));
    }

    private int divisionCount(Object country) {
        try {
            Object divisionsValue = ReflectionUtils.getFieldValue(country, BrasfootConstants.COUNTRY_DIVISIONS);
            if (divisionsValue instanceof List<?> divisions) {
                return divisions.size();
            }
        } catch (ReflectiveOperationException ignored) {
            // Country has no readable divisions list.
        }
        return 0;
    }

    private String groupLabel(String countryId) {
        if (countryId.startsWith(BrasfootConstants.ROOT_PRIMARY_COUNTRIES + "[")) {
            return "Principal";
        }
        return "Secundario";
    }

    private String stringField(Object obj, String fieldName) {
        try {
            Object value = ReflectionUtils.getFieldValue(obj, fieldName);
            return value == null ? "" : String.valueOf(value);
        } catch (ReflectiveOperationException e) {
            return "";
        }
    }

    private int intField(Object obj, String fieldName) {
        try {
            return ((Number) ReflectionUtils.getFieldValue(obj, fieldName)).intValue();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Nao foi possivel ler o campo: " + fieldName, e);
        }
    }
}
