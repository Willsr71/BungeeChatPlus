package net.willsr71.bungeechatplus;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import net.willsr71.bungeechatplus.bukkit.Constants;
import net.willsr71.bungeechatplus.commands.CommandBase;
import org.mcstats.BungeeMetrics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BungeeChatPlus extends Plugin implements Listener {
    public static BungeeChatPlus instance;
    public final Map<String, String> replyTarget = new HashMap<>();
    public final Map<String, String> persistentConversations = new HashMap<>();
    public final Map<String, List<String>> ignoredPlayers = new HashMap<>();
    public final Map<String, AntiSpamData> spamDataMap = new HashMap<>();
    public Map<String, FilterData> filterDataMap = new HashMap<>();
    public List<String> localPlayers = new ArrayList<>();
    public List<String> silencedPlayers = new ArrayList<>();
    public MuteData mutedPlayers;
    public List<String> excludedServers = new ArrayList<>();
    public List<String> swearList = new ArrayList<>();
    public ChatParser chatParser = new ChatParser();
    public ConfigManager configManager;
    public ConfigManager playerListsManager;
    public Configuration config;
    public Configuration playerLists;
    public BukkitBridge bukkitBridge;
    public CommandBase commandBase;

    public static String version;
    public boolean debug = false;

    @Override
    public void onEnable() {
        instance = this;

        version = getDescription().getVersion();

        configManager = new ConfigManager(this, "config.yml");
        playerListsManager = new ConfigManager(this, "playerLists.yml");

        getProxy().getPluginManager().registerListener(this, this);
        getProxy().registerChannel(Constants.channel);
        bukkitBridge = new BukkitBridge(this);
        bukkitBridge.enable();

        mutedPlayers = new MuteData(this);

        commandBase = new CommandBase(this);

        reload();

        // Enable Metrics
        try {
            BungeeMetrics metrics = new BungeeMetrics(this);
            metrics.start();
            getLogger().log(Level.INFO, "Enabled Bungee Metrics");
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Error enabling Bungee Metrics", e);
        }
    }

    public void reload() {
        configManager.reloadConfig();
        playerListsManager.reloadConfig();
        config = configManager.getConfig();
        playerLists = playerListsManager.getConfig();
        String configVersion = config.getString("dontTouch.version.seriouslyThisWillEraseYourConfig");
        if (configVersion == null || !configVersion.equals(version)) {
            configManager.replaceConfig();
            config = configManager.getConfig();
        }

        debug = config.getBoolean("dontTouch.debug");
        if (debug) getLogger().log(Level.INFO, "Debug mode is ENABLED");
        else getLogger().log(Level.INFO, "Debug mode is DISABLED");

        excludedServers = config.getStringList("excludeServers");
        localPlayers = playerLists.getStringList("localPlayers");
        silencedPlayers = playerLists.getStringList("silencedPlayers");
        if (playerLists.get("mutedPlayers") != null && !playerLists.getString("mutedPlayers").equals("None")) {
            if (debug) getLogger().log(Level.INFO, playerLists.get("mutedPlayers").toString());
            Configuration playerList = playerLists.getSection("mutedPlayers");
            for (String player : playerList.getKeys()) {
                mutedPlayers.setMuted(player, playerList.getString(player + ".reason"), playerList.getLong(player + ".expire"));
            }
        }

        commandBase.reloadCommands();

        savePlayerLists();

        getLogger().info("Reloaded");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(final ChatEvent event) {
        // ignore canceled chat
        if (event.isCancelled()) return;
        if (!(event.getSender() instanceof ProxiedPlayer)) return;

        final ProxiedPlayer player = (ProxiedPlayer) event.getSender();

        // ignore commands
        if (event.isCommand()) {
            BCPLogger.logCommand(player, event.getMessage());
            return;
        }

        if (persistentConversations.containsKey(player.getName())) {
            final ProxiedPlayer target = getProxy().getPlayer(persistentConversations.get(player.getName()));
            if (target != null) {
                getProxy().getScheduler().runAsync(this, () -> sendPrivateMessage(event.getMessage(), target, player));
                event.setCancelled(true);
                return;
            } else {
                player.sendMessage(chatParser.parse(config.getString("unknownTarget").replace(
                        "%target%", wrapVariable(persistentConversations.get(player.getName())))));
                endConversation(player, true);
            }
        }

        // is this global chat?
        if (!config.getBoolean("alwaysGlobalChat", true)) return;

        if (excludedServers.contains(player.getServer().getInfo().getName())) return;

        // cancel event
        event.setCancelled(true);

        final String message = event.getMessage();

        getProxy().getScheduler().runAsync(this, () -> sendGlobalChatMessage(player, message));
    }

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        String commandLine = event.getCursor();
        if (!commandLine.startsWith("/")) return;
        if (commandLine.matches("^/(?:" + Joiner.on('|').join(Iterables.concat(config.getStringList("pmCommandAliases"),
                config.getStringList("pmConversationCommandAliases"))) + ").*$")) {
            event.getSuggestions().clear();
            String[] split = commandLine.split(" ");
            String begin = split[split.length - 1];
            for (ProxiedPlayer player : getProxy().getPlayers()) {
                if (player.getName().toLowerCase().contains(begin.toLowerCase()) || player.getDisplayName().toLowerCase().contains(begin.toLowerCase())) {
                    event.getSuggestions().add(player.getName());
                }
            }
        }
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        String name = event.getPlayer().getName();
        if (replyTarget.containsKey(name)) replyTarget.remove(name);
        if (ignoredPlayers.containsKey(name)) ignoredPlayers.remove(name);
        if (persistentConversations.containsKey(name)) persistentConversations.remove(name);
        if (spamDataMap.containsKey(name)) spamDataMap.remove(name);
    }

    public void sendGlobalChatMessage(ProxiedPlayer player, String message) {
        try {
            if (checkMuted(player)) return;
            if (checkSpam(player)) return;
            if (checkSilenced(player)) return;
            message = preparePlayerChat(message, player);
            message = replaceRegex(message);
            message = applyTagLogic(message);

            // filter caps
            boolean isCapsing = isUsingCaps(message);
            if (isCapsing && config.getBoolean("antiCapsAutoLowercase")) message = message.toLowerCase();

            // filter the other stuff
            if (config.getBoolean("antiSwearEnabled")) {
                message = filterSwears(message);
            }

            String text = config.getString("chatFormat");
            text = replaceVars(player, text, message);
            try {
                text = bukkitBridge.replaceVariables(player, text, "");
            } catch (Exception e) {
                player.sendMessage(chatParser.parse(config.getString("replaceVarError")));
                getLogger().log(Level.WARNING, "Failed to parse bukkit variables. Is BungeeChatPlus installed?", e);
                text = config.getString("backupChatFormat");
                text = replaceVars(player, text, message);
            }

            // broadcast message
            BaseComponent[] msg = chatParser.parse(text);
            for (ProxiedPlayer target : getProxy().getPlayers()) {
                if (silencedPlayers.contains(target.getName())) {
                    continue;
                }

                if (ignoredPlayers.get(target.getName()) != null && ignoredPlayers.get(target.getName()).contains(player.getName())) {
                    continue;
                }

                Server server = target.getServer();

                if (server == null || excludedServers.contains(server.getInfo().getName())) {
                    continue;
                }

                if (localPlayers.contains(target.getName())) {
                    if (player.getServer().getInfo().getName().equals(target.getServer().getInfo().getName()) || player.hasPermission("bungeechatplus.forceglobalchat")) {
                        target.sendMessage(msg);
                    }
                } else {
                    target.sendMessage(msg);
                }
            }
            BCPLogger.logChat(player, message);

            if (isCapsing) player.sendMessage(chatParser.parse(config.getString("antiCapsMessage")));
            autoReply(player, message);
        } catch (Throwable th) {
            try {
                player.sendMessage(chatParser.parse(config.getString("internalError")));
            } catch (Exception e) {
                e.printStackTrace();
                // maybe the player is offline?
            }
            getLogger().log(Level.SEVERE, "Error while processing chat message", th);
        }
    }

    public void sendGlobalConsoleChatMessage(String message) {
        try {
            message = replaceRegex(message);
            message = applyTagLogic(message);

            // replace variables
            String text = config.getString("chatFormat").replace("%player%", config.getString("consoleName", "SERVER"));
            text = text.replace("%message%", message);
            text = text.replaceAll("%(group|prefix|suffix|tabName|displayName)%", "");

            // broadcast message
            BaseComponent[] msg = chatParser.parse(text);
            for (ProxiedPlayer target : getProxy().getPlayers()) {
                Server server = target.getServer();
                if (server == null || !excludedServers.contains(server.getInfo().getName())) {
                    target.sendMessage(msg);
                }
            }
            if (config.getBoolean("logChat", false)) {
                getProxy().getLogger().info(config.getString("consoleName", "SERVER") + ": " + message);
            }
        } catch (Throwable th) {
            getLogger().log(Level.SEVERE, "Error while processing chat message", th);
        }
    }

    public void sendPrivateMessage(String text, ProxiedPlayer target, ProxiedPlayer player) {
        if (checkSpam(player)) return;
        // check ignored
        if (ignoredPlayers.get(target.getName()) != null && ignoredPlayers.get(target.getName()).contains(player.getName())) {
            text = config.getString("ignored").replace("%target%", wrapVariable(target.getName()));
            player.sendMessage(chatParser.parse(text));
            return;
        }

        text = preparePlayerChat(text, player);
        text = replaceRegex(text);

        player.sendMessage(chatParser.parse(replaceVars(player, target, config.getString("pmSend"), text)));
        target.sendMessage(chatParser.parse(replaceVars(player, target, config.getString("pmReceive"), text)));

        replyTarget.put(target.getName(), player.getName());

        if (config.getBoolean("playSoundPrivateMessage", true)) {
            bukkitBridge.playSound(target, config.getString("pmSound"));
        }
    }

    public String replaceVars(ProxiedPlayer player, ProxiedPlayer target, String format, String message) {
        format = format.replace("%sender-name%", wrapVariable(player.getDisplayName()));
        format = format.replace("%target-name%", wrapVariable(target.getDisplayName()));
        format = format.replace("%sender-server%", wrapVariable(player.getServer().getInfo().getName()));
        format = format.replace("%target-server%", wrapVariable(target.getServer().getInfo().getName()));
        format = format.replace("%message%", message);
        return format;
    }

    public String replaceVars(ProxiedPlayer player, String format, String message) {
        ServerInfo serverInfo = player.getServer().getInfo();

        String type;
        if (localPlayers.contains(player.getName())) {
            type = config.getString("varLocalChat");
        } else {
            type = config.getString("varGlobalChat");
        }

        String forced;
        if (player.hasPermission("bungeechatplus.forceglobalchat")) {
            forced = config.getString("varForcedGlobal");
        } else {
            forced = config.getString("varNotForced");
        }

        format = format.replace("%message%", message);
        format = format.replace("%player%", wrapVariable(player.getDisplayName()));
        format = format.replace("%name%", wrapVariable(player.getDisplayName()));
        format = format.replace("%server%", wrapVariable(serverInfo.getName()));
        format = format.replace("%type%", wrapVariable(type));
        format = format.replace("%forced%", wrapVariable(forced));
        return format;
    }

    public boolean checkSpam(ProxiedPlayer player) {
        if (!config.getBoolean("enableAntiSpam", true)) return false;
        String name = player.getName();
        if (!spamDataMap.containsKey(name)) {
            spamDataMap.put(name, new AntiSpamData());
        }
        AntiSpamData antiSpamData = spamDataMap.get(name);
        if (antiSpamData.isSpamming()) {
            player.sendMessage(chatParser.parse(config.getString("antiSpamDenyMessage")));
            return true;
        }
        return false;
    }

    public boolean checkMuted(ProxiedPlayer player) {
        if (!config.getBoolean("muteEnabled", true)) return false;

        String name = player.getName();
        if (mutedPlayers.isMuted(name)) {
            String text = config.getString("muteDenyMessage");
            text = text.replace("%reason%", wrapVariable(mutedPlayers.getReason(player.getName())));
            text = text.replace("%duration%", wrapVariable(DateUtils.formatDateDiff(mutedPlayers.getExpire(player.getName()))));
            player.sendMessage(chatParser.parse(text));
            return true;
        }
        return false;
    }

    public boolean checkSilenced(ProxiedPlayer player) {
        if (!config.getBoolean("silenceEnabled", true)) return false;

        if (silencedPlayers.contains(player.getName())) {
            player.sendMessage(chatParser.parse(config.getString("silenced")));
            return true;
        }
        return false;
    }

    public boolean isUsingCaps(String text) {
        if (!config.getBoolean("antiCapsEnabled")) return false;
        if (debug)
            getLogger().log(Level.INFO, text.length() + " <= " + config.getInt("antiCapsActivationLength") + " = " + (text.length() <= config.getInt("antiAntiCapsActivationLength")));
        if (text.length() <= config.getInt("antiCapsActivationLength")) return false;
        int uppercase = 0;
        int total = 0;
        for (int x = 0; x < text.length(); x++) {
            if (text.charAt(x) > 64 && text.charAt(x) < 91) {
                uppercase = uppercase + 1;
            }
            total = total + 1;
        }
        return (Math.floorDiv(uppercase * 100, total) > config.getInt("antiCapsActivationPercentage"));
    }

    public String filterSwears(String text) {
        for (String blockedSwear : swearList) {
            String replaceString = "";
            for (int x = 0; x < blockedSwear.length(); x++) {
                replaceString = replaceString + "*";
            }
            text = text.replace(blockedSwear, replaceString);
        }
        return text;
    }

    public void endConversation(ProxiedPlayer player, boolean force) {
        if (force || persistentConversations.containsKey(player.getName())) {
            if (persistentConversations.containsKey(player.getName())) {
                player.sendMessage(chatParser.parse(config.getString("pmConversationEndMessage").replace("%target%", wrapVariable(persistentConversations.get(player.getName())))));
                persistentConversations.remove(player.getName());
            } else {
                player.sendMessage(chatParser.parse(config.getString("pmConversationEndMessage").replace("%target%", "nobody")));
            }
        }
    }

    public ProxiedPlayer getReplyTarget(ProxiedPlayer player) {
        String t = replyTarget.get(player.getName());
        if (t == null) {
            return player;
        }
        return getProxy().getPlayer(t);
    }

    public void autoReply(ProxiedPlayer player, String message) {
        List list = config.getList("autoreply");
        if (list == null) return;
        for (Object entry : list) {
            Map map = (Map) entry;
            if (Pattern.compile((String) map.get("message")).matcher(message).find()) {
                player.sendMessage(chatParser.parse((String) map.get("reply")));
            }
        }
    }

    public String replaceRegex(String str) {
        List list = config.getList("regex");
        if (list == null) return str;
        for (Object entry : list) {
            Map map = (Map) entry;
            str = str.replaceAll(String.valueOf(map.get("search")), String.valueOf(map.get("replace")));
        }
        return str;
    }

    public String wrapVariable(String variable) {
        if (config.getBoolean("allowBBCodeInVariables", false)) {
            return variable;
        } else {
            return "[nobbcode]" + variable + "[/nobbcode]";
        }
    }

    public String preparePlayerChat(String text, ProxiedPlayer player) {
        if (!player.hasPermission("bungeechatplus.chat.color")) {
            text = ChatColor.translateAlternateColorCodes('&', text);
            text = ChatColor.stripColor(text);
        }
        if (!player.hasPermission("bungeechatplus.chat.bbcode")) {
            text = chatParser.stripBBCode(text);
        }
        return text;
    }

    public void savePlayerLists() {
        playerLists.set("localPlayers", localPlayers);
        playerLists.set("silencedPlayers", silencedPlayers);
        playerLists.set("mutedPlayers", null);
        ArrayList<MutedPlayer> mutedList = mutedPlayers.getMutedPlayerData();
        if (mutedList.size() > 0) {
            for (MutedPlayer player : mutedList) {
                if (debug)
                    getLogger().log(Level.INFO, "mutedPlayers." + player.getName() + ".reason = " + player.getReason());
                if (debug)
                    getLogger().log(Level.INFO, "mutedPlayers." + player.getName() + ".expire = " + player.getExpire());
                playerLists.set("mutedPlayers." + player.getName() + ".reason", player.getReason());
                playerLists.set("mutedPlayers." + player.getName() + ".expire", player.getExpire());
            }
        } else {
            playerLists.set("mutedPlayers", "None");
        }
        playerListsManager.saveConfig(playerLists);
    }

    public String applyTagLogic(String text) {
        if (!config.getBoolean("enableTaggingPlayers", true)) return text;
        Matcher matcher = Pattern.compile("@(?<name>[^ ]{1,16})").matcher(text);
        StringBuffer stringBuffer = new StringBuffer(text.length());
        while (matcher.find()) {
            String name = matcher.group("name");
            ProxiedPlayer taggedPlayer = getProxy().getPlayer(name);
            if (taggedPlayer != null) {
                matcher.appendReplacement(stringBuffer, config.getString("taggedPlayer"));
                if (config.getBoolean("playSoundToTaggedPlayer", true)) {
                    bukkitBridge.playSound(taggedPlayer, config.getString("playerTaggedSound"));
                }
            } else {
                matcher.appendReplacement(stringBuffer, "$0");
            }
        }
        matcher.appendTail(stringBuffer);
        return stringBuffer.toString();
    }

    public void startConversation(ProxiedPlayer player, ProxiedPlayer target) {
        persistentConversations.put(player.getName(), target.getName());
        player.sendMessage(chatParser.parse(config.getString("pmConversationStartMessage").replace("%target%", wrapVariable(target.getName()))));
    }

    public void sendCommandDisabled(String player, String command) {
        getProxy().getPlayer(player).sendMessage(chatParser.parse(config.getString("commandDisabled").replace("%command%", command)));
    }
}
