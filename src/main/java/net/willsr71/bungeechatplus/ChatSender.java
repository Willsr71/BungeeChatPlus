package net.willsr71.bungeechatplus;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

import java.util.logging.Level;

public class ChatSender {
    BungeeChatPlus plugin;

    public void sendGlobalChatMessage(ProxiedPlayer player, String message) {
        try {
            if(plugin.checkMuted(player)) return;
            if(plugin.checkSpam(player)) return;
            message = plugin.preparePlayerChat(message, player);
            message = plugin.replaceRegex(message);
            message = plugin.applyTagLogic(message);

            // filter chat
            boolean isCapsing = plugin.isUsingCaps(message);
            if(plugin.config.getBoolean("antiCapsEnabled")) {
                if (isCapsing && plugin.config.getBoolean("antiCapsAutoLowercase")) {
                    message = message.toLowerCase();
                }
            }
            if(plugin.config.getBoolean("antiSwearEnabled")){
                message = plugin.filterSwears(message);
            }

            String text = plugin.config.getString("chatFormat");
            text = replaceVars(player, text, message);
            try {
                text = plugin.bukkitBridge.replaceVariables(player, text, "");
            }catch (Exception e){
                player.sendMessage(ChatParser.parse("&cChat formatting failed. Reverting to backup."));
                text = plugin.config.getString("backupChatFormat");
                text = replaceVars(player, text, message);
            }

            // broadcast message
            BaseComponent[] msg = ChatParser.parse(text);
            for (ProxiedPlayer target : plugin.getProxy().getPlayers()) {
                if (plugin.ignoredPlayers.get(target.getName()) != null && plugin.ignoredPlayers.get(target.getName()).contains(player.getName()))
                    continue;
                Server server = target.getServer();
                if (server == null || !plugin.excludedServers.contains(server.getInfo().getName())) {
                    if(plugin.localPlayers.contains(target.getName())){
                        if(player.getServer().getInfo().getName().equals(target.getServer().getInfo().getName()) || player.hasPermission("bungeechatplus.forceglobalchat")){
                            target.sendMessage(msg);
                        }
                    }else {
                        target.sendMessage(msg);
                    }
                    //player.sendMessage(ChatParser.parse(target.getName() + " -> " + player.getName() + " = " + (player.getServer().getInfo().getName().equals(target.getServer().getInfo().getName()))));
                }
            }
            if(plugin.config.getBoolean("logChat", false)){
                plugin.getProxy().getLogger().info(player.getName() + ": " + message);
            }
            if(plugin.config.getBoolean("antiCapsEnabled") && isCapsing){
                player.sendMessage(ChatParser.parse(plugin.config.getString("antiCapsMessage")));
            }
        } catch (Throwable th) {
            try {
                player.sendMessage(ChatParser.parse(plugin.config.getString("internalError")));
            } catch (Throwable ignored) {
                // maybe the player is offline?
            }
            plugin.getLogger().log(Level.SEVERE, "Error while processing chat message", th);
        }
    }

    public void sendGlobalConsoleChatMessage(String message) {
        try {
            message = plugin.replaceRegex(message);
            message = plugin.applyTagLogic(message);

            // replace variables
            String text = plugin.config.getString("chatFormat").replace("%player%", plugin.config.getString("consoleName", "SERVER"));
            text = text.replace("%message%", message);
            text = text.replaceAll("%(group|prefix|suffix|balance|currency|currencyPl|tabName|displayName|world|health|level)%", "");

            // broadcast message
            BaseComponent[] msg = ChatParser.parse(text);
            for (ProxiedPlayer target : plugin.getProxy().getPlayers()) {
                Server server = target.getServer();
                if (server == null || !plugin.excludedServers.contains(server.getInfo().getName())) {
                    target.sendMessage(msg);
                }
            }
            if(plugin.config.getBoolean("logChat", false)){
                plugin.getProxy().getLogger().info(plugin.config.getString("consoleName", "SERVER") + ": " + message);
            }
        } catch (Throwable th) {
            plugin.getLogger().log(Level.SEVERE, "Error while processing chat message", th);
        }
    }

    public void sendPrivateMessage(String text, ProxiedPlayer target, ProxiedPlayer player) {
        if (plugin.checkSpam(player)) return;
        // check ignored
        if (plugin.ignoredPlayers.get(target.getName()) != null && plugin.ignoredPlayers.get(target.getName()).contains(player.getName())) {
            text = plugin.config.getString("ignored").replace(
                    "%target%", plugin.wrapVariable(target.getName()));
            player.sendMessage(ChatParser.parse(text));
            return;
        }

        text = plugin.preparePlayerChat(text, player);
        text = plugin.replaceRegex(text);

        player.sendMessage(ChatParser.parse(
                plugin.bukkitBridge.replaceVariables(target, plugin.bukkitBridge.replaceVariables(player, plugin.config.getString("pmSend").replace(
                        "%target%", plugin.wrapVariable(target.
                                getDisplayName())).replace(
                        "%player%", plugin.wrapVariable(player.
                                getDisplayName())).replace(
                        "%message%", text), ""), "t")));

        target.sendMessage(ChatParser.parse(
                plugin.bukkitBridge.replaceVariables(target, plugin.bukkitBridge.replaceVariables(player, plugin.config.getString("pmReceive").replace(
                        "%target%", plugin.wrapVariable(target.
                                getDisplayName())).replace(
                        "%player%", plugin.wrapVariable(player.
                                getDisplayName())).replace(
                        "%message%", text), ""), "t")));

        plugin.replyTarget.put(target.getName(), player.getName());

        if (plugin.config.getBoolean("playSoundPrivateMessage", true)) {
            plugin.bukkitBridge.playSound(target, plugin.config.getString("pmSound"));
        }
    }

    public String replaceVars(ProxiedPlayer player, String format, String message){
        ServerInfo serverInfo = player.getServer().getInfo();

        String type;
        if (plugin.localPlayers.contains(player.getName())) {
            type = plugin.config.getString("varLocalChat");
        }else{
            type = plugin.config.getString("varGlobalChat");
        }

        String forced;
        if (player.hasPermission("bungeechatplus.forceglobalchat")) {
            forced = plugin.config.getString("varForcedGlobal");
        }else{
            forced = plugin.config.getString("varNotForced");
        }

        format = format.replace("%player%", plugin.wrapVariable(player.getDisplayName()));
        format = format.replace("%message%", message);
        format = format.replace("%server%", serverInfo.getName());
        format = format.replace("%server-players%", serverInfo.getPlayers().size() + "");
        format = format.replace("%server-motd%", serverInfo.getMotd());
        format = format.replace("%type%", type);
        format = format.replace("%forced%", forced);
        return format;
    }
}
