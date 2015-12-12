package net.willsr71.bungeechatplus;

import net.willsr71.bungeechatplus.commands.MutedPlayer;

import java.util.ArrayList;

public class MuteData {
    private BungeeChatPlus plugin;
    private ArrayList<MutedPlayer> mutedPlayers = new ArrayList<>();

    public MuteData(BungeeChatPlus plugin) {
        this.plugin = plugin;
    }

    public void setMuted(String player, String reason, String enforcer, String expire) {
        if (isMuted(player)) {
            String message = replaceVars("muteMuteFail", player, reason, enforcer, expire);
            plugin.getProxy().getPlayer(enforcer).sendMessage(plugin.chatParser.parse(message));
            return;
        }

        MutedPlayer mutedPlayer = new MutedPlayer(player, reason, enforcer, expire);
        mutedPlayers.add(mutedPlayer);
    }

    public boolean isMuted(String player) {
        return false;
    }

    public String replaceVars(String configString, String player, String reason, String enforcer, String expire) {
        String format = plugin.config.getString(configString);
        format = format.replace("%target%", plugin.wrapVariable(player));
        format = format.replace("%reason%", plugin.wrapVariable(reason));
        format = format.replace("%enforcer%", plugin.wrapVariable(enforcer));
        format = format.replace("%duration%", plugin.wrapVariable(expire));
        return format;
    }

    /*
    private ArrayList<String[]> mutedPlayers = new ArrayList<>();

    public void setMuted(String player, String reason, String expire) {
        mutedPlayers.add(new String[]{player, reason, expire});
    }

    public boolean setUnMuted(String player) {
        for (int x = 0; x < mutedPlayers.size(); x++) {
            if (mutedPlayers.get(x)[0].equals(player)) {
                mutedPlayers.remove(x);
                return true;
            }
        }
        return false;
    }

    public boolean isMuted(String player) {
        boolean isMuted = false;
        for (String[] muted : mutedPlayers) {
            if (muted[0].equals(player)) {
                isMuted = true;
            }
        }
        return isMuted;
    }

    public String getReason(String player) {
        String reason = "Error fetching reason";
        for (String[] muted : mutedPlayers) {
            if (muted[0].equals(player)) {
                reason = muted[1];
            }
        }
        return reason;
    }

    public String getDuration(String player) {
        String duration = "Error fetching duration";
        for (String[] muted : mutedPlayers) {
            if (muted[0].equals(player)) {
                duration = muted[2];
            }
        }
        return duration;
    }

    public ArrayList<String> getMutedPlayers() {
        ArrayList<String> mutedList = new ArrayList<>();
        for (String[] muted : mutedPlayers) {
            mutedList.add(muted[0]);
        }
        return mutedList;
    }

    public ArrayList<String[]> getMutedPlayerData() {
        return mutedPlayers;
    }

    public int size() {
        return mutedPlayers.size();
    }*/
}
