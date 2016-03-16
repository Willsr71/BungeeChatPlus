package net.willsr71.bungeechatplus.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.willsr71.bungeechatplus.BungeeChatPlus;

public class CommandGlobalChat extends Command {

    private BungeeChatPlus plugin;

    public CommandGlobalChat(BungeeChatPlus plugin, String name, String permission, String... aliases) {
        super(name, permission, aliases);
        this.plugin = plugin;
    }

    @Override
    public void execute(final CommandSender cs, final String[] args) {
        if (!plugin.config.getBoolean("globalChatCommandEnabled")) {
            plugin.sendCommandDisabled(cs.getName(), "globalchat");
            return;
        }

        String message = "";
        for (String arg : args) {
            message = message + arg + " ";
        }

        if (!(cs instanceof ProxiedPlayer)) {
            if (!message.trim().isEmpty()) plugin.sendGlobalConsoleChatMessage(message);
            else cs.sendMessage(plugin.chatParser.parse("/g <message>"));
            return;
        }

        if (message.isEmpty()) {
            plugin.endConversation((ProxiedPlayer) cs, false);
            return;
        }

        final String finalMessage = message;
        plugin.getProxy().getScheduler().runAsync(plugin, () -> plugin.sendGlobalChatMessage((ProxiedPlayer) cs, finalMessage));
    }
}
