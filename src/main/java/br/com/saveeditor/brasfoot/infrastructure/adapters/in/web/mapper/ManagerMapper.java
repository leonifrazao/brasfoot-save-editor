package br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.mapper;

import br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.record.in.ManagerUpdateRequest;
import br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.record.out.ManagerResponse;
import br.com.saveeditor.brasfoot.domain.Manager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps the Manager domain object to explicit web contracts.
 */
@Component
public class ManagerMapper {

    /**
     * Converts a single Manager domain object to the HTTP response contract.
     *
     * @param manager the Manager domain object to convert
     * @return the converted ManagerResponse
     */
    public ManagerResponse toResponse(Manager manager) {
        return new ManagerResponse(
                manager.getId(),
                manager.getName(),
                manager.getIsHuman(),
                manager.getTeamId(),
                manager.getConfidenceBoard(),
                manager.getConfidenceFans()
        );
    }

    /**
     * Converts a ManagerUpdateRequest to a Manager domain object.
     * Only sets fields that are provided in the request (for PATCH operations).
     *
     * @param request the ManagerUpdateRequest to convert
     * @return the converted Manager domain object
     */
    public Manager toDomain(ManagerUpdateRequest request) {
        Manager manager = new Manager();
        manager.setName(request.getName());
        manager.setConfidenceBoard(request.getConfidenceBoard());
        manager.setConfidenceFans(request.getConfidenceFans());
        return manager;
    }

    /**
     * Converts a list of Manager domain objects to the HTTP response contract list.
     *
     * @param managers the list of Manager domain objects to convert
     * @return the converted list of ManagerResponse objects
     */
    public List<ManagerResponse> toResponseList(List<Manager> managers) {
        return managers.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
