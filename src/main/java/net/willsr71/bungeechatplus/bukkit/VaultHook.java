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
    public static Economy economy = null;
    public static Chat chat = null;
    BungeeChatPlusBukkit plugin;

    public VaultHook(BungeeChatPlusBukkit plugin) {
        this.plugin = plugin;
        refresh();
    }

    public void refresh() {
        setupChat();
        setupPermissions();
        setupEconomy();
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> chatProvider = Bukkit.getServer().getServicesManager().getRegistration(Chat.class);
        if (chatProvider == null) {
            plugin.getLogger().log(Level.SEVERE, "Error getting chat hook. Is vault installed?");
            return false;
        }
        chat = chatProvider.getProvider();
        plugin.getLogger().log(Level.INFO, "Chat hook: " + chatProvider.getProvider().getName());
        return true;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
        if (permissionProvider == null) {
            plugin.getLogger().log(Level.SEVERE, "Error getting Permissions hook. Is vault installed?");
            return false;
        }
        permission = permissionProvider.getProvider();
        plugin.getLogger().log(Level.INFO, "Permissions hook: " + permissionProvider.getProvider().getName());
        return true;
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider == null) {
            plugin.getLogger().log(Level.WARNING, "Error getting Economy hook.");
            return false;
        }
        economy = economyProvider.getProvider();
        plugin.getLogger().log(Level.INFO, "Economy hook: " + economyProvider.getProvider().getName());
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

    public String getBalance(Player player) {
        if (economy == null) return "0";
        return Double.toString(economy.getBalance(player.getName()));
    }

    public String getCurrencyName() {
        if (economy == null) return "$";
        return economy.currencyNameSingular();
    }

    public String getCurrencyNamePl() {
        if (economy == null) return "$";
        return economy.currencyNamePlural();
    }
}
