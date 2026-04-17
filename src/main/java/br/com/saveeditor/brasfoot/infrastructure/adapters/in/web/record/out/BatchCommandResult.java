package br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.record.out;

public record BatchCommandResult(
        int index,
        String type,
        boolean success,
        Object data,
        String error
) {
    public static BatchCommandResult success(int index, String type, Object data) {
        return new BatchCommandResult(index, type, true, data, null);
    }

    public static BatchCommandResult failure(int index, String type, String error) {
        return new BatchCommandResult(index, type, false, null, error);
    }
}
