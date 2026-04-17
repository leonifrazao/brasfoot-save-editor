package br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.record.in;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ManagerBatchOperationRequest.class, name = "manager.update"),
        @JsonSubTypes.Type(value = TeamBatchOperationRequest.class, name = "team.update"),
        @JsonSubTypes.Type(value = PlayerBatchOperationRequest.class, name = "player.update")
})
public sealed interface SessionBatchOperationRequest
        permits ManagerBatchOperationRequest, TeamBatchOperationRequest, PlayerBatchOperationRequest {

    String type();
}
