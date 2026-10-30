# SkySense (Fabric, Minecraft 26.2)

An in-game menu with a sidebar, a resizable window, and a rebindable open key (default: INSERT).
Rebind it in Options > Controls > Key Binds > "SkySense".

## Build without installing anything (GitHub Actions)
1. Create a new GitHub repository and upload everything in this folder.
   (Hidden folders like `.github` are easy to miss - if it isn't in the repo, add the file
   `.github/workflows/build.yml` through "Add file > Create new file".)
2. Open the repo's Actions tab. The "Build mod" workflow runs on every push (or press "Run workflow").
3. When it finishes, open the run and download the "skysense-jar" artifact. Unzip it to get the .jar.
4. Put the jar in `.minecraft/mods` together with Fabric API for 26.2.

## Build locally
1. Install JDK 25.
2. `gradle wrapper --gradle-version 9.5.1`, then `./gradlew build` (Windows: `gradlew.bat build`).
3. Your mod is `build/libs/skysense-1.0.0.jar` (ignore the `-sources` jar).

## Fonts
See `src/main/resources/assets/skysense/font/README.txt`.
