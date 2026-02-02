{ pkgs ? import <nixpkgs> { config.allowUnfree = true; } }:

pkgs.mkShell {
  packages = [
    pkgs.jdk17
    pkgs.maven
  ];

  # Set Environment Variables directly
  JAVA_HOME = "${pkgs.jdk17}";
  
  shellHook = ''
    echo "Environment ready. Java: $JAVA_HOME"
  '';
}