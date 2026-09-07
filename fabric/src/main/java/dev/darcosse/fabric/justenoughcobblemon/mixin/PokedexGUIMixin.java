package dev.darcosse.fabric.justenoughcobblemon.mixin;

import com.cobblemon.mod.common.api.pokedex.entry.PokedexEntry;
import com.cobblemon.mod.common.api.pokedex.entry.PokedexForm;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUI;
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants;
import com.cobblemon.mod.common.client.gui.pokedex.ScaledButton;
import com.cobblemon.mod.common.client.gui.pokedex.widgets.PokemonInfoWidget;
import dev.darcosse.common.justenoughcobblemon.client.gui.PokespawnWidget;
import dev.darcosse.common.justenoughcobblemon.network.SpawnDataCache;
import dev.darcosse.common.justenoughcobblemon.util.SpawnInfo;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Adds a "spawn locations" tab to the Cobblemon Pokedex GUI.
 *
 * DESIGN NOTES (verified against Cobblemon 1.8.0 sources):
 *
 * - Our tab is appended AFTER every vanilla tab, at index tabIcons.length (6 in
 *   1.8.0, where index 5 is TAB_MOVES). We never claim an index Cobblemon uses,
 *   so their unchecked casts -- notably `tabInfoElement as MovesLearnsetWidget`
 *   in mouseScrolled -- can never see our widget.
 *
 * - Tab geometry is left untouched: vanilla lays tabs out at x + 190.5 with a
 *   22px step, and the selection arrow at x + 191.5 + 22 * tabInfoIndex. With
 *   BASE_WIDTH = 345 a 7th tab ends at 330.5, so it fits without rescaling and
 *   the vanilla arrow formula stays correct for our index. No @ModifyArg needed.
 *
 * - getSufficientlyKnownForms() gates both the selection arrow and the active
 *   tab highlight behind CAUGHT forms, except for tabs listed in looseUnlockTabs
 *   (drops, moves) which only need an encounter. Our tab needs the loose rule.
 *
 * @author Darcosse
 * @version 1.3.0
 * @since 2026
 */
@Mixin(value = PokedexGUI.class, remap = false)
public abstract class PokedexGUIMixin {

    @Shadow @Final private static ResourceLocation[] tabIcons;
    @Shadow @Final private List<ScaledButton> tabButtons;
    @Shadow private int tabInfoIndex;
    @Shadow public GuiEventListener tabInfoElement;
    @Shadow private PokedexEntry selectedEntry;
    @Shadow private PokedexForm selectedForm;
    @Shadow private PokemonInfoWidget pokemonInfoWidget;

    @Unique
    private static final ResourceLocation JEC$SPAWN_ICON =
            ResourceLocation.fromNamespaceAndPath("cobblemon", "textures/gui/pokedex/tab_locations.png");

    /** Left edge of the tab row, as in vanilla setUpTabs. */
    @Unique
    private static final float JEC$TAB_ORIGIN_X = 190.5F;

    /** X of the selection arrow relative to the row: vanilla uses origin + 1px. */
    @Unique
    private static final float JEC$ARROW_ORIGIN_X = 191.5F;

    /** Width the vanilla row spans: 6 tabs at a 22px step = 110px. */
    @Unique
    private static final float JEC$TAB_ROW_WIDTH = 110F;

    /**
     * Step between tabs, squeezed so that all tabs still fit inside the blue bar
     * drawn in pokedex_screen.png, which is sized for the vanilla row width.
     * With 7 tabs this yields ~18.33px instead of 22px.
     */
    @Unique
    private static float jec$tabStep() {
        return JEC$TAB_ROW_WIDTH / tabIcons.length;
    }

    /** Our tab always sits one past the last vanilla tab. */
    @Unique
    private static int jec$spawnTabIndex() {
        return tabIcons.length;
    }

    @Unique
    private static PokedexGUI jec$cast(Object object) {
        return (PokedexGUI)(Object) object;
    }

    @Unique
    private boolean jec$isEncountered() {
        return selectedEntry != null &&
                !CobblemonClient.INSTANCE.getClientPokedexData().getEncounteredForms(selectedEntry).isEmpty();
    }

    @Unique
    private void jec$removeSpawnButtons(PokedexGUI gui) {
        if (tabInfoElement instanceof PokespawnWidget w) {
            gui.removeWidget(w.getLeftButton());
            gui.removeWidget(w.getRightButton());
        }
    }

    /**
     * Rebuilds the tab row with one extra button appended after the vanilla ones.
     * Geometry is identical to vanilla setUpTabs -- only the icon count changes.
     */
    @Overwrite
    public void setUpTabs() {
        PokedexGUI gui = jec$cast(this);
        int x = (gui.width - PokedexGUIConstants.BASE_WIDTH) / 2;
        int y = (gui.height - PokedexGUIConstants.BASE_HEIGHT) / 2;

        ResourceLocation[] icons = Arrays.copyOf(tabIcons, tabIcons.length + 1);
        icons[jec$spawnTabIndex()] = JEC$SPAWN_ICON;

        if (!tabButtons.isEmpty()) tabButtons.clear();

        float step = jec$tabStep();
        for (int i = 0; i < icons.length; i++) {
            int j = i;
            tabButtons.add(new ScaledButton(
                    x + JEC$TAB_ORIGIN_X + (i * step),
                    y + 181.5F,
                    PokedexGUIConstants.TAB_ICON_SIZE,
                    PokedexGUIConstants.TAB_ICON_SIZE,
                    icons[i],
                    PokedexGUIConstants.SCALE,
                    false,
                    btn -> { if (gui.canSelectTab(j)) gui.displaytabInfoElement(j, true); }
            ));
        }

        for (ScaledButton btn : tabButtons) {
            gui.addRenderableWidget(btn);
        }
    }

