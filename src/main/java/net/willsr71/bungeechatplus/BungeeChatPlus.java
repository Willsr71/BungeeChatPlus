package net.willsr71.bungeechatplus;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import lombok.SneakyThrows;
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
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import net.willsr71.bungeechatplus.bukkit.Constants;
import net.willsr71.bungeechatplus.commands.*;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BungeeChatPlus extends Plugin implements Listener {
    public final Map<String, String> replyTarget = new HashMap<>();
    public final Map<String, String> persistentConversations = new HashMap<>();
    public final Map<String, List<String>> ignoredPlayers = new HashMap<>();
    public final Map<String, AntiSpamData> spamDataMap = new HashMap<>();
    public Configuration config;
    public Configuration playerLists;
    public static BungeeChatPlus instance;

    public List<String> forceGlobalChatPlayers = new ArrayList<>();
    public List<String> localPlayers = new ArrayList<>();
    public MuteData mutedPlayers = new MuteData();
    public List<String> excludedServers = new ArrayList<>();
    public List<String> swearList = new ArrayList<>();

    public BukkitBridge bukkitBridge;

    @Override
    public void onEnable() {
        instance = this;

        saveResource("config.yml");
        saveResource("playerLists.yml");

        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new InputStreamReader(new FileInputStream(new File(getDataFolder(), "config.yml")), Charsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            playerLists = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new InputStreamReader(new FileInputStream(new File(getDataFolder(), "playerLists.yml")), Charsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            reloadConfig();
        }catch (FileNotFoundException e){
            sendGlobalConsoleChatMessage("&4Failed to load BungeeChatPlus Configs");
            getLogger().log(Level.SEVERE, "Failed for load BungeeChatPlus Configs");
        }

        /*if (config.getStringList("excludeServers") != null) {
            excludedServers = config.getStringList("excludeServers");
        }
        */

        getProxy().registerChannel(Constants.channel);
        bukkitBridge = new BukkitBridge(this);
        bukkitBridge.enable();

        super.getProxy().getPluginManager().registerListener(this, this);
        List<String> aliases;

        if(config.getBoolean("adminEnabled", true)){
            aliases = config.getStringList("adminCommandAliases");
            if (aliases == null || aliases.isEmpty()) aliases = Arrays.asList("bungeechatplus", "bcp");
            super.getProxy().getPluginManager().registerCommand(this,
                    new CommandReload(this, aliases.get(0), "bungeechatplus.admin", aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1])));
        }

        if(config.getBoolean("toggleChatCommandEnabled", true)){
            aliases = config.getStringList("toggleChatCommandAliases");
            if (aliases == null || aliases.isEmpty()) aliases = Arrays.asList("togglechat", "chattoggle");
            super.getProxy().getPluginManager().registerCommand(this,
                    new CommandToggleChat(this, aliases.get(0), null, aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1])));
        }

        if(config.getBoolean("globalChatCommandEnabled", true)){
            aliases = config.getStringList("globalChatCommandAliases");
            if (aliases == null || aliases.isEmpty()) aliases = Arrays.asList("global", "g");
            super.getProxy().getPluginManager().registerCommand(this,
                    new CommandGlobalChat(this, aliases.get(0), null, aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1])));
        }

        if(config.getBoolean("localChatCommandEnabled", true)){
            aliases = config.getStringList("localChatCommandAliases");
            if (aliases == null || aliases.isEmpty()) aliases = Arrays.asList("local", "l");
            super.getProxy().getPluginManager().registerCommand(this,
                    new CommandLocalChat(this, aliases.get(0), null, aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1])));
        }

        if(config.getBoolean("pmEnabled", true)){
            aliases = config.getStringList("pmCommandAliases");
            if (aliases == null || aliases.isEmpty()) aliases = Arrays.asList("w", "msg", "message", "tell", "whisper");
            super.getProxy().getPluginManager().registerCommand(this,
                    new CommandMessage(this, aliases.get(0), null, aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1])));

            aliases = config.getStringList("pmReplyCommandAliases");
            if (aliases == null || aliases.isEmpty()) aliases = Arrays.asList("reply", "r");
            super.getProxy().getPluginManager().registerCommand(this,
                    new CommandReply(this, aliases.get(0), null, aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1])));

            aliases = config.getStringList("pmConversationCommandAliases");
            if (aliases == null || aliases.isEmpty()) aliases = Arrays.asList("chat", "conversation");
            super.getProxy().getPluginManager().registerCommand(this,
                    new CommandConversation(this, aliases.get(0), null, aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1])));
        }

        if(config.getBoolean("muteEnabled", true)){
            aliases = config.getStringList("muteCommandAliases");
            if (aliases == null || aliases.isEmpty()) aliases = Arrays.asList("mute", "bungeemute");
            super.getProxy().getPluginManager().registerCommand(this,
                    new CommandMute(this, aliases.get(0), "bungeechatplus.mute", aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1])));

            aliases = config.getStringList("muteUnmuteCommandAliases");
            if (aliases == null || aliases.isEmpty()) aliases = Arrays.asList("unmute", "bungeeunmute");
            super.getProxy().getPluginManager().registerCommand(this,
                    new CommandUnMute(this, aliases.get(0), "bungeechatplus.mute", aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1])));

            aliases = config.getStringList("muteListCommandAliases");
            if (aliases == null || aliases.isEmpty()) aliases = Arrays.asList("mutelist", "bungeemutelist", "listmuted", "bungeelistmuted");
            super.getProxy().getPluginManager().registerCommand(this,
                    new CommandListMuted(this, aliases.get(0), null, aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1])));
        }

        if(config.getBoolean("ignoreEnabled", true)){
            aliases = config.getStringList("ignoreCommandAliases");
            if (aliases == null || aliases.isEmpty()) aliases = Arrays.asList("ignore", "ignoreplayer");
            super.getProxy().getPluginManager().registerCommand(this,
                    new CommandIgnore(this, aliases.get(0), null, aliases.subList(1, aliases.size()).toArray(new String[aliases.size() - 1])));
        }

        // Enable Metrics
        try {
            BungeeMetrics metrics = new BungeeMetrics(this);
            metrics.start();
            getLogger().log(Level.INFO, "Enabled Bungee Metrics");
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Error enabling Bungee Metrics", e);
        }
    }

    /**
     * Checks whether a player is spamming
     *
     * @param player the player
     * @return true if chat should be cancelled
     */
    public boolean checkSpam(ProxiedPlayer player) {
        if (!config.getBoolean("enableAntiSpam", true)) return false;
        String name = player.getName();
        if (!spamDataMap.containsKey(name)) {
            spamDataMap.put(name, new AntiSpamData());
        }
        AntiSpamData antiSpamData = spamDataMap.get(name);
        if (antiSpamData.isSpamming()) {
            player.sendMessage(ChatParser.parse(config.getString("antiSpamDenyMessage")));
            return true;
        }
        return false;
    }

    public boolean checkMuted(ProxiedPlayer player){
        if(!config.getBoolean("enableMute", true)) return false;

        String name = player.getName();
        if(mutedPlayers.isMuted(name)){
            String text = config.getString("muteDenyMessage");
            text = text.replace("%reason%", wrapVariable(mutedPlayers.getReason(player.getName())));
            text = text.replace("%duration%", wrapVariable(mutedPlayers.getDuration(player.getName())));
            player.sendMessage(ChatParser.parse(text));
            return true;
        }
        return false;
    }

    public boolean isUsingCaps(String text){
        boolean isUsingCaps;
        if(text.length() > config.getInt("antiCapsMinChatLength")) {
            int uppercase = 0;
            int total = 0;
            for (int x = 0; x < text.length(); x++) {
                int c = text.charAt(x);
                if (c > 64 && c < 91) {
                    uppercase = uppercase + 1;
                }
                total = total + 1;
            }
            //sendGlobalConsoleChatMessage(Math.floorDiv(uppercase * 100, total) + " > " + config.getInt("antiCapsActivationPercentage"));
            isUsingCaps = (Math.floorDiv(uppercase * 100, total) > config.getInt("antiCapsActivationPercentage"));
        }else{
            isUsingCaps = false;
        }
        return isUsingCaps;
    }

    public String filterSwears(String text){
        for(String blockedSwear : swearList){
            String replaceString = "";
            for(int x=0; x < blockedSwear.length(); x++){
                replaceString = replaceString + "*";
            }
            text = text.replace(blockedSwear, replaceString);
        }
        return text;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(final ChatEvent event) {
        // ignore canceled chat
        if (event.isCancelled()) return;

        if (!(event.getSender() instanceof ProxiedPlayer)) return;

        // ignore commands
        if (event.isCommand()) {
            return;
        }

        final ProxiedPlayer player = (ProxiedPlayer) event.getSender();

        if (persistentConversations.containsKey(player.getName())) {
            final ProxiedPlayer target = getProxy().getPlayer(persistentConversations.get(player.getName()));
            if (target != null) {
                getProxy().getScheduler().runAsync(this, new Runnable() {
                    @Override
                    public void run() {
                        sendPrivateMessage(event.getMessage(), target, player);
                    }
                });
                event.setCancelled(true);
                return;
            } else {
                player.sendMessage(ChatParser.parse(config.getString("unknownTarget").replace(
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

        getProxy().getScheduler().runAsync(this, new Runnable() {
            @Override
            public void run() {
                sendGlobalChatMessage(player, message);
            }
        });
    }

    public void endConversation(ProxiedPlayer player, boolean force) {
        if (force || persistentConversations.containsKey(player.getName())) {
            if(persistentConversations.containsKey(player.getName())) {
                player.sendMessage(ChatParser.parse(config.getString("pnConversationEndMessage").replace("%target%", wrapVariable(persistentConversations.get(player.getName())))));
                persistentConversations.remove(player.getName());
            } else {
                player.sendMessage(ChatParser.parse(config.getString("endConversationEndMessage").replace("%target%", "nobody")));
            }
        }
    }

    public String replaceVars(ProxiedPlayer player, String format, String message){
        ServerInfo serverInfo = player.getServer().getInfo();

        String type;
        if (localPlayers.contains(player.getName())) {
            type = config.getString("varLocalChat");
        }else{
            type = config.getString("varGlobalChat");
        }

        String forced;
        if (player.hasPermission("bungeechatplus.forceglobalchat")) {
            forced = config.getString("varForcedGlobal");
        }else{
            forced = config.getString("varNotForced");
        }

        format = format.replace("%player%", wrapVariable(player.getDisplayName()));
        format = format.replace("%message%", message);
        format = format.replace("%server%", serverInfo.getName());
        format = format.replace("%server-players%", serverInfo.getPlayers().size() + "");
        format = format.replace("%server-motd%", serverInfo.getMotd());
        format = format.replace("%type%", type);
        format = format.replace("%forced%", forced);
        return format;
    }

    public void tellOps(ProxiedPlayer player, String message){
        for (ProxiedPlayer target : getProxy().getPlayers()){
            if (!target.getName().equals(player.getName()) && forceGlobalChatPlayers.contains(target.getName())){
                target.sendMessage(ChatParser.parse(message));
            }
        }
    }

    public void sendGlobalChatMessage(ProxiedPlayer player, String message) {
        try {
            if(checkMuted(player)){
                tellOps(player, message);
                return;
            }
            if(checkSpam(player)){
                return;
            }
            message = preparePlayerChat(message, player);
            message = replaceRegex(message);
            message = applyTagLogic(message);

            // filter chat
            boolean isCapsing = isUsingCaps(message);
            if(config.getBoolean("antiCapsEnabled")) {
                if (isCapsing && config.getBoolean("antiCapsAutoLowercase")) {
                    message = message.toLowerCase();
                }
            }
            if(config.getBoolean("antiSwearEnabled")){
                message = filterSwears(message);
            }

            String text = config.getString("chatFormat");
            text = replaceVars(player, text, message);
            try {
                text = bukkitBridge.replaceVariables(player, text, "");
            }catch (Exception e){
                player.sendMessage(ChatParser.parse("&cChat formatting failed. Reverting to backup."));
                text = config.getString("backupChatFormat");
                text = replaceVars(player, text, message);
            }

            // broadcast message
            BaseComponent[] msg = ChatParser.parse(text);
            for (ProxiedPlayer target : getProxy().getPlayers()) {
                if (ignoredPlayers.get(target.getName()) != null && ignoredPlayers.get(target.getName()).contains(player.getName()))
                    continue;
                Server server = target.getServer();
                if (server == null || !excludedServers.contains(server.getInfo().getName())) {
                    if(localPlayers.contains(target.getName())){
                        if(player.getServer().getInfo().getName().equals(target.getServer().getInfo().getName()) || player.hasPermission("bungeechatplus.forceglobalchat")){
                            target.sendMessage(msg);
                        }
                    }else {
                        target.sendMessage(msg);
                    }
                    //player.sendMessage(ChatParser.parse(target.getName() + " -> " + player.getName() + " = " + (player.getServer().getInfo().getName().equals(target.getServer().getInfo().getName()))));
                }
            }
            if(config.getBoolean("logChat", false)){
                getProxy().getLogger().info(player.getName() + ": " + message);
            }
            if(config.getBoolean("antiCapsEnabled") && isCapsing){
                player.sendMessage(ChatParser.parse(config.getString("antiCapsMessage")));
            }
        } catch (Throwable th) {
            try {
                player.sendMessage(ChatParser.parse(config.getString("internalError")));
            } catch (Throwable ignored) {
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
            text = text.replaceAll("%(group|prefix|suffix|balance|currency|currencyPl|tabName|displayName|world|health|level)%", "");

            // broadcast message
            BaseComponent[] msg = ChatParser.parse(text);
            for (ProxiedPlayer target : getProxy().getPlayers()) {
                Server server = target.getServer();
                if (server == null || !excludedServers.contains(server.getInfo().getName())) {
                    target.sendMessage(msg);
                }
            }
            if(config.getBoolean("logChat", false)){
                getProxy().getLogger().info(config.getString("consoleName", "SERVER") + ": " + message);
            }
        } catch (Throwable th) {
            getLogger().log(Level.SEVERE, "Error while processing chat message", th);
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

    public ProxiedPlayer getReplyTarget(ProxiedPlayer player) {
        String t = replyTarget.get(player.getName());
        if (t == null) {
            return player;
        }
        return getProxy().getPlayer(t);
    }

    @SneakyThrows
    private void saveResource(String name) {
        if (!getDataFolder().exists())
            getDataFolder().mkdir();

        File file = new File(getDataFolder(), name);

        if (!file.exists()) {
            try {
                Files.copy(getResourceAsStream(name), file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        String commandLine = event.getCursor();
        if (!commandLine.startsWith("/")) return;
        if (!commandLine.startsWith("@")) return;
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
            text = ChatParser.stripBBCode(text);
        }
        return text;
    }

    public void reloadConfig() throws FileNotFoundException {
        config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new InputStreamReader(new FileInputStream(new File(getDataFolder(), "config.yml")), Charsets.UTF_8));
        playerLists = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new InputStreamReader(new FileInputStream(new File(getDataFolder(), "playerLists.yml")), Charsets.UTF_8));
        localPlayers = playerLists.getStringList("localPlayers");
        if(playerLists.get("mutedPlayers")==null) return;
        if(playerLists.getString("mutedPlayers").equals("None")) return;
        getLogger().log(Level.INFO, playerLists.get("mutedPlayers").toString());
        Configuration playerList = playerLists.getSection("mutedPlayers");
        for (String player : playerList.getKeys()) {
            mutedPlayers.setMuted(player, playerList.getString(player + ".reason"), playerList.getString(player + ".expire"));
        }
    }

    public void savePlayerLists(){
        playerLists.set("localPlayers", localPlayers);
        playerLists.set("mutedPlayers", null);
        ArrayList<String[]> mutedList = mutedPlayers.getMutedPlayerData();
        if(mutedList.size() > 0) {
            for (String[] muted : mutedList) {
                String player = muted[0];
                String reason = muted[1];
                String expire = muted[2];
                getLogger().log(Level.INFO, "mutedPlayers." + player + ".reason = " + reason);
                getLogger().log(Level.INFO, "mutedPlayers." + player + ".expire = " + expire);
                playerLists.set("mutedPlayers." + player + ".reason", reason);
                playerLists.set("mutedPlayers." + player + ".expire", expire);
            }
        }else{
            playerLists.set("mutedPlayers", "None");
        }
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(playerLists, new File(getDataFolder(), "playerLists.yml"));
        }catch (IOException e){
            getLogger().log(Level.SEVERE, "Error saving Player Lists.", e);
        }
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

    public void sendPrivateMessage(String text, ProxiedPlayer target, ProxiedPlayer player) {
        if (checkSpam(player)) {
            return;
        }
        // check ignored
        if (ignoredPlayers.get(target.getName()) != null && ignoredPlayers.get(target.getName()).contains(player.getName())) {
            text = config.getString("ignored").replace(
                    "%target%", wrapVariable(target.getName()));
            player.sendMessage(ChatParser.parse(text));
            return;
        }

        text = preparePlayerChat(text, player);
        text = replaceRegex(text);

        player.sendMessage(ChatParser.parse(
                bukkitBridge.replaceVariables(target, bukkitBridge.replaceVariables(player, config.getString("pmSend").replace(
                        "%target%", wrapVariable(target.
                                getDisplayName())).replace(
                        "%player%", wrapVariable(player.
                                getDisplayName())).replace(
                        "%message%", text), ""), "t")));

        target.sendMessage(ChatParser.parse(
                bukkitBridge.replaceVariables(target, bukkitBridge.replaceVariables(player, config.getString("pmReceive").replace(
                        "%target%", wrapVariable(target.
                                getDisplayName())).replace(
                        "%player%", wrapVariable(player.
                                getDisplayName())).replace(
                        "%message%", text), ""), "t")));

        replyTarget.put(target.getName(), player.getName());

        if (config.getBoolean("playSoundPrivateMessage", true)) {
            bukkitBridge.playSound(target, config.getString("pmSound"));
        }
    }

    public void startConversation(ProxiedPlayer player, ProxiedPlayer target) {
        persistentConversations.put(player.getName(), target.getName());
        player.sendMessage(ChatParser.parse(config.getString("pmConversationStartMessage").replace("%target%", wrapVariable(target.getName()))));
    }
}