# Technology Stack

**Analysis Date:** 2026-04-05

## Languages

**Primary:**
- Java 17 - Core application logic

**Secondary:**
- Shell/Bash - Nix shell environments (`shell.nix`)

## Runtime

**Environment:**
- JVM (Java Virtual Machine) 17+

**Package Manager:**
- Maven 3.x
- Lockfile: Missing (Standard Maven behavior doesn't enforce a lockfile like `package.json`, depends on `pom.xml`)

## Frameworks

**Core:**
- Spring Boot 3.2.1 - Application foundation and dependency injection
- Spring Shell 3.2.0 - Interactive CLI framework

**Testing:**
- Not explicitly defined in the initial POM parsing (Likely JUnit 5 via Spring Boot Starter Test if added)

**Build/Dev:**
- Maven Assembly Plugin - Creating executable jars/distributions
- Nix (`shell.nix`) - Reproducible development environments

## Key Dependencies

**Critical:**
- `com.brasfoot:brasfoot-game:1.0` - Proprietary game jar used to interact with the game's internal data structures.
- `com.esotericsoftware:kryo:4.0.2` - Fast object graph serialization framework, likely used to read/write the save game files.

**Infrastructure/Utilities:**
- `com.google.code.gson:gson:2.8.9` - JSON processing (e.g., presets, configurations).
- `com.formdev:flatlaf:3.2.5` - Cross-platform Look and Feel (implies some GUI capabilities alongside the Spring Shell).
- `com.esotericsoftware:reflectasm:1.11.5` - High performance reflection used by Kryo.
- `org.objenesis:objenesis:2.5.1` - Instantiating objects without calling constructors (used in serialization).

## Configuration

**Environment:**
- `src/main/resources/application.properties` - Spring Boot configuration
- `config.properties` - External application properties

**Build:**
- `pom.xml` - Maven project object model and build configuration

## Platform Requirements

**Development:**
- JDK 17
- Maven

**Production:**
- JRE 17+
- Local filesystem access for reading/writing save files.

---

*Stack analysis: 2026-04-05*