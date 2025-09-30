# Release Guide for SleepPlugin

This document describes how to create a new release of SleepPlugin.

## Automated Release Process

The plugin uses GitHub Actions for automated builds and releases.

### Creating a New Release

1. **Update Version Numbers**
   - Update version in `build.gradle.kts` (2 places: `version` and `paper.version`)
   - Update version in `src/main/resources/config.yml`
   - Update version in `src/main/resources/lang/en_EN.yml`
   - Update version in `src/main/resources/lang/ru_RU.yml`
   - Update constant `PLUGIN_VERSION` in `SleepPlugin.java`
   - Update version in `displayPluginInfo()` method

2. **Update Documentation**
   - Add new version section to `CHANGELOG.md`
   - Update version references in `README.md`
   - Update version references in `modrinth.md`

3. **Commit Changes**
   ```bash
   git add .
   git commit -m "Release version 1.0.3"
   git push origin master
   ```

4. **Create and Push Tag**
   ```bash
   git tag v1.0.3
   git push origin v1.0.3
   ```

5. **Automatic Actions**
   - GitHub Actions will automatically build the plugin
   - A GitHub Release will be created with the JAR file
   - Release notes will be extracted from CHANGELOG.md

## Manual Build

If you need to build manually:

```bash
# Clone repository
git clone https://github.com/NovaDAndrew/sleep-plugin.git
cd sleep-plugin

# Build with Gradle
./gradlew build

# Find the JAR file
ls build/libs/
```

## GitHub Actions Workflows

### build.yml
- Triggers on: push to master/main/develop, pull requests
- Actions: Builds the plugin and uploads artifacts
- Retention: 30 days

### release.yml
- Triggers on: version tags (v*.*.*)
- Actions: 
  - Builds the plugin
  - Creates GitHub Release
  - Uploads JAR file
  - Extracts changelog from CHANGELOG.md

### quality-check.yml
- Triggers on: push to master/main/develop, pull requests
- Actions:
  - Runs Gradle check
  - Verifies JAR compilation
  - Checks for Java warnings

## Versioning Scheme

SleepPlugin follows Semantic Versioning (SemVer):
- **MAJOR** version (1.x.x): Breaking changes or major rewrites
- **MINOR** version (x.1.x): New features, backwards compatible
- **PATCH** version (x.x.1): Bug fixes, backwards compatible

Example: `v1.0.3`

## Post-Release Checklist

After creating a release:
- [ ] Verify GitHub Release is created successfully
- [ ] Download and test the released JAR file
- [ ] Update Modrinth page if needed
- [ ] Announce the release in relevant communities
- [ ] Update documentation if needed

## Troubleshooting

### Build Fails on GitHub Actions
- Check Java version (must be 21+)
- Verify gradlew has execute permissions
- Check for syntax errors in build.gradle.kts

### Release Not Created
- Verify tag format is correct (v*.*.*)
- Check GITHUB_TOKEN permissions
- Ensure CHANGELOG.md has the version section

### JAR File Missing from Release
- Check build logs for errors
- Verify build/libs/ directory contains the JAR
- Ensure the workflow has write permissions
