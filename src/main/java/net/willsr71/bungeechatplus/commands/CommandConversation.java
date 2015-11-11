package net.willsr71.bungeechatplus.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.willsr71.bungeechatplus.BungeeChatPlus;

public class CommandConversation extends Command {
    private final BungeeChatPlus plugin;

    public CommandConversation(BungeeChatPlus plugin, String name, String permission, String... aliases) {
        super(name, permission, aliases);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if (!plugin.config.getBoolean("pmEnabled")) {
            plugin.sendCommandDisabled(cs.getName(), "conversation");
            return;
        }

        if (!(cs instanceof ProxiedPlayer)) {
            cs.sendMessage(plugin.chatParser.parse("Only players can do this"));
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
            player.sendMessage(plugin.chatParser.parse(text));
            return;
        }

        plugin.startConversation(player, target);
    }
}
