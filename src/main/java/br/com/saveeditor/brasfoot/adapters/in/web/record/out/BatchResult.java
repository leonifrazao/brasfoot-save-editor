package br.com.saveeditor.brasfoot.adapters.in.web.record.out;

public class BatchResult<T> {
    private int index;
    private boolean success;
    private T data;
    private String error;

    public BatchResult(int index, boolean success, T data, String error) {
        this.index = index;
        this.success = success;
        this.data = data;
        this.error = error;
    }

    public BatchResult() {}

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    // Convenience factory methods
    public static <T> BatchResult<T> success(int index, T data) {
        return new BatchResult<>(index, true, data, null);
    }

    public static <T> BatchResult<T> failure(int index, String error) {
        return new BatchResult<>(index, false, null, error);
    }
}
