# Release Guide for SleepPlugin

This document describes how to create a new release of SleepPlugin.

## Automated Release Process

The plugin uses GitHub Actions for automated builds and releases.

### Creating a New Release

1. **Update Version Numbers**
   - Update `version` in `build.gradle.kts` (2 places: top-level `version` and the `bukkit { version }` block)
   - Update version in `src/main/resources/config.yml`
   - Update version in `src/main/resources/lang/en_EN.yml`
   - Update version in `src/main/resources/lang/ru_RU.yml`
   - Update version in `src/main/resources/lang/template.yml`
   - Update constant `PLUGIN_VERSION` in `SleepPlugin.java`
   - Update version in `displayPluginInfo()` method

2. **Update Documentation**
   - Add new version section to `CHANGELOG.md` (header format: `## Version X.Y.Z (YYYY-MM-DD)`)
   - Update version references in `README.md`
   - Update version references in `modrinth.md`

3. **Commit Changes**
   ```bash
   git add .
   git commit -m "Release version 1.0.4"
   git push origin 26.x
   ```

4. **Create and Push Tag**
   ```bash
   git tag v1.0.4
   git push origin v1.0.4
   ```

5. **Automatic Actions**
   - GitHub Actions will automatically build the plugin
   - A GitHub Release will be created with the JAR file
   - Release notes will be extracted from CHANGELOG.md
   - Release requirements: Minecraft 26.x, Java 25+

## Manual Build

If you need to build manually:

```bash
# Requires Java 25+
export JAVA_HOME=/opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home

# Clone repository
git clone https://github.com/NovaDAndrew/sleep-plugin.git
cd sleep-plugin

# Build with Gradle
./gradlew build

# Find the JAR file (named after the plugin version, e.g. SleepPlugin-1.0.4.jar)
ls build/libs/
```

## GitHub Actions Workflows

### build.yml
- Triggers on: push to master/main/develop/26.x, pull requests
- Actions: Builds the plugin and uploads artifacts
- Artifact name: `SleepPlugin-<version>` (e.g. `SleepPlugin-1.0.4`)
- Retention: 30 days

### release.yml
- Triggers on: version tags (v*.*.*)
- Actions: 
  - Builds the plugin
  - Creates GitHub Release
  - Uploads JAR file
  - Extracts changelog from CHANGELOG.md

### quality-check.yml
- Triggers on: push to master/main/develop/26.x, pull requests
- Actions:
  - Runs Gradle check
  - Verifies JAR compilation
  - Checks for Java warnings

## Versioning Scheme

SleepPlugin follows Semantic Versioning (SemVer):
- **MAJOR** version (1.x.x): Breaking changes or major rewrites
- **MINOR** version (x.1.x): New features, backwards compatible
- **PATCH** version (x.x.1): Bug fixes, backwards compatible

Example: `v1.0.4`

## Post-Release Checklist

After creating a release:
- [ ] Verify GitHub Release is created successfully
- [ ] Verify the attached JAR is named `SleepPlugin-<version>.jar`
- [ ] Download and test the released JAR file
- [ ] Update Modrinth page (version + body from `modrinth.md`)
- [ ] Announce the release in relevant communities
- [ ] Update documentation if needed

## Troubleshooting

### Build Fails on GitHub Actions
- Check Java version (must be 25+)
- Verify gradlew has execute permissions
- Check for syntax errors in build.gradle.kts

### Release Not Created
- Verify tag format is correct (v*.*.*)
- Check GITHUB_TOKEN permissions
- Ensure CHANGELOG.md has the version section in `## Version X.Y.Z` format

### JAR File Missing from Release
- Check build logs for errors
- Verify build/libs/ directory contains the JAR
- Ensure the workflow has write permissions

### Artifact Named with a Commit Hash
- This was fixed: the build artifact is now named after the plugin version.
- If the SHA still appears, ensure the "Get plugin version" step is present in `build.yml`.
