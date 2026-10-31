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

## Notes
Settings are saved to `.minecraft/config/skysense.properties`.
The menu uses Minecraft's own font.

## What SkySense does - and doesn't do
SkySense is a **client-side, cosmetic** mod. It gives you no gameplay advantage.

**Does**
- Adds an in-game menu window (open key is rebindable in Options > Controls).
- "Hide Absorption Hearts": when switched on, your own HUD simply doesn't draw absorption hearts.
  The only game value it reads is your own absorption amount, and it never changes it.
- "Viewmodel" (Visual tab): changes how your own held item and hand are drawn on YOUR screen - position, rotation,
  scale, swing speed, and a few swing-animation tweaks. Nobody else sees it, and attack/interaction packets are untouched.
- Saves its settings to `config/skysense.properties`.

**Doesn't**
- Send or modify any network packets. It has no networking code at all.
- Read other players, entities, or the world. It cannot reveal hidden information.
- Move you, aim, attack, interact, or automate any input. It only listens to its own keybind and clicks inside its own menu.
- Run on a server. It is marked client-only (`"environment": "client"`), so a dedicated server ignores it and servers don't need it installed.

Every server sets its own rules about client mods, so check yours. HUD-only mods like this one are commonly allowed,
but SkySense can't promise that for any particular server.

