<!-- GSD:project-start source:PROJECT.md -->
## Project

**Brasfoot Save Editor API**

A REST API for editing Brasfoot save game files. The application allows users to upload a save file, edit in-memory game state (Teams, Players, Managers, Tournaments, Finances), and download the modified save file. It is built using Java, Spring Boot, and Maven, and strictly follows Hexagonal Architecture (Ports and Adapters).

**Core Value:** Enable programmatic, reliable, and comprehensive editing of Brasfoot save files through a clean REST API without any coupled UI or dead code.

### Constraints

- **Architecture**: Strict Hexagonal Architecture (Ports and Adapters) — To ensure the domain logic is completely decoupled from web and file I/O concerns.
- **Tech Stack**: Java, Spring Boot, Maven — Standardizing on the Spring ecosystem for the API layer.
- **State Management**: In-memory editing — The API must accept a file, keep the state in memory for editing via subsequent API calls (or edit in a single transaction), and provide a way to download the result.
<!-- GSD:project-end -->

<!-- GSD:stack-start source:codebase/STACK.md -->
## Technology Stack

## Languages
- Java 17 - Core application logic
- Shell/Bash - Nix shell environments (`shell.nix`)
## Runtime
- JVM (Java Virtual Machine) 17+
- Maven 3.x
- Lockfile: Missing (Standard Maven behavior doesn't enforce a lockfile like `package.json`, depends on `pom.xml`)
## Frameworks
- Spring Boot 3.2.1 - Application foundation and dependency injection
- Spring Shell 3.2.0 - Interactive CLI framework
- Not explicitly defined in the initial POM parsing (Likely JUnit 5 via Spring Boot Starter Test if added)
- Maven Assembly Plugin - Creating executable jars/distributions
- Nix (`shell.nix`) - Reproducible development environments
## Key Dependencies
- `com.brasfoot:brasfoot-game:1.0` - Proprietary game jar used to interact with the game's internal data structures.
- `com.esotericsoftware:kryo:4.0.2` - Fast object graph serialization framework, likely used to read/write the save game files.
- `com.google.code.gson:gson:2.8.9` - JSON processing (e.g., presets, configurations).
- `com.formdev:flatlaf:3.2.5` - Cross-platform Look and Feel (implies some GUI capabilities alongside the Spring Shell).
- `com.esotericsoftware:reflectasm:1.11.5` - High performance reflection used by Kryo.
- `org.objenesis:objenesis:2.5.1` - Instantiating objects without calling constructors (used in serialization).
## Configuration
- `src/main/resources/application.properties` - Spring Boot configuration
- `config.properties` - External application properties
- `pom.xml` - Maven project object model and build configuration
## Platform Requirements
- JDK 17
- Maven
- JRE 17+
- Local filesystem access for reading/writing save files.
<!-- GSD:stack-end -->

<!-- GSD:conventions-start source:CONVENTIONS.md -->
## Conventions

## Naming Patterns
- PascalCase for all classes and interfaces (e.g., `BrasfootEditorApplication.java`, `GeneralCommands.java`).
- Suffixes indicate the class role (e.g., `*Service.java` for business logic, `*Commands.java` for CLI controllers, `*Constants.java` for static constants).
- camelCase (e.g., `loadFile`, `saveService`).
- Descriptive names reflecting the action performed.
- camelCase for standard variables.
- UPPER_SNAKE_CASE for `public static final` constants (e.g., `TEAMS_LIST` in `src/main/java/br/com/saveeditor/brasfoot/util/BrasfootConstants.java`).
## Code Style
- Standard Java indentation (likely 4 spaces).
- Standard Java brace placement (same line for class/method declarations).
- No explicit linter configuration (like Checkstyle or PMD) detected in the project root.
## Architecture & Dependency Management
- Spring Boot constructor injection is the primary pattern used.
- Classes declare `final` dependencies and initialize them via constructors (e.g., `GeneralCommands.java`).
- Relies on Spring Shell annotations (`@ShellComponent`, `@ShellMethod`, `@ShellOption`).
- Commands are organized functionally (e.g., `GeneralCommands.java`, `CheatCommands.java`, `PresetCommands.java`).
## Import Organization
## Error Handling & Logging
- No robust logging framework configuration (like `slf4j` / `Logback`) used for standard output.
- Custom `ConsoleHelper` class (`src/main/java/br/com/saveeditor/brasfoot/util/ConsoleHelper.java`) is used to print colored warnings, errors, and confirmations directly to the terminal UI.
## Comments
- Javadoc comments are used for class-level documentation, especially in utility classes like `src/main/java/br/com/saveeditor/brasfoot/util/BrasfootConstants.java` to explain obfuscated game fields.
- Inline comments explain specific logical checks or safety features (e.g., unsaved changes prompt in `GeneralCommands.java`).
## Obfuscated Code Management
- Due to the nature of editing a closed-source game (Brasfoot), obfuscated fields (e.g., `"dm"`, `"aj"`) are strictly mapped to human-readable static constants in `src/main/java/br/com/saveeditor/brasfoot/util/BrasfootConstants.java` rather than being hardcoded in service logic.
<!-- GSD:conventions-end -->

<!-- GSD:architecture-start source:ARCHITECTURE.md -->
## Architecture

## Pattern Overview
- Command-driven interactive shell
- Stateful session management holding the current loaded save
- Layered service architecture separating command parsing from domain logic
- In-memory data manipulation with undo/redo capabilities
## Layers
- Purpose: Handles user input, parses commands, formats terminal output
- Location: `src/main/java/br/com/saveeditor/brasfoot/shell/`
- Contains: Spring Shell `@ShellComponent` classes and Prompt Providers
- Depends on: Services, Shell Context
- Used by: Spring Shell framework
- Purpose: Maintains the session state (current file, active team, etc.)
- Location: `src/main/java/br/com/saveeditor/brasfoot/shell/EditorShellContext.java`
- Contains: Session state beans
- Depends on: Models
- Used by: Shell Commands, Services
- Purpose: Core logic for editing game data, applying presets, managing files
- Location: `src/main/java/br/com/saveeditor/brasfoot/service/`
- Contains: Spring `@Service` components (e.g., `SaveFileService`, `CheatService`, `UndoService`)
- Depends on: Models, Context
- Used by: Shell Layer
- Purpose: In-memory representation of the Brasfoot save file data structures
- Location: `src/main/java/br/com/saveeditor/brasfoot/model/`
- Contains: POJOs representing Teams, Players, Game State
- Depends on: None
- Used by: Services, Shell, Context
## Data Flow
- Session state is held in `EditorShellContext`.
- Loaded save data is held in memory, manipulated, and only written back to disk via an explicit `save` command via `SaveFileService`.
## Key Abstractions
- Purpose: Reads binary/structured save files into object models and writes them back.
- Examples: `src/main/java/br/com/saveeditor/brasfoot/service/SaveFileService.java`
- Pattern: Repository / Data Mapper pattern.
- Purpose: Tracks changes made to models to allow reverting actions.
- Examples: `src/main/java/br/com/saveeditor/brasfoot/service/UndoService.java`
- Pattern: Command / Memento pattern (likely implementing snapshots or action logs).
## Entry Points
- Location: `src/main/java/br/com/saveeditor/brasfoot/BrasfootEditorApplication.java`
- Triggers: Java execution via CLI/Maven
- Responsibilities: Bootstraps the Spring context, initializes Spring Shell, and starts the interactive loop.
- Location: `src/main/java/br/com/saveeditor/brasfoot/shell/CustomPromptProvider.java`
- Triggers: Shell loop
- Responsibilities: Displays dynamic terminal prompts based on current application state.
## Error Handling
- Services throw domain-specific exceptions.
- Shell command methods catch exceptions and return formatted error strings to the terminal (e.g., colored red).
- Spring Shell default exception handlers for unknown commands.
## Cross-Cutting Concerns
<!-- GSD:architecture-end -->

<!-- GSD:skills-start source:skills/ -->
## Project Skills

No project skills found. Add skills to any of: `.claude/skills/`, `.agents/skills/`, `.cursor/skills/`, or `.github/skills/` with a `SKILL.md` index file.
<!-- GSD:skills-end -->

<!-- GSD:workflow-start source:GSD defaults -->
## GSD Workflow Enforcement

Before using Edit, Write, or other file-changing tools, start work through a GSD command so planning artifacts and execution context stay in sync.

Use these entry points:
- `/gsd-quick` for small fixes, doc updates, and ad-hoc tasks
- `/gsd-debug` for investigation and bug fixing
- `/gsd-execute-phase` for planned phase work

Do not make direct repo edits outside a GSD workflow unless the user explicitly asks to bypass it.
<!-- GSD:workflow-end -->



<!-- GSD:profile-start -->
## Developer Profile

> Profile not yet configured. Run `/gsd-profile-user` to generate your developer profile.
> This section is managed by `generate-claude-profile` -- do not edit manually.
<!-- GSD:profile-end -->
