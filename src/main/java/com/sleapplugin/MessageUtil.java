package com.sleapplugin;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class MessageUtil {
    
    private static Boolean isPaperServer = null;
    
    private static boolean isPaper() {
        if (isPaperServer == null) {
            try {
                Class.forName("net.kyori.adventure.text.Component");
                isPaperServer = true;
            } catch (ClassNotFoundException e) {
                isPaperServer = false;
            }
        }
        return isPaperServer;
    }
    
    public static void sendMessage(Player player, String message, MessageColor color) {
        if (isPaper()) {
            sendPaperMessage(player, message, color);
        } else {
            sendSpigotMessage(player, message, color);
        }
    }
    
    private static void sendPaperMessage(Player player, String message, MessageColor color) {
        try {
            Class<?> componentClass = Class.forName("net.kyori.adventure.text.Component");
            Class<?> namedTextColorClass = Class.forName("net.kyori.adventure.text.format.NamedTextColor");
            Class<?> textColorClass = Class.forName("net.kyori.adventure.text.format.TextColor");
            
            Object textComponent = componentClass.getMethod("text", String.class)
                    .invoke(null, message);
            
            Object colorValue = null;
            switch (color) {
                case YELLOW:
                    colorValue = namedTextColorClass.getField("YELLOW").get(null);
                    break;
                case GREEN:
                    colorValue = namedTextColorClass.getField("GREEN").get(null);
                    break;
                case GOLD:
                    colorValue = namedTextColorClass.getField("GOLD").get(null);
                    break;
                case WHITE:
                    colorValue = namedTextColorClass.getField("WHITE").get(null);
                    break;
            }
            
            if (colorValue != null) {
                textComponent = componentClass.getMethod("color", textColorClass)
                        .invoke(textComponent, colorValue);
            }
            
            player.getClass().getMethod("sendMessage", componentClass)
                    .invoke(player, textComponent);
        } catch (Exception e) {
            sendSpigotMessage(player, message, color);
        }
    }
    
    private static void sendSpigotMessage(Player player, String message, MessageColor color) {
        ChatColor chatColor;
        switch (color) {
            case YELLOW:
                chatColor = ChatColor.YELLOW;
                break;
            case GREEN:
                chatColor = ChatColor.GREEN;
                break;
            case GOLD:
                chatColor = ChatColor.GOLD;
                break;
            case WHITE:
            default:
                chatColor = ChatColor.WHITE;
                break;
        }
        player.sendMessage(chatColor + message);
    }
    
    public enum MessageColor {
        YELLOW,
        GREEN,
        GOLD,
        WHITE
    }
}
