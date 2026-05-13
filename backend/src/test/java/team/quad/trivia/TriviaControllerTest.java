package team.quad.trivia;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.client.RestTestClient;
import team.quad.trivia.model.Question;
import team.quad.trivia.opentdb.exception.TokenEmptyException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@WebMvcTest(TriviaController.class)
@AutoConfigureRestTestClient
@ExtendWith(MockitoExtension.class)
class TriviaControllerTest {

    @Autowired
    RestTestClient restTestClient;

    @MockitoBean
    TriviaService mockTriviaService;

    @Captor
    ArgumentCaptor<Question.Filter> filterArgumentCaptor;

    @Captor
    ArgumentCaptor<String> stringArgumentCaptor;

    @Test
    void getCategories_shouldReturnCategories() {
        // Given
        when(mockTriviaService.getCategories())
                .thenReturn(List.of(new Question.Category(10, "Foo")));

        // When / Then
        restTestClient.get()
                .uri("/categories")
                .exchange()
                .expectBody()
                .json("[ { \"id\": 10, \"name\": \"Foo\" } ]", JsonCompareMode.LENIENT);
    }

    @Test
    void getQuestion_shouldInitiateSession_whenNoSessionCookieIsSet() {
        // Given
        when(mockTriviaService.getSessionToken()).thenReturn("new-token");
        when(mockTriviaService.getQuestion(any(), any()))
                .thenReturn(dummyQuestion());

        // When / Then
        restTestClient.get()
                .uri("/question")
                .exchange()
                .expectBody()
                .jsonPath("$.question").isEqualTo("dummy-question");

        // Then
        verify(mockTriviaService).getSessionToken();
        verify(mockTriviaService).getQuestion(filterArgumentCaptor.capture(), stringArgumentCaptor.capture());
        assertThat(stringArgumentCaptor.getValue()).isEqualTo("new-token");
    }

    @Test
    void getQuestion_shouldUseExistingSession_whenSessionCookieIsSet() {
        // Given
        when(mockTriviaService.getQuestion(any(), any()))
                .thenReturn(dummyQuestion());

        // When / Then
        restTestClient.get()
                .uri("/question")
                .header("Cookie", "session_cookie=existing-token")
                .exchange()
                .expectBody()
                .jsonPath("$.question").isEqualTo("dummy-question");

        // Then
        verify(mockTriviaService, never()).getSessionToken();
        verify(mockTriviaService).getQuestion(filterArgumentCaptor.capture(), stringArgumentCaptor.capture());
        assertThat(stringArgumentCaptor.getValue()).isEqualTo("existing-token");
    }

    @Test
    void sessionReset_shouldReturn204_whenSuccessful() {
        // Given
        // default mock behaviour of mockTriviaService.resetSession() (void)

        // When/Then
        restTestClient.post()
                .uri("/session-reset")
                .header("Cookie", "session_cookie=existing-token")
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.NO_CONTENT)
                .expectBody()
                .isEmpty();

        // Then
        verify(mockTriviaService).resetSession(stringArgumentCaptor.capture());
        assertThat(stringArgumentCaptor.getValue()).isEqualTo("existing-token");
    }

    @Test
    void sessionReset_shouldReturn204_whenSessionTokenNotProvided() {
        // When
        restTestClient.post()
                .uri("/session-reset")
                // No session_cookie header
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.NO_CONTENT);

        // Then
        verifyNoInteractions(mockTriviaService);
    }

    @Test
    void getQuestion_shouldReturn409_whenTokenIsEmpty() {
        // Given
        when(mockTriviaService.getQuestion(any(), any())).thenThrow(new TokenEmptyException());

        // When
        restTestClient.get()
                .uri("/question")
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CONFLICT)
                .expectBody()
                .json("""
                        {
                            "code": "SESSION_TOKEN_EMPTY",
                            "message": "For the current session, there are no questions left matching the requested filter.",
                            "detail": "Either change the filter or reset the session token through the /reset-session endpoint."
                        }
                        """, JsonCompareMode.LENIENT);
    }

    private Question dummyQuestion() {
        return new Question(
                Question.Type.BOOLEAN,
                Question.Difficulty.MEDIUM,
                "dummy-category",
                "dummy-question",
                "dummy-correct-answer",
                List.of("dummy-incorrect-answer"));
    }

}