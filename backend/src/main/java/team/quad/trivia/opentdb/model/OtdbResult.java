package team.quad.trivia.opentdb.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.util.UriUtils;
import team.quad.trivia.model.Question;

import java.nio.charset.StandardCharsets;
import java.util.List;

public record OtdbResult(
        Type type,
        Difficulty difficulty,
        String category,
        String question,
        @JsonProperty("correct_answer") String correctAnswer,
        @JsonProperty("incorrect_answers") List<String> incorrectAnswers
) {

    public enum Type {
        @JsonProperty("boolean") BOOLEAN,
        @JsonProperty("multiple") MULTIPLE;

        public Question.Type toDomain() {
            return Question.Type.valueOf(this.name());
        }
    }

    public enum Difficulty {
        @JsonProperty("easy") EASY,
        @JsonProperty("medium") MEDIUM,
        @JsonProperty("hard") HARD;

        public Question.Difficulty toDomain() {
            return Question.Difficulty.valueOf(this.name());
        }
    }

    public Question toDomain() {
        return new Question(
                this.type.toDomain(),
                this.difficulty.toDomain(),
                UriUtils.decode(this.category, StandardCharsets.UTF_8),
                UriUtils.decode(this.question, StandardCharsets.UTF_8),
                UriUtils.decode(this.correctAnswer, StandardCharsets.UTF_8),
                this.incorrectAnswers.stream().map(this::decode).toList()
        );
    }

    private String decode(String encoded) {
        return UriUtils.decode(encoded, StandardCharsets.UTF_8);
    }
}
