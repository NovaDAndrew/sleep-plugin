package com.sleapplugin;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.platform.PlayerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class LuckPermsHook {

    private static final String META_WEIGHT_KEY = "sleepplugin.weight";
    private static final String PERM_EXEMPT = "sleepplugin.exempt";
    private static final String PERM_BYPASS_MIN_PLAYERS = "sleepplugin.bypass.min-players";

    private LuckPerms luckPerms;

    public void init() {
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
            try {
                luckPerms = LuckPermsProvider.get();
                Bukkit.getLogger().info("[SleepPlugin] LuckyPerms detected, weighted sleep votes enabled");
            } catch (Exception e) {
                luckPerms = null;
                Bukkit.getLogger().info("[SleepPlugin] LuckyPerms present but API unavailable, using default weights");
            }
        }
    }

    public boolean isAvailable() {
        return luckPerms != null;
    }

    public int playerWeight(Player player) {
        if (luckPerms == null) {
            return 1;
        }
        try {
            PlayerAdapter<Player> adapter = luckPerms.getPlayerAdapter(Player.class);
            String value = adapter.getMetaData(player).getMetaValue(META_WEIGHT_KEY);
            if (value != null) {
                int weight = Integer.parseInt(value.trim());
                return Math.max(1, Math.min(100, weight));
            }
        } catch (NumberFormatException e) {
            // Invalid meta value, fall back to default weight
        } catch (Exception e) {
            // Any API failure, fall back to default weight
        }
        return 1;
    }

    public boolean isExempt(Player player) {
        return player.hasPermission(PERM_EXEMPT);
    }

    public boolean canBypassMinPlayers(Player player) {
        return player.hasPermission(PERM_BYPASS_MIN_PLAYERS);
    }
}
