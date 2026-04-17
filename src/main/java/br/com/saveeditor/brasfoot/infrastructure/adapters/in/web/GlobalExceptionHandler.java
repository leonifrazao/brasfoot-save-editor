package br.com.saveeditor.brasfoot.adapters.in.web;

import br.com.saveeditor.brasfoot.domain.exceptions.SessionDeletedException;
import br.com.saveeditor.brasfoot.domain.exceptions.SessionExpiredException;
import br.com.saveeditor.brasfoot.domain.exceptions.SessionNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        log.warn("MaxUploadSizeExceededException: {}", ex.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.PAYLOAD_TOO_LARGE, "File size exceeds the 500MB limit.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("IllegalArgumentException: {}", ex.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler({SessionNotFoundException.class, SessionDeletedException.class})
    public ProblemDetail handleSessionNotFound(RuntimeException ex) {
        log.warn("Session exception (NotFound/Deleted): {}", ex.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(SessionExpiredException.class)
    public ProblemDetail handleSessionExpired(SessionExpiredException ex) {
        log.warn("SessionExpiredException: {}", ex.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.GONE, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Unhandled Exception: ", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred: " + ex.getMessage());
    }
}