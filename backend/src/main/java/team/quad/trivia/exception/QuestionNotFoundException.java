package team.quad.trivia.exception;

public class QuestionNotFoundException extends RuntimeException {
    public QuestionNotFoundException(String questionHash) {
        super(("Question with id '%s' has not been found in the question store. " +
                "It has not been retrieved yet or the service has since restarted.")
                        .formatted(questionHash));
    }
}
