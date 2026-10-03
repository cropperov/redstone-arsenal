# Redstone Arsenal (NeoForge 1.21.1)

Redstone-fired missiles you can mount on any block face (pixel-precise), a rideable tank, and an orbital strike cannon.

## Get the .jar

**Option A, no install (GitHub):** push this folder to a GitHub repo. The Actions workflow in `.github/workflows/build.yml`
builds it; download `redstone-arsenal-jar` from the run's Artifacts. The file inside is your mod jar.

**Option B, local:** install JDK 21 and Gradle 8.8+, then in this folder:

    gradle wrapper
    ./gradlew build        (Windows: gradlew.bat build)

The jar lands in `build/libs/redstonearsenal-1.0.0.jar`. Drop it in your `mods/` folder (NeoForge 1.21.1).

If Gradle says it cannot resolve `net.neoforged:neoforge:21.1.172`, change `neo_version` in `gradle.properties` to the newest `21.1.x`.

## What is in it

| Thing | How it works |
|---|---|
| Explosive / Cluster / Terraformer Missile | Right-click any block face (floor, wall, ceiling) to mount. Position snaps to the 16x16 pixel grid of that face. Fires along the face normal when the block it sits on (or the air block in front) gets a redstone signal. Left-click an unlaunched missile to pick it back up. |
| Terraformer ("former") missile | Flattens a radius-12 area to impact height and covers it in grass and flowers. |
| Tank | Right-click the ground to place, right-click to ride. W/S drive, A/D pivot, turret follows your look direction, left-click fires. Left-click while not riding to pick it up. |
| Orbital Strike Cannon | Place it, click a block with the Strike Designator to pick a target, click the cannon with the designator to link, then power it with redstone. 5s charge-up, 7s of blasts. Tune power in `OrbitalCannonBlockEntity` constants. |

All textures come from `tools/gen_textures.py` (run `python3 tools/gen_textures.py` to regenerate; needs Pillow).
