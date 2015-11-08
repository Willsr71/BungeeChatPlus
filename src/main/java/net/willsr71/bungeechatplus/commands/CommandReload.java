package net.willsr71.bungeechatplus.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.willsr71.bungeechatplus.BungeeChatPlus;

public class CommandReload extends Command {
    private final BungeeChatPlus plugin;

    public CommandReload(BungeeChatPlus plugin, String name, String permission, String... aliases) {
        super(name, permission, aliases);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        plugin.reload();
        cs.sendMessage(plugin.chatParser.parse("[color=blue][[color=red]BungeeChatPlus[/color]][/color] &aConfiguration has been reloaded."));
    }
}
