package br.com.saveeditor.brasfoot.adapters.in.web.mapper;

import br.com.saveeditor.brasfoot.adapters.in.web.record.in.ManagerUpdateRequest;
import br.com.saveeditor.brasfoot.adapters.in.web.record.out.ManagerDto;
import br.com.saveeditor.brasfoot.domain.Manager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Manager domain objects and ManagerDto transfer objects.
 * Handles bidirectional transformation for REST API requests and responses.
 */
@Component
public class ManagerMapper {

    /**
     * Converts a single Manager domain object to a ManagerDto.
     *
     * @param manager the Manager domain object to convert
     * @return the converted ManagerDto
     */
    public ManagerDto toDto(Manager manager) {
        ManagerDto dto = new ManagerDto();
        dto.setId(manager.getId());
        dto.setName(manager.getName());
        dto.setIsHuman(manager.getIsHuman());
        dto.setTeamId(manager.getTeamId());
        dto.setConfidenceBoard(manager.getConfidenceBoard());
        dto.setConfidenceFans(manager.getConfidenceFans());
        dto.setAge(manager.getAge());
        dto.setNationality(manager.getNationality());
        dto.setReputation(manager.getReputation());
        dto.setTrophies(manager.getTrophies());
        return dto;
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
        manager.setAge(request.getAge());
        manager.setNationality(request.getNationality());
        manager.setReputation(request.getReputation());
        manager.setTrophies(request.getTrophies());
        return manager;
    }

    /**
     * Converts a list of Manager domain objects to a list of ManagerDtos.
     *
     * @param managers the list of Manager domain objects to convert
     * @return the converted list of ManagerDtos
     */
    public List<ManagerDto> toDtoList(List<Manager> managers) {
        return managers.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
