package net.willsr71.bungeechatplus;

import java.util.ArrayList;

public class MuteData {
    ArrayList<String []> mutedPlayers = new ArrayList<>();

    public void setMuted(String player, String reason, String expire){
        mutedPlayers.add(new String[]{player, reason, expire});
    }

    public void setUnMuted(String player){
        for (int x = 0; x < mutedPlayers.size(); x++) {
            if (mutedPlayers.get(x)[0].equals(player)) {
                mutedPlayers.remove(x);
                break;
            }
        }
    }

    public void checkMuted(){

    }

    public boolean isMuted(String player){
        checkMuted();
        boolean isMuted = false;
        for(String[] muted : mutedPlayers){
            if(muted[0].equals(player)){
                isMuted = true;
            }
        }
        return isMuted;
    }

    public String getReason(String player){
        String reason = "Error fetching reason";
        for(String[] muted : mutedPlayers){
            if(muted[0].equals(player)){
                reason = muted[1];
            }
        }
        return reason;
    }

    public String getDuration(String player){
        String duration = "Error fetching duration";
        for(String[] muted : mutedPlayers){
            if(muted[0].equals(player)){
                duration = muted[2];
            }
        }
        return duration;
    }

    public ArrayList<String> getMutedPlayers(){
        ArrayList<String> mutedList = new ArrayList<>();
        for(String[] muted : mutedPlayers){
            mutedList.add(muted[0]);
        }
        return mutedList;
    }

    public ArrayList<String []> getMutedPlayerData(){
        return mutedPlayers;
    }

    public int size(){ return mutedPlayers.size(); }
}
