---
phase: quick
plan: 1
type: execute
wave: 1
depends_on: []
files_modified:
  - src/main/java/br/com/saveeditor/brasfoot/application/services/PlayerManagementService.java
autonomous: true
requirements: []
must_haves:
  truths:
    - "-1 is accepted as a valid energy value for a player"
    - "Validation failures return a 400 Bad Request with the actual validation message"
  artifacts:
    - path: "src/main/java/br/com/saveeditor/brasfoot/application/services/PlayerManagementService.java"
      provides: "Player update logic with corrected energy validation and exception handling"
  key_links:
    - from: "src/main/java/br/com/saveeditor/brasfoot/application/services/PlayerManagementService.java"
      to: "br.com.saveeditor.brasfoot.adapters.in.web.GlobalExceptionHandler"
      via: "IllegalArgumentException being propagated rather than wrapped in a RuntimeException"
---

<objective>
Fix player energy validation limits and stop swallowing validation exceptions during updates.

Purpose: -1 is a valid energy value in Brasfoot (meaning undefined). Also, validation errors were being wrapped in generic RuntimeExceptions, masking the real error from the user and returning a 500 Internal Server Error instead of a 400 Bad Request.
Output: Corrected `PlayerManagementService.java` that propagates `IllegalArgumentException`.
</objective>

<context>
@src/main/java/br/com/saveeditor/brasfoot/application/services/PlayerManagementService.java
</context>

<tasks>

<task type="auto">
  <name>Task 1: Fix energy validation</name>
  <files>src/main/java/br/com/saveeditor/brasfoot/application/services/PlayerManagementService.java</files>
  <action>
    Locate the `energy != null` validation in the `updatePlayer` method.
    Change the lower bound check from `energy < 0` to `energy < -1`.
    Update the exception message to `"Invalid energy: must be between -1 and 100"`.
  </action>
  <verify>
    <automated>grep -q "energy < -1 || energy > 100" src/main/java/br/com/saveeditor/brasfoot/application/services/PlayerManagementService.java</automated>
  </verify>
  <done>Energy validation accepts -1 as a valid lower limit.</done>
</task>

<task type="auto">
  <name>Task 2: Stop swallowing IllegalArgumentException in updatePlayer</name>
  <files>src/main/java/br/com/saveeditor/brasfoot/application/services/PlayerManagementService.java</files>
  <action>
    In the `updatePlayer` method, there is a `try-catch (Exception e)` block surrounding the validation checks and reflection property setting.
    Add a `catch (IllegalArgumentException e)` block *before* the generic `Exception` catch block that simply re-throws the `IllegalArgumentException` (so it isn't wrapped in a `RuntimeException`).
    Optionally log the validation error at debug/warn level before re-throwing.
  </action>
  <verify>
    <automated>grep -q "catch (IllegalArgumentException" src/main/java/br/com/saveeditor/brasfoot/application/services/PlayerManagementService.java</automated>
  </verify>
  <done>IllegalArgumentException is explicitly caught and re-thrown without being wrapped in a generic RuntimeException.</done>
</task>

</tasks>
