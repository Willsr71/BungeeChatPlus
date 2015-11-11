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
        String player = cs.getName();

        if (!plugin.config.getBoolean("filterEnabled")) {
            plugin.sendCommandDisabled(cs.getName(), "filter");
            return;
        }

        if (args.length == 1 && args[0].equals("list")) {
            plugin.commandBase.commandFilterList.execute(cs, args);
            return;
        }

        if (args.length != 2 || !(args[0].equals("add") || args[0].equals("remove"))) {
            cs.sendMessage(plugin.chatParser.parse("/filter <add|remove|list> <string>"));
            return;
        }

        if (args[0].equals("add")) {
            if (plugin.filterDataMap.get(player) == null) {
                List<String> filters = new ArrayList<>();
                filters.add(args[1]);

                plugin.filterDataMap.put(player, new FilterData(player, filters));
            } else {
                if (plugin.filterDataMap.get(player).filterExists(args[1])) {
                    cs.sendMessage(plugin.chatParser.parse(plugin.config.getString("filterAlreadyFiltered").replace("%filter%", args[1])));
                    return;
                }

                plugin.filterDataMap.get(player).addFilter(args[1]);
            }

            cs.sendMessage(plugin.chatParser.parse(plugin.config.getString("filterAdd").replace("%filter%", args[1])));
        } else if (args[0].equals("remove")) {
            if (plugin.filterDataMap.get(player) == null || !plugin.filterDataMap.get(player).isFiltered(args[1])) {
                cs.sendMessage(plugin.chatParser.parse(plugin.config.getString("filterNotFiltered").replace("%filter%", args[1])));
                return;
            }

            plugin.filterDataMap.get(player).removeFilter(args[1]);

            if (plugin.filterDataMap.get(player).getFilters().size() == 0) {
                plugin.filterDataMap.remove(player);
            }

            cs.sendMessage(plugin.chatParser.parse(plugin.config.getString("filterRemove").replace("%filter%", args[1])));
        }
    }
}
