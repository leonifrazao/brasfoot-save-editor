package br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.record.out;

import java.util.List;

public record BatchCommandResponse(
        List<BatchCommandResult> results
) {
}
