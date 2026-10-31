package dev.darcosse.common.justenoughcobblemon.config

import com.google.gson.annotations.SerializedName

/**
 * Client settings, stored in `config/justenoughcobblemon/client.json`.
 *
 * Only display choices live here: they affect what this player sees and nothing else. Every
 * field name is a JSON key, so renaming a field resets that setting for everyone who changed it.
 * Fields are `var` with an initializer because Gson fills them by reflection after calling the
 * no-argument constructor, which keeps the defaults for keys missing from the file.
 *
 * @author Darcosse
 * @version 1.0
 * @since 2026
 */
class ClientConfig {

    /**
     * Written into the file as a reminder of the accepted values, since JSON has no comments.
     * Not read by the mod.
     */
    @SerializedName("_help")
    var help: String = "Each tooltip line accepts: \"always\", \"sneak\" (only while Shift, Jade's show-details input, is held) or \"never\". Changes apply within a second, no restart needed."

    /** Format version of this file, for future migrations. Not meant to be edited. */
    var configVersion: Int = 1

    /** Lines of the Jade tooltips, grouped by block or entity. */
    var jade: Jade = Jade()

    /**
     * One section per Jade tooltip.
     */
    class Jade {
        var pokeSnack = PokeSnack()
        var discShelf = DiscShelf()
        var berryTree = BerryTree()
        var pasture = Pasture()
        var healingMachine = HealingMachine()
        var fossilMachine = FossilMachine()
        var cookingPot = CookingPot()
        var plants = Plants()
        var displayCase = DisplayCase()
        var pokemon = Pokemon()
    }

    /** Placed Poké Snack. */
    class PokeSnack {
        var bitesLeft = DisplayMode.ALWAYS

        /** Bait effect rates of a lure snack (shiny chance, type chance...). */
        var baitEffects = DisplayMode.ALWAYS

        /** Icons of the ingredients it was cooked with. */
        var ingredients = DisplayMode.SNEAK
    }

    /** Disc Shelf. */
    class DiscShelf {
        /** Name of the disc under the crosshair, and the tooltip icon. */
        var pointedDisc = DisplayMode.ALWAYS

        /** Type, category, power, accuracy and PP of a pointed TM. */
        var moveDetails = DisplayMode.ALWAYS

        /** "Discs: x/14"; only ever shown when no disc is pointed at. */
        var discCount = DisplayMode.ALWAYS

        /** Row of icons of every stored disc. */
        var contents = DisplayMode.ALWAYS
    }

    /** Berry tree. */
    class BerryTree {
        var stage = DisplayMode.ALWAYS
        var mulch = DisplayMode.ALWAYS

        /** Time until the next growth stage; needs this mod and Jade on the server. */
        var nextStage = DisplayMode.ALWAYS
    }

    /** Pasture block; needs this mod and Jade on the server. */
    class Pasture {
        var owner = DisplayMode.ALWAYS

        /** "Pokémon: x/limit". */
        var count = DisplayMode.ALWAYS

        /** Species, level and shiny marker of the pastured Pokémon. */
        var pokemonList = DisplayMode.SNEAK
    }

    /** Healing machine. */
    class HealingMachine {
        /** Charge bar, or "Infinite charge". */
        var charge = DisplayMode.ALWAYS

        /** Time until fully charged, or until the heal in progress ends; needs the server. */
        var timers = DisplayMode.ALWAYS
    }

    /** Fossil machine (analyzer, restoration tank, monitor); needs this mod and Jade on the server. */
    class FossilMachine {
        /** Fossil items inside. */
        var fossil = DisplayMode.ALWAYS

        /** Pokémon the fossil restores into. */
        var result = DisplayMode.ALWAYS

        /** Organic matter bar, restoration bar, or "ready" message. */
        var progress = DisplayMode.ALWAYS
    }

    /** Cooking pot on a campfire; needs this mod and Jade on the server. */
    class CookingPot {
        /** Dish the current ingredients make. */
        var dish = DisplayMode.ALWAYS

        /** Cooking progress arrow. */
        var progress = DisplayMode.ALWAYS

        /** Reminder that the pot only cooks with its lid on. */
        var lidWarning = DisplayMode.ALWAYS
    }

    /** Plants Jade does not already cover natively. */
    class Plants {
        /** Growth of Tumblestones and other growable stones. */
        var stoneGrowth = DisplayMode.ALWAYS

        /** Herb a Revival Herb mutated into, or whether it still can. */
        var herbMutation = DisplayMode.ALWAYS
    }

    /** Display Case. */
    class DisplayCase {
        /** Name and icon of the exhibited item. */
        var item = DisplayMode.ALWAYS
    }

    /** Pokémon entities. */
    class Pokemon {
        /** EVs given when defeated. */
        var evYield = DisplayMode.ALWAYS
    }
}
