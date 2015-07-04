package net.willsr71.bungeechatplus.bukkit;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {

    BungeeChatPlusBukkit plugin;

    public VaultHook(BungeeChatPlusBukkit plugin) {
        this.plugin = plugin;
        setupPermissions();
        setupChat();
        setupEconomy();
    }

    public static Permission permission = null;
    public static Economy economy = null;
    public static Chat chat = null;

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> permissionProvider = Bukkit.
                getServer().getServicesManager().getRegistration(
                net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> chatProvider = Bukkit.getServer().
                getServicesManager().getRegistration(
                net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
        }

        return (chat != null);
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().
                getServicesManager().getRegistration(
                net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }

    public void refresh() {
        setupChat();
        setupEconomy();
        setupPermissions();
    }

    public String getGroup(Player player) {
        try {
            if (permission != null && permission.getPrimaryGroup(player) != null) {
                return permission.getPrimaryGroup(player);
            }
        } catch (UnsupportedOperationException ignored) {
        }
        return "unknown";
    }

    public String getPrefix(Player player) {
        if (chat != null) {
            return chat.getPlayerPrefix(player);
        }
        return "unknown";
    }

    public String getSuffix(Player player) {
        if (chat != null) {
            return chat.getPlayerSuffix(player);
        }
        return "unknown";
    }

    public String getBalance(Player player) {
        if (economy != null) {
            return Double.toString(economy.getBalance(player.getName()));
        }
        return "-";
    }

    public String getCurrencyName() {
        if (economy != null) {
            return economy.currencyNameSingular();
        }
        return "$";
    }

    public String getCurrencyNamePl() {
        if (economy != null) {
            return economy.currencyNamePlural();
        }
        return "$";
    }
}
