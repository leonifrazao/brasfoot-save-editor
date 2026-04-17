package br.com.saveeditor.brasfoot.adapters.in.web.mapper;

import br.com.saveeditor.brasfoot.adapters.in.web.record.out.PlayerDto;
import br.com.saveeditor.brasfoot.domain.Player;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Player domain objects and PlayerDto transfer objects.
 * Handles bidirectional transformation for REST API responses.
 */
@Component
public class PlayerMapper {

    /**
     * Converts a single Player domain object to a PlayerDto.
     *
     * @param player the Player domain object to convert
     * @return the converted PlayerDto
     */
    public PlayerDto toDto(Player player) {
        return new PlayerDto(
                player.getId(),
                player.getName(),
                player.getAge(),
                player.getOverall(),
                player.getPosition(),
                player.getEnergy(),
                player.getMorale()
        );
    }

    /**
     * Converts a list of Player domain objects to a list of PlayerDtos.
     *
     * @param players the list of Player domain objects to convert
     * @return the converted list of PlayerDtos
     */
    public List<PlayerDto> toDtoList(List<Player> players) {
        return players.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
