-- Sliding window rate limiter using Redis sorted sets
-- KEYS[1] = rate limit key (e.g., "rl:{clientId}:{routeId}")
-- ARGV[1] = current timestamp in microseconds
-- ARGV[2] = window size in microseconds
-- ARGV[3] = max requests allowed in window
-- ARGV[4] = request cost (default 1)
-- ARGV[5] = unique request ID (for the sorted set member)
--
-- Returns: {allowed (0/1), current_count, limit, window_reset_epoch_seconds}

local key = KEYS[1]
local now = tonumber(ARGV[1])
local window = tonumber(ARGV[2])
local limit = tonumber(ARGV[3])
local cost = tonumber(ARGV[4]) or 1
local request_id = ARGV[5]

local window_start = now - window

-- Remove expired entries outside the window
redis.call('ZREMRANGEBYSCORE', key, '-inf', window_start)

-- Count current requests in window
local current_count = redis.call('ZCARD', key)

local allowed = 0

if current_count + cost <= limit then
  -- Add the new request(s) — each unit of cost gets its own entry
  for i = 1, cost do
    redis.call('ZADD', key, now, request_id .. ':' .. i)
  end
  current_count = current_count + cost
  allowed = 1
end

-- Set TTL to window size (in seconds, rounded up)
local ttl_seconds = math.ceil(window / 1000000)
redis.call('EXPIRE', key, ttl_seconds)

-- Calculate when the window resets (oldest entry timestamp + window)
local oldest = redis.call('ZRANGE', key, 0, 0, 'WITHSCORES')
local reset_at = now + window
if #oldest >= 2 then
  reset_at = tonumber(oldest[2]) + window
end

-- Convert reset_at from microseconds to epoch seconds
local reset_epoch_seconds = math.ceil(reset_at / 1000000)

return {allowed, current_count, limit, reset_epoch_seconds}
