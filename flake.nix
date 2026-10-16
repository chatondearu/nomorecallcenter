{
  description = "nomorecallcenter — Node monorepo + Android APK development environment";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs =
    { nixpkgs, flake-utils, ... }:
    flake-utils.lib.eachDefaultSystem (
      system:
      let
        pkgs = import nixpkgs {
          inherit system;
          config = {
            allowUnfree = true;
            android_sdk.accept_license = true;
          };
        };
        # AGP 8.7 + compileSdk 35 (android/app/build.gradle.kts)
        androidComposition = pkgs.androidenv.composeAndroidPackages {
          cmdLineToolsVersion = "13.0";
          platformToolsVersion = "37.0.1";
          # AGP 8.7 pulls build-tools 34; compileSdk 35 needs platform 35.
          buildToolsVersions = [
            "34.0.0"
            "35.0.0"
          ];
          platformVersions = [ "35" ];
          includeEmulator = false;
          includeSystemImages = false;
          includeNDK = false;
        };
        androidSdk = androidComposition.androidsdk;
        androidHome = "${androidSdk}/libexec/android-sdk";
      in
      {
        devShells.default = pkgs.mkShell {
          packages = with pkgs; [
            nodejs_22
            pnpm
            git
            jdk17
            docker-compose
            android-tools
            androidSdk
          ];

          ANDROID_HOME = androidHome;
          ANDROID_SDK_ROOT = androidHome;
          JAVA_HOME = "${pkgs.jdk17}";

          shellHook = ''
            # Prefer nixpkgs pnpm over a project-local corepack shim.
            export PATH="${pkgs.pnpm}/bin:${pkgs.nodejs_22}/bin:$PATH"
            export PNPM_HOME="''${PNPM_HOME:-$PWD/.pnpm}"
            export PATH="$PNPM_HOME:$PATH"
            export JAVA_HOME="${pkgs.jdk17}"
            export ANDROID_HOME="${androidHome}"
            export ANDROID_SDK_ROOT="$ANDROID_HOME"
            echo "[nix develop] node=$(node --version) pnpm=$(pnpm --version) java=$(java -version 2>&1 | head -1)"
            echo "[nix develop] ANDROID_HOME=$ANDROID_HOME"
            echo "[nix develop] next: pnpm install | cd android && ./gradlew :matcher:test :app:assembleDebug"
          '';
        };
      }
    );
}
