package net.willsr71.bungeechatplus.commands;

import net.willsr71.bungeechatplus.BungeeChatPlus;
import net.willsr71.bungeechatplus.ChatParser;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.util.logging.Level;

public class CommandBase extends Command {
    private final BungeeChatPlus plugin;

    public CommandBase(BungeeChatPlus plugin, String name, String permission, String... aliases) {
        super(name, permission, aliases);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if (args.length == 0){
            cs.sendMessage(ChatParser.parse("&7You must specify a command to execute"));
            return;
        }
        String command=args[0].toLowerCase();
        String commandargs="";
        for (String arg: args){
            commandargs = (commandargs + " " + arg).trim();
        }

        plugin.getLogger().log(Level.INFO, commandargs);
        plugin.getProxy().getPluginManager().dispatchCommand(cs, commandargs);
    }
}
