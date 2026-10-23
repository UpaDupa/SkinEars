# Skin Ears — Minecraft Java Edition 26.3 (Fabric)

**This ZIP is the complete editable source project, not a built .jar.** A build and game launch could not be run in the creation environment (Java 25 and Gradle dependencies were unavailable). API compatibility is based on the publicly available Minecraft 26.3 and Fabric sources, not on a successful build.

## Features

- Two ears: **Cat** (upright, pointed) or **Dog** (droopy).
- The ear meshes follow the player's animated head.
- Automatically chooses a dominant shade from the *top 8x8 pixels of the head*, including transparent hat-layer pixels over it.
- Works on player skins served by Mojang's `textures.minecraft.net` and bundled/resource-pack skins. Async sampling avoids pausing the game; a brown fallback is used if the skin image cannot be read.
- Pick a custom color with **Red, Green, Blue** sliders; switch back to Auto at any time.
- **Reset to default** chooses cat ears and skin-matching color.
- Settings are saved to `.minecraft/config/skinears.json`.
- **K** opens the menu; the keybind can be changed in Minecraft controls.
- Client-side only, no server installation necessary.

## Build JAR online (no Java or Gradle installation needed)

This environment could not build a tested JAR, but the project includes a GitHub Actions build workflow.

1. Create a new GitHub repository.
2. Unzip the project and upload **the files inside** `SkinEars-26.3` to the repository root, including the hidden `.github` folder. Alternatively, push the unzipped folder with git.
3. Open the repository's **Actions** tab and choose **Build Skin Ears mod JAR**. If needed, enable Actions.
4. Click **Run workflow** and select the `main` branch; wait for the build result.
5. If it succeeds, open the workflow run and download the **Skin-Ears-26.3-mod** build artifact, then extract the downloaded ZIP to get `skin-ears-1.0.0.jar`.
6. Put that JAR in Minecraft's `mods` folder with Fabric API.

**Important:** The build workflow has not been executed here. It may reveal compile-time errors that need fixes; the presence of a workflow does not guarantee a working JAR.

## Build

Requirements: **Java JDK 25**, **Gradle 9.7.1+**, internet access for Maven/Minecraft dependencies.

1. Extract the ZIP.
2. Open a terminal inside the `SkinEars-26.3` folder.
3. Run `gradle build` (or import as Gradle project into an IDE and run its `build` task).
4. Find `build/libs/skin-ears-1.0.0.jar` (ignore `-sources` variants).
5. Install **Fabric Loader** for Minecraft **Java 26.3**, plus a matching version of **Fabric API**.
6. Put the built JAR and Fabric API in `.minecraft/mods`, and launch the Fabric 26.3 profile.
7. Open a world, switch to third-person view (F5), and press **K**.

## Multiplayer / visibility

Each client performs its own rendering. A client's chosen custom color and dog/cat selection affect **their own avatar on their own screen**. Other avatars get automatic cat ears matched to *their* skins on this client. There is no network syncing of other players' custom choices. For everyone to see matching custom options, a future network-sync extension and suitable server support would be necessary.

## Notes

- The downloaded skin is only read from Mojang's official skin host, over HTTPS.
- On first appearance or while offline, ears use a neutral brown until the skin sample is ready.
- The ears may intersect certain helmets, cosmetics or unusual animations; they're a simple Minecraft-style 3D model.
- New versions of Fabric can rename APIs; if a future Fabric API causes a compilation error, use the Fabric docs for Minecraft 26.3 to update the imports.

## Sources / references

- Minecraft 26.3 release notes: https://www.minecraft.net/en-us/article/minecraft-java-edition-26-3
- Fabric for Minecraft 26.3: https://www.fabricmc.net/2026/09/15/263.html
- Fabric 26.3 sample build: https://github.com/FabricMC/fabric-example-mod/tree/26.3
- Fabric layer callback (26.3): https://github.com/FabricMC/fabric-api/blob/26.3/fabric-rendering-v1/src/client/java/net/fabricmc/fabric/api/client/rendering/v1/LivingEntityRenderLayerRegistrationCallback.java

Released under the MIT license (project source code only).
