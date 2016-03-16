package net.willsr71.bungeechatplus.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.willsr71.bungeechatplus.BungeeChatPlus;
import net.willsr71.bungeechatplus.DateUtils;

public class CommandTempMute extends Command {

    private BungeeChatPlus plugin;

    public CommandTempMute(BungeeChatPlus plugin, String name, String permission, String... aliases) {
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
            cs.sendMessage(plugin.chatParser.parse("/tempmute <player> <duration> <reason>"));
            return;
        }

        String reason = "";
        for (int x = 2; x < args.length; x++) {
            reason = (reason + " " + args[x]).trim();
        }

        long date = 0;
        try {
            date = DateUtils.parseDateDiff(args[1], true);
        } catch (Exception e) {
            cs.sendMessage(plugin.chatParser.parse("&cInvalid time"));
            return;
        }

        ProxiedPlayer toMute = plugin.getProxy().getPlayer(args[0]);

        if (toMute == null) {
            text = plugin.config.getString("unknownTarget").replace("%target%", plugin.wrapVariable(args[0]));
            cs.sendMessage(plugin.chatParser.parse(text));
            return;
        }

        // add player to mute list
        if (!plugin.mutedPlayers.isMuted(toMute.getName())) {
            plugin.mutedPlayers.setMuted(toMute.getName(), reason, date);
            cs.sendMessage(plugin.chatParser.parse(replaceVars(plugin.config.getString("muteSuccess"), toMute.getName(), reason, DateUtils.formatDateDiff(date))));
            toMute.sendMessage(plugin.chatParser.parse(replaceVars(plugin.config.getString("muteMessage"), toMute.getName(), reason, DateUtils.formatDateDiff(date))));
        } else {
            cs.sendMessage(plugin.chatParser.parse(replaceVars(plugin.config.getString("muteMuteFail"), toMute.getName(), reason, DateUtils.formatDateDiff(date))));
        }
        plugin.savePlayerLists();
    }
}