# SleepPlugin

[![Build](https://github.com/NovaDAndrew/sleep-plugin/actions/workflows/build.yml/badge.svg)](https://github.com/NovaDAndrew/sleep-plugin/actions/workflows/build.yml)
[![Code Quality](https://github.com/NovaDAndrew/sleep-plugin/actions/workflows/quality-check.yml/badge.svg)](https://github.com/NovaDAndrew/sleep-plugin/actions/workflows/quality-check.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Modrinth](https://img.shields.io/modrinth/dt/sleep-plugin?label=Modrinth&logo=modrinth)](https://modrinth.com/plugin/sleep-plugin)

Minecraft Paper plugin for enhanced sleep mechanics - only half of online players need to sleep to skip the night. Available for Minecraft 1.21.x and 26.x.

## Features

- Half of players needed to skip night
- Configurable sleep percentage (per world)
- Weighted sleep votes with LuckyPerms (optional soft-dependency)
- Smart counting for odd player counts
- Multiple message modes (normal, minimal, silent)
- Storm and night skipping
- Ignore players in Nether and End dimensions 
- Smooth time transition from night to morning
- Configuration update system (preserves settings during updates)
- Multi-world support
- Multi-language support (English, Russian, and custom languages)
- Bossbar showing sleep progress
- Phantom prevention (`spawn_phantoms` game rule)
- Admin command `/sleep reload|status`

## Examples

- 2 players online: 1 player needs to sleep
- 3 players online: 1 player needs to sleep ((3-1)/2 = 1)
- 4 players online: 2 players need to sleep
- 5 players online: 2 players need to sleep ((5-1)/2 = 2)
- 6 players online: 3 players need to sleep

With `sleep-percentage: 25`:

- 4 players online: 1 player needs to sleep
- 8 players online: 2 players need to sleep
- 20 players online: 5 players need to sleep

More examples are in the [`examples/`](examples/) folder (annotated `config.yml` and LuckyPerms setup guide).

## Requirements

- Minecraft: 1.21.x or 26.x
- Server: Paper (or compatible)
- Java: 21+ (for 1.21.x) / 25+ (for 26.x)

## Installation

1. Download `SleepPlugin-1.0.4.jar`
2. Place in server's `plugins` folder
3. Restart server

## Configuration

After first server start with the plugin, a configuration file will be created at `plugins/SleepPlugin/config.yml`:

```yaml
# SleepPlugin Configuration
# Do not change this version number manually
version: "1.0.4"

language: en_EN  
skip-delay: 3   
morning-time: 1000  
message-mode: normal 
min-players-required: 2 
ignore-nether-end-players: true 
sleep-percentage: 50 
prevent-phantoms: true 
bossbar:
  enabled: true
  color: YELLOW
  style: SOLID
  title: "Sleeping %s/%s"
smooth-time-transition:
  enabled: true 
  duration-ticks: 60 
  steps: 60 
storm-settings:
  skip-storms: true 
# world-settings:
#   world:
#     sleep-percentage: 50
#     min-players-required: 2
```

### Settings:

- `language`: Language for plugin messages (en_EN or ru_RU)
- `skip-delay`: Time in seconds before night is skipped
- `morning-time`: Minecraft time value to set when skipping to morning
- `message-mode`: Controls how verbose the plugin messages are
  - `normal`: Standard detailed messages
  - `minimal`: Short concise messages
  - `silent`: No messages at all
- `min-players-required`: Minimum number of players needed to activate sleep mechanics (plugin won't work with fewer players)
- `ignore-nether-end-players`: When true, players in Nether or End won't be counted for sleep calculations
- `sleep-percentage`: Percentage of online players that must be sleeping to skip the night (clamped to 1-100, default 50). At least 1 player is always required
- `prevent-phantoms`: When true, disables phantom spawning (`spawn_phantoms`/`doInsomnia` game rule) while the plugin is active
- `bossbar`: Settings for the bossbar showing sleep progress
  - `enabled`: Whether to show the sleep progress bossbar
  - `color`: Bossbar color (PINK, BLUE, RED, GREEN, YELLOW, PURPLE, WHITE)
  - `style`: Bossbar style (SOLID, SEGMENTED_6, SEGMENTED_10, SEGMENTED_12, SEGMENTED_20)
  - `title`: Bossbar title shown to sleeping players (`%s` = sleeping count / required count)
- `smooth-time-transition`: Settings for the smooth time transition feature
  - `enabled`: Whether to enable smooth transition or use instant time change
  - `duration-ticks`: How long the transition should take (in ticks, 20 ticks = 1 second)
  - `steps`: Number of intermediate steps (higher = smoother)
- `storm-settings`: Settings for the storm skipping feature
  - `skip-storms`: When true, players can skip storms by sleeping
- `world-settings`: Per-world overrides. Add a section named after a world to override `sleep-percentage` and `min-players-required` for that world only (e.g., `world-settings.my_world.sleep-percentage: 30`)

### Command

- `/sleep status` — shows the current sleep percentage and per-world settings (permission: `sleepplugin.admin`)
- `/sleep reload` — reloads the config and language files (permission: `sleepplugin.admin`)

## LuckyPerms Integration

SleepPlugin optionally integrates with [LuckyPerms](https://luckperms.net). It is a **soft-dependency**: without LuckyPerms the plugin behaves exactly as before (every player counts as 1). Just install LuckyPerms and it is detected automatically.

**Weighted sleep votes** — set the `sleepplugin.weight` meta on a group or player to make their sleep count as multiple votes:

```
/lp group vip meta set sleepplugin.weight 2
```

A VIP with weight 2 counts as 2 sleeping players, so fewer donors are needed to skip the night. Weight is clamped to 1-100 and works with LuckyPerms contexts, so you can scope it per world/server:

```
/lp group vip meta set sleepplugin.weight 2 server=survival
```

**Permissions:**

- `sleepplugin.exempt` — the player is not counted for sleep calculations at all (e.g., admins in creative/spectator). Default: false.
- `sleepplugin.bypass.min-players` — a sleeping player with this permission skips the night even when below `min-players-required` or when sleeping alone. Default: false.

**Note:** `sleepplugin.exempt` and `sleepplugin.bypass.min-players` are plain Bukkit permissions — they work with any permissions plugin, not only LuckyPerms.

## Custom Language Files

You can create your own language translations by:

1. Navigate to `plugins/SleepPlugin/lang/`
2. Copy `template.yml` (automatically created on first run) or `en_EN.yml`/`ru_RU.yml` as a template
3. Rename it to your language code (e.g., `de_DE.yml`, `fr_FR.yml`, `es_ES.yml`)
4. Translate all message strings in the file
5. Set `language: de_DE` (or your language code) in `config.yml`
6. Restart the server

The plugin will automatically detect and load any custom language file you create!

**Note:** `template.yml` is created automatically on first plugin startup with helpful comments for translation.

## Building

1. Install Java 25+
2. Clone repository
3. Run: `./gradlew build`
4. JAR file will be in `build/libs/`

## License

MIT License
