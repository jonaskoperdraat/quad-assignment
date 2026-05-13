package team.quad.trivia;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import team.quad.trivia.exception.QuestionNotFoundException;
import team.quad.trivia.model.CheckAnswerResult;
import team.quad.trivia.model.Question;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TriviaService {

    private final TriviaRepo triviaRepo;
    private final QuestionIdGenerator questionIdGenerator;

    /**
     * This is where we store questions so we can later check answers.
     * In a production application this would probably be replaced by some
     * centralized database.
     */
    private final Map<String, Question> questionStore =  new HashMap<>();

    @Getter
    private List<Question.Category> categories = new ArrayList<>();

    /**
     * Listening to ContextRefreshedEvent in order to pre-load the list of categories
     * from the upstream service.
     * @param ctxRefreshEvt
     */
    @EventListener
    public void handleContextRefreshEvent(ContextRefreshedEvent ctxRefreshEvt) {
        log.info("Context Refresh Event received. Retrieving categories");
        this.categories = triviaRepo.getCategories();
    }

    /**
     * Retrieve a question matching the given filter
     * @param filter the criteria for the question
     * @return a question matching the given filter
     */
    public Question getQuestion(@Nullable Question.Filter filter, String sessionToken) {
        var question = triviaRepo.getQuestion(filter, sessionToken);
        // Questions returned by triviaRepo do not have an Id. In order to store
        // them and verify answers, they need a stable Id. Therefore, we generate
        // an Id, using the questionIdGenerator.
        question = question.withId(questionIdGenerator.generateId(question));
        questionStore.put(question.id(), question);
        return question;
    }

    public String getSessionToken() {
        return triviaRepo.getSessionToken();
    }

    public void resetSession(String sessionToken) {
        triviaRepo.resetSession(sessionToken);
    }

    /**
     * Checks a given answer is correct.
     * @param questionId id of the question to check
     * @param answer the answer to check
     * @return a {@link CheckAnswerResult} containing whether the answer is correct and the correct answer.
     */
    public CheckAnswerResult checkAnswer(String questionId,
                                         String answer) {
        var question = questionStore.get(questionId);
        if (question == null) {
            throw new QuestionNotFoundException(questionId);
        }
        return new CheckAnswerResult(
                question.correctAnswer().equals(answer),
                question.correctAnswer());
    }

}
