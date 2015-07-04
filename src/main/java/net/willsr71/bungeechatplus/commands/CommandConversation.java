package net.willsr71.bungeechatplus.commands;

import net.willsr71.bungeechatplus.ChatParser;
import net.willsr71.bungeechatplus.BungeeChatPlus;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandConversation extends Command {
    private final BungeeChatPlus plugin;

    public CommandConversation(BungeeChatPlus plugin, String name, String permission, String... aliases) {
        super(name, permission, aliases);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if (!(cs instanceof ProxiedPlayer)) {
            cs.sendMessage(ChatParser.parse("Only players can do this"));
            return;
        }
        if (args.length < 1) {
            plugin.endConversation((ProxiedPlayer) cs, true);
            return;
        }
        final ProxiedPlayer target = plugin.getProxy().getPlayer(args[0]);
        final ProxiedPlayer player = (ProxiedPlayer) cs;
        if (target == null) {
            String text = plugin.config.getString("unknownTarget").replace("%target%", plugin.wrapVariable(args[0]));
            player.sendMessage(ChatParser.parse(text));
            return;
        }

        plugin.startConversation(player, target);
    }
}
