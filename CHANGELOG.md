# Changelog

All notable changes to Just Enough Cobblemon are listed here.

## [1.5.0] - 2026-10-31

This release adds Jade tooltips for Cobblemon's blocks and Pokémon, and a config file to choose what they show.

### Added

- **Jade integration** (optional). Looking at a Cobblemon block or Pokémon with Jade installed now shows:
  - **Poké Snack:** bites left, bait effect rates, and the ingredients it was cooked with.
  - **Disc Shelf:** the disc you point at, named after its move for TMs, with the move's type, category, power, accuracy and PP. Without a pointed disc, the number of stored discs. Icons of every stored disc.
  - **Berry tree:** growth stage, mulch and its uses left, time until the next stage.
  - **Pasture Block:** owner, Pokémon count out of the limit, and the pastured Pokémon with their level and a shiny marker.
  - **Healing Machine:** charge bar, time until fully charged, time left on a heal in progress.
  - **Fossil machine** (analyzer, restoration tank and monitor): fossil inside, the Pokémon it restores into, organic matter, restoration progress, and when the Pokémon is ready.
  - **Cooking Pot:** the dish the current ingredients make, with seasoning applied, the cooking progress, and a reminder when the lid is off.
  - **Tumblestones:** growth, and a warning when they cannot grow where they are.
  - **Revival Herb:** the herb it mutated into, or whether Surprise Mulch can still make it mutate.
  - **Display Case:** the exhibited item; a TM shows its move.
  - **Pokémon:** EVs given when defeated.

  Jade already shows Pokémon owners and the growth of apricorns, mints, leeks and berry trees, so those are not repeated.
  Without Jade, nothing changes and nothing extra is loaded.
- **Config files** in `config/justenoughcobblemon/`:
  - `client.json`: every tooltip line can be shown `always`, only while sneaking (`sneak`), or `never`. The Poké Snack ingredients and the pasture list are `sneak` by default to keep tooltips short.
  - `server.json`: lets server owners stop sending the data behind the berry, pasture, healing machine, fossil and cooking pot lines, and cap how many Pokémon a pasture tooltip names.

  Edits apply within a second, without restarting. A file with a JSON error is never overwritten: the previous settings stay active and a warning is logged.

### Changed

- **JEI is now required on both loaders.** Fabric declared it optional; NeoForge already required it, by accident (see below). The game now refuses to start without JEI on either loader instead of starting without the drop pages.

### Technical

- NeoForge dependencies are declared with `type = "required"` / `"optional"`. NeoForge 21.1 silently ignores the older `mandatory` field and treats such a dependency as required, which is why JEI was already compulsory there and Jade briefly was too.
- Jade is a compile-only dependency, fetched from the Modrinth Maven repository. The Fabric plugin is a `jade` entrypoint and the NeoForge plugin carries `@WailaPlugin`; all registration lives in the common plugin, like JEI's.
- All Jade code sits in the `compat.jade` package, which nothing else references, so players without Jade never load it.
- Every piece of data the server sends to Jade carries a format version. A client and a server on different versions hide the affected lines instead of showing wrong values.
- Jade's own per-plugin switches are not used: every line is configured in `client.json` instead.
- Jade is added to the development runtime (`modLocalRuntime`) so the integration can be tested from the IDE.

## [1.4.0] - 2026-10-27

This release reworks the JEI loot integration: more accurate numbers, alternate forms, and a cleaner page.

### Added

- **Form drops.** Forms that have their own drop table now get their own pages, right after the base species.
  This covers Alolan, Galarian and Hisuian forms, Rotom appliances, Wormadam cloaks, Low Key Toxtricity, Noice Face Eiscue and others: 77 forms in Cobblemon 1.8.4.
  Each page shows the form's own model. Hovering the model shows the form name, translated through Cobblemon's own language files.
- **Catalysts.** Press the "show uses" key (U by default) on a Poké Ball or on any Pokédex to open the Pokémon drops category directly.
- **Quantity ranges.** Drops with a variable amount show it in their tooltip, for example `×0–2`.
- **Slot frames** around every drop, like vanilla inventories.

### Changed

- **Requires exactly Cobblemon 1.8.4.** Fabric and NeoForge now refuse to load the mod with any other Cobblemon version, instead of accepting 1.8.0 and later.
- **Drop chances show your real odds of getting the item.** Cobblemon first decides whether a drop is picked, then rolls the quantity.
  A drop picked 100% of the time with a quantity of 0 to 2 only gives the item 2 times out of 3, so it now shows `67%` instead of `100%`.
- Percentages are formatted cleanly: `100%`, `12.5%`, never `100.0%` or `33.333332%`.
- Recipes are sorted by National Pokédex number instead of an arbitrary order.
- The category title is translated in all supported languages.

### Fixed

- Large Pokémon (Wailord, Wailmer, Aggron…) no longer cover the species name tooltip, and they are now lit like smaller ones.
- Model animations now play at a steady speed. Before, their speed depended on the world's time and they froze outside a world.
- Several models on the same screen no longer share one animation state, which made them jitter or take the wrong pose.
- Species added by datapacks or other mods (outside the `cobblemon` namespace) now show their model and name.
- Drop entries pointing to an item that does not exist (missing mod, typo in a datapack) no longer appear as empty slots with a percentage. They are skipped, and a warning is written to the log.
- A third row of drops no longer overflows the bottom of the category.
- Drop percentages could silently disappear from tooltips if JEI copied an item stack.

### Technical

- The common module now compiles against JEI's loader-neutral API only, so Fabric-only classes can no longer slip into shared code.
- Replaced the deprecated `IRecipeCategory.getBackground()` with `getWidth()` / `getHeight()`.
- The Fabric and NeoForge JEI plugins now only carry `@JeiPlugin`; all registration lives in the common plugin.
- Development environment updated to NeoForge 21.1.265. Cobblemon 1.8.3 and later need MixinExtras 0.5 or newer, and NeoForge 21.1.182 only provided 0.4.1, which crashed the NeoForge dev client on startup.
