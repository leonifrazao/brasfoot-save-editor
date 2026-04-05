# Architecture

**Analysis Date:** 2026-04-05

## Pattern Overview

**Overall:** Interactive CLI Application (Spring Shell / Spring Boot)

**Key Characteristics:**
- Command-driven interactive shell
- Stateful session management holding the current loaded save
- Layered service architecture separating command parsing from domain logic
- In-memory data manipulation with undo/redo capabilities

## Layers

**Shell (Presentation):**
- Purpose: Handles user input, parses commands, formats terminal output
- Location: `src/main/java/br/com/saveeditor/brasfoot/shell/`
- Contains: Spring Shell `@ShellComponent` classes and Prompt Providers
- Depends on: Services, Shell Context
- Used by: Spring Shell framework

**Context (State Management):**
- Purpose: Maintains the session state (current file, active team, etc.)
- Location: `src/main/java/br/com/saveeditor/brasfoot/shell/EditorShellContext.java`
- Contains: Session state beans
- Depends on: Models
- Used by: Shell Commands, Services

**Service (Business Logic):**
- Purpose: Core logic for editing game data, applying presets, managing files
- Location: `src/main/java/br/com/saveeditor/brasfoot/service/`
- Contains: Spring `@Service` components (e.g., `SaveFileService`, `CheatService`, `UndoService`)
- Depends on: Models, Context
- Used by: Shell Layer

**Model (Domain):**
- Purpose: In-memory representation of the Brasfoot save file data structures
- Location: `src/main/java/br/com/saveeditor/brasfoot/model/`
- Contains: POJOs representing Teams, Players, Game State
- Depends on: None
- Used by: Services, Shell, Context

## Data Flow

**Command Execution Flow:**

1. User enters a command in the terminal (e.g., `cheat money 1000000`).
2. Spring Shell routes to the appropriate method in a command class (e.g., `CheatCommands`).
3. Command class delegates to a Service (e.g., `CheatService.applyMoneyCheat()`).
4. Service updates the in-memory Model and registers the change with `UndoService`.
5. Service returns success/failure to the Command class.
6. Command class formats the result and displays it to the user.

**State Management:**
- Session state is held in `EditorShellContext`.
- Loaded save data is held in memory, manipulated, and only written back to disk via an explicit `save` command via `SaveFileService`.

## Key Abstractions

**Save File Handling:**
- Purpose: Reads binary/structured save files into object models and writes them back.
- Examples: `src/main/java/br/com/saveeditor/brasfoot/service/SaveFileService.java`
- Pattern: Repository / Data Mapper pattern.

**Undo/Redo System:**
- Purpose: Tracks changes made to models to allow reverting actions.
- Examples: `src/main/java/br/com/saveeditor/brasfoot/service/UndoService.java`
- Pattern: Command / Memento pattern (likely implementing snapshots or action logs).

## Entry Points

**Spring Boot Application:**
- Location: `src/main/java/br/com/saveeditor/brasfoot/BrasfootEditorApplication.java`
- Triggers: Java execution via CLI/Maven
- Responsibilities: Bootstraps the Spring context, initializes Spring Shell, and starts the interactive loop.

**Interactive Shell:**
- Location: `src/main/java/br/com/saveeditor/brasfoot/shell/CustomPromptProvider.java`
- Triggers: Shell loop
- Responsibilities: Displays dynamic terminal prompts based on current application state.

## Error Handling

**Strategy:** Exception translation to user-friendly shell messages

**Patterns:**
- Services throw domain-specific exceptions.
- Shell command methods catch exceptions and return formatted error strings to the terminal (e.g., colored red).
- Spring Shell default exception handlers for unknown commands.

## Cross-Cutting Concerns

**Logging:** Standard SLF4J / Logback (Spring Boot default), likely routing to a file to avoid messing up the interactive terminal output.
**Validation:** Validating bounds on player attributes and funds before applying cheats.
**Configuration:** `src/main/java/br/com/saveeditor/brasfoot/config/` containing `@Configuration` beans for customizing the shell environment.

---

*Architecture analysis: 2026-04-05*
