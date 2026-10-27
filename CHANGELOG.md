# Changelog

All notable changes to Just Enough Cobblemon are listed here.

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
