package team.quad.trivia.opentdb;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import team.quad.trivia.TriviaRepo;
import team.quad.trivia.model.Question;
import team.quad.trivia.opentdb.exception.InvalidParameterException;
import team.quad.trivia.opentdb.exception.NoResultException;
import team.quad.trivia.opentdb.exception.TokenEmptyException;
import team.quad.trivia.opentdb.exception.TokenNotFoundException;
import team.quad.trivia.opentdb.model.OtdbApiResponse;
import team.quad.trivia.opentdb.model.OtdbCategoryResponse;
import team.quad.trivia.opentdb.model.OtdbGetTokenResponse;
import team.quad.trivia.opentdb.model.OtdbResult;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class OtdbClient implements TriviaRepo {

    private final RestClient otdbRestClient;
    private final ObjectMapper mapper;

    /**
     * Retrieve a list of available categories
     *
     * @return the available categories
     */
    public List<Question.Category> getCategories() {
        log.info("Retrieving categories from Open Trivia DB");
        // Retrieve categories from upstream service
        OtdbCategoryResponse responseBody = otdbRestClient.get()
                .uri("api_category.php")
                .retrieve()
                .body(OtdbCategoryResponse.class);

        // Check if categories are present
        if (responseBody == null || responseBody.triviaCategories() == null
                || responseBody.triviaCategories().isEmpty()) {
            throw new OtdbClientException("Unable to retrieve categories from Open Trivia DB");
        }

        // Convert response to domain entity and return
        return responseBody.toDomain();
    }

    public Question getQuestion(@Nullable Question.Filter filter, String sessionToken) {
        log.info("Retrieving question with filter: {}", filter);
        var uriComponentsBuilder = UriComponentsBuilder.fromUriString("api.php")
                // Get a single question
                .queryParam("amount", 1)
                .queryParam("token", sessionToken)
                .queryParam("encode", "url3986");

        // Construct request uri containing filter parameters if necessary
        if (filter != null) {
            if (filter.type() != null) {
                OtdbResult.Type type = OtdbResult.Type.valueOf(filter.type().name());
                uriComponentsBuilder.queryParam("type", mapper.convertValue(type, String.class));
            }
            if (filter.category() != null) {
                uriComponentsBuilder.queryParam("category", filter.category());
            }
            if (filter.difficulty() != null) {
                OtdbResult.Difficulty difficulty = OtdbResult.Difficulty.valueOf(filter.difficulty().name());
                uriComponentsBuilder.queryParam("difficulty", mapper.convertValue(difficulty, String.class));
            }
        }

        // Retrieve question
        var responseBody = otdbRestClient.get()
                .uri(uriComponentsBuilder.build().toUri())
                .retrieve()
                .body(OtdbApiResponse.class);

        if (responseBody == null) {
            throw new OtdbClientException("Received empty response body");
        }

        log.debug("Received question response body: {}", responseBody);

        // Check for erroneous response codes in the response body
        handleResponseCode(responseBody);

        if (responseBody.results() == null || responseBody.results().isEmpty()) {
            throw new OtdbClientException("Unable to retrieve question(s) from Open Trivia DB");
        }

        // Convert response to domain object and return
        return responseBody.results().getFirst().toDomain();

    }

    @Override
    public String getSessionToken() {
        log.info("Requesting session token from Open Trivia DB");
        var response = otdbRestClient.get()
                .uri(UriComponentsBuilder.fromUriString("api_token.php?")
                        .queryParam("command", "request")
                        .build()
                        .toUri())
                .retrieve()
                .body(OtdbGetTokenResponse.class);

        if (response == null) {
            throw new OtdbClientException("Result of requesting a session token returned an empty response body.");
        }
        if (!StringUtils.hasText(response.token())) {
            throw new OtdbClientException("Result of requesting a session token returned a response with an empty token.");
        }
        return response.token();
    }

    @Override
    public void resetSession(String token) {
        log.info("Resetting session token: {}", token);
        var response = otdbRestClient.get()
                .uri(UriComponentsBuilder.fromUriString("api_token.php")
                        .queryParam("command", "reset")
                        .queryParam("token", token)
                        .build()
                        .toUri())
                .retrieve()
                .body(OtdbGetTokenResponse.class);
        if (response == null) {
            throw new OtdbClientException("Result of requesting a session token reset returned an empty response body.");
        }
        if (!StringUtils.hasText(response.token())) {
            throw new OtdbClientException("Result of requesting a session token reset returned a response with an empty token.");
        }
        if (!token.equals(response.token())) {
            throw new OtdbClientException("Result of requesting a session token reset returned a different token.");
        }
    }

    /**
     * Various fault scenarios are returned with a HTTP status code of 200. This would indicate a successful request
     * handling, but the code within the response can indicate differently. This method checks for these response
     * codes and converts those into exceptions being thrown, which we can act upon later.
     */
    private void handleResponseCode(OtdbApiResponse otdbApiResponse) {
        switch (otdbApiResponse.code()) {
            case NO_RESULT -> {
                throw new NoResultException();
            }
            case INVALID_PARAMETER -> {
                throw new InvalidParameterException();
            }
            case TOKEN_NOT_FOUND -> {
                throw new TokenNotFoundException();
            }
            case TOKEN_EMPTY -> {
                throw new TokenEmptyException();
            }
        }
    }
}

