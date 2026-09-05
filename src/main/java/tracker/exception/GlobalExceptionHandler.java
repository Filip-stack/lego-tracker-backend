package tracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpClientErrorException.NotFound.class)
    public ResponseEntity<Map<String, String>> handleRebrickableNotFound(HttpClientErrorException.NotFound ex) {

        // budowa wlasnego komunikatu bledu w formacie json
        Map<String, String> errorResponse = Map.of(
                "error", "Zestaw nie istnieje",
                "message", "Zestaw LEGO o podanym numerze nie zostal znaleziony",
                "status", "404"
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
}
