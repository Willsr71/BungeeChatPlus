package net.willsr71.bungeechatplus.commands;

import net.willsr71.bungeechatplus.BungeeChatPlus;
import net.willsr71.bungeechatplus.ChatParser;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.io.IOException;

public class CommandReload extends Command {
    private final BungeeChatPlus plugin;

    public CommandReload(BungeeChatPlus plugin, String name, String permission, String... aliases) {
        super(name, permission, aliases);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (strings.length == 1 && strings[0].equalsIgnoreCase("reload")) {
            try {
                plugin.reloadConfig();
                commandSender.sendMessage(ChatParser.parse("[color=blue][[color=red]BungeeChatPlus[/color]][/color] &aConfiguration has been reloaded."));
            } catch (IOException e) {
                e.printStackTrace();
                commandSender.sendMessage(ChatParser.parse("[color=blue][[color=red]BungeeChatPlus[/color]][/color] &cError reloading the config. See the console for more details"));
            }
        } else {
            commandSender.sendMessage(ChatParser.parse("[color=blue][[color=red]BungeeChatPlus[/color]][/color] &f[suggest]/bungeechatplus reload[/suggest]"));
        }
    }
}