    /**
     * Treat our tab like a looseUnlockTab: seeing the Pokemon is enough.
     * This drives the selection arrow in render() and the active tab highlight.
     */
    @Inject(method = "getSufficientlyKnownForms", at = @At("HEAD"), cancellable = true, remap = false)
    private void jec$looseUnlockForSpawnTab(PokedexEntry entry, int tabIndex,
                                            CallbackInfoReturnable<Collection<PokedexForm>> cir) {
        if (tabIndex == jec$spawnTabIndex()) {
            cir.setReturnValue(CobblemonClient.INSTANCE.getClientPokedexData().getEncounteredForms(entry));
        }
    }

    /**
     * Builds our widget when our tab is selected, and cancels so Cobblemon's
     * `when (tabIndex)` never runs. For every other index we clean up our own
     * buttons and let the original method proceed untouched.
     */
    @Inject(method = "displaytabInfoElement", at = @At("HEAD"), cancellable = true, remap = false)
    private void jec$displaySpawnTab(int tabIndex, boolean update, CallbackInfo ci) {
        PokedexGUI gui = jec$cast(this);

        // Leaving our tab: drop our arrows before the original logic takes over.
        if (tabInfoIndex == jec$spawnTabIndex()) {
            jec$removeSpawnButtons(gui);
        }

        if (tabIndex != jec$spawnTabIndex()) return;

        for (int i = 0; i < tabButtons.size(); i++) {
            tabButtons.get(i).setWidgetActive(i == tabIndex && jec$isEncountered());
        }

        tabInfoIndex = tabIndex;
        // lateinit on the Kotlin side: null until init() has run.
        if (pokemonInfoWidget != null) pokemonInfoWidget.setSuppressViewport(false);
        if (tabInfoElement != null) gui.removeWidget(tabInfoElement);

        int x = (gui.width - PokedexGUIConstants.BASE_WIDTH) / 2;
        int y = (gui.height - PokedexGUIConstants.BASE_HEIGHT) / 2;

        PokespawnWidget widget = new PokespawnWidget(x + 180, y + 135);
        if (jec$isEncountered()) {
            List<SpawnInfo> spawns = SpawnDataCache.INSTANCE.getSpawnsForSpecies(selectedEntry.getSpeciesId());
            widget.setSpawns(spawns);
            if (spawns.size() > 1) {
                gui.addRenderableWidget(widget.getLeftButton());
                gui.addRenderableWidget(widget.getRightButton());
            }
        }
        tabInfoElement = widget;
        gui.addRenderableWidget(widget);

        // Mirror vanilla: keep tab icons above the tab content.
        for (ScaledButton btn : tabButtons) gui.removeWidget(btn);
        for (ScaledButton btn : tabButtons) gui.addRenderableWidget(btn);

        if (update) gui.updateTabInfoElement();
        ci.cancel();
    }

    /**
     * Realigns the selection arrow with the compressed tab step.
     *
     * ORDINAL: counting blitk calls in render() -- base texture, screenBackground,
     * globeIcon, caughtSeenIcon (seen), caughtSeenIcon (caught), categoryBarOverlay,
     * categoryFilterIcon, then tabSelectArrow -- the arrow is ordinal 7.
     * Vanilla passes x = (x + 191.5 + 22 * tabInfoIndex) / SCALE, already divided,
     * so we return an already-divided value too.
     */
    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/cobblemon/mod/common/api/gui/GuiUtilsKt;blitk$default(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/resources/ResourceLocation;Ljava/lang/Number;Ljava/lang/Number;Ljava/lang/Number;Ljava/lang/Number;Ljava/lang/Number;Ljava/lang/Number;Ljava/lang/Number;Ljava/lang/Number;Ljava/lang/Number;Ljava/lang/Number;Ljava/lang/Number;Ljava/lang/Number;Ljava/lang/Number;ZFILjava/lang/Object;)V",
                    ordinal = 7
            ),
            index = 2,
            remap = true
    )
    private Number jec$modifyArrowX(Number original) {
        PokedexGUI self = jec$cast(this);
        int x = (self.width - PokedexGUIConstants.BASE_WIDTH) / 2;
        return (x + JEC$ARROW_ORIGIN_X + (jec$tabStep() * tabInfoIndex)) / PokedexGUIConstants.SCALE;
    }

    /**
     * Our widget is populated at creation time, so there is nothing to refresh.
     * Cancelling also avoids the original's trailing cast of tabInfoElement to
     * DescriptionWidget when the Pokemon is seen but not caught.
     */
    @Inject(method = "updateTabInfoElement", at = @At("HEAD"), cancellable = true, remap = false)
    private void jec$skipUpdateOnSpawnTab(CallbackInfo ci) {
        if (tabInfoIndex == jec$spawnTabIndex()) {
            ci.cancel();
        }
    }
}