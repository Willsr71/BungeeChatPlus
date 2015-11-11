package net.willsr71.bungeechatplus.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.willsr71.bungeechatplus.BungeeChatPlus;
import net.willsr71.bungeechatplus.FilterData;

import java.util.ArrayList;
import java.util.List;

public class CommandFilter extends Command {
    private BungeeChatPlus plugin;

    public CommandFilter(BungeeChatPlus plugin, String name, String permission, String... aliases) {
        super(name, permission, aliases);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if (!plugin.config.getBoolean("filterEnabled")) {
            plugin.sendCommandDisabled(cs.getName(), "filter");
            return;
        }

        if (args.length == 0) {
            cs.sendMessage(plugin.chatParser.parse(plugin.config.getString("filterNoArgs")));
            return;
        }

        if (plugin.filterDataMap.get(cs.getName()).isFiltered(args[0])) {
            cs.sendMessage(plugin.chatParser.parse(plugin.config.getString("filterAlreadyFiltered").replace("%filter%", args[0])));
            return;
        }

        List<String> filters = new ArrayList<>();
        filters.add(args[0]);

        plugin.filterDataMap.put(cs.getName(), new FilterData(cs.getName(), filters));

        cs.sendMessage(plugin.chatParser.parse(plugin.config.getString("filterAdd").replace("%filter%", args[0])));
    }
}
