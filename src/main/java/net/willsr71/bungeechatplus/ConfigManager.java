package net.willsr71.bungeechatplus;


import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.nio.file.Files;

public class ConfigManager {
    private BungeeChatPlus plugin;

    private String configName;
    private File file;
    private Configuration fileConfig;

    public ConfigManager(BungeeChatPlus plugin, String configName){
        this.plugin = plugin;
        this.configName = configName;
        File dataFolder = plugin.getDataFolder();
        if(!dataFolder.exists()) dataFolder.mkdirs();

        file = new File(dataFolder, configName);
        if(!file.exists()) createConfig();

        reloadConfig();
    }

    public Configuration getConfig(){
        return fileConfig;
    }

    public void reloadConfig(){
        try {
            fileConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
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
            Files.copy(plugin.getResourceAsStream(configName), file.toPath());
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void deleteConfig(){
        file.delete();
    }
}
