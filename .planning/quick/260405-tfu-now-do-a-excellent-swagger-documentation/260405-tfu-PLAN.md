---
phase: quick
plan: 260405-tfu
type: execute
wave: 1
depends_on: []
files_modified: []
autonomous: true
requirements: []
must_haves:
  truths:
    - "Swagger UI is available and accessible"
    - "API Documentation provides clear, descriptive titles, descriptions, and contact information"
    - "Core endpoints have rich descriptions so anyone can understand them"
  artifacts:
    - path: "pom.xml"
      provides: "Swagger dependency"
    - path: "src/main/java/br/com/saveeditor/brasfoot/config/OpenApiConfig.java"
      provides: "Global OpenAPI metadata configuration"
  key_links:
    - from: "Swagger UI"
      to: "/v3/api-docs"
      via: "Web browser access to /swagger-ui.html"
      pattern: "OpenApiConfig"
---

<objective>
Implement excellent, easy-to-understand Swagger/OpenAPI documentation for the REST API.
Purpose: To ensure anyone can interact with and understand the Brasfoot Save Editor API endpoints, capabilities, and expected payloads.
Output: Integrated Swagger UI with rich global metadata and documented key controllers.
</objective>

<execution_context>
@$HOME/.config/opencode/get-shit-done/workflows/execute-plan.md
</execution_context>

<context>
@pom.xml
</context>

<tasks>

<task type="auto">
  <name>Task 1: Add Springdoc OpenAPI dependency</name>
  <files>pom.xml</files>
  <action>Add `springdoc-openapi-starter-webmvc-ui` (compatible with Spring Boot 3.x) to the `pom.xml` dependencies list. Also, add the `spring-boot-starter-validation` dependency if it's not present, as Springdoc uses it to enhance documentation with constraints.</action>
  <verify>
    <automated>mvn dependency:tree | grep springdoc</automated>
  </verify>
  <done>The dependency is correctly added and resolvable via Maven.</done>
</task>

<task type="auto">
  <name>Task 2: Create global OpenAPI Configuration</name>
  <files>src/main/java/br/com/saveeditor/brasfoot/config/OpenApiConfig.java</files>
  <action>Create `OpenApiConfig` class. Use `@Configuration` and `@OpenAPIDefinition`. Define rich `@Info` including a clear title ("Brasfoot Save Editor API"), version ("v1"), descriptive summary/description explaining the workflow (Upload save -> Edit in-memory -> Download), and contact information. Define server URLs.</action>
  <verify>
    <automated>cat src/main/java/br/com/saveeditor/brasfoot/config/OpenApiConfig.java | grep "@OpenAPIDefinition"</automated>
  </verify>
  <done>Global configuration class is created with comprehensive metadata.</done>
</task>

<task type="auto">
  <name>Task 3: Annotate core controllers</name>
  <files>src/main/java/br/com/saveeditor/brasfoot/web/controller/*.java</files>
  <action>Find the main REST Controllers (e.g., SessionController, PlayerController, TeamController, BatchController). Add descriptive `@Tag(name = "...", description = "...")` to the classes. For complex endpoints (like file upload, batch editing, or downloading), add `@Operation(summary = "...", description = "...")` and document the expected responses with `@ApiResponse`. Ensure terminology is plain and easy for a beginner to grasp.</action>
  <verify>
    <automated>grep -r "@Operation" src/main/java/br/com/saveeditor/brasfoot/web/controller/</automated>
  </verify>
  <done>Main controllers have human-readable Swagger annotations.</done>
</task>

</tasks>
