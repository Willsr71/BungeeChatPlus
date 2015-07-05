package net.willsr71.bungeechatplus.commands;

import net.willsr71.bungeechatplus.BungeeChatPlus;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.willsr71.bungeechatplus.ChatParser;

public class CommandToggleChat extends Command {

    private BungeeChatPlus plugin;

    public CommandToggleChat(BungeeChatPlus plugin, String name, String permission, String... aliases) {
        super(name, permission, aliases);
        this.plugin = plugin;
    }

    @Override
    public void execute(final CommandSender cs, final String[] args) {
        if (!plugin.localPlayers.contains(cs.getName())) {
            plugin.localPlayers.add(cs.getName());
            cs.sendMessage(ChatParser.parse(plugin.config.getString("localChatMessage")));
        } else {
            plugin.localPlayers.remove(cs.getName());
            cs.sendMessage(ChatParser.parse(plugin.config.getString("globalChatMessage")));
        }
        plugin.savePlayerLists();
    }
}
