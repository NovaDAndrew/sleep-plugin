# Contributing to SleepPlugin

Thank you for your interest in contributing to SleepPlugin! This document provides guidelines for contributing to the project.

## How to Contribute

### Reporting Bugs

If you find a bug, please open an issue on GitHub with:
- A clear, descriptive title
- Steps to reproduce the issue
- Expected behavior vs actual behavior
- Your server version, plugin version, and Java version
- Any relevant logs or error messages

### Suggesting Features

Feature suggestions are welcome! Please open an issue with:
- A clear description of the feature
- Why this feature would be useful
- Any implementation ideas (optional)

### Pull Requests

1. **Fork the repository** and create your branch from `master`
2. **Make your changes** following the code style guidelines below
3. **Test your changes** on a local Paper server
4. **Update documentation** if needed (README.md, CHANGELOG.md)
5. **Commit your changes** with clear, descriptive commit messages
6. **Push to your fork** and submit a pull request

## Code Style Guidelines

### Java Code Style

- **Indentation**: 4 spaces (no tabs)
- **Naming conventions**:
  - Classes: `PascalCase`
  - Methods: `camelCase`
  - Variables: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`
- **Braces**: Opening brace on same line
- **Comments**: Use Javadoc for public methods, inline comments for complex logic
- **Line length**: Try to keep lines under 120 characters

Example:
```java
public class MyClass {
    private static final int MAX_VALUE = 100;
    private String myVariable;
    
    /**
     * Does something useful
     * @param input the input parameter
     * @return the result
     */
    public String doSomething(String input) {
        if (input == null) {
            return null;
        }
        return input.toUpperCase();
    }
}
```

### Configuration Files

- **YAML files**: Use 2 spaces for indentation
- **Comments**: Add comments for complex configurations
- **Keys**: Use `kebab-case` for multi-word keys

### Commit Messages

Write clear, descriptive commit messages:
- Use present tense ("Add feature" not "Added feature")
- Keep first line under 50 characters
- Add detailed description after blank line if needed

Good examples:
```
Add storm skip functionality
Fix null pointer in bed leave event
Update documentation for custom languages
```

## Development Setup

1. **Prerequisites**:
   - Java 21 or higher
   - Gradle 8.x (wrapper included)
   - A Paper 1.21.5 test server

2. **Clone and build**:
   ```bash
   git clone https://github.com/NovaDAndrew/sleep-plugin.git
   cd sleep-plugin
   ./gradlew build
   ```

3. **Test**:
   - Copy `build/libs/SleepPlugin-*.jar` to your test server's `plugins/` folder
   - Start the server and test your changes
   - Check console for errors

## Testing Guidelines

Before submitting a pull request, test:
- Plugin loads without errors
- All features work as expected
- No console errors or warnings
- Config updates correctly
- Language files load properly
- Multi-world support works
- Edge cases (1 player, odd numbers, etc.)

## Adding Translations

To add a new language:

1. Copy `src/main/resources/lang/template.yml`
2. Rename to your language code (e.g., `de_DE.yml`)
3. Translate all message strings
4. Test by setting `language: de_DE` in config.yml
5. Submit a pull request with your translation

## Documentation

When making changes that affect:
- **User-facing features**: Update README.md and modrinth.md
- **Configuration**: Update config examples in documentation
- **Breaking changes**: Add migration notes to CHANGELOG.md

## Questions?

If you have questions about contributing:
- Open a discussion on GitHub
- Check existing issues and pull requests
- Contact the maintainer: NovaDAndrew

## Code of Conduct

- Be respectful and constructive
- Focus on the code, not the person
- Accept constructive criticism gracefully
- Help others learn and grow

## License

By contributing, you agree that your contributions will be licensed under the MIT License.
