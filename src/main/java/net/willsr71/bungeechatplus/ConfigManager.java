package net.willsr71.bungeechatplus;


import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.nio.file.Files;

public class ConfigManager {
    private BungeeChatPlus plugin;

    private String name;
    private File file;
    private Configuration config;

    public ConfigManager(BungeeChatPlus plugin, String name){
        this.plugin = plugin;
        this.name = name;
        File dataFolder = plugin.getDataFolder();
        if(!dataFolder.exists()) dataFolder.mkdirs();

        file = new File(dataFolder, name);
        if(!file.exists()) createConfig();

        reloadConfig();
    }

    public Configuration getConfig(){
        return config;
    }

    public void reloadConfig(){
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void saveConfig(Configuration config){
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, file);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void replaceConfig(){
        deleteConfig();
        createConfig();
    }

    private void createConfig(){
        try {
            Files.copy(plugin.getResourceAsStream(name), file.toPath());
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void deleteConfig(){
        file.delete();
    }
}
