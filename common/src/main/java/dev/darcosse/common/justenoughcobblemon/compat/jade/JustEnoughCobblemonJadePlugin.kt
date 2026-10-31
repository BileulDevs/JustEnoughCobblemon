package dev.darcosse.common.justenoughcobblemon.compat.jade

import com.cobblemon.mod.common.block.BerryBlock
import com.cobblemon.mod.common.block.DiscShelfBlock
import com.cobblemon.mod.common.block.DisplayCaseBlock
import com.cobblemon.mod.common.block.FossilAnalyzerBlock
import com.cobblemon.mod.common.block.GrowableStoneBlock
import com.cobblemon.mod.common.block.HealingMachineBlock
import com.cobblemon.mod.common.block.MonitorBlock
import com.cobblemon.mod.common.block.PastureBlock
import com.cobblemon.mod.common.block.PokeSnackBlock
import com.cobblemon.mod.common.block.RestorationTankBlock
import com.cobblemon.mod.common.block.RevivalHerbBlock
import com.cobblemon.mod.common.block.campfirepot.CampfireBlock
import com.cobblemon.mod.common.block.entity.BerryBlockEntity
import com.cobblemon.mod.common.block.entity.CampfireBlockEntity
import com.cobblemon.mod.common.block.entity.FossilMultiblockEntity
import com.cobblemon.mod.common.block.entity.HealingMachineBlockEntity
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import dev.darcosse.common.justenoughcobblemon.compat.jade.provider.BerryTreeProvider
import dev.darcosse.common.justenoughcobblemon.compat.jade.provider.CookingPotProvider
import dev.darcosse.common.justenoughcobblemon.compat.jade.provider.DiscShelfProvider
import dev.darcosse.common.justenoughcobblemon.compat.jade.provider.DisplayCaseProvider
import dev.darcosse.common.justenoughcobblemon.compat.jade.provider.EvYieldProvider
import dev.darcosse.common.justenoughcobblemon.compat.jade.provider.FossilMachineProvider
import dev.darcosse.common.justenoughcobblemon.compat.jade.provider.HealingMachineProvider
import dev.darcosse.common.justenoughcobblemon.compat.jade.provider.PastureProvider
import dev.darcosse.common.justenoughcobblemon.compat.jade.provider.PlantProvider
import dev.darcosse.common.justenoughcobblemon.compat.jade.provider.PokeSnackProvider
import snownee.jade.api.IWailaClientRegistration
import snownee.jade.api.IWailaCommonRegistration
import snownee.jade.api.IWailaPlugin

/**
 * Loader-agnostic Jade plugin: registers every Cobblemon tooltip this mod adds.
 *
 * This class, and everything in this package, must only ever be loaded by Jade. Jade finds it
 * through a loader-specific subclass (a "jade" entrypoint on Fabric, the WailaPlugin annotation
 * on NeoForge), so without Jade nothing here is loaded. Referencing any of these classes from the
 * rest of the mod would crash players who do not have Jade with NoClassDefFoundError.
 *
 * Settings do not go through Jade: every provider is marked required so Jade lists no switch for
 * it, and each tooltip line follows its own mode in config/justenoughcobblemon/client.json. The
 * server decides which data it sends in server.json.
 *
 * @author Darcosse
 * @version 1.1
 * @since 2026
 */
open class JustEnoughCobblemonJadePlugin : IWailaPlugin {

    /**
     * Server data providers, called on both sides. Only providers that need data the client does
     * not have are registered here; the others are purely client side.
     */
    override fun register(registration: IWailaCommonRegistration) {
        registration.registerBlockDataProvider(BerryTreeProvider, BerryBlockEntity::class.java)
        // Registered on the block, not the block entity: only the bottom half of a pasture has a
        // block entity, and the provider resolves it from either half.
        registration.registerBlockDataProvider(PastureProvider, PastureBlock::class.java)
        registration.registerBlockDataProvider(HealingMachineProvider, HealingMachineBlockEntity::class.java)
        registration.registerBlockDataProvider(FossilMachineProvider, FossilMultiblockEntity::class.java)
        registration.registerBlockDataProvider(CookingPotProvider, CampfireBlockEntity::class.java)
    }

    /** Tooltip providers and icons; client only. */
    override fun registerClient(registration: IWailaClientRegistration) {
        registration.registerBlockComponent(PokeSnackProvider, PokeSnackBlock::class.java)

        registration.registerBlockComponent(DiscShelfProvider, DiscShelfBlock::class.java)
        registration.registerBlockIcon(DiscShelfProvider, DiscShelfBlock::class.java)

        registration.registerBlockComponent(BerryTreeProvider, BerryBlock::class.java)
        registration.registerBlockComponent(PastureProvider, PastureBlock::class.java)
        registration.registerBlockComponent(HealingMachineProvider, HealingMachineBlock::class.java)

        // The fossil machine is three blocks sharing one structure; each shows the same tooltip.
        registration.registerBlockComponent(FossilMachineProvider, FossilAnalyzerBlock::class.java)
        registration.registerBlockComponent(FossilMachineProvider, RestorationTankBlock::class.java)
        registration.registerBlockComponent(FossilMachineProvider, MonitorBlock::class.java)

        registration.registerBlockComponent(CookingPotProvider, CampfireBlock::class.java)

        registration.registerBlockComponent(PlantProvider, GrowableStoneBlock::class.java)
        registration.registerBlockComponent(PlantProvider, RevivalHerbBlock::class.java)

        registration.registerBlockComponent(DisplayCaseProvider, DisplayCaseBlock::class.java)
        registration.registerBlockIcon(DisplayCaseProvider, DisplayCaseBlock::class.java)

        registration.registerEntityComponent(EvYieldProvider, PokemonEntity::class.java)
    }
}
