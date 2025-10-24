{ pkgs }:
{
  deps = [
    pkgs.curl
    pkgs.unzip
    pkgs.which
    pkgs.git
    pkgs.jdk17
    pkgs.gradle
    pkgs.glibcLocales
  ];
}
