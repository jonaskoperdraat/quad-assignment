package team.quad.trivia.opentdb.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OtdbResponseCode {
    SUCCESS(0),
    NO_RESULT(1),
    INVALID_PARAMETER(2),
    TOKEN_NOT_FOUND(3),
    TOKEN_EMPTY(4),
    RATE_LIMIT(5);

    private final int code;
}
