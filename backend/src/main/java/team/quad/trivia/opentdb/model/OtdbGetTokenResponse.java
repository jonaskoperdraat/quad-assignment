package team.quad.trivia.opentdb.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OtdbGetTokenResponse(
        @JsonProperty("response_code") int code,
        @JsonProperty("response_message") String message,
        String token
) {
}
