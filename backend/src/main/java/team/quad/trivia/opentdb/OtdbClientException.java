package team.quad.trivia.opentdb;

public class OtdbClientException extends RuntimeException {
    public OtdbClientException() {
        super();
    }
    public OtdbClientException(String msg) {
        super(msg);
    }

}
