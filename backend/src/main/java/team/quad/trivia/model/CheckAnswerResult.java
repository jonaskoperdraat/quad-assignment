package team.quad.trivia.model;

public record CheckAnswerResult(
        boolean isCorrect,
        String correctAnswer
) {
}
