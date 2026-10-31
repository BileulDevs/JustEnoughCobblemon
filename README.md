# Just Enough Cobblemon

A lightweight utility addon for [Cobblemon](https://www.curseforge.com/minecraft/mc-mods/cobblemon) that surfaces what the base mod keeps hidden:

- **what every Pokémon drops**, through JEI;
- **where every Pokémon spawns**, through a new tab inside the Pokédex;
- **what Cobblemon blocks and Pokémon are doing**, through [Jade](https://www.curseforge.com/minecraft/mc-mods/jade) tooltips: Poké Snack effects, stored TMs, berry growth, pastured Pokémon, fossil restoration and more.

JEI is required. Jade is optional: without it, everything else works and the tooltips are simply absent.

[![CurseForge](https://img.shields.io/curseforge/dt/1459880?logo=curseforge&label=Downloads&color=f16436)](https://www.curseforge.com/minecraft/mc-mods/just-enough-cobblemon)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-green?logo=minecraft)](https://www.minecraft.net)
[![Fabric](https://img.shields.io/badge/Fabric-supported-dbb37d?logo=fabric)](https://fabricmc.net)
[![NeoForge](https://img.shields.io/badge/NeoForge-supported-e04e14)](https://neoforged.net)

---

## Features

### Spawn tab in the Pokédex

A new tab on the Pokédex info panel shows exactly where and when a Pokémon appears. It lists the spawn bucket, weight, level range, biomes, structures, Y range, light level, time of day, moon phase, weather, and every other condition Cobblemon supports.

Entries distinguish between forms, so Galarian and Hisuian variants show their own conditions rather than being lumped in with the base species.

Spawn data unlocks per species once you have **encountered** it, so it follows your progress instead of handing you the whole world map on day one.

### Loot tables in JEI

Every Pokémon's drop table, with your real chance of getting each item and its quantity range. Regional and alternate forms with their own drops get their own pages.

Works both ways: look up an item to see which Pokémon drops it, or look up a Pokémon to see everything it can give you.

### Jade tooltips

With Jade installed, looking at a Cobblemon block or Pokémon tells you more about it:

| Target | What you see |
|---|---|
| **Poké Snack** | Bites left, bait effect rates (shiny chance, type chance…), ingredients |
| **Disc Shelf** | The disc you point at (a TM shows its move, type, category, power, accuracy and PP), how many discs are stored, every stored disc |
| **Berry tree** | Growth stage, mulch and its uses left, time until the next stage |
| **Pasture Block** | Owner, Pokémon count out of the limit, list of pastured Pokémon with level and shiny marker |
| **Healing Machine** | Charge bar, time until fully charged, time left on a heal |
| **Fossil machine** | Fossil inside, the Pokémon it restores into, organic matter, restoration progress |
| **Cooking Pot** | The dish the current ingredients make, cooking progress, a reminder when the lid is off |
| **Tumblestones** | Growth, and a warning when they cannot grow where they are |
| **Revival Herb** | The herb it mutated into, or whether Surprise Mulch can still make it mutate |
| **Display Case** | The exhibited item |
| **Pokémon** | EVs given when defeated |

Every line can be set to show **always**, only while **sneaking**, or **never**; see [Configuration](#configuration). Some lines are shown only while sneaking by default (Poké Snack ingredients, the pasture list) to keep tooltips short.

Jade already shows the owner of a Pokémon and the growth of apricorns, mints, leeks and berry trees on its own, so this mod does not repeat them.

### Everywhere else

- Works on **Fabric** and **NeoForge**
- Translated into **English, French, German, Spanish, Portuguese (BR), Simplified Chinese and Japanese**

---

## How to use

### The Spawn tab

1. Open your **Pokédex** and pick a Pokémon you've already encountered.
2. Click the **Spawn** tab — the location pin at the bottom of the info panel.
3. Use the arrows to page through each spawn entry.

Each entry lists the full set of conditions for that one location.

### JEI

Hover an item and press **R** to see which Pokémon drop it, or hover a Pokémon and press **U** to see its complete loot table. Pressing **U** on a Poké Ball or a Pokédex opens the Pokémon drops category directly.

### Jade

Look at a block or Pokémon. Hold **Shift** to reveal the lines set to "sneak" (this follows Jade's "show details" input, so a key bound to it in Jade works too).

---

## Configuration

The mod creates two files in `config/justenoughcobblemon/` on first launch. Edits apply **within a second, without restarting**. A file with a JSON error is never overwritten: the previous settings stay active and a warning is written to the log.

### `client.json` — what you see

One entry per tooltip line. Each accepts `"always"`, `"sneak"` or `"never"`.

```json
{
  "jade": {
    "pokeSnack": {
      "bitesLeft": "always",
      "baitEffects": "always",
      "ingredients": "sneak"
    },
    "pasture": {
      "owner": "always",
      "count": "always",
      "pokemonList": "sneak"
    },
    "pokemon": {
      "evYield": "always"
    }
  }
}
```

The real file lists every tooltip and every line. Missing keys take their default value, so you only need to change what you care about.

### `server.json` — what the server shares

Some Jade lines need data only the server has. Server owners choose what they send:

```json
{
  "jade": {
    "berryTree": true,
    "pasture": true,
    "pastureMaxListed": 6,
    "healingMachine": true,
    "fossilMachine": true,
    "cookingPot": true
  }
}
```

Set `pasture` to `false` to keep pastures private. `pastureMaxListed` (0–32) caps how many Pokémon a pasture tooltip names; the rest show as "… and N more". In single player, the game reads your own `server.json`.

---

## Installation

1. Install Cobblemon 1.8.4 for Minecraft 1.21.1. Other Cobblemon versions are not supported.
2. Install the Kotlin bridge for your loader — Fabric Language Kotlin on Fabric, Kotlin for Forge on NeoForge. Cobblemon needs it.
3. Install JEI for your loader.
4. Drop the Just Enough Cobblemon jar matching your loader into `mods/`.
5. Optionally add Jade for the tooltips.

### Dependencies

| | Required | Version |
|---|---|---|
| Minecraft | yes | 1.21.1 |
| Cobblemon | yes | 1.8.4 exactly |
| Fabric API + Fabric Language Kotlin | yes, on Fabric | — |
| Kotlin for Forge | yes, on NeoForge | — |
| JEI | yes | 19.0+ |
| Jade | optional | 15.0+ |

The game refuses to start without JEI. Without Jade you only lose the tooltips; everything else keeps working. EMI can be installed alongside JEI.

### Client or server?

Install it on the client to get everything in single player.

On a multiplayer server, a client-only install gives you the Pokédex tab, the loot lookups, and the Jade lines the client can work out alone (Poké Snack, Disc Shelf, plants, Display Case, EV yield, berry stage, healing machine charge).

Installing it **on the server as well** adds:

- spawn data that reflects the server's own configuration (custom spawn files, datapacks, anything the server has altered) instead of the defaults;
- the Jade lines that need server data: pastures, the fossil machine, the cooking pot, berry timers, healing machine timers. These also need Jade on the server.

---

## Credits & License

- **Developer:** Darcosse
- **License:** MIT — include it in your modpacks freely, no need to ask

Bug reports and suggestions go to the [issue tracker](https://github.com/BileulDevs/JustEnoughCobblemon/issues).
