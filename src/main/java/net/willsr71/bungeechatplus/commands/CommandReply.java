package net.willsr71.bungeechatplus.commands;

import net.willsr71.bungeechatplus.BungeeChatPlus;
import net.willsr71.bungeechatplus.ChatParser;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandReply extends Command {

    private BungeeChatPlus plugin;

    public CommandReply(BungeeChatPlus plugin, String name, String permission, String... aliases) {
        super(name, permission, aliases);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender cs, final String[] args) {
        if (!(cs instanceof ProxiedPlayer)) {
            cs.sendMessage(ChatParser.parse("Only players can do this"));
            return;
        }

        final ProxiedPlayer player = (ProxiedPlayer) cs;

        final ProxiedPlayer target = plugin.getReplyTarget(player);

        if (target == null) {
            String text = plugin.config.getString("unknownTarget").replace(
                    "%target%",
                    plugin.wrapVariable(args[0]));
            player.sendMessage(ChatParser.parse(text));
            return;
        }
        String text = "";
        for (String arg : args) {
            text = text + arg + " ";
        }

        final String finalText = text;
        plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.sendPrivateMessage(finalText, target, player);
            }
        });
    }

}
