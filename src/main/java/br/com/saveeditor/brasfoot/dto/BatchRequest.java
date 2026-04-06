package br.com.saveeditor.brasfoot.dto;

import br.com.saveeditor.brasfoot.adapters.in.web.dto.PlayerUpdateRequest;
import br.com.saveeditor.brasfoot.adapters.in.web.dto.TeamUpdateRequest;

import java.util.List;

public class BatchRequest {

    public static class BatchPlayerRequest {
        private String id;
        private PlayerUpdateRequest data;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public PlayerUpdateRequest getData() { return data; }
        public void setData(PlayerUpdateRequest data) { this.data = data; }
    }

    public static class BatchTeamRequest {
        private String id;
        private TeamUpdateRequest data;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public TeamUpdateRequest getData() { return data; }
        public void setData(TeamUpdateRequest data) { this.data = data; }
    }

    private List<BatchPlayerRequest> players;
    private List<BatchTeamRequest> teams;

    public List<BatchPlayerRequest> getPlayers() {
        return players;
    }

    public void setPlayers(List<BatchPlayerRequest> players) {
        this.players = players;
    }

    public List<BatchTeamRequest> getTeams() {
        return teams;
    }

    public void setTeams(List<BatchTeamRequest> teams) {
        this.teams = teams;
    }
}
