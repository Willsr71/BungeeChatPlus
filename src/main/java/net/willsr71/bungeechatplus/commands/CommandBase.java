package net.willsr71.bungeechatplus.commands;

import net.willsr71.bungeechatplus.BungeeChatPlus;

import java.util.Arrays;
import java.util.List;

public class CommandBase {
    public CommandBungeeChatPlus commandBungeeChatPlus;
    public CommandConversation commandConversation;
    public CommandFilter commandFilter;
    public CommandFilterList commandFilterList;
    public CommandGlobalChat commandGlobalChat;
    public CommandIgnore commandIgnore;
    public CommandListMuted commandListMuted;
    public CommandLocalChat commandLocalChat;
    public CommandMessage commandMessage;
    public CommandMute commandMute;
    public CommandReload commandReload;
    public CommandReply commandReply;
    public CommandTempMute commandTempMute;
    public CommandToggleChat commandToggleChat;
    public CommandUnMute commandUnMute;
    public boolean commandsLoaded = false;
    private BungeeChatPlus plugin;

    public CommandBase(BungeeChatPlus plugin) {
        this.plugin = plugin;
    }

    public void reloadCommands() {
        loadCommands();

        plugin.getLogger().info("Cleaning up...");

        if (!plugin.config.getBoolean("pmEnabled")) {
            for (String player : plugin.persistentConversations.keySet()) {
                plugin.endConversation(plugin.getProxy().getPlayer(player), true);
            }
            if (plugin.debug) plugin.getLogger().warning("Cleared persistent conversations");
        }

        if (!plugin.config.getBoolean("filterEnabled")) {
            for (String player : plugin.filterDataMap.keySet()) {
                plugin.filterDataMap.remove(player);
            }
            if (plugin.debug) plugin.getLogger().warning("Cleared filters");
        }

        if (!plugin.config.getBoolean("ignoreEnabled")) {
            for (String player : plugin.ignoredPlayers.keySet()) {
                for (String ignored : plugin.ignoredPlayers.get(player)) {
                    plugin.getProxy().getPlayer(player).sendMessage(plugin.chatParser.parse(plugin.config.getString("ignoreUnignore").replace("%target%", ignored)));
                    plugin.ignoredPlayers.remove(player);
                }
            }
            if (plugin.debug) plugin.getLogger().warning("Cleared ignored players");
        }

        if (!plugin.config.getBoolean("muteEnabled")) {
            plugin.getLogger().warning("Mute is disabled, however not clearing muted players. If this is needed then please do it manually.");
        }

        if (!plugin.config.getBoolean("toggleChatEnabled")) {
            for (int x = 0; x < plugin.localPlayers.size(); x = x + 1) {
                try {
                    plugin.getProxy().getPlayer(plugin.localPlayers.get(x)).sendMessage(plugin.chatParser.parse(plugin.config.getString("globalChatMessage")));
                } catch (NullPointerException e) {
                    if (plugin.debug)
                        plugin.getLogger().warning("Could not send message to player: " + plugin.localPlayers.get(x));
                }
                plugin.localPlayers.remove(x);
            }

            /*
            for (String player : plugin.localPlayers) {
                plugin.localPlayers.remove(player);
            }
            */

            if (plugin.debug) plugin.getLogger().warning("Cleared togglechat local players");
        }
    }

