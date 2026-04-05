# Codebase Structure

**Analysis Date:** 2026-04-05

## Directory Layout

```
/mnt/c/Users/shadz/OneDrive/Documentos/brasfoot-save-editor/
├── src/main/java/br/com/saveeditor/brasfoot/
│   ├── config/       # Spring Configuration (Shell, Logging)
│   ├── model/        # Domain Models (Teams, Players, Save structures)
│   ├── service/      # Business Logic (Cheats, Files, State)
│   ├── shell/        # Spring Shell Commands and Context
│   └── util/         # Utility classes (Parsing, Formatting)
├── src/main/resources/
│   ├── presets/      # Default cheat presets (e.g., max-money)
│   └── application.yml/properties # Spring Boot configuration
├── lib/              # External libraries not present in Maven Central
├── presets/          # User-defined presets
└── target/           # Maven build output
```

## Directory Purposes

**config/:**
- Purpose: Spring Boot beans and configuration settings
- Contains: Java `@Configuration` classes, shell prompt customization
- Key files: `src/main/java/br/com/saveeditor/brasfoot/config/*Config.java`

**model/:**
- Purpose: The in-memory data structures that represent a Brasfoot save
- Contains: POJOs, Enums
- Key files: `src/main/java/br/com/saveeditor/brasfoot/model/*.java`

**service/:**
- Purpose: Execute business rules, manipulate models, and handle file operations
- Contains: Spring `@Service` classes doing the actual work
- Key files: `src/main/java/br/com/saveeditor/brasfoot/service/CheatService.java`, `SaveFileService.java`

**shell/:**
- Purpose: Define the commands available to the user in the interactive terminal
- Contains: Spring `@ShellComponent` classes and the application state context
- Key files: `src/main/java/br/com/saveeditor/brasfoot/shell/EditorShellContext.java`, `GeneralCommands.java`

## Key File Locations

**Entry Points:**
- `src/main/java/br/com/saveeditor/brasfoot/BrasfootEditorApplication.java`: Spring Boot Application class

**Configuration:**
- `pom.xml`: Maven build and dependencies
- `src/main/resources/application.properties`: or `.yml`, holds Spring profiles

**Core Logic:**
- `src/main/java/br/com/saveeditor/brasfoot/service/CheatService.java`: Logic for modifying game attributes
- `src/main/java/br/com/saveeditor/brasfoot/shell/EditorShellContext.java`: Holds the currently loaded save file and session state

**Testing:**
- `src/test/java/br/com/saveeditor/brasfoot/`: Unit and integration tests (Spring Boot Test)

## Naming Conventions

**Files:**
- Commands: `*Commands.java` (e.g., `CheatCommands.java`)
- Services: `*Service.java` (e.g., `SaveFileService.java`)
- Configuration: `*Config.java`

**Directories:**
- Standard Maven convention (`src/main/java`, `src/main/resources`, `src/test/java`)

## Where to Add New Code

**New Command/Feature:**
- Primary code: `src/main/java/br/com/saveeditor/brasfoot/shell/MyFeatureCommands.java`
- Logic/Service: `src/main/java/br/com/saveeditor/brasfoot/service/MyFeatureService.java`
- Tests: `src/test/java/br/com/saveeditor/brasfoot/service/MyFeatureServiceTest.java`

**New Domain Model:**
- Implementation: `src/main/java/br/com/saveeditor/brasfoot/model/`

**Utilities:**
- Shared helpers: `src/main/java/br/com/saveeditor/brasfoot/util/`

## Special Directories

**presets/:**
- Purpose: Contains text or JSON files representing saved configurations of cheats that can be applied in bulk
- Generated: User-created or loaded dynamically
- Committed: Root `presets/` is likely a working directory, `src/main/resources/presets/` are default bundled presets.

**lib/:**
- Purpose: Local jar files for unmanaged dependencies
- Generated: No
- Committed: Yes

---

*Structure analysis: 2026-04-05*
