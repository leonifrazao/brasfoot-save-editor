# Codebase Concerns

**Analysis Date:** 2026-04-05

## Tech Debt

**Logging and Output:**
- Issue: Hardcoded `System.out.println` statements used directly in core business logic services instead of a logging framework (e.g., SLF4J/Logback) or returning results to the shell layer.
- Files: `src/main/java/br/com/saveeditor/brasfoot/service/CheatService.java`, `src/main/java/br/com/saveeditor/brasfoot/config/PreferencesManager.java`
- Impact: Makes it impossible to configure log levels, send output to files, or cleanly separate UI text from business logic. Tests (when added) will spam the console.
- Fix approach: Introduce a proper logging facade or refactor services to return Result objects/DTOs that the Shell commands can print.

**Error Handling / Nullability:**
- Issue: Widespread use of `return null` as a fallback or error indicator instead of `Optional` or throwing domain exceptions.
- Files: `src/main/java/br/com/saveeditor/brasfoot/service/GameDataService.java`, `src/main/java/br/com/saveeditor/brasfoot/service/PresetService.java`, `src/main/java/br/com/saveeditor/brasfoot/config/LabelTranslator.java`
- Impact: High risk of `NullPointerException`s (NPEs) throughout the application if callers forget to check for nulls.
- Fix approach: Refactor methods to return `java.util.Optional<T>` or throw custom exceptions for expected failure cases.

## Known Bugs

**Not detected**

## Security Considerations

**Not applicable**

## Performance Bottlenecks

**Not detected**

## Fragile Areas

**Reflection Utilities:**
- Files: `src/main/java/br/com/saveeditor/brasfoot/util/ReflectionUtils.java`
- Why fragile: Relies on reflection which can easily break during refactoring or when upgrading Java versions if module encapsulation changes.
- Safe modification: Ensure exhaustive tests are written for this class before making any structural changes to the classes it inspects.
- Test coverage: Complete gap.

## Scaling Limits

**Not detected**

## Dependencies at Risk

**Not detected**

## Missing Critical Features

**Not detected**

## Test Coverage Gaps

**Core Logic and Services:**
- What's not tested: The entire application. There is no `src/test` directory or any test framework configured.
- Files: `src/main/java/**/*.java`
- Risk: Any refactoring, dependency updates, or feature additions have a high likelihood of introducing regressions. There is no automated safety net for critical editor logic or cheat injections.
- Priority: High

---

*Concerns audit: 2026-04-05*