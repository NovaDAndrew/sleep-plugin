# SleepPlugin

[![Build](https://github.com/NovaDAndrew/sleep-plugin/actions/workflows/build.yml/badge.svg)](https://github.com/NovaDAndrew/sleep-plugin/actions/workflows/build.yml)
[![Code Quality](https://github.com/NovaDAndrew/sleep-plugin/actions/workflows/quality-check.yml/badge.svg)](https://github.com/NovaDAndrew/sleep-plugin/actions/workflows/quality-check.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Modrinth](https://img.shields.io/modrinth/dt/sleep-plugin?label=Modrinth&logo=modrinth)](https://modrinth.com/plugin/sleep-plugin)

Minecraft plugin for enhanced sleep mechanics — only half of online players need to sleep to skip the night. Multi-platform workspace: Bukkit family (Spigot/Paper/Purpur/Folia), Fabric, Quilt, Forge, NeoForge.

## Features

- Half of players needed to skip night
- Smart counting for odd player counts
- Multiple message modes (normal, minimal, silent)
- Storm and night skipping
- Ignore players in Nether and End dimensions 
- Smooth time transition from night to morning
- Configuration update system (preserves settings during updates)
- Multi-world support
- Multi-language support (English, Russian, and custom languages)

## Examples

- 2 players online: 1 player needs to sleep
- 3 players online: 1 player needs to sleep ((3-1)/2 = 1)
- 4 players online: 2 players need to sleep
- 5 players online: 2 players need to sleep ((5-1)/2 = 2)
- 6 players online: 3 players need to sleep

## Requirements

- Minecraft: 1.21.5
- Server/Loader: Bukkit family (Spigot/Paper/Purpur/Folia) or Fabric/Quilt/Forge/NeoForge
- Java: 21+

## Installation

- Bukkit/Spigot/Paper/Folia: download `platform-bukkit-1.0.4.jar`, place in `plugins/`, restart
- Fabric: download `platform-fabric-1.0.4.jar` (requires proper Fabric mod loader setup)
- Quilt: download `platform-quilt-1.0.4.jar` (requires Quilt loader)
- Forge: download `platform-forge-1.0.4.jar` (requires Forge setup)
- NeoForge: download `platform-neoforge-1.0.4.jar` (requires NeoForge setup)

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
smooth-time-transition:
  enabled: true 
  duration-ticks: 60 
  steps: 60 
storm-settings:
  skip-storms: true 
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
- `smooth-time-transition`: Settings for the smooth time transition feature
  - `enabled`: Whether to enable smooth transition or use instant time change
  - `duration-ticks`: How long the transition should take (in ticks, 20 ticks = 1 second)
  - `steps`: Number of intermediate steps (higher = smoother)
- `storm-settings`: Settings for the storm skipping feature
  - `skip-storms`: When true, players can skip storms by sleeping

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

1. Install Java 21+
2. Clone repository
3. Run: `./gradlew build`
4. JAR file will be in `build/libs/`

## License

MIT License
