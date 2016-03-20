package net.willsr71.bungeechatplus;

public class AntiSpamData {
    long lastMessages[] = {0L, 0L, 0L};
    long lastSpam = 0L;

    public boolean isSpamming() {
        long now = System.currentTimeMillis();
        if (now - lastSpam < BungeeChatPlus.instance.config.getInt("antiSpamMuteTime") * 1000) {
            return true;
        }
        long delta = now - lastMessages[2];
        lastMessages[2] = lastMessages[1];
        lastMessages[1] = lastMessages[0];
        lastMessages[0] = now;
        // 4 messages in less than 4 seconds -> spam
        boolean isSpamming = delta < 4000;
        if (isSpamming) {
            lastSpam = now;
        }
        return isSpamming;
    }
}
