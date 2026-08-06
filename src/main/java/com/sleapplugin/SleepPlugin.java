package com.sleapplugin;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SleepPlugin extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {
    
    private final Map<World, Set<Player>> sleepingPlayers = new HashMap<>();
    private final Map<World, BukkitRunnable> sleepTasks = new HashMap<>();
    private final Map<UUID, Long> lastProgressMessageTime = new ConcurrentHashMap<>();
    private final Map<World, BossBar> bossBars = new HashMap<>();
    private final Map<World, Boolean> worldInsomniaState = new HashMap<>();
    private final Map<String, WorldSettings> worldSettings = new HashMap<>();
    private LanguageManager lang;
    
    private int skipDelay;
    private int morningTime;
    private String messageMode;
    private int globalSleepPercentage;
    private int globalMinPlayersRequired;
    private boolean ignoreNetherEndPlayers;
    private boolean preventPhantoms;
    private boolean skipStorms;
    private boolean smoothTimeEnabled;
    private int smoothTimeDuration;
    private int smoothTimeSteps;
    private boolean bossbarEnabled;
    private BarColor bossbarColor;
    private BarStyle bossbarStyle;
    private String bossbarTitle;
    
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
        
        readConfigValues();
        
        updateLanguageFiles(configUpdater);
        
        lang = new LanguageManager(this, getConfig().getString("language", "en_EN"));
        
        Bukkit.getPluginManager().registerEvents(this, this);
        
        if (getCommand("sleep") != null) {
            getCommand("sleep").setExecutor(this);
            getCommand("sleep").setTabCompleter(this);
        }
        
        applyPhantomPrevention();
        
        displayPluginInfo();
        
        getLogger().info(lang.getMessage("plugin_enabled"));
    }
    
    private void readConfigValues() {
        skipDelay = getConfig().getInt("skip-delay", 3);
        morningTime = getConfig().getInt("morning-time", 1000);
        messageMode = getConfig().getString("message-mode", "normal");
        globalSleepPercentage = clampPercentage(getConfig().getInt("sleep-percentage", 50));
        globalMinPlayersRequired = getConfig().getInt("min-players-required", 2);
        ignoreNetherEndPlayers = getConfig().getBoolean("ignore-nether-end-players", true);
        preventPhantoms = getConfig().getBoolean("prevent-phantoms", true);
        skipStorms = getConfig().getBoolean("storm-settings.skip-storms", true);
        smoothTimeEnabled = getConfig().getBoolean("smooth-time-transition.enabled", true);
        smoothTimeDuration = getConfig().getInt("smooth-time-transition.duration-ticks", 60);
        smoothTimeSteps = getConfig().getInt("smooth-time-transition.steps", 60);
        
        bossbarEnabled = getConfig().getBoolean("bossbar.enabled", true);
        bossbarColor = parseColor(getConfig().getString("bossbar.color", "YELLOW"));
        bossbarStyle = parseStyle(getConfig().getString("bossbar.style", "SOLID"));
        bossbarTitle = getConfig().getString("bossbar.title", "Sleeping %s/%s");
        
        worldSettings.clear();
        ConfigurationSection section = getConfig().getConfigurationSection("world-settings");
        if (section != null) {
            for (String worldName : section.getKeys(false)) {
                ConfigurationSection ws = section.getConfigurationSection(worldName);
                if (ws == null) {
                    continue;
                }
                boolean enabled = ws.getBoolean("enabled", true);
                int percentage = clampPercentage(ws.getInt("sleep-percentage", globalSleepPercentage));
                int minPlayers = ws.getInt("min-players-required", globalMinPlayersRequired);
                worldSettings.put(worldName, new WorldSettings(enabled, percentage, minPlayers));
            }
        }
    }
    
    private int clampPercentage(int value) {
        return Math.max(1, Math.min(100, value));
    }
    
    private BarColor parseColor(String name) {
        try {
            return BarColor.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return BarColor.YELLOW;
        }
    }
    
    private BarStyle parseStyle(String name) {
        try {
            return BarStyle.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return BarStyle.SOLID;
        }
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
        
        for (BossBar bar : bossBars.values()) {
            bar.removeAll();
        }
        bossBars.clear();
        
        restorePhantomSettings();
        
        getLogger().info(lang.getMessage("plugin_disabled"));
    }
    
    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        if (event.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) {
            return;
        }
        
        Player player = event.getPlayer();
        World world = player.getWorld();
        
        if (!settingsFor(world).enabled) {
            return;
        }
        
        if (!isNightOrStorm(world)) {
            return;
        }
        
        sleepingPlayers.computeIfAbsent(world, k -> new HashSet<>()).add(player);
        
        updateBossBar(world);
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
        
        int onlinePlayersInWorld = countOnlinePlayers(world);
        int requiredSleeping = calculateRequiredSleeping(onlinePlayersInWorld, world);
        
        int currentSleeping = sleeping != null ? sleeping.size() : 0;
        BukkitRunnable task = sleepTasks.get(world);
        if (task != null && !task.isCancelled() && currentSleeping < requiredSleeping) {
            task.cancel();
            sleepTasks.remove(world);
            removeBossBar(world);
            
            if (!messageMode.equals("silent")) {
                String messageKey = messageMode.equals("minimal") ? "sleep_canceled_minimal" : "sleep_canceled";
                broadcastToWorld(world, lang.getMessage(messageKey), MessageUtil.MessageColor.YELLOW);
            }
        } else {
            updateBossBar(world);
        }
    }
    
    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        applyToWorld(event.getWorld());
    }
    
    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        World world = event.getWorld();
        removeBossBar(world);
        BukkitRunnable task = sleepTasks.remove(world);
        if (task != null) {
            task.cancel();
        }
        sleepingPlayers.remove(world);
        worldInsomniaState.remove(world);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendToSender(sender, lang.getMessage("command_usage"), MessageUtil.MessageColor.WHITE);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "reload":
                reloadPlugin();
                sendToSender(sender, lang.getMessage("command_reload_success"), MessageUtil.MessageColor.GREEN);
                break;
            case "status":
                sendToSender(sender, lang.getMessage("command_status",
                        globalSleepPercentage, globalMinPlayersRequired, worldSettings.size()), MessageUtil.MessageColor.WHITE);
                for (World world : Bukkit.getWorlds()) {
                    WorldSettings ws = settingsFor(world);
                    int sleeping = sleepingPlayers.containsKey(world) ? sleepingPlayers.get(world).size() : 0;
                    int online = countOnlinePlayers(world);
                    int required = online > 0 ? calculateRequiredSleeping(online, world) : 0;
                    if (required == Integer.MAX_VALUE) {
                        required = 0;
                    }
                    sendToSender(sender, lang.getMessage("command_status_world",
                            world.getName(), sleeping, required, ws.sleepPercentage, ws.minPlayersRequired, ws.enabled),
                            MessageUtil.MessageColor.WHITE);
                }
                break;
            default:
                sendToSender(sender, lang.getMessage("command_usage"), MessageUtil.MessageColor.WHITE);
        }
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String prefix = args[0].toLowerCase();
            for (String option : new String[]{"reload", "status"}) {
                if (option.startsWith(prefix)) {
                    completions.add(option);
                }
            }
            return completions;
        }
        return Collections.emptyList();
    }
    
    private void reloadPlugin() {
        reloadConfig();
        restorePhantomSettings();
        
        readConfigValues();
        
        ConfigUpdater configUpdater = new ConfigUpdater(this, PLUGIN_VERSION);
        configUpdater.updateConfig("config.yml", true);
        
        updateLanguageFiles(configUpdater);
        
        lang = new LanguageManager(this, getConfig().getString("language", "en_EN"));
        
        applyPhantomPrevention();
        
        for (World world : new ArrayList<>(bossBars.keySet())) {
            removeBossBar(world);
        }
        for (World world : Bukkit.getWorlds()) {
            updateBossBar(world);
        }
    }
    
    private void checkSleepRequirement(World world) {
        Set<Player> sleeping = sleepingPlayers.get(world);
        if (sleeping == null || sleeping.isEmpty()) {
            return;
        }
        
        int onlinePlayersInWorld = countOnlinePlayers(world);
        
        if (onlinePlayersInWorld == 0) {
            return;
        }
        
        WorldSettings settings = settingsFor(world);
        if (onlinePlayersInWorld < settings.minPlayersRequired) {
            return;
        }
        
        int requiredSleeping = calculateRequiredSleeping(onlinePlayersInWorld, world);
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
        
        updateBossBar(world);
    }
    
    private int calculateRequiredSleeping(int onlinePlayers, World world) {
        if (onlinePlayers <= 1) {
            return Integer.MAX_VALUE;
        }
        
        int percentage = settingsFor(world).sleepPercentage;
        int required = (onlinePlayers * percentage) / 100;
        return Math.min(onlinePlayers, Math.max(1, required));
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
                    removeBossBar(world);
                    return;
                }
                
                int currentOnline = (int) world.getPlayers().stream()
                        .filter(p -> !p.isSleepingIgnored())
                        .count();
                
                if (currentSleeping.size() >= calculateRequiredSleeping(currentOnline, world)) {
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
                    removeBossBar(world);
                }
                
                sleepTasks.remove(world);
            }
        };
    
        task.runTaskLater(this, skipDelay * 20L); 
        sleepTasks.put(world, task);
    }
    
    private WorldSettings settingsFor(World world) {
        WorldSettings override = worldSettings.get(world.getName());
        if (override != null) {
            return override;
        }
        return new WorldSettings(true, globalSleepPercentage, globalMinPlayersRequired);
    }
    
    private int countOnlinePlayers(World world) {
        return (int) Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.getWorld().equals(world))
                .filter(p -> !p.isSleepingIgnored())
                .filter(p -> !shouldIgnorePlayer(p))
                .count();
    }
    
    private void updateBossBar(World world) {
        if (!bossbarEnabled) {
            removeBossBar(world);
            return;
        }
        
        Set<Player> sleeping = sleepingPlayers.get(world);
        if (sleeping == null || sleeping.isEmpty()) {
            removeBossBar(world);
            return;
        }
        
        int online = countOnlinePlayers(world);
        if (online <= 0) {
            removeBossBar(world);
            return;
        }
        
        int required = calculateRequiredSleeping(online, world);
        if (required == Integer.MAX_VALUE) {
            removeBossBar(world);
            return;
        }
        
        BossBar bar = bossBars.computeIfAbsent(world, w -> Bukkit.createBossBar("", bossbarColor, bossbarStyle));
        
        int current = sleeping.size();
        bar.setTitle(formatBossBarTitle(current, required));
        bar.setProgress(Math.min(1.0, (double) current / required));
        bar.setVisible(true);
        
        List<Player> currentViewers = new ArrayList<>(bar.getPlayers());
        for (Player player : sleeping) {
            if (!currentViewers.contains(player)) {
                bar.addPlayer(player);
            }
        }
        for (Player viewer : currentViewers) {
            if (!sleeping.contains(viewer) || !viewer.getWorld().equals(world)) {
                bar.removePlayer(viewer);
            }
        }
    }
    
    private String formatBossBarTitle(int current, int required) {
        try {
            return String.format(bossbarTitle, current, required);
        } catch (Exception e) {
            return "Sleeping " + current + "/" + required;
        }
    }
    
    private void removeBossBar(World world) {
        BossBar bar = bossBars.remove(world);
        if (bar != null) {
            bar.removeAll();
        }
    }
    
    private void applyPhantomPrevention() {
        if (!preventPhantoms) {
            return;
        }
        for (World world : Bukkit.getWorlds()) {
            applyToWorld(world);
        }
    }
    
    private void applyToWorld(World world) {
        if (!preventPhantoms) {
            return;
        }
        if (!worldInsomniaState.containsKey(world)) {
            worldInsomniaState.put(world, getInsomniaState(world));
        }
        setInsomniaState(world, false);
    }
    
    private void restorePhantomSettings() {
        for (Map.Entry<World, Boolean> entry : worldInsomniaState.entrySet()) {
            setInsomniaState(entry.getKey(), entry.getValue());
        }
        worldInsomniaState.clear();
    }
    
    @SuppressWarnings("removal")
    private static GameRule<Boolean> insomniaRule() {
        GameRule<Boolean> rule = GameRule.getByName("spawn_phantoms");
        if (rule == null) {
            rule = GameRule.getByName("doInsomnia");
        }
        return rule;
    }
    
    private boolean getInsomniaState(World world) {
        try {
            GameRule<Boolean> rule = insomniaRule();
            if (rule != null) {
                Boolean value = world.getGameRuleValue(rule);
                return value == null || value;
            }
        } catch (LinkageError e) {
            // Some implementations may not expose the rule; treat as default true
        }
        return true;
    }
    
    private void setInsomniaState(World world, boolean enabled) {
        try {
            GameRule<Boolean> rule = insomniaRule();
            if (rule != null) {
                world.setGameRule(rule, enabled);
                return;
            }
            getLogger().warning("Could not find game rule 'doInsomnia' in " + world.getName() + ", phantom prevention skipped");
        } catch (LinkageError e) {
            getLogger().warning("Failed to toggle 'doInsomnia' in " + world.getName() + ": " + e.getMessage());
        }
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
    
    private void sendToSender(CommandSender sender, String message, MessageUtil.MessageColor color) {
        if (sender instanceof Player) {
            MessageUtil.sendMessage((Player) sender, message, color);
        } else {
            sender.sendMessage(message);
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
            "  ║  • Configurable sleep percentage (per world)            ║",
            "  ║  • Multiple message modes (normal/minimal/silent)       ║",
            "  ║  • Bossbar sleep progress                               ║",
            "  ║  • Phantom prevention (doInsomnia)                      ║",
            "  ║  • Smooth time transition (day/night)                   ║",
            "  ║  • Multi-language support (EN/RU + custom langs)        ║",
            "  ║  • Admin command: /sleep reload|status                  ║",
            "  ╚═════════════════════════════════════════════════════════╝",
            ""
        };
        
        for (String line : infoLines) {
            getLogger().info(line);
        }
    }
    
    private static class WorldSettings {
        final boolean enabled;
        final int sleepPercentage;
        final int minPlayersRequired;
        
        WorldSettings(boolean enabled, int sleepPercentage, int minPlayersRequired) {
            this.enabled = enabled;
            this.sleepPercentage = sleepPercentage;
            this.minPlayersRequired = minPlayersRequired;
        }
    }
}
