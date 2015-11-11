package net.willsr71.bungeechatplus.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.willsr71.bungeechatplus.BungeeChatPlus;

import java.util.ArrayList;

public class CommandFilterList extends Command {
    private BungeeChatPlus plugin;

    public CommandFilterList(BungeeChatPlus plugin, String name, String permission, String... aliases) {
        super(name, permission, aliases);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if (!plugin.config.getBoolean("filterEnabled")) {
            plugin.sendCommandDisabled(cs.getName(), "filter");
            return;
        }

        if (plugin.filterDataMap.get(cs.getName()) == null || plugin.filterDataMap.get(cs.getName()).getFilters().size() == 0) {
            cs.sendMessage(plugin.chatParser.parse(plugin.config.getString("filterNoFilters")));
            return;
        }

        ArrayList<String> filters = plugin.filterDataMap.get(cs.getName()).getFilters();
        String line = "&7Your filters: ";

        for (String filter : filters) {
            line = line + "&6" + filter + "&7, ";
        }

        line = line.substring(0, line.lastIndexOf("&7, "));

        cs.sendMessage(plugin.chatParser.parse(line));
    }
}
