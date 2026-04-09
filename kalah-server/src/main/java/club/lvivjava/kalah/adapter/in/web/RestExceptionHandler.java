package club.lvivjava.kalah.adapter.in.web;

import club.lvivjava.kalah.adapter.in.web.dto.ApiErrorBody;
import club.lvivjava.kalah.domain.DomainException;
import club.lvivjava.kalah.domain.GameNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(GameNotFoundException.class)
    public ResponseEntity<ApiErrorBody> notFound(GameNotFoundException e) {
        return err(HttpStatus.NOT_FOUND, e.getCode(), e.getMessage());
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiErrorBody> domain(DomainException e) {
        HttpStatus st = switch (e.getCode()) {
            case "forbidden" -> HttpStatus.FORBIDDEN;
            case "conflict" -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };
        return err(st, e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorBody> validation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .orElse("Validation failed");
        return err(HttpStatus.UNPROCESSABLE_ENTITY, "validation_error", msg);
    }

    private static ResponseEntity<ApiErrorBody> err(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status)
                .body(new ApiErrorBody(new ApiErrorBody.ErrorEnvelope(code, message)));
    }
}
