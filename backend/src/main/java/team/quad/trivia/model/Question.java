package team.quad.trivia.model;

import java.util.List;

public record Question(
        String id,
        Type type,
        Difficulty difficulty,
        String categoryName,
        String question,
        String correctAnswer,
        List<String> incorrectAnswers
) {
    public Question(Type type, Difficulty difficulty, String categoryName, String question, String correctAnswer, List<String> incorrectAnswers) {
        this(null, type, difficulty, categoryName, question, correctAnswer, incorrectAnswers);
    }

    public Question withId(String id) {
        return new Question(
                id,
                type,
                difficulty,
                categoryName,
                question,
                correctAnswer,
                incorrectAnswers
        );
    }

    public record Category(
            int id,
            String name
    ) {
    }

    public enum Type {
        MULTIPLE,
        BOOLEAN
    }

    public enum Difficulty {
        EASY,
        MEDIUM,
        HARD
    }

    public record Filter(
            Integer category,
            Difficulty difficulty,
            Type type) {
    }

}
