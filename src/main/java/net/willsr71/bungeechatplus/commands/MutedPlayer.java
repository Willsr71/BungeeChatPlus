package net.willsr71.bungeechatplus.commands;

public class MutedPlayer {
    private String player;
    private String reason;
    private String enforcer;
    private String expire;

    public MutedPlayer(String player, String reason, String enforcer, String expire) {
        this.player = player;
        this.reason = reason;
        this.enforcer = enforcer;
        this.expire = expire;
    }

    public boolean hasExpired() {
        return false;
    }

    public String getPlayer() {
        return player;
    }

    public String getReason() {
        return reason;
    }

    public String getEnforcer() {
        return enforcer;
    }

    public String getExpire() {
        return expire;
    }
}
