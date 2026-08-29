package dev.devangsharma.gatekeeper.common.dto;

public record RateLimitResult(
        boolean allowed,
        long currentCount,
        long limit,
        long remaining,
        long resetAtEpochSecond
) {
    public static RateLimitResult allowed(long currentCount, long limit, long resetAtEpochSecond) {
        return new RateLimitResult(true, currentCount, limit, limit - currentCount, resetAtEpochSecond);
    }

    public static RateLimitResult denied(long currentCount, long limit, long resetAtEpochSecond) {
        return new RateLimitResult(false, currentCount, limit, 0, resetAtEpochSecond);
    }
}
