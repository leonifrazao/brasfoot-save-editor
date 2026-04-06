package br.com.saveeditor.brasfoot.adapters.in.web.record.out;

import java.util.List;

public class BatchResponse<T> {
    private List<BatchResult<T>> results;

    public BatchResponse(List<BatchResult<T>> results) {
        this.results = results;
    }

    public BatchResponse() {}

    public List<BatchResult<T>> getResults() {
        return results;
    }

    public void setResults(List<BatchResult<T>> results) {
        this.results = results;
    }
}
