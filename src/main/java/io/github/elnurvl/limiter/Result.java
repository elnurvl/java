package io.github.elnurvl.limiter;

import java.time.Duration;

/**
 * The outcome of a rate limit check.
 *
 * @param allowed whether the request was permitted
 * @param remainingTokens remaining capacity after this call
 * @param retryAfter time until capacity is available; {@link Duration#ZERO} when allowed
 * @param limit the configured maximum capacity
 */
public record Result(boolean allowed, int remainingTokens, Duration retryAfter, int limit) {}
