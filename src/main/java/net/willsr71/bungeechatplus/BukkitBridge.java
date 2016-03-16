package net.willsr71.bungeechatplus;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.willsr71.bungeechatplus.bukkit.Constants;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BukkitBridge implements Listener {

    BungeeChatPlus plugin;

    ConcurrentHashMap<Integer, String> buf = new ConcurrentHashMap<>();

    int cnt = 0;

    public BukkitBridge(BungeeChatPlus plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        plugin.getProxy().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (event.getTag().equals(Constants.channel)) {
            event.setCancelled(true);
            if (event.getReceiver() instanceof ProxiedPlayer && event.
                    getSender() instanceof Server) {
                try {
                    DataInputStream in = new DataInputStream(
                            new ByteArrayInputStream(event.getData()));

                    String subchannel = in.readUTF();

                    if (subchannel.equals(Constants.subchannel_chatMsg)) {
                        buf.put(in.readInt(), in.readUTF());
                    }

                } catch (IOException ex) {
                    plugin.getLogger().log(Level.SEVERE,
                            "Exception while parsing data from Bukkit", ex);
                }
            }
        }
    }

    public void playSound(ProxiedPlayer player, String sound) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            DataOutputStream outputStream1 = new DataOutputStream(outputStream);
            outputStream1.writeUTF(Constants.subchannel_playSound);
            outputStream1.writeUTF(sound);
            outputStream1.flush();
            outputStream1.close();
            player.getServer().sendData(Constants.channel, outputStream.toByteArray());
        } catch (IOException ex) {
            Logger.getLogger(BukkitBridge.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
    }

    public String replaceVariables(ProxiedPlayer player, String text, String prefix) {
        int tries = 0;
        while (text.matches("^.*%" + prefix + "(group|prefix|suffix|tabName|displayName)%.*$")
                && tries < 3) {
            try {
                int id = getId();
                if (buf.containsKey(id)) buf.remove(id);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                DataOutputStream outputStream1 = new DataOutputStream(outputStream);
                outputStream1.writeUTF(Constants.subchannel_chatMsg);
                outputStream1.writeUTF(text);
                outputStream1.writeUTF(prefix);
                outputStream1.writeInt(id);
                outputStream1.writeBoolean(plugin.config.getBoolean("allowBBCodeInVariables", false));
                outputStream1.flush();
                outputStream1.close();
                player.getServer().sendData(Constants.channel, outputStream.toByteArray());
                for (int i = 0; i < 10 && !buf.containsKey(id); i++) {
                    Thread.sleep(100);
                }
                if (buf.containsKey(id)) {
                    text = buf.get(id);
                    buf.remove(id);
                    tries = 0;
                    break;
                }
            } catch (Throwable th) {
                th.printStackTrace();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            tries++;
        }
        if (tries > 0) {
            throw new RuntimeException("Unable to process chat message from " + player.getName() + " make sure you have installed BungeeChatPlus on " + (player.getServer() != null ? player.getServer().getInfo().getName() : "(unknown server)"));
        }
        text = text.replace("%" + prefix + "server%", plugin.wrapVariable(player.getServer() != null ? player.getServer().getInfo().getName() : "unknown"));
        return text;
    }

    private int getId() {
        return cnt++;
    }
}
