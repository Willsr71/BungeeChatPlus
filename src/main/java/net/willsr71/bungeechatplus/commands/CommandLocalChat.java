package net.willsr71.bungeechatplus.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.willsr71.bungeechatplus.BungeeChatPlus;

public class CommandLocalChat extends Command {

    private BungeeChatPlus plugin;

    public CommandLocalChat(BungeeChatPlus plugin, String name, String permission, String... aliases) {
        super(name, permission, aliases);
        this.plugin = plugin;
    }

    @Override
    public void execute(final CommandSender cs, final String[] args) {
        if (!plugin.config.getBoolean("localChatCommandEnabled")) {
            plugin.sendCommandDisabled(cs.getName(), "localchat");
            return;
        }

        if (!(cs instanceof ProxiedPlayer)) {
            cs.sendMessage(plugin.chatParser.parse("Player only command"));
            return;
        }

        String message = "";
        for (String arg : args) {
            message = message + arg + " ";
        }

        if (message.isEmpty()) {
            plugin.endConversation((ProxiedPlayer) cs, false);
            return;
        }

        final String finalMessage = message.trim();
        plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
            @Override
            public void run() {
                ((ProxiedPlayer) cs).chat(finalMessage);
            }
        });
    }
}
