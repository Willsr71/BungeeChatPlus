package net.willsr71.bungeechatplus.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.willsr71.bungeechatplus.BungeeChatPlus;
import net.willsr71.bungeechatplus.DateUtils;
import net.willsr71.bungeechatplus.MutedPlayer;

public class CommandListMuted extends Command {

    private BungeeChatPlus plugin;

    public CommandListMuted(BungeeChatPlus plugin, String name, String permission, String... aliases) {
        super(name, permission, aliases);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if (!plugin.config.getBoolean("muteEnabled")) {
            plugin.sendCommandDisabled(cs.getName(), "mute");
            return;
        }

        String text = "&7Muted Players:";
        if (plugin.mutedPlayers.size() > 0) {
            for (MutedPlayer player : plugin.mutedPlayers.getMutedPlayerData()) {
                text = text + "\n&6Player: &7" + player.getName() + ", &6Expire: &7" + DateUtils.formatDateDiff(player.getExpire()) + ", &6Reason: &7" + player.getReason();
            }
        } else {
            text = text + " None";
        }

        cs.sendMessage(plugin.chatParser.parse(text));
    }
}
