package com.sleapplugin;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SleepPlugin extends JavaPlugin implements Listener {
    
    private final Map<World, Set<Player>> sleepingPlayers = new HashMap<>();
    private final Map<World, BukkitRunnable> sleepTasks = new HashMap<>();
    private final Map<UUID, Long> lastProgressMessageTime = new ConcurrentHashMap<>();
    private LanguageManager lang;
    
    private int skipDelay;
    private int morningTime;
    private String messageMode;
    private int minPlayersRequired;
    private boolean ignoreNetherEndPlayers;
    private boolean skipStorms;
    private boolean smoothTimeEnabled;
    private int smoothTimeDuration;
    private int smoothTimeSteps;
    
    private static final long PROGRESS_MESSAGE_COOLDOWN = 3000;
    
    private static final String PLUGIN_VERSION = "1.0.4";
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        ConfigUpdater configUpdater = new ConfigUpdater(this, PLUGIN_VERSION);
        boolean configUpdated = configUpdater.updateConfig("config.yml", true);
        
        if (configUpdated) {
            String oldVersion = getConfig().getString("version", "1.0.1");
            getLogger().info("Configuration updated from v" + oldVersion + " to v" + PLUGIN_VERSION);
            getLogger().info("New settings have been added while preserving your existing configuration.");
        }
        
        String language = getConfig().getString("language", "en_EN");
        skipDelay = getConfig().getInt("skip-delay", 3);
        morningTime = getConfig().getInt("morning-time", 1000);
        messageMode = getConfig().getString("message-mode", "normal");
        minPlayersRequired = getConfig().getInt("min-players-required", 2);
        ignoreNetherEndPlayers = getConfig().getBoolean("ignore-nether-end-players", true);
        skipStorms = getConfig().getBoolean("storm-settings.skip-storms", true);
        smoothTimeEnabled = getConfig().getBoolean("smooth-time-transition.enabled", true);
        smoothTimeDuration = getConfig().getInt("smooth-time-transition.duration-ticks", 60);
        smoothTimeSteps = getConfig().getInt("smooth-time-transition.steps", 60);
        
        updateLanguageFiles(configUpdater);
        
        lang = new LanguageManager(this, language);
        
        Bukkit.getPluginManager().registerEvents(this, this);
        
        displayPluginInfo();
        
        getLogger().info(lang.getMessage("plugin_enabled"));
    }
    
    private void updateLanguageFiles(ConfigUpdater configUpdater) {
        File langDir = new File(getDataFolder(), "lang");
        if (!langDir.exists()) {
            langDir.mkdirs();
        }
        
        File templateFile = new File(langDir, "template.yml");
        if (!templateFile.exists()) {
            saveResource("lang/template.yml", false);
            getLogger().info("Created template.yml for custom translations");
        }
        
        String[] bundledLanguages = {"en_EN", "ru_RU"};
        for (String langCode : bundledLanguages) {
            boolean updated = configUpdater.updateLanguageFile(langCode);
            if (updated) {
                getLogger().info("Language file " + langCode + ".yml has been updated to v" + PLUGIN_VERSION);
            }
        }
        
        File[] langFiles = langDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (langFiles != null) {
            for (File langFile : langFiles) {
                String fileName = langFile.getName();
                String langCode = fileName.replace(".yml", "");
                
                boolean isBundled = false;
                for (String bundled : bundledLanguages) {
                    if (bundled.equals(langCode)) {
                        isBundled = true;
                        break;
                    }
                }
                
                // Skip template file
                if (!isBundled && !langCode.equals("template")) {
                    getLogger().info("Found custom language file: " + langCode + ".yml");
                }
            }
        }
    }
    
    @Override
    public void onDisable() {
        for (BukkitRunnable task : sleepTasks.values()) {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        }
        sleepTasks.clear();
        sleepingPlayers.clear();
        
        getLogger().info(lang.getMessage("plugin_disabled"));
    }
    
    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        if (event.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) {
            return;
        }
        
        Player player = event.getPlayer();
        World world = player.getWorld();
        
        if (!isNightOrStorm(world)) {
            return;
        }
        
        sleepingPlayers.computeIfAbsent(world, k -> new HashSet<>()).add(player);
        
        checkSleepRequirement(world);
    }
    
    @EventHandler
    public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        
        Set<Player> sleeping = sleepingPlayers.get(world);
        if (sleeping != null) {
            sleeping.remove(player);
            if (sleeping.isEmpty()) {
                sleepingPlayers.remove(world);
            }
        }

        int onlinePlayersInWorld = (int) Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.getWorld().equals(world))
                .filter(p -> !p.isSleepingIgnored())
                .filter(p -> !shouldIgnorePlayer(p))
                .count();
        
        int requiredSleeping = calculateRequiredSleeping(onlinePlayersInWorld);
        
        int currentSleeping = sleeping != null ? sleeping.size() : 0;
        BukkitRunnable task = sleepTasks.get(world);
        if (task != null && !task.isCancelled() && currentSleeping < requiredSleeping) {
            task.cancel();
            sleepTasks.remove(world);
            
            if (!messageMode.equals("silent")) {
                String messageKey = messageMode.equals("minimal") ? "sleep_canceled_minimal" : "sleep_canceled";
                broadcastToWorld(world, lang.getMessage(messageKey), MessageUtil.MessageColor.YELLOW);
            }
        }
    }
    
    private void checkSleepRequirement(World world) {
        Set<Player> sleeping = sleepingPlayers.get(world);
        if (sleeping == null || sleeping.isEmpty()) {
            return;
        }
        
        int onlinePlayersInWorld = (int) Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.getWorld().equals(world))
                .filter(p -> !p.isSleepingIgnored())
                .filter(p -> !shouldIgnorePlayer(p))
                .count();
        
        if (onlinePlayersInWorld == 0) {
            return;
        }
        
        if (onlinePlayersInWorld < minPlayersRequired) {
            return;
        }
        
        int requiredSleeping = calculateRequiredSleeping(onlinePlayersInWorld);
        int currentSleeping = sleeping.size();
        
        if (currentSleeping >= requiredSleeping) {
            startNightSkip(world, currentSleeping, onlinePlayersInWorld);
        } else if (!messageMode.equals("silent")) {
            UUID worldId = world.getUID();
            long currentTime = System.currentTimeMillis();
            
            if (!lastProgressMessageTime.containsKey(worldId) || 
                    currentTime - lastProgressMessageTime.get(worldId) > PROGRESS_MESSAGE_COOLDOWN) {
                
                lastProgressMessageTime.put(worldId, currentTime);
                
                String messageKey = isOnlyStorm(world) ? "storm_progress" : "sleep_progress";
                String message = lang.getMessage(messageKey, currentSleeping, requiredSleeping);
                
                sendMessageToWorld(world, message);
            }
        }
    }
    
    private int calculateRequiredSleeping(int onlinePlayers) {
        if (onlinePlayers <= 1) {
            return Integer.MAX_VALUE;
        }
        
        if (onlinePlayers % 2 == 1) {
            return (onlinePlayers - 1) / 2;
        } else {
            return onlinePlayers / 2;
        }
    }
    
    private void startNightSkip(World world, int sleepingCount, int totalCount) {
        BukkitRunnable existingTask = sleepTasks.get(world);
        if (existingTask != null && !existingTask.isCancelled()) {
            existingTask.cancel();
        }
        
        if (!messageMode.equals("silent")) {
            UUID worldId = world.getUID();
            long currentTime = System.currentTimeMillis();
            
            if (!lastProgressMessageTime.containsKey(worldId) || 
                    currentTime - lastProgressMessageTime.get(worldId) > PROGRESS_MESSAGE_COOLDOWN) {
                
                lastProgressMessageTime.put(worldId, currentTime);
                
                String baseKey = isOnlyStorm(world) ? "storm" : "sleep";
                String messageKey = messageMode.equals("minimal") ? baseKey + "_countdown_minimal" : baseKey + "_countdown";
                String message;
                
                if (messageMode.equals("minimal")) {
                    message = lang.getMessage(messageKey, sleepingCount, totalCount);
                } else {
                    message = lang.getMessage(messageKey, skipDelay, sleepingCount, totalCount);
                }
                
                broadcastToWorld(world, message, MessageUtil.MessageColor.GREEN);
            }
        }
        
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                Set<Player> currentSleeping = sleepingPlayers.get(world);
                if (currentSleeping == null || currentSleeping.isEmpty()) {
                    sleepTasks.remove(world);
                    return;
                }
                
                int currentOnline = (int) world.getPlayers().stream()
                        .filter(p -> !p.isSleepingIgnored())
                        .count();
                
                if (currentSleeping.size() >= calculateRequiredSleeping(currentOnline)) {
                    boolean wasNight = isNight(world);
                    boolean wasStorm = world.isThundering() || world.hasStorm();
                    
                    if (world.isThundering()) {
                        world.setThundering(false);
                        world.setStorm(false);
                    }
                    
                    if (wasNight) {
                        if (smoothTimeEnabled) {
                            smoothlySetTime(world, morningTime);
                        } else {
                            world.setTime(morningTime);
                        }
                    }
                    
                    if (!messageMode.equals("silent")) {
                        String baseKey;
                        if (wasNight) {
                            baseKey = "sleep";
                        } else if (wasStorm) {
                            baseKey = "storm";
                        } else {
                            baseKey = null;
                        }
                        
                        if (baseKey != null) {
                            UUID worldId = world.getUID();
                            long currentTime = System.currentTimeMillis();
                            
                            if (!lastProgressMessageTime.containsKey(worldId) || 
                                    currentTime - lastProgressMessageTime.get(worldId) > PROGRESS_MESSAGE_COOLDOWN) {
                                
                                lastProgressMessageTime.put(worldId, currentTime);
                                
                                String messageKey;
                                
                                if (wasNight && smoothTimeEnabled && baseKey.equals("sleep")) {
                                    messageKey = messageMode.equals("minimal") ? 
                                        "sleep_skipping_smooth_minimal" : "sleep_skipping_smooth";
                                } else {
                                    messageKey = messageMode.equals("minimal") ? 
                                        baseKey + "_success_minimal" : baseKey + "_success";
                                }
                                
                                broadcastToWorld(world, lang.getMessage(messageKey), MessageUtil.MessageColor.GOLD);
                            }
                        }
                    }
                    
                    sleepingPlayers.remove(world);
                }
                
                sleepTasks.remove(world);
            }
        };
    
        task.runTaskLater(this, skipDelay * 20L); 
        sleepTasks.put(world, task);
    }
    
    private boolean isNightOrStorm(World world) {
        boolean night = isNight(world);
        boolean storm = skipStorms && world.hasStorm();
        return night || storm;
    }
    
    private boolean isOnlyStorm(World world) {
        return skipStorms && world.hasStorm() && !isNight(world);
    }
    
    private boolean shouldIgnorePlayer(Player player) {
        if (!ignoreNetherEndPlayers) {
            return false;
        }
        
        World.Environment env = player.getWorld().getEnvironment();
        return env == World.Environment.NETHER || env == World.Environment.THE_END;
    }
    
    private boolean isNight(World world) {
        long time = world.getTime();
        return time >= 12541 && time <= 23458; 
    }
    
    private void broadcastToWorld(World world, String message, MessageUtil.MessageColor color) {
        for (Player player : world.getPlayers()) {
            MessageUtil.sendMessage(player, message, color);
        }
    }
    
    private void sendMessageToWorld(World world, String message) {
        for (Player player : world.getPlayers()) {
            MessageUtil.sendMessage(player, message, MessageUtil.MessageColor.WHITE);
        }
    }
    
    private void smoothlySetTime(World world, long targetTime) {
        long currentTime = world.getTime();
        long diff = (targetTime - currentTime + 24000L) % 24000L; 
        
        if (diff < 100) {
            world.setTime(targetTime);
            return;
        }
        
        final int stepCount = smoothTimeSteps;
        final int ticksPerStep = Math.max(1, smoothTimeDuration / stepCount);
        
        for (int i = 0; i < stepCount; i++) {
            final int step = i;
            Bukkit.getScheduler().runTaskLater(this, () -> {
                double progress = (double)(step + 1) / stepCount;
                double smoothProgress = (1 - Math.cos(Math.PI * progress)) / 2;
                long newTime = (currentTime + (long)(diff * smoothProgress)) % 24000L;
                world.setTime(newTime);
                
                if (step == stepCount - 1) {
                    world.setTime(targetTime);
                }
            }, ticksPerStep * i);
        }
    }
    
    private void displayPluginInfo() {
        String[] infoLines = {
            "\n",
            "  ╔═════════════════════════════════════════════════════════╗",
            "  ║                    SleepPlugin v1.0.4                   ║",
            "  ╠═════════════════════════════════════════════════════════╣",
            "  ║  Author: NovaDAndrew                                    ║",
            "  ║  Modrinth: https://modrinth.com/plugin/sleep-plugin     ║",
            "  ║  GitHub: https://github.com/NovaDAndrew/sleep-plugin    ║",
            "  ╠═════════════════════════════════════════════════════════╣",
            "  ║  Features:                                              ║",
            "  ║  • Skip night with only half of players sleeping        ║",
            "  ║  • Multiple message modes (normal/minimal/silent)       ║",
            "  ║  • Configurable minimum player requirement              ║",
            "  ║  • Smooth time transition (day/night)                   ║",
            "  ║  • Multi-language support (EN/RU + custom langs)        ║",
            "  ║  • Compatible with both Paper and Spigot servers        ║",
            "  ╚═════════════════════════════════════════════════════════╝",
            ""
        };
        
        for (String line : infoLines) {
            getLogger().info(line);
        }
    }
}
