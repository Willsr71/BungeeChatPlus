package net.willsr71.bungeechatplus.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.willsr71.bungeechatplus.BungeeChatPlus;
import net.willsr71.bungeechatplus.DateUtils;

public class CommandMute extends Command {

    private BungeeChatPlus plugin;

    public CommandMute(BungeeChatPlus plugin, String name, String permission, String... aliases) {
        super(name, permission, aliases);
        this.plugin = plugin;
    }

    public String replaceVars(String format, String player, String reason, String expire) {
        format = format.replace("%target%", plugin.wrapVariable(player));
        format = format.replace("%reason%", plugin.wrapVariable(reason));
        format = format.replace("%duration%", plugin.wrapVariable(expire));
        return format;
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if (!plugin.config.getBoolean("muteEnabled")) {
            plugin.sendCommandDisabled(cs.getName(), "mute");
            return;
        }

        String text;
        if (args.length < 2) {
            cs.sendMessage(plugin.chatParser.parse("/mute <player> <reason>"));
            return;
        }

        String reason = "";
        for (int x = 1; x < args.length; x++) {
            reason = (reason + " " + args[x]).trim();
        }

        long date = -1;

        ProxiedPlayer toMute = plugin.getProxy().getPlayer(args[0]);

        // add player to mute list
        if (!plugin.mutedPlayers.isMuted(args[0])) {
            plugin.mutedPlayers.setMuted(args[0], reason, date);
            cs.sendMessage(plugin.chatParser.parse(replaceVars(plugin.config.getString("muteSuccess"), args[0], reason, "eternity")));

            if (toMute != null) {
                toMute.sendMessage(plugin.chatParser.parse(replaceVars(plugin.config.getString("muteMessage"), args[0], reason, "eternity")));
            }
        } else {
            cs.sendMessage(plugin.chatParser.parse(replaceVars(plugin.config.getString("muteMuteFail"), args[0], reason, DateUtils.formatDateDiff(plugin.mutedPlayers.getExpire(args[0])))));
        }
        plugin.savePlayerLists();
    }
}