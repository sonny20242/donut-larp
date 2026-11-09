# Donut Larp (Fabric 1.21.4)

A client-side DonutSMP-inspired stats and identity display. The pink/chocolate menu opens with **Right Shift**.

## Included

- **Stats tab:** local Money, Shards, Kills, Deaths and Playtime values.
- **Identity tab:** local display name and visual rank tag; can replace your own entry in the tab list and your name in unsigned/system chat lines.
- **Options tab:** enable/disable the mod, fake panel, local balance deduction on `/pay`, and a DonutSMP-only toggle.
- **HUD panel:** compact local profile panel on the right, including real ping when available.
- **`/pay` handling:** when you type `/pay player amount`, the mod subtracts the amount from the *local display balance* and shows a `LOCAL BALANCE` action-bar message. The original `/pay` command is still sent unchanged to the server. The server alone decides whether the payment succeeds.

## Important limits

- This does **not** alter your authenticated Minecraft username, UUID, skin, rank, stats, or balance on DonutSMP.
- Other players do not see your fake name, rank, or stats. Player-signed chat is intentionally not rewritten.
- The local balance is an estimate changed when a matching `/pay` command is entered. It is not a receipt or confirmation; a server-side payment can still fail.
- Please do not use the local-only panel or screenshots from it as proof of wealth or payment in a trade.
- `DonutSMP only` checks whether the server address contains `donutsmp`. Turn it off to test in singleplayer or when connecting through an address that does not include that text.
- `VIP` and `MVP` are included as visual labels; `Elite` and `Legend` are also editable placeholders, not a verified current DonutSMP rank list.

## Easiest build method: GitHub Actions

This project includes `.github/workflows/build.yml`, which asks GitHub to build the mod for you.

1. Extract this ZIP on your computer.
2. Create a new **public or private GitHub repository** (or use your existing repository).
3. Upload the *contents* of the extracted `DonutLarp` folder to the repository's main branch. Keep the `.github/workflows/build.yml` file and the `src` folder in their existing paths.
4. Commit the upload. GitHub should start the **Build Donut Larp** workflow automatically.
5. Open the repository's **Actions** tab, select the newest **Build Donut Larp** run, and wait for it to finish.
6. If it succeeds, download the `donutlarp-1.21.4` artifact near the bottom of that run page. Extract the downloaded artifact ZIP to get the mod `.jar`.
7. Put the `.jar` in your Minecraft `mods` folder alongside Fabric API for Minecraft 1.21.4.

If the workflow fails, open the failed run, select the **Build mod** step, and copy the error text here. The source has not been compiled or tested yet, so the first build may reveal an issue that needs fixing.

## Build locally instead

Requires **JDK 21**, **Gradle 8.10.2**, and internet access for Gradle to download Fabric/Minecraft dependencies. From this directory, run `gradle build`. The JAR will be in `build/libs/`.

## Version targets

- Minecraft: `1.21.4`
- Yarn: `1.21.4+build.8`
- Fabric Loader: `0.16.9`
- Fabric API: `0.119.2+1.21.4`
- Fabric Loom: `1.9.2`
