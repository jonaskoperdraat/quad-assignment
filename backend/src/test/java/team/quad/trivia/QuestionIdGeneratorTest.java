package team.quad.trivia;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import team.quad.trivia.model.Question;

import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;

class QuestionIdGeneratorTest {

    private final QuestionIdGenerator generator = new QuestionIdGenerator();

    QuestionIdGeneratorTest() throws NoSuchAlgorithmException {
    }

    @ParameterizedTest
    @CsvSource({"Foo,201a6b3053cc1422d2c3670b62616221d2290929", "Bar,e496fd20136d4bb7828ebb0ab925b1bd977208e4"})
    void testGenerate(String question, String expectedHash) {
        // Given
        var q = new Question(null, null, null, null, question, null, null);

        // When
        var hash = generator.generateId(q);

        // Then
        assertThat(hash).isEqualTo(expectedHash);
    }
}