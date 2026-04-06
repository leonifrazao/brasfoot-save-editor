# Discussion: Extract Mappers from Controllers

## Problem Statement

The current codebase has mapping logic embedded directly in controller classes:

- **PlayerController**: Lines 117-127 contain `mapToDto()` method
- **TeamController**: Lines 106-113 contain `mapToDto()` method  
- **ManagerController**: Lines 117-130 contain `toDto()` and lines 132-140 contain `toDomain()` methods

Additionally, inline stream mapping occurs at multiple points (e.g., PlayerController lines 44-46, 104-106).

## Hexagonal Architecture Concern

Per the project's strict Hexagonal Architecture (Ports and Adapters), mappers should be **separate adapter concerns**, not embedded in controllers. This violates the Single Responsibility Principle:

- Controllers should handle HTTP request/response lifecycle
- Mappers should handle domain ↔ DTO transformation
- Separation allows mappers to be reused, tested independently, and potentially support multiple adapters

## Proposed Solution

Create dedicated mapper classes in a new package:
```
src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/mapper/
  ├── PlayerMapper.java
  ├── TeamMapper.java
  └── ManagerMapper.java
```

Each mapper:
1. Converts domain objects to DTOs (e.g., `Player` → `PlayerDto`)
2. Converts request objects to domain objects (e.g., `ManagerUpdateRequest` → `Manager`)
3. Injected into controllers via constructor dependency injection

Controllers will call mappers instead of containing mapping logic.

## Benefits

- **Better SoC**: Mapping logic is separated from routing/HTTP handling
- **Reusability**: Mappers can be used by multiple adapters (REST, GraphQL, etc.)
- **Testability**: Mappers can be unit tested independently
- **Maintainability**: Changes to DTO structure don't touch controller logic
- **Scalability**: New adapters can reuse the same mappers

## Assumptions

1. All DTOs use record syntax (modern Java)
2. Domain objects use getter/setter or record accessors
3. Mappers will be simple POJO transformations (no complex logic)
4. Controllers will use constructor injection for mapper dependencies

## Decisions

- **Naming**: Use `{Entity}Mapper` pattern (e.g., `PlayerMapper`, not `PlayerDtoFactory`)
- **Scope**: Extract PlayerMapper, TeamMapper, ManagerMapper initially
- **Injection**: Constructor-based DI via Spring
- **Testing**: Unit tests for each mapper (TDD approach optional)
- **Removal**: After extracting, remove all `map*()` and `to*()` methods from controllers
