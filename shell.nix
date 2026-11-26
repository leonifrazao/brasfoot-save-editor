{ pkgs ? import <nixpkgs> { config.allowUnfree = true; } }:

pkgs.mkShell {
  packages = [
    pkgs.jdk17
    pkgs.maven
  ];

  JAVA_HOME = "${pkgs.jdk17}";

  shellHook = ''
    echo "Brasfoot Save Editor - Development Environment Ready!"
    echo "==================================================="
    echo "Java version: $(${pkgs.jdk17}/bin/java -version 2>&1 | head -n 1)"
    echo "Useful commands:"
    echo "  - Abrir app : mvn spring-boot:run"
    echo "  - Testes    : mvn test"
  '';
}
