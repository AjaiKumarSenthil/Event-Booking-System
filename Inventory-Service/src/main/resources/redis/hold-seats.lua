-- Atomically reserve every seat in KEYS or none of them.
--
-- KEYS    = list of seat-hold keys (e.g. "seat:hold:<showSeatId>")
-- ARGV[1] = TTL in seconds
-- ARGV[2] = value stored under each key (kept short; presence is what matters)
--
-- Returns 1 if every key was newly created (full hold acquired),
-- 0 if any key already existed (partial holds are rolled back via DEL).

local ttl = tonumber(ARGV[1])
local value = ARGV[2]
local acquired = {}

for i = 1, #KEYS do
    local ok = redis.call('SET', KEYS[i], value, 'NX', 'EX', ttl)
    if ok then
        acquired[#acquired + 1] = KEYS[i]
    else
        for j = 1, #acquired do
            redis.call('DEL', acquired[j])
        end
        return 0
    end
end

return 1
