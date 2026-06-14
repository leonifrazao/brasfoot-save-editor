{ pkgs ? import <nixpkgs> { config.allowUnfree = true; } }:

let
  qtLibraryPath = pkgs.lib.makeLibraryPath [
    pkgs.qt6.qtbase
    pkgs.qt6.qtwayland
  ];
  qtPluginPath = "${pkgs.qt6.qtbase}/lib/qt-6/plugins:${pkgs.qt6.qtwayland}/lib/qt-6/plugins";
in
pkgs.mkShell {
  packages = [
    # Desktop app dependencies
    pkgs.jdk17
    pkgs.maven
    pkgs.qt6.qtbase
    pkgs.qt6.qtwayland
  ];

  JAVA_HOME = "${pkgs.jdk17}";
  LD_LIBRARY_PATH = qtLibraryPath;
  QT_PLUGIN_PATH = qtPluginPath;
  QT_QPA_PLATFORM_PLUGIN_PATH = "${pkgs.qt6.qtbase}/lib/qt-6/plugins/platforms";

  shellHook = ''
    echo "Brasfoot Save Editor - Development Environment Ready!"
    echo "==================================================="
    echo "Java version: $(${pkgs.jdk17}/bin/java -version 2>&1 | head -n 1)"
    echo "Qt version: ${pkgs.qt6.qtbase.version}"
    echo "==================================================="
    echo "Useful commands:"
    echo "  - Abrir app : mvn spring-boot:run"
    echo "  - Testes    : mvn test"
  '';
}
