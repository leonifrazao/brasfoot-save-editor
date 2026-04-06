package br.com.saveeditor.brasfoot.dto;

import java.util.List;

public class BatchResponse {
    private List<BatchOperationResult> playerResults;
    private List<BatchOperationResult> teamResults;

    public List<BatchOperationResult> getPlayerResults() {
        return playerResults;
    }

    public void setPlayerResults(List<BatchOperationResult> playerResults) {
        this.playerResults = playerResults;
    }

    public List<BatchOperationResult> getTeamResults() {
        return teamResults;
    }

    public void setTeamResults(List<BatchOperationResult> teamResults) {
        this.teamResults = teamResults;
    }
}
