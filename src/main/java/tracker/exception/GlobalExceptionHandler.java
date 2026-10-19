package tracker.exception;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.Map;
import java.util.stream.Collectors;

@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(HttpClientErrorException.NotFound.class)
    public ResponseEntity<Map<String, String>> handleRebrickableNotFound(HttpClientErrorException.NotFound ex) {
        return error(HttpStatus.NOT_FOUND, "Zestaw nie istnieje",
                "Zestaw LEGO o podanym numerze nie zostal znaleziony");
    }

    @ExceptionHandler(SetNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleSetNotFound(SetNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "Zestaw nie istnieje", ex.getMessage());
    }

    @ExceptionHandler(DuplicateSetException.class)
    public ResponseEntity<Map<String, String>> handleDuplicate(DuplicateSetException ex) {
        return error(HttpStatus.CONFLICT, "Zestaw juz istnieje", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return error(HttpStatus.BAD_REQUEST, "Niepoprawne dane", ex.getMessage());
    }

    // bledy walidacji body (@Valid @RequestBody)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return error(HttpStatus.BAD_REQUEST, "Niepoprawne dane", details);
    }

    // bledy walidacji parametrow (@RequestParam, @PathVariable)
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, String>> handleParamValidation(HandlerMethodValidationException ex) {
        String details = ex.getAllErrors().stream()
                .map(e -> e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return error(HttpStatus.BAD_REQUEST, "Niepoprawne parametry", details);
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String error, String message) {
        Map<String, String> body = Map.of(
                "error", error,
                "message", message,
                "status", String.valueOf(status.value())
        );
        return new ResponseEntity<>(body, status);
    }
}
