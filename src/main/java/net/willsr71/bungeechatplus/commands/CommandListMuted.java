package net.willsr71.bungeechatplus.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.willsr71.bungeechatplus.BungeeChatPlus;
import net.willsr71.bungeechatplus.ChatParser;

public class CommandListMuted extends Command {

    private BungeeChatPlus plugin;

    public CommandListMuted(BungeeChatPlus plugin, String name, String permission, String... aliases) {
        super(name, permission, aliases);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        String text = "&7Muted Players:";
        if(plugin.mutedPlayers.size() > 0){
            for(String[] player : plugin.mutedPlayers.getMutedPlayerData()){
                text = text + "\n&6Player: &7" + player[0] + ", &6Expire: &7" + player[2] + ", &6Reason: &7" + player[1];
            }
        }else{
            text = text + " None";
        }

        cs.sendMessage(ChatParser.parse(text));
    }
}
