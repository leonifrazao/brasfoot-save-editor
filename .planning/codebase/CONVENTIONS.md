# Coding Conventions

**Analysis Date:** 2026-04-05

## Naming Patterns

**Files & Classes:**
- PascalCase for all classes and interfaces (e.g., `BrasfootEditorApplication.java`, `GeneralCommands.java`).
- Suffixes indicate the class role (e.g., `*Service.java` for business logic, `*Commands.java` for CLI controllers, `*Constants.java` for static constants).

**Functions & Methods:**
- camelCase (e.g., `loadFile`, `saveService`).
- Descriptive names reflecting the action performed.

**Variables & Fields:**
- camelCase for standard variables.
- UPPER_SNAKE_CASE for `public static final` constants (e.g., `TEAMS_LIST` in `src/main/java/br/com/saveeditor/brasfoot/util/BrasfootConstants.java`).

## Code Style

**Formatting:**
- Standard Java indentation (likely 4 spaces).
- Standard Java brace placement (same line for class/method declarations).

**Linting:**
- No explicit linter configuration (like Checkstyle or PMD) detected in the project root.

## Architecture & Dependency Management

**Dependency Injection:**
- Spring Boot constructor injection is the primary pattern used.
- Classes declare `final` dependencies and initialize them via constructors (e.g., `GeneralCommands.java`).

**Command Line Interface (CLI):**
- Relies on Spring Shell annotations (`@ShellComponent`, `@ShellMethod`, `@ShellOption`).
- Commands are organized functionally (e.g., `GeneralCommands.java`, `CheatCommands.java`, `PresetCommands.java`).

## Import Organization

**Order:**
1. Standard Java imports (`java.*`).
2. Spring Framework imports (`org.springframework.*`).
3. Internal project imports (`br.com.saveeditor.brasfoot.*`).

## Error Handling & Logging

**Patterns:**
- No robust logging framework configuration (like `slf4j` / `Logback`) used for standard output.
- Custom `ConsoleHelper` class (`src/main/java/br/com/saveeditor/brasfoot/util/ConsoleHelper.java`) is used to print colored warnings, errors, and confirmations directly to the terminal UI.

## Comments

**When to Comment:**
- Javadoc comments are used for class-level documentation, especially in utility classes like `src/main/java/br/com/saveeditor/brasfoot/util/BrasfootConstants.java` to explain obfuscated game fields.
- Inline comments explain specific logical checks or safety features (e.g., unsaved changes prompt in `GeneralCommands.java`).

## Obfuscated Code Management

**Constants Mapping:**
- Due to the nature of editing a closed-source game (Brasfoot), obfuscated fields (e.g., `"dm"`, `"aj"`) are strictly mapped to human-readable static constants in `src/main/java/br/com/saveeditor/brasfoot/util/BrasfootConstants.java` rather than being hardcoded in service logic.

---

*Convention analysis: 2026-04-05*
