package team.quad.trivia.opentdb;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.web.client.RestClient;
import team.quad.trivia.model.Question;
import team.quad.trivia.opentdb.exception.NoResultException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest({OtdbClient.class})
@Import(OtdbConfiguration.class)
public class OtdbClientTest {

    @Autowired
    private OtdbClient client;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private RestClient openTdbRestClient;

    @Test
    void getCategories() {
        // Given
        server.expect(requestTo("https://opentdb.com/api_category.php"))
                .andRespond(withSuccess("""
                        {
                            "trivia_categories": [
                                {
                                    "id": 1,
                                    "name": "foo"
                                },
                                {
                                    "id": 2,
                                    "name": "bar"
                                }
                            ]
                        }
                        """, MediaType.APPLICATION_JSON));

        // When
        var categories = client.getCategories();

        // Then
        assertThat(categories).hasSize(2);
        assertThat(categories).extracting(Question.Category::name).contains("foo", "bar");
        server.verify();
    }

    @Test
    void getQuestion() {
        // Given
        server.expect(requestTo("https://opentdb.com/api.php?amount=1&token=foo&encode=url3986"))
                .andRespond(withSuccess("""
                        {
                            "response_code": 0,
                            "results": [
                                {
                                    "type": "boolean",
                                    "difficulty": "hard",
                                    "category": "misc",
                                    "question": "To be or not to be",
                                    "correct_answer": "yes",
                                    "incorrect_answers": [
                                        "i don't know"
                                    ]
                                }
                            ]
                        }
                     """, MediaType.APPLICATION_JSON));

        // When
        var question = client.getQuestion(null, "foo");

        // Then
        assertThat(question.id()).isNull();
        assertThat(question.type()).isEqualTo(Question.Type.BOOLEAN);
        assertThat(question.difficulty()).isEqualTo(Question.Difficulty.HARD);
        assertThat(question.categoryName()).isEqualTo("misc");
        assertThat(question.correctAnswer()).isEqualTo("yes");
        assertThat(question.incorrectAnswers()).containsExactly("i don't know");
        server.verify();
    }


    @Test
    void getQuestionWithFilter() {
        // Given
        server.expect(requestTo("https://opentdb.com/api.php?amount=1&token=foo&encode=url3986&type=multiple&category=10&difficulty=hard"))
                .andRespond(withSuccess("""
                        {
                            "response_code": 0,
                            "results": [
                                {
                                    "type": "boolean",
                                    "difficulty": "hard",
                                    "category": "misc",
                                    "question": "To be or not to be",
                                    "correct_answer": "yes",
                                    "incorrect_answers": [
                                        "i don't know",
                                        "who cares?",
                                        "do be do be do"
                                    ]
                                }
                            ]
                        }
                     """, MediaType.APPLICATION_JSON));

        // When
        var question = client.getQuestion(new Question.Filter(10, Question.Difficulty.HARD, Question.Type.MULTIPLE), "foo");

        // Then
        assertThat(question).isNotNull();
        server.verify();
    }

    @Test
    void testNoResults() {
        // Given
        server.expect(requestTo("https://opentdb.com/api.php?amount=1&token=foo&encode=url3986&category=4"))
                        .andRespond(withSuccess("""
                                {
                                    "response_code": 1,
                                    "results": []
                                }
                                """, MediaType.APPLICATION_JSON));
        // When/Then
        assertThatThrownBy(() -> client.getQuestion(new Question.Filter(4, null, null), "foo"))
                .isInstanceOf(NoResultException.class);
        server.verify();
    }

}