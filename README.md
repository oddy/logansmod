# Hollow Guest

A NeoForge mod for Minecraft 26.2 that adds the Hollow Guest: a tall, pale mob that ignores you until you acknowledge it.

## How it behaves

- Idle: it stands still and stares at nearby players.
- It notices you if you look straight at it, say "hollow guest" (or "hollowguest") in chat, or hurt it.
- Once it has noticed you, it hunts you. It calms down after about 20 seconds with nobody in range. Creative and spectator players are ignored.
- It climbs walls whenever it is pressed up against one.
- While hunting, if it gets stuck it opens doors in its way and breaks through blocks to reach you (needs mob griefing on). It won't break bedrock, obsidian or anything tougher, or blocks that hold items like chests.
- Size: 3.25 blocks tall, 60 health, 8 attack damage. Drops 1 to 3 bones.
- Spawns rarely in the Overworld, in the dark.

In game: `/summon hollowguest:hollow_guest`, or take the spawn egg from the Spawn Eggs creative tab.

## Build

Needs Java 25 (Gradle downloads it automatically).

    ./gradlew build        # jar ends up in build/libs/hollowguest-<version>.jar
    ./gradlew runClient    # dev client
    ./gradlew runServer    # dev server

## Where things live

| What | Where |
| --- | --- |
| Behavior and stats | `src/main/java/com/hollowguest/entity/HollowGuest.java` |
| Opening doors and breaking blocks | `src/main/java/com/hollowguest/entity/BreakThroughGoal.java` |
| Walking at you when there's no path | `src/main/java/com/hollowguest/entity/DirectChaseGoal.java` |
| Registration, chat trigger | `src/main/java/com/hollowguest/HollowGuestMod.java` |
| 3D model and animation | `src/main/java/com/hollowguest/client/HollowGuestModel.java` |
| Skin (64x80) | `src/main/resources/assets/hollowguest/textures/entity/hollow_guest.png`, made by `tools/gen_texture.py` |
| Spawn egg icon | `src/main/resources/assets/hollowguest/textures/item/hollow_guest_spawn_egg.png`, made by `tools/gen_egg.py` |
| Natural spawning | `src/main/resources/data/hollowguest/neoforge/biome_modifier/hollow_guest_spawns.json` |
| Drops | `src/main/resources/data/hollowguest/loot_table/entities/hollow_guest.json` |

Regenerate the art with:

    python3 tools/gen_texture.py src/main/resources/assets/hollowguest/textures/entity/hollow_guest.png
    python3 tools/gen_egg.py src/main/resources/assets/hollowguest/textures/item/hollow_guest_spawn_egg.png
