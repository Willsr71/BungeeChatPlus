package net.willsr71.bungeechatplus.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.willsr71.bungeechatplus.BungeeChatPlus;

public class CommandUnMute extends Command {

    private BungeeChatPlus plugin;

    public CommandUnMute(BungeeChatPlus plugin, String name, String permission, String... aliases) {
        super(name, permission, aliases);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if (!plugin.config.getBoolean("muteEnabled")) {
            plugin.sendCommandDisabled(cs.getName(), "mute");
            return;
        }

        String toMuteName;
        if (args.length == 1) {
            toMuteName = args[0];
        } else if (args.length == 0) {
            toMuteName = cs.getName();
        } else {
            cs.sendMessage(plugin.chatParser.parse("/unmute [player]"));
            return;
        }

        if (plugin.mutedPlayers.setUnMuted(toMuteName)) {
            cs.sendMessage(plugin.chatParser.parse(plugin.config.getString("muteUnmuted").replace("%target%", plugin.wrapVariable(toMuteName))));

            if (plugin.getProxy().getPlayer(toMuteName) != null) {
                plugin.getProxy().getPlayer(toMuteName).sendMessage(plugin.chatParser.parse(plugin.config.getString("mutePardonMessage")));
            }
        } else {
            cs.sendMessage(plugin.chatParser.parse(plugin.config.getString("muteUnmuteFail").replace("%target%", plugin.wrapVariable(toMuteName))));
        }
        plugin.savePlayerLists();
    }
}