    public void loadCommands() {
        if (commandsLoaded) {
            return;
        }

        // Load commands in alphabetical order for easier organization

        plugin.getLogger().info("Loading commands...");

        List<String> aliases;

        aliases = plugin.config.getStringList("bcpCommandAliases");
        if (aliases == null || aliases.isEmpty()) aliases = Arrays.asList("bcp", "bungeechatplus");
        commandBungeeChatPlus = new CommandBungeeChatPlus(plugin, aliases.get(0), null, aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1]));
        plugin.getProxy().getPluginManager().registerCommand(plugin, commandBungeeChatPlus);

        aliases = plugin.config.getStringList("pmConversationCommandAliases");
        if (aliases == null || aliases.isEmpty()) aliases = Arrays.asList("chat", "conversation");
        commandConversation = new CommandConversation(plugin, aliases.get(0), null, aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1]));
        plugin.getProxy().getPluginManager().registerCommand(plugin, commandConversation);

        aliases = plugin.config.getStringList("filterCommandAliases");
        if (aliases == null || aliases.isEmpty()) aliases = Arrays.asList("filter", "filterchat");
        commandFilter = new CommandFilter(plugin, aliases.get(0), null, aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1]));
        plugin.getProxy().getPluginManager().registerCommand(plugin, commandFilter);

        aliases = plugin.config.getStringList("filterListCommandAliases");
        if (aliases == null || aliases.isEmpty()) aliases = Arrays.asList("filterlist", "filterchatlist");
        commandFilterList = new CommandFilterList(plugin, aliases.get(0), null, aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1]));
        plugin.getProxy().getPluginManager().registerCommand(plugin, commandFilterList);

        aliases = plugin.config.getStringList("globalChatCommandAliases");
        if (aliases == null || aliases.isEmpty()) aliases = Arrays.asList("global", "g");
        commandGlobalChat = new CommandGlobalChat(plugin, aliases.get(0), null, aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1]));
        plugin.getProxy().getPluginManager().registerCommand(plugin, commandGlobalChat);

        aliases = plugin.config.getStringList("ignoreCommandAliases");
        if (aliases == null || aliases.isEmpty()) aliases = Arrays.asList("ignore", "ignoreplayer");
        commandIgnore = new CommandIgnore(plugin, aliases.get(0), null, aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1]));
        plugin.getProxy().getPluginManager().registerCommand(plugin, commandIgnore);

        aliases = plugin.config.getStringList("muteListCommandAliases");
        if (aliases == null || aliases.isEmpty())
            aliases = Arrays.asList("mutelist", "bungeemutelist", "listmuted", "bungeelistmuted");
        commandListMuted = new CommandListMuted(plugin, aliases.get(0), null, aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1]));
        plugin.getProxy().getPluginManager().registerCommand(plugin, commandListMuted);

        aliases = plugin.config.getStringList("localChatCommandAliases");
        if (aliases == null || aliases.isEmpty()) aliases = Arrays.asList("local", "l");
        commandLocalChat = new CommandLocalChat(plugin, aliases.get(0), null, aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1]));
        plugin.getProxy().getPluginManager().registerCommand(plugin, commandLocalChat);

        aliases = plugin.config.getStringList("pmCommandAliases");
        if (aliases == null || aliases.isEmpty())
            aliases = Arrays.asList("w", "msg", "message", "tell", "whisper", "pm");
        commandMessage = new CommandMessage(plugin, aliases.get(0), null, aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1]));
        plugin.getProxy().getPluginManager().registerCommand(plugin, commandMessage);

        aliases = plugin.config.getStringList("muteCommandAliases");
        if (aliases == null || aliases.isEmpty()) aliases = Arrays.asList("mute", "bungeemute");
        commandMute = new CommandMute(plugin, aliases.get(0), "bungeechatplus.mute.mute", aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1]));
        plugin.getProxy().getPluginManager().registerCommand(plugin, commandMute);

        aliases = plugin.config.getStringList("reloadCommandAliases");
        if (aliases == null || aliases.isEmpty()) aliases = Arrays.asList("bcpreload", "bungeechatplusreload");
        commandReload = new CommandReload(plugin, aliases.get(0), "bungeechatplus.reload", aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1]));
        plugin.getProxy().getPluginManager().registerCommand(plugin, commandReload);

        aliases = plugin.config.getStringList("pmReplyCommandAliases");
        if (aliases == null || aliases.isEmpty()) aliases = Arrays.asList("reply", "r");
        commandReply = new CommandReply(plugin, aliases.get(0), null, aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1]));
        plugin.getProxy().getPluginManager().registerCommand(plugin, commandReply);

        aliases = plugin.config.getStringList("muteTempMuteCommandAliases");
        if (aliases == null || aliases.isEmpty()) aliases = Arrays.asList("tempmute", "bungeetempmute");
        commandTempMute = new CommandTempMute(plugin, aliases.get(0), "bungeechatplus.mute.tempmute", aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1]));
        plugin.getProxy().getPluginManager().registerCommand(plugin, commandTempMute);

        aliases = plugin.config.getStringList("toggleChatCommandAliases");
        if (aliases == null || aliases.isEmpty()) aliases = Arrays.asList("togglechat", "chattoggle");
        commandToggleChat = new CommandToggleChat(plugin, aliases.get(0), null, aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1]));
        plugin.getProxy().getPluginManager().registerCommand(plugin, commandToggleChat);

        aliases = plugin.config.getStringList("muteUnmuteCommandAliases");
        if (aliases == null || aliases.isEmpty()) aliases = Arrays.asList("unmute", "bungeeunmute");
        commandUnMute = new CommandUnMute(plugin, aliases.get(0), "bungeechatplus.mute.unmute", aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1]));
        plugin.getProxy().getPluginManager().registerCommand(plugin, commandUnMute);

        commandsLoaded = true;
    }
}
