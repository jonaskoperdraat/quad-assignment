package team.quad.trivia;

import jakarta.annotation.Nullable;
import team.quad.trivia.model.Question;

import java.util.List;

public interface TriviaRepo {

    List<Question.Category> getCategories();

    Question getQuestion(@Nullable Question.Filter questionFilter, String sessionToken);

    String getSessionToken();

    void resetSession(String sessionToken);
}
