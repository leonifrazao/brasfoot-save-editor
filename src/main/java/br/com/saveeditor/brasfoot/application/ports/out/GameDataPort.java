package br.com.saveeditor.brasfoot.application.ports.out;

import java.util.List;

public interface GameDataPort {
    List<Object> getTeams(Object root);

    Object getTeamById(Object root, int id);

    List<Object> getPlayers(Object team);

    List<Object> getManagers(Object root);
}
