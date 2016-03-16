package net.willsr71.bungeechatplus;

public class MutedPlayer {
    private String name;
    private String reason;
    private long expire;

    public MutedPlayer(String name, String reason, long expire) {
        this.name = name;
        this.reason = reason;
        this.expire = expire;
    }

    public String getName() {
        return name;
    }

    public String getReason() {
        return reason;
    }

    public long getExpire() {
        return expire;
    }
}
