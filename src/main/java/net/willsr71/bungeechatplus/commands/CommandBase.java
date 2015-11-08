package net.willsr71.bungeechatplus.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import net.willsr71.bungeechatplus.BungeeChatPlus;

public class CommandBase extends Command {
    private final BungeeChatPlus plugin;

    public CommandBase(BungeeChatPlus plugin, String name, String permission, String... aliases) {
        super(name, permission, aliases);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if (args.length == 0) {
            cs.sendMessage(plugin.chatParser.parse("&7You must specify a command to execute"));
            return;
        }

        if (plugin.debug) cs.sendMessage(plugin.chatParser.parse("GetProxy: " + plugin.getProxy().getPlayers().size()));
        if (plugin.debug)
            cs.sendMessage(plugin.chatParser.parse("ProxiedServer: " + ProxyServer.getInstance().getPlayers().size()));

        if (args[0].equals("reload")) {
            args[0] = "bcpreload";
        }

        String command = "";
        for (String arg : args) {
            command = (command + " " + arg).trim();
        }

        plugin.getProxy().getPluginManager().dispatchCommand(cs, command);
    }
}
