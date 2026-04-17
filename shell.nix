{ pkgs ? import <nixpkgs> { config.allowUnfree = true; } }:

pkgs.mkShell {
  packages = [
    # Backend dependencies
    pkgs.jdk17
    pkgs.maven

    # Frontend dependencies
    pkgs.nodejs_20

    # Infrastructure
    pkgs.docker
    pkgs.docker-compose
  ];

  # Set Environment Variables directly
  JAVA_HOME = "${pkgs.jdk17}";
  
  shellHook = ''
    echo "Brasfoot Save Editor - Development Environment Ready!"
    echo "==================================================="
    echo "Java version: $(${pkgs.jdk17}/bin/java -version 2>&1 | head -n 1)"
    echo "Node version: $(${pkgs.nodejs_20}/bin/node --version)"
    echo "npm version: $(${pkgs.nodejs_20}/bin/npm --version)"
    echo "==================================================="
    echo "Useful commands:"
    echo "  - Backend  : mvn spring-boot:run"
    echo "  - Frontend : cd frontend && npm run dev"
    echo "  - Docker   : docker compose up -d"
  '';
}