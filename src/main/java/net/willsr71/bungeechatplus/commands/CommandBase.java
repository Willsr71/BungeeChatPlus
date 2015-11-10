package net.willsr71.bungeechatplus.commands;

import net.willsr71.bungeechatplus.BungeeChatPlus;

import java.util.Arrays;
import java.util.List;

public class CommandBase {
    public CommandBungeeChatPlus commandBungeeChatPlus;
    public CommandConversation commandConversation;
    public CommandFilter commandFilter;
    public CommandGlobalChat commandGlobalChat;
    public CommandIgnore commandIgnore;
    public CommandListMuted commandListMuted;
    public CommandLocalChat commandLocalChat;
    public CommandMessage commandMessage;
    public CommandMute commandMute;
    public CommandReload commandReload;
    public CommandReply commandReply;
    public CommandToggleChat commandToggleChat;
    public CommandUnMute commandUnMute;
    public boolean commandsLoaded = false;
    private BungeeChatPlus plugin;

    public CommandBase(BungeeChatPlus plugin) {
        this.plugin = plugin;
    }

    public void reloadCommands() {
        List<String> aliases;

        aliases = plugin.config.getStringList("bcpCommandAliases");
        if (aliases == null || aliases.isEmpty()) aliases = Arrays.asList("bcp", "bungeechatplus");
        commandBungeeChatPlus = new CommandBungeeChatPlus(plugin, aliases.get(0), null, aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1]));

        aliases = plugin.config.getStringList("reloadCommandAliases");
        if (aliases == null || aliases.isEmpty()) aliases = Arrays.asList("bcpreload", "bungeechatplusreload");
        commandReload = new CommandReload(plugin, aliases.get(0), "bungeechatplus.reload", aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1]));

        aliases = plugin.config.getStringList("toggleChatCommandAliases");
        if (aliases == null || aliases.isEmpty()) aliases = Arrays.asList("togglechat", "chattoggle");
        commandToggleChat = new CommandToggleChat(plugin, aliases.get(0), null, aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1]));

        aliases = plugin.config.getStringList("globalChatCommandAliases");
        if (aliases == null || aliases.isEmpty()) aliases = Arrays.asList("global", "g");
        commandGlobalChat = new CommandGlobalChat(plugin, aliases.get(0), null, aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1]));

        aliases = plugin.config.getStringList("localChatCommandAliases");
        if (aliases == null || aliases.isEmpty()) aliases = Arrays.asList("local", "l");
        commandLocalChat = new CommandLocalChat(plugin, aliases.get(0), null, aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1]));

        aliases = plugin.config.getStringList("pmCommandAliases");
        if (aliases == null || aliases.isEmpty())
            aliases = Arrays.asList("w", "msg", "message", "tell", "whisper", "pm");
        commandMessage = new CommandMessage(plugin, aliases.get(0), null, aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1]));

        aliases = plugin.config.getStringList("pmReplyCommandAliases");
        if (aliases == null || aliases.isEmpty()) aliases = Arrays.asList("reply", "r");
        commandReply = new CommandReply(plugin, aliases.get(0), null, aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1]));

        aliases = plugin.config.getStringList("pmConversationCommandAliases");
        if (aliases == null || aliases.isEmpty()) aliases = Arrays.asList("chat", "conversation");
        commandConversation = new CommandConversation(plugin, aliases.get(0), null, aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1]));

        aliases = plugin.config.getStringList("muteCommandAliases");
        if (aliases == null || aliases.isEmpty()) aliases = Arrays.asList("mute", "bungeemute");
        commandMute = new CommandMute(plugin, aliases.get(0), null, aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1]));

        aliases = plugin.config.getStringList("muteUnmuteCommandAliases");
        if (aliases == null || aliases.isEmpty()) aliases = Arrays.asList("unmute", "bungeeunmute");
        commandUnMute = new CommandUnMute(plugin, aliases.get(0), null, aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1]));

        aliases = plugin.config.getStringList("muteListCommandAliases");
        if (aliases == null || aliases.isEmpty())
            aliases = Arrays.asList("mutelist", "bungeemutelist", "listmuted", "bungeelistmuted");
        commandListMuted = new CommandListMuted(plugin, aliases.get(0), null, aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1]));

        aliases = plugin.config.getStringList("ignoreCommandAliases");
        if (aliases == null || aliases.isEmpty()) aliases = Arrays.asList("ignore", "ignoreplayer");
        commandIgnore = new CommandIgnore(plugin, aliases.get(0), null, aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1]));

        aliases = plugin.config.getStringList("filterCommandAliases");
        if (aliases == null || aliases.isEmpty()) aliases = Arrays.asList("filter", "filterchat");
        commandFilter = new CommandFilter(plugin, aliases.get(0), null, aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1]));

        commandsLoaded = true;
    }
}
