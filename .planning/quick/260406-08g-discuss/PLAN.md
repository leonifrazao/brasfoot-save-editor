---
phase: quick
plan: "260406-08g-extract-mappers"
type: "auto"
wave: 1
autonomous: true
---

# Extract Mappers from Controllers

## Objective

Move mapping logic from web controllers into dedicated, reusable mapper classes to enforce Hexagonal Architecture separation of concerns.

## Context

Current state: PlayerController, TeamController, and ManagerController contain embedded `mapToDto()`, `toDto()`, and `toDomain()` methods. This violates SoC in Hexagonal Architecture.

Target state: Dedicated mapper classes in `adapters/in/web/mapper/` package, injected into controllers, with all mapping logic extracted.

## Tasks

### Task 1: Create PlayerMapper
**Type**: auto  
**TDD**: false

Create `src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/mapper/PlayerMapper.java` with:
- Method: `PlayerDto toDto(Player player)` 
- Method: `List<PlayerDto> toDtoList(List<Player> players)`

Extract mapping logic from PlayerController.mapToDto() into PlayerMapper.toDto().

**Done Criteria**:
- [ ] PlayerMapper.java created with both methods
- [ ] Mapping logic is identical to the original
- [ ] Code compiles
- [ ] Builds without errors

**Success Verification**:
```bash
mvn clean compile
```

---

### Task 2: Create TeamMapper
**Type**: auto  
**TDD**: false

Create `src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/mapper/TeamMapper.java` with:
- Method: `TeamDto toDto(Team team)`
- Method: `List<TeamDto> toDtoList(List<Team> teams)`

Extract mapping logic from TeamController.mapToDto() into TeamMapper.toDto().

**Done Criteria**:
- [ ] TeamMapper.java created with both methods
- [ ] Mapping logic is identical to the original
- [ ] Code compiles

**Success Verification**:
```bash
mvn clean compile
```

---

### Task 3: Create ManagerMapper
**Type**: auto  
**TDD**: false

Create `src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/mapper/ManagerMapper.java` with:
- Method: `ManagerDto toDto(Manager manager)`
- Method: `Manager toDomain(ManagerUpdateRequest request)`
- Method: `List<ManagerDto> toDtoList(List<Manager> managers)`

Extract mapping logic from ManagerController.toDto() and ManagerController.toDomain() into ManagerMapper.

**Done Criteria**:
- [ ] ManagerMapper.java created with all methods
- [ ] Mapping logic is identical to the original
- [ ] Code compiles

**Success Verification**:
```bash
mvn clean compile
```

---

### Task 4: Inject PlayerMapper into PlayerController
**Type**: auto  
**TDD**: false

Modify `PlayerController`:
1. Add `PlayerMapper playerMapper` field
2. Inject via constructor
3. Replace all `mapToDto()` calls with `playerMapper.toDto()`
4. Replace inline stream mappings with `playerMapper.toDtoList()`
5. Remove `private PlayerDto mapToDto(Player player)` method

**Done Criteria**:
- [ ] Constructor accepts PlayerMapper
- [ ] All mapping calls delegate to mapper
- [ ] Original mapToDto() method removed
- [ ] Code compiles
- [ ] No functionality change (same DTOs returned)

**Success Verification**:
```bash
mvn clean compile && mvn test
```

---

### Task 5: Inject TeamMapper into TeamController
**Type**: auto  
**TDD**: false

Modify `TeamController`:
1. Add `TeamMapper teamMapper` field
2. Inject via constructor
3. Replace all `mapToDto()` calls with `teamMapper.toDto()`
4. Replace inline stream mappings with `teamMapper.toDtoList()`
5. Remove `private TeamDto mapToDto(Team team)` method

**Done Criteria**:
- [ ] Constructor accepts TeamMapper
- [ ] All mapping calls delegate to mapper
- [ ] Original mapToDto() method removed
- [ ] Code compiles

**Success Verification**:
```bash
mvn clean compile && mvn test
```

---

### Task 6: Inject ManagerMapper into ManagerController
**Type**: auto  
**TDD**: false

Modify `ManagerController`:
1. Add `ManagerMapper managerMapper` field
2. Inject via constructor
3. Replace all `toDto()` calls with `managerMapper.toDto()`
4. Replace all `toDomain()` calls with `managerMapper.toDomain()`
5. Replace inline stream mappings with `managerMapper.toDtoList()`
6. Remove `private ManagerDto toDto(Manager manager)` and `private Manager toDomain(ManagerUpdateRequest request)` methods

**Done Criteria**:
- [ ] Constructor accepts ManagerMapper
- [ ] All mapping calls delegate to mapper
- [ ] Original toDto() and toDomain() methods removed
- [ ] Code compiles

**Success Verification**:
```bash
mvn clean compile && mvn test
```

---

## Verification

After all tasks complete:
- [ ] All mappers exist and are properly structured
- [ ] All controllers use injected mappers, not embedded logic
- [ ] Maven builds successfully: `mvn clean package`
- [ ] No test failures
- [ ] No embedded mapping methods remain in controllers

## Output

**Files Created**:
- `src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/mapper/PlayerMapper.java`
- `src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/mapper/TeamMapper.java`
- `src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/mapper/ManagerMapper.java`

**Files Modified**:
- `src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/PlayerController.java`
- `src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/TeamController.java`
- `src/main/java/br/com/saveeditor/brasfoot/adapters/in/web/ManagerController.java`

**Architecture Improvement**:
- Separation of concerns: routing (controllers) vs. transformation (mappers)
- Reusability: mappers can be used by other adapters
- Testability: mappers can be unit tested independently
- Maintainability: DTO changes don't require controller modifications
