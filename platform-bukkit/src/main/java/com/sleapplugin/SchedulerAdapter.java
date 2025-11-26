package com.sleapplugin;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;

public class SchedulerAdapter {

    public static void runLater(JavaPlugin plugin, World world, Runnable task, long ticks) {
        try {
            Object server = Bukkit.getServer();
            Method getRegionScheduler = server.getClass().getMethod("getRegionScheduler");
            Object regionScheduler = getRegionScheduler.invoke(server);

            Method runDelayed = regionScheduler.getClass().getMethod(
                    "runDelayed", JavaPlugin.class, World.class, Runnable.class, long.class
            );
            runDelayed.invoke(regionScheduler, plugin, world, task, ticks);
            return;
        } catch (Throwable ignored) {
        }

        Bukkit.getScheduler().runTaskLater(plugin, task, ticks);
    }
}

