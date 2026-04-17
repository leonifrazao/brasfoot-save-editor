package br.com.saveeditor.brasfoot.application.shared;

import lombok.Getter;

import java.util.List;

@Getter
public class BatchResponse<T> {
    private final List<BatchResult<T>> results;

    public BatchResponse(List<BatchResult<T>> results) {
        this.results = results;
    }

}
