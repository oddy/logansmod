# Logan's mod — working notes for Claude

## People

- Logan owns this project and decides what the mod does. He mostly talks by voice, so keep replies short, simple and friendly, and avoid jargon.
- His dad Beau sets up tooling and may step in with technical notes.
- Designs brainstormed with Logan in chat (shapes, colors, behavior) are the source of truth for models and skins. When Logan makes his own Blockbench model for something, his version wins.

## Platform

- Minecraft 26.2, NeoForge 26.2.0.88, ModDevGradle, Java 25 (Mojang mappings).
- Built from the official MDK: https://github.com/NeoForgeMDKs/MDK-26.2-ModDevGradle
- Version is `mod_version` in `gradle.properties`. Bump it for each playtest build.

## Build and test

1. `./gradlew build` — jar lands in `build/libs/`.
2. Smoke-test on a dev server before handing anything over. Stdin isn't forwarded to `runServer`, so enable RCON in `run/server.properties` (`enable-rcon=true`, `rcon.port=25575`, `rcon.password=...`) and send commands like `summon hollowguest:hollow_guest 0 -60 0` over RCON. Check `run/logs/latest.log` for errors.
3. Commit and push to `oddy/logansmod` (`main`).

## Playtest step (after every build Logan should try)

- Copy the jar into the Prism instance shown as "logans-mod-playtest". On disk that's
  `C:\Users\logan\AppData\Roaming\PrismLauncher\instances\26.2(1)\minecraft\mods`
  (the folder is named `26.2(1)`; the display name lives in `instance.cfg`).
- Remove the previous `hollowguest-*.jar` from that folder so there's only one copy. The device file tools can't delete; if an old jar is still there, ask Beau to remove it or overwrite by using the same file name.
- Logan launches it from Prism himself.

## Blockbench

- Logan's Blockbench projects and skins are in `C:\Users\logan\work` (loose `.bbmodel` and `.png` files; the `blockbench` subfolder is unused).
- Read `.bbmodel` files directly (they're JSON with embedded textures). Never launch Blockbench; if you need to see his screen, only take screenshots of or switch to his existing window, so a second instance never opens.
- Entity models here are written as Java (`HollowGuestModel.createBodyLayer`); texture size and UV offsets must match the skin PNG.
