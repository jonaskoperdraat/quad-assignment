package team.quad.trivia;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import team.quad.trivia.exception.QuestionNotFoundException;
import team.quad.trivia.opentdb.exception.TokenEmptyException;

@RestControllerAdvice
@Slf4j
public class TriviaControllerErrorHandler {

    public record TriviaApiError(String code, String message, Object detail) {
        public TriviaApiError(String code, String message) {
            this(code, message, null);
        }
    }

    @ExceptionHandler(QuestionNotFoundException.class)
    public ResponseEntity<TriviaApiError> handleQuestionNotFoundException(QuestionNotFoundException ex) {
        log.debug("Handling QuestionNotFoundException", ex);
        return ResponseEntity
                .badRequest()
                .body(new TriviaApiError("QUESTION_NOT_FOUND", ex.getMessage(), null));
    }

    @ExceptionHandler(HttpClientErrorException.TooManyRequests.class)
    public ResponseEntity<TriviaApiError> handleTooManyRequests(HttpClientErrorException.TooManyRequests ex) {
        log.debug("Handling HttpClientErrorException.TooManyRequests", ex);
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new TriviaApiError("TOO_MANY_REQUESTS", "Exceeding upstream service rate limit."));

    }

    @ExceptionHandler(TokenEmptyException.class)
    public ResponseEntity<TriviaApiError> handleTokenEmptyException(TokenEmptyException ex) {
        log.debug("Handling TokenEmptyException", ex);
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new TriviaApiError("SESSION_TOKEN_EMPTY",
                        "For the current session, there are no questions left matching the requested filter.",
                        "Either change the filter or reset the session token through the /reset-session endpoint."));
    }

}
