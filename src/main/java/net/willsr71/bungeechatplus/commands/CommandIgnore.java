package net.willsr71.bungeechatplus.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.willsr71.bungeechatplus.BungeeChatPlus;
import net.willsr71.bungeechatplus.ChatParser;

import java.util.ArrayList;
import java.util.List;

public class CommandIgnore extends Command {

    private BungeeChatPlus plugin;

    public CommandIgnore(BungeeChatPlus plugin, String name, String permission, String... aliases) {
        super(name, permission, aliases);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if (!(cs instanceof ProxiedPlayer)) {
            cs.sendMessage(ChatParser.parse("Only players can do this"));
            return;
        }

        if (args.length != 1) {
            cs.sendMessage(ChatParser.parse("/ignore <player>"));
        }

        ProxiedPlayer toIgnore = plugin.getProxy().getPlayer(args[0]);

        if (toIgnore == null) {
            String text = plugin.config.getString("unknownTarget").replace(
                    "%target%",
                    plugin.wrapVariable(args[0]));
            cs.sendMessage(ChatParser.parse(text));
            return;
        }

        // add player to ignore list
        List<String> ignoreList = plugin.ignoredPlayers.get(cs.getName());
        if (ignoreList == null) ignoreList = new ArrayList<>(1);
        if (!ignoreList.contains(toIgnore.getName())) {
            ignoreList.add(toIgnore.getName());
            String text = plugin.config.getString("ignoreSuccess").replace(
                    "%target%",
                    plugin.wrapVariable(args[0]));
            cs.sendMessage(ChatParser.parse(text));
        } else {
            ignoreList.remove(toIgnore.getName());
            String text = plugin.config.getString("ignoreUnignore").replace(
                    "%target%",
                    plugin.wrapVariable(args[0]));
            cs.sendMessage(ChatParser.parse(text));
        }
        plugin.ignoredPlayers.put(cs.getName(), ignoreList);
    }
}
