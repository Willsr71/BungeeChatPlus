package net.willsr71.bungeechatplus.bukkit;

import lombok.SneakyThrows;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.*;
import java.util.logging.Level;

public class BungeeChatPlusBukkit extends JavaPlugin implements Listener {

    VaultHook vaultHook = null;

    @Override
    public void onEnable() {

        getServer().getMessenger().registerOutgoingPluginChannel(this,
                Constants.channel);
        getServer().getMessenger().registerIncomingPluginChannel(this,
                Constants.channel, new PluginMessageListener() {

                    @Override
                    @SneakyThrows
                    public void onPluginMessageReceived(String string, Player player, byte[] bytes) {
                        DataInputStream in = new DataInputStream(
                                new ByteArrayInputStream(bytes));
                        try {
                            String subchannel = in.readUTF();
                            if (subchannel.equalsIgnoreCase(Constants.subchannel_chatMsg)) {
                                String text = in.readUTF();
                                String prefix = in.readUTF();
                                int id = in.readInt();
                                boolean allowBBCode = in.readBoolean();
                                processChatMessage(player, text, prefix, id, allowBBCode);
                            }
                            if (subchannel.equalsIgnoreCase(Constants.subchannel_playSound)) {
                                player.playSound(player.getLocation(), Sound.valueOf(in.readUTF()), 5, 1);
                            }
                        }catch (IOException e){
                            e.printStackTrace();
                        }

                    }
                });
        getServer().getPluginManager().registerEvents(this, this);

        // check for vault hook
        Plugin vault = getServer().getPluginManager().getPlugin("Vault");
        if (vault != null) {
            getLogger().info("hooked Vault");
            vaultHook = new VaultHook(this);
        }

        // Enable Metrics
        try {
            BukkitMetrics metrics = new BukkitMetrics(this);
            metrics.start();
            getLogger().log(Level.INFO, "Enabled Bungee Metrics");
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Error enabling Bungee Metrics", e);
        }
    }

    @SneakyThrows
    private void processChatMessage(Player player, String text, String prefix, int id, boolean allowBBCode) {
        if (vaultHook != null) {
            vaultHook.refresh();
        }
        if (vaultHook != null && text.contains("%" + prefix + "group%")) {
            text = text.replace("%" + prefix + "group%", wrapVariable(vaultHook.getGroup(player), allowBBCode));
        }
        if (vaultHook != null && text.contains("%" + prefix + "prefix%")) {
            text = text.replace("%" + prefix + "prefix%", wrapVariable(vaultHook.getPrefix(player), allowBBCode));
        }
        if (vaultHook != null && text.contains("%" + prefix + "suffix%")) {
            text = text.replace("%" + prefix + "suffix%", wrapVariable(vaultHook.getSuffix(player), allowBBCode));
        }
        if (vaultHook != null && text.contains("%" + prefix + "balance%")) {
            text = text.replace("%" + prefix + "balance%", wrapVariable(vaultHook.getBalance(player), allowBBCode));
        }
        if (vaultHook != null && text.contains("%" + prefix + "currency%")) {
            text = text.replace("%" + prefix + "currency%", wrapVariable(vaultHook.getCurrencyName(), allowBBCode));
        }
        if (vaultHook != null && text.contains("%" + prefix + "currencyPl%")) {
            text = text.replace("%" + prefix + "currencyPl%", wrapVariable(vaultHook.getCurrencyNamePl(), allowBBCode));
        }
        if (text.contains("%" + prefix + "tabName%")) {
            text = text.replace("%" + prefix + "tabName%", wrapVariable(player.getPlayerListName(), allowBBCode));
        }
        if (text.contains("%" + prefix + "displayName%")) {
            text = text.replace("%" + prefix + "displayName%", wrapVariable(player.getDisplayName(), allowBBCode));
        }
        if (text.contains("%" + prefix + "world%")) {
            text = text.replace("%" + prefix + "world%", wrapVariable(player.getWorld().getName(), allowBBCode));
        }
        if (text.contains("%" + prefix + "health%")) {
            text = text.replace("%" + prefix + "health%", wrapVariable(Double.toString(player.getHealth()), allowBBCode));
        }
        if (text.contains("%" + prefix + "level%")) {
            text = text.replace("%" + prefix + "level%", wrapVariable(Integer.toString(player.getLevel()), allowBBCode));
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream1 = new DataOutputStream(outputStream);
        try {
            outputStream1.writeUTF(Constants.subchannel_chatMsg);
            outputStream1.writeInt(id);
            outputStream1.writeUTF(text);
            outputStream1.flush();
            outputStream1.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        player.sendPluginMessage(this, Constants.channel, outputStream.toByteArray());
    }

    public String wrapVariable(String variable, boolean allowBBCode) {
        if (allowBBCode) {
            return variable;
        } else {
            return "[nobbcode]" + variable + "[/nobbcode]";
        }
    }
}