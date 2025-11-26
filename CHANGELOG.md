# SleepPlugin Changelog

## Version 1.0.4 (2025-11-26)

### Platforms
- Restructured the project into a multi-platform architecture: `core` + platform modules
- Added `platform-bukkit` with a Folia-compatible scheduler for smooth time transitions
- Added skeleton modules: `platform-fabric`, `platform-quilt`, `platform-forge`, `platform-neoforge`
- CI and Release workflows now publish artifacts for all platforms

### Changes
- Bumped project version to `1.0.4`
- Updated configs and language files to `1.0.4`
- Simplified build: Gradle 8.5, JDK 21

---
## Version 1.0.3 (2025-10-xx)

### New Features
- Added support for custom language files - users can now create their own localizations beyond English and Russian
- Added `template.yml` language file template for easier translation creation
- Added GitHub Actions for automated builds and releases

### Improvements
- Removed language file validation restrictions - any custom language file is now automatically supported
- Enhanced LanguageManager to dynamically load any .yml language file from the lang/ directory
- Added automated CI/CD pipeline with GitHub Actions
- Added build status badges to README

### Technical Changes
- Language files are no longer restricted to en_EN and ru_RU only
- Plugin now accepts any valid language code format (e.g., de_DE, fr_FR, es_ES, etc.)
- Created three GitHub Actions workflows: build, release, and quality check
- Automated release creation when version tags are pushed

---

## Version 1.0.2 (2025-05-25)

### New Features
- Added smooth time transition from night to morning
- Added ActionBar progress notifications (reduces chat spam)
- Added storm skipping functionality
- Added intelligent sleep tracking (doesn't cancel if enough players still sleeping)
- Added configuration update system that preserves user settings
- Added support for Nether/End player exclusion

### Improvements
- Improved message display system with three modes: normal, minimal, silent
- Enhanced configuration with more customization options
- Improved sleep mechanics with smart counting for odd player counts
- Better performance with optimized code
- Reduced chat spam with cooldown system for notifications

### Configuration Changes
- Added `version` field to track configuration versions
- Added `ignore-nether-end-players` option
- Added `smooth-time-transition` section with customization options
- Added `storm-settings` section for storm-related options
- Added `min-players-required` to set minimum players for activation

### Bug Fixes
- Fixed issue with sleep being canceled when sufficient players still sleeping
- Fixed unnecessary sleep notifications when playing alone
- Fixed incorrect message display for skip delay time
