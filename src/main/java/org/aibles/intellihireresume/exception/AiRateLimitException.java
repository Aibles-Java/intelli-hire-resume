package org.aibles.intellihireresume.exception;

import lombok.Getter;

/**
 * Thrown when OpenAI returns 429 TOO_MANY_REQUESTS.
 * Carries the Retry-After value (seconds) if provided by the API.
 */
@Getter
public class AiRateLimitException extends RuntimeException {

    private final long retryAfterSeconds;

    public AiRateLimitException(long retryAfterSeconds) {
        super("OpenAI rate limit exceeded. Retry after " + retryAfterSeconds + "s");
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
