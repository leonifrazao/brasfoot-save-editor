package br.com.saveeditor.brasfoot.application.services;

import br.com.saveeditor.brasfoot.application.ports.in.GetManagerUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.UpdateManagerUseCase;
import br.com.saveeditor.brasfoot.domain.Manager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
public class ManagerManagementService implements GetManagerUseCase, UpdateManagerUseCase {
    
    @Override
    public List<Manager> getManagers(String sessionId) {
        return new ArrayList<>(); // Dummy implementation for speed
    }

    @Override
    public Optional<Manager> getManager(String sessionId, int managerId) {
        return Optional.empty();
    }

    @Override
    public Manager updateManager(String sessionId, int managerId, Manager updateData) {
        return new Manager();
    }
}
