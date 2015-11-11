package net.willsr71.bungeechatplus.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.willsr71.bungeechatplus.BungeeChatPlus;

public class CommandMessage extends Command {

    private BungeeChatPlus plugin;

    public CommandMessage(BungeeChatPlus plugin, String name, String permission, String... aliases) {
        super(name, permission, aliases);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender cs, final String[] args) {
        if (!plugin.config.getBoolean("pmEnabled")) {
            plugin.sendCommandDisabled(cs.getName(), "pm");
            return;
        }

        if (!(cs instanceof ProxiedPlayer)) {
            cs.sendMessage(plugin.chatParser.parse("Only players can do this"));
            return;
        }
        if (args.length < 1) {
            plugin.endConversation((ProxiedPlayer) cs, false);
            return;
        }
        final ProxiedPlayer target = plugin.getProxy().getPlayer(args[0]);
        final ProxiedPlayer player = (ProxiedPlayer) cs;
        if (target == null) {
            String text = plugin.config.getString("unknownTarget").replace(
                    "%target%", plugin.wrapVariable(args[0]));
            player.sendMessage(plugin.chatParser.parse(text));
            return;
        }
        String text = "";
        for (int i = 1; i < args.length; i++) {
            text = text + args[i] + " ";
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
