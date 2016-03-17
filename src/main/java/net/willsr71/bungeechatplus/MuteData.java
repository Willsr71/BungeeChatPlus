package net.willsr71.bungeechatplus;

import java.util.ArrayList;
import java.util.Date;

public class MuteData {
    private BungeeChatPlus plugin;
    private ArrayList<MutedPlayer> mutedPlayers = new ArrayList<>();

    public MuteData(BungeeChatPlus plugin) {
        this.plugin = plugin;
    }

    public void setMuted(String player, String reason, long duration) {
        mutedPlayers.add(new MutedPlayer(player, reason, duration));
    }

    public boolean setUnMuted(String player) {
        for (MutedPlayer mutedPlayer : mutedPlayers) {
            if (mutedPlayer.getName().equals(player)) {
                mutedPlayers.remove(mutedPlayer);
                return true;
            }
        }
        return false;
    }

    public boolean isMuted(String player) {
        for (MutedPlayer mutedPlayer : mutedPlayers) {
            if (mutedPlayer.getName().equals(player)) {
                if (mutedPlayer.getExpire() == -1) {
                    return true;
                }

                if (new Date(mutedPlayer.getExpire()).after(new Date())) {
                    return true;
                }

                setUnMuted(mutedPlayer.getName());
                plugin.savePlayerLists();
                return false;
            }
        }
        return false;
    }

    public String getReason(String player) {
        for (MutedPlayer mutedPlayer : mutedPlayers) {
            if (mutedPlayer.getName().equals(player)) {
                return mutedPlayer.getReason();
            }
        }
        return "Error fetching reason";
    }

    public long getExpire(String player) {
        for (MutedPlayer mutedPlayer : mutedPlayers) {
            if (mutedPlayer.getName().equals(player)) {
                return mutedPlayer.getExpire();
            }
        }
        return 0;
    }

    public ArrayList<String> getMutedPlayers() {
        ArrayList<String> mutedList = new ArrayList<>();
        for (MutedPlayer mutedPlayer : mutedPlayers) {
            mutedList.add(mutedPlayer.getName());
        }
        return mutedList;
    }

    public ArrayList<MutedPlayer> getMutedPlayerData() {
        return mutedPlayers;
    }

    public int size() {
        return mutedPlayers.size();
    }
}
