package net.willsr71.bungeechatplus.bukkit;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.logging.Level;

public class VaultHook {
    public static Permission permission = null;
    public static Chat chat = null;
    BungeeChatPlusBukkit plugin;

    public VaultHook(BungeeChatPlusBukkit plugin) {
        this.plugin = plugin;
        refresh();
    }

    public void refresh() {
        setupChat();
        setupPermissions();
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> chatProvider = Bukkit.getServer().getServicesManager().getRegistration(Chat.class);
        if (chatProvider == null) {
            plugin.getLogger().log(Level.SEVERE, "Error getting chat hook. Is vault installed?");
            return false;
        }
        chat = chatProvider.getProvider();
        return true;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
        if (permissionProvider == null) {
            plugin.getLogger().log(Level.SEVERE, "Error getting Permissions hook. Is vault installed?");
            return false;
        }
        permission = permissionProvider.getProvider();
        return true;
    }

    public String getGroup(Player player) {
        if (permission == null) return "ERR";
        return permission.getPrimaryGroup(player);
    }

    public String getPrefix(Player player) {
        if (chat == null) return "ERR";
        return chat.getPlayerPrefix(player);
    }

    public String getSuffix(Player player) {
        if (chat == null) return "ERR";
        return chat.getPlayerSuffix(player);
    }
}
