package br.com.saveeditor.brasfoot.application.shared;

import lombok.Getter;

@Getter
public class BatchResult<T> {
    private final int index;
    private final boolean success;
    private final T data;
    private final String error;

    public BatchResult(int index, boolean success, T data, String error) {
        this.index = index;
        this.success = success;
        this.data = data;
        this.error = error;
    }

    public static <T> BatchResult<T> success(int index, T data) {
        return new BatchResult<>(index, true, data, null);
    }

    public static <T> BatchResult<T> failure(int index, String error) {
        return new BatchResult<>(index, false, null, error);
    }
}
