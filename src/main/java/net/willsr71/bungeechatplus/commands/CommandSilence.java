package net.willsr71.bungeechatplus.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.willsr71.bungeechatplus.BungeeChatPlus;

public class CommandSilence extends Command {
    private BungeeChatPlus plugin;

    public CommandSilence(BungeeChatPlus plugin, String name, String permission, String... aliases) {
        super(name, permission, aliases);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender cs, final String[] args) {
        if (!plugin.config.getBoolean("silenceEnabled")) {
            plugin.sendCommandDisabled(cs.getName(), "silence");
            return;
        }

        String player = cs.getName();
        if (args.length == 1 && cs.hasPermission("bungeechatplus.silence.others")) {
            player = args[0];
        }

        ProxiedPlayer proxiedPlayer = plugin.getProxy().getPlayer(player);

        if (!plugin.silencedPlayers.contains(player)) {
            plugin.silencedPlayers.add(player);

            if (!cs.getName().equals(player)) {
                cs.sendMessage(plugin.chatParser.parse(plugin.config.getString("silencePlayer").replace("%target%", player)));
            }

            if (proxiedPlayer != null) {
                proxiedPlayer.sendMessage(plugin.chatParser.parse(plugin.config.getString("silencePlayer").replace("%target%", player)));
            }
        } else {
            plugin.silencedPlayers.remove(player);

            if (!cs.getName().equals(player)) {
                cs.sendMessage(plugin.chatParser.parse(plugin.config.getString("unsilencePlayer").replace("%target%", player)));
            }

            if (proxiedPlayer != null) {
                proxiedPlayer.sendMessage(plugin.chatParser.parse(plugin.config.getString("unsilencePlayer").replace("%target%", player)));
            }
        }

        plugin.savePlayerLists();
    }
}
