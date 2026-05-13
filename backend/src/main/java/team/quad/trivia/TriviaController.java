package team.quad.trivia;

import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.hc.core5.http.HttpHeaders;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.context.request.NativeWebRequest;
import team.quad.trivia.model.Question;
import team.quad.trivia.rest.CategoriesApi;
import team.quad.trivia.rest.CheckAnswerApi;
import team.quad.trivia.rest.QuestionApi;
import team.quad.trivia.rest.SessionResetApi;
import team.quad.trivia.rest.model.ApiCategory;
import team.quad.trivia.rest.model.ApiCheckAnswer200Response;
import team.quad.trivia.rest.model.ApiCheckAnswerRequest;
import team.quad.trivia.rest.model.ApiDifficulty;
import team.quad.trivia.rest.model.ApiGetQuestion200Response;
import team.quad.trivia.rest.model.ApiType;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@RequiredArgsConstructor
@Controller
public class TriviaController implements QuestionApi, CheckAnswerApi, CategoriesApi, SessionResetApi {

    private final TriviaService triviaService;

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return QuestionApi.super.getRequest();
    }

    @Override
    public ResponseEntity<ApiGetQuestion200Response> getQuestion(@Nullable ApiType type, @Nullable ApiDifficulty difficulty, @Nullable Integer category, @Nullable String sessionCookie) {
        // Check session, create new one if none exists
        var sessionToken = sessionCookie;
        if (sessionToken == null) {
            sessionToken = triviaService.getSessionToken();
        }

        // Retrieve a question
        var question = triviaService.getQuestion(createFilter(type, difficulty, category), sessionToken);

        // Randomize answers
        var answers = new ArrayList<String>(question.incorrectAnswers().size() + 1);
        answers.addAll(question.incorrectAnswers());
        answers.add(question.correctAnswer());
        Collections.shuffle(answers);

        // Create response body
        var resBody = ApiGetQuestion200Response.builder()
                .id(question.id())
                .type(ApiType.valueOf(question.type().name()))
                .difficulty(ApiDifficulty.valueOf(question.difficulty().name()))
                .category(question.categoryName())
                .question(question.question())
                .answers(answers)
                .build();

        // Create session cookie containing session token
        ResponseCookie cookie = ResponseCookie.from("session_cookie", sessionToken)
                .httpOnly(true)
                .secure(false) // local development
                .path("/") // We want it at the /question path, but also at the /session-reset path
                .maxAge(Duration.ofHours(8))
                .build();

        // Return
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(resBody);
    }

    @Override
    public ResponseEntity<ApiCheckAnswer200Response> checkAnswer(@Nullable ApiCheckAnswerRequest request) {
        // Check the answer
        var result = triviaService.checkAnswer(request.getQuestionId(), request.getAnswer());

        // Formulate response
        var responseBuilder = ApiCheckAnswer200Response.builder()
                .isCorrect(result.isCorrect());
        if (!result.isCorrect()) {
            responseBuilder.correctAnswer(result.correctAnswer());
        }

        // Return
        return ResponseEntity.ok(responseBuilder.build());
    }

    @Override
    public ResponseEntity<List<ApiCategory>> getCategories() {
        return ResponseEntity.ok(triviaService.getCategories()
                .stream()
                .map(cat -> ApiCategory.builder()
                        .id(cat.id())
                        .name(cat.name())
                        .build())
                .toList());
    }

    @Override
    public ResponseEntity<Void> sessionReset(@Nullable String sessionCookie) {
        if (sessionCookie != null) {
            triviaService.resetSession(sessionCookie);
        }

        return ResponseEntity.noContent().build();
    }

    private static @Nonnull Question.Filter createFilter(@Nullable ApiType type, @Nullable ApiDifficulty difficulty, @Nullable Integer category) {
        return new Question.Filter(
                category,
                ofNullable(difficulty).map(ApiDifficulty::name)
                        .map(Question.Difficulty::valueOf).orElse(null),
                ofNullable(type).map(ApiType::name)
                        .map(Question.Type::valueOf).orElse(null)
        );
    }
}