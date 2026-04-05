package br.com.saveeditor.brasfoot.domain.exceptions;

public class SessionDeletedException extends RuntimeException {
    public SessionDeletedException(String message) {
        super(message);
    }
}
