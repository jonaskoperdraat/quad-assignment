package team.quad.trivia.opentdb.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OtdbApiResponse(
        @JsonProperty("response_code") OtdbResponseCode code,
        List<OtdbResult> results) {
}
