package br.com.saveeditor.brasfoot.adapters.in.web;

import br.com.saveeditor.brasfoot.adapters.in.web.dto.ManagerDto;
import br.com.saveeditor.brasfoot.adapters.in.web.dto.ManagerUpdateRequest;
import br.com.saveeditor.brasfoot.application.ports.in.GetManagerUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.UpdateManagerUseCase;
import br.com.saveeditor.brasfoot.domain.Manager;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/managers")
public class ManagerController {

    private final GetManagerUseCase getManagerUseCase;
    private final UpdateManagerUseCase updateManagerUseCase;

    public ManagerController(GetManagerUseCase getManagerUseCase, UpdateManagerUseCase updateManagerUseCase) {
        this.getManagerUseCase = getManagerUseCase;
        this.updateManagerUseCase = updateManagerUseCase;
    }

    @GetMapping
    public ResponseEntity<List<ManagerDto>> getAllManagers(@PathVariable String sessionId) {
        List<Manager> managers = getManagerUseCase.getManagers(sessionId);
        List<ManagerDto> dtos = managers.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{managerId}")
    public ResponseEntity<ManagerDto> getManager(
            @PathVariable String sessionId,
            @PathVariable int managerId) {
        return getManagerUseCase.getManager(sessionId, managerId)
                .map(manager -> ResponseEntity.ok(toDto(manager)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{managerId}")
    public ResponseEntity<ManagerDto> updateManager(
            @PathVariable String sessionId,
            @PathVariable int managerId,
            @Valid @RequestBody ManagerUpdateRequest request) {
        Manager updateData = toDomain(request);
        Manager updated = updateManagerUseCase.updateManager(sessionId, managerId, updateData);
        return ResponseEntity.ok(toDto(updated));
    }

    private ManagerDto toDto(Manager manager) {
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

    private Manager toDomain(ManagerUpdateRequest request) {
        Manager manager = new Manager();
        manager.setName(request.getName());
        manager.setAge(request.getAge());
        manager.setNationality(request.getNationality());
        manager.setReputation(request.getReputation());
        manager.setTrophies(request.getTrophies());
        return manager;
    }
}
