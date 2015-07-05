package net.willsr71.bungeechatplus;

public class VersionChecker {
    BungeeChatPlus plugin;

    public boolean isConfigVersionUpToDate(){
        return plugin.config.getString("versionDontTouch.seriouslyThisWillEraseYourConfig").equals("1.8");
        //this will be more elaborate in the future
    }
}