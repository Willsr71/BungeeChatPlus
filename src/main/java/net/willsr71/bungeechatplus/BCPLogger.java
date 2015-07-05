package net.willsr71.bungeechatplus;

import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BCPLogger {
    private static SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

    public static void logChat(ProxiedPlayer player, String message){
        if(!BungeeChatPlus.instance.config.getBoolean("logChat")) return;
        log("chatLog.txt", player.getName() + ": " + message);
    }

    public static void logCommand(ProxiedPlayer player, String command){
        if(!BungeeChatPlus.instance.config.getBoolean("logCommands")) return;
        log("commandLog.txt", player.getName() + ": " + command);
    }

    private static void log(String file, String line){
        line = getTime() + line;
        try(PrintWriter log = new PrintWriter(new BufferedWriter(new FileWriter(file, true)))) {
            log.println(line);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private static String getTime(){
        String time = format.format(Calendar.getInstance().getTime());
        time = "[" + time + "] ";
        return time;
    }
}
