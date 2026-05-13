package team.quad.trivia;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import team.quad.trivia.model.Question;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
@Slf4j
public class QuestionIdGenerator {

    private final MessageDigest digest;

    public QuestionIdGenerator() throws NoSuchAlgorithmException {
        log.debug("Initializing QuestionIdGenerator");
        // Using SHA-1 as a trade-off between collision resistance and hash length
        this.digest = MessageDigest.getInstance("SHA-1");
        log.info("Initialized QuestionIdGenator with SHA-1 Message Digest.");
    }

    public String generateId(Question question) {
        digest.update(question.question().getBytes(StandardCharsets.UTF_8));
        var bytes = digest.digest();

        StringBuilder hexString = new StringBuilder();

        for (byte b : bytes) {
            hexString.append(String.format("%02x", b));
        }

        return hexString.toString();
    }
}
