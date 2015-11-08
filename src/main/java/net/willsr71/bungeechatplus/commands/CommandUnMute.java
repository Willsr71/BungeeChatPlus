package net.willsr71.bungeechatplus.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.willsr71.bungeechatplus.BungeeChatPlus;

public class CommandUnMute extends Command {

    private BungeeChatPlus plugin;

    public CommandUnMute(BungeeChatPlus plugin, String name, String permission, String... aliases) {
        super(name, permission, aliases);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        String toMuteName;
        if (args.length != 1) {
            toMuteName = cs.getName();
        } else {
            toMuteName = args[0];
        }

        ProxiedPlayer toMute = plugin.getProxy().getPlayer(toMuteName);

        if (toMute == null) {
            String text = plugin.config.getString("unknownTarget").replace("%target%", plugin.wrapVariable(toMuteName));
            cs.sendMessage(plugin.chatParser.parse(text));
            return;
        }

        // add player to mute list
        //if(plugin.mutedPlayers.isMuted(toMute.getName())) {
        if (plugin.mutedPlayers.setUnMuted(toMute.getName())) {
            cs.sendMessage(plugin.chatParser.parse(plugin.config.getString("muteUnmuted").replace("%target%", plugin.wrapVariable(toMute.getName()))));
            toMute.sendMessage(plugin.chatParser.parse(plugin.config.getString("mutePardonMessage")));
        } else {
            cs.sendMessage(plugin.chatParser.parse(plugin.config.getString("muteUnmuteFail").replace("%target%", plugin.wrapVariable(toMute.getName()))));
        }
        plugin.savePlayerLists();
    }
}
