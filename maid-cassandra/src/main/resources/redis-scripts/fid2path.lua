--[[
-- see https://redis.io/commands/eval
eval "return {KEYS[1],KEYS[2],ARGV[1],ARGV[2]}" 2 key1 key2 first second

eval "return redis.call('set','foo','bar')" 0
eval "return redis.call('set',KEYS[1],'bar')" 1 foo


]]

--[[
fid  -->  pid:name
hmset x 1 0:one 2 0:tow 3 1:three 4 3:four
eval "local function recurseSearch(fid) if fid == 0 then return '/' end local pidAndName = redis.call('hget', KEYS[1], fid) if (not pidAndName) then return nil end local ind = string.find(pidAndName, ':') local pid = tonumber(string.sub(pidAndName, 0, ind - 1)) local name = string.sub(pidAndName, ind + 1) local dirname = recurseSearch(pid) if dirname == '/' then return dirname .. name else return dirname .. '/' .. name end end return recurseSearch(KEYS[2])" 2 x 4

]]
local function recurseSearch(fid)
    if fid == 0 then
        return '/'
    end

    local pidAndName = redis.call('hget', KEYS[1], fid)
    if (not pidAndName) then return nil end
    local ind = string.find(pidAndName, ':')
    local pid = tonumber(string.sub(pidAndName, 0, ind - 1))
    local name = string.sub(pidAndName, ind + 1)

    local dirname = recurseSearch(pid)
    if dirname == '/' then
        return dirname .. name
    else
        return dirname .. '/' .. name
    end
end

return recurseSearch(KEYS[2])
