package br.com.saveeditor.brasfoot.dto;

public class BatchOperationResult {
    private String id;
    private int status;
    private String message;

    public BatchOperationResult() {}

    public BatchOperationResult(String id, int status, String message) {
        this.id = id;
        this.status = status;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
