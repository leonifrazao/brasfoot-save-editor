package br.com.saveeditor.brasfoot.adapters.in.web.mapper;

import br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.mapper.ManagerMapper;
import br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.record.in.ManagerUpdateRequest;
import br.com.saveeditor.brasfoot.domain.Manager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ManagerMapperTest {

    private final ManagerMapper managerMapper = new ManagerMapper();

    @Test
    void toDomain_mapsConfidenceFieldsFromPatchRequest() {
        ManagerUpdateRequest request = new ManagerUpdateRequest();
        request.setConfidenceBoard(100);
        request.setConfidenceFans(100);

        Manager manager = managerMapper.toDomain(request);

        assertEquals(100, manager.getConfidenceBoard());
        assertEquals(100, manager.getConfidenceFans());
    }
}
