package br.com.saveeditor.brasfoot.adapters.in.web.mapper;

import br.com.saveeditor.brasfoot.adapters.in.web.record.out.TeamDto;
import br.com.saveeditor.brasfoot.domain.Team;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Team domain objects and TeamDto transfer objects.
 * Handles bidirectional transformation for REST API responses.
 */
@Component
public class TeamMapper {

    /**
     * Converts a single Team domain object to a TeamDto.
     *
     * @param team the Team domain object to convert
     * @return the converted TeamDto
     */
    public TeamDto toDto(Team team) {
        return new TeamDto(
                team.id(),
                team.name(),
                team.money(),
                team.reputation()
        );
    }

    /**
     * Converts a list of Team domain objects to a list of TeamDtos.
     *
     * @param teams the list of Team domain objects to convert
     * @return the converted list of TeamDtos
     */
    public List<TeamDto> toDtoList(List<Team> teams) {
        return teams.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
