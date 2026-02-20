package dev.darcosse.forge.justenoughcobblemon.mixin;

import com.cobblemon.mod.common.api.pokedex.entry.PokedexEntry;
import com.cobblemon.mod.common.api.pokedex.entry.PokedexForm;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUI;
import com.cobblemon.mod.common.client.gui.pokedex.ScaledButton;
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants;
import com.cobblemon.mod.common.client.gui.pokedex.widgets.AbilitiesWidget;
import com.cobblemon.mod.common.client.gui.pokedex.widgets.DescriptionWidget;
import com.cobblemon.mod.common.client.gui.pokedex.widgets.DropsScrollingWidget;
import com.cobblemon.mod.common.client.gui.pokedex.widgets.SizeWidget;
import com.cobblemon.mod.common.client.gui.pokedex.widgets.StatsWidget;
import dev.darcosse.common.justenoughcobblemon.client.gui.PokespawnWidget;
import dev.darcosse.common.justenoughcobblemon.network.SpawnDataCache;
import dev.darcosse.common.justenoughcobblemon.util.SpawnInfo;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Mixin for the Pokédex GUI on Forge/NeoForge.
 * Manages the manual injection of the spawn locations tab and custom arrow rendering.
 *
 * @author Darcosse
 * @version 1.0
 * @since 2026
 */
@Mixin(value = PokedexGUI.class, remap = false)
public class PokedexGUIMixin {

    @Shadow private List<ScaledButton> tabButtons;
    @Shadow public GuiEventListener tabInfoElement;
    @Shadow public int tabInfoIndex;
    @Shadow private static ResourceLocation tabSelectArrow;
    @Shadow private PokedexEntry selectedEntry;
    @Shadow private PokedexForm selectedForm;
    @Shadow public boolean canSelectTab(int tabIndex) { return false; }
    @Shadow public void updateTabInfoElement() {}

    @Unique private int savedTabInfoIndex = -1;

    /**
     * Checks if the Pokémon has been encountered.
     */
    @Unique
    private boolean isEncountered() {
        return selectedEntry != null &&
                !CobblemonClient.INSTANCE.getClientPokedexData().getEncounteredForms(selectedEntry).isEmpty();
    }

    /**
     * Checks if the current form has been caught.
     */
    @Unique
    private boolean isCaught() {
        return selectedEntry != null &&
                CobblemonClient.INSTANCE.getClientPokedexData().getCaughtForms(selectedEntry).contains(selectedForm);
    }

    /**
     * Temporarily hides the original selection arrow before rendering.
     */
    @Inject(method = "render", at = @At("HEAD"))
    private void hideOriginalArrow(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        savedTabInfoIndex = tabInfoIndex;
        tabInfoIndex = -100;
    }

    /**
     * Manually draws the selection arrow at the correct position for the 6-tab layout.
     */
    @Inject(method = "render", at = @At("TAIL"))
    private void drawCorrectArrow(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        tabInfoIndex = savedTabInfoIndex;
        PokedexGUI self = (PokedexGUI)(Object)this;
        int x = (self.width - PokedexGUIConstants.BASE_WIDTH) / 2;
        int y = (self.height - PokedexGUIConstants.BASE_HEIGHT) / 2;

        int newX = (int)(x + 191.5F + (22 * tabInfoIndex));
        int newY = y + 177;
        context.pose().pushPose();
        context.pose().scale(0.5F, 0.5F, 1.0F);
        context.blit(tabSelectArrow, newX * 2, newY * 2, 0, 0, 12, 6, 12, 6);
        context.pose().popPose();
    }

    /**
     * Adjusts existing tab positions and injects the custom spawn locations tab.
     */
    @Inject(method = "setUpTabs", at = @At("TAIL"))
    private void injectMyTab(CallbackInfo ci) {
        PokedexGUI self = (PokedexGUI)(Object)this;
        int x = (self.width - PokedexGUIConstants.BASE_WIDTH) / 2;
        int y = (self.height - PokedexGUIConstants.BASE_HEIGHT) / 2;

        for (int i = 0; i < tabButtons.size(); i++) {
            float newX = x + 190.5F + (i * 22F);
            tabButtons.get(i).setButtonX(newX);
            tabButtons.get(i).setX((int) newX);
        }

        ResourceLocation myIcon = ResourceLocation.fromNamespaceAndPath("cobblemon", "textures/gui/pokedex/tab_locations.png");

        ScaledButton myTab = new ScaledButton(
                x + 190.5F + (5 * 22F),
                y + 181.5F,
                PokedexGUIConstants.TAB_ICON_SIZE,
                PokedexGUIConstants.TAB_ICON_SIZE,
                myIcon,
                0.5F,
                false,
                (btn) -> {
                    if (canSelectTab(5)) displaytabInfoElement(5, true);
                }
        );
        tabButtons.add(myTab);
        ((ScreenAccessor)(Object)self).invokeAddRenderableWidget(myTab);
    }

    /**
     * Overwrites tab element display logic to handle the custom PokespawnWidget
     * and specific visibility rules for encountered vs caught Pokémon.
     */
    @Overwrite
    public void displaytabInfoElement(int tabIndex, boolean update) {
        PokedexGUI self = (PokedexGUI)(Object)this;
        int x = (self.width - PokedexGUIConstants.BASE_WIDTH) / 2;
        int y = (self.height - PokedexGUIConstants.BASE_HEIGHT) / 2;

        for (int i = 0; i < tabButtons.size(); i++) {
            boolean active;
            if (i == 5) {
                active = isEncountered() && i == tabIndex;
            } else {
                active = isCaught() && i == tabIndex;
            }
            tabButtons.get(i).setWidgetActive(active);
        }

        if (tabInfoIndex == 1 && tabInfoElement instanceof AbilitiesWidget w) {
            ((ScreenAccessor)(Object)self).invokeRemoveWidget(w.getLeftButton());
            ((ScreenAccessor)(Object)self).invokeRemoveWidget(w.getRightButton());
        } else if (tabInfoIndex == 3 && tabInfoElement instanceof StatsWidget w) {
            ((ScreenAccessor)(Object)self).invokeRemoveWidget(w.getLeftButton());
            ((ScreenAccessor)(Object)self).invokeRemoveWidget(w.getRightButton());
            ((ScreenAccessor)(Object)self).invokeRemoveWidget(w.getLeftSubButton());
            ((ScreenAccessor)(Object)self).invokeRemoveWidget(w.getRightSubButton());
        } else if (tabInfoIndex == 5 && tabInfoElement instanceof PokespawnWidget w) {
            ((ScreenAccessor)(Object)self).invokeRemoveWidget(w.getLeftButton());
            ((ScreenAccessor)(Object)self).invokeRemoveWidget(w.getRightButton());
        }

        tabInfoIndex = tabIndex;
        if (tabInfoElement != null) {
            ((ScreenAccessor)(Object)self).invokeRemoveWidget(tabInfoElement);
        }

        switch (tabIndex) {
            case 0 -> tabInfoElement = new DescriptionWidget(x + 180, y + 135);
            case 1 -> tabInfoElement = new AbilitiesWidget(x + 180, y + 135);
            case 2 -> tabInfoElement = new SizeWidget(x + 180, y + 135);
            case 3 -> tabInfoElement = new StatsWidget(x + 180, y + 135);
            case 4 -> tabInfoElement = new DropsScrollingWidget(x + 189, y + 135);
            case 5 -> {
                PokespawnWidget widget = new PokespawnWidget(x + 180, y + 135);
                if (isEncountered()) {
                    List<SpawnInfo> spawns = SpawnDataCache.INSTANCE.getSpawnsForSpecies(selectedEntry.getSpeciesId());
                    widget.setSpawns(spawns);
                    if (spawns.size() > 1) {
                        ((ScreenAccessor)(Object)self).invokeAddRenderableWidget(widget.getLeftButton());
                        ((ScreenAccessor)(Object)self).invokeAddRenderableWidget(widget.getRightButton());
                    }
                }
                tabInfoElement = widget;
            }
        }

        if (tabInfoElement instanceof Renderable && tabInfoElement instanceof NarratableEntry) {
            ((ScreenAccessor)(Object)self).invokeAddRenderableWidget(
                    (GuiEventListener & Renderable & NarratableEntry) tabInfoElement
            );
        }

        if (update) updateTabInfoElement();
    }

    /**
     * Ensures clean widget removal when switching Pokémon entries while on the spawn tab.
     */
    @Inject(method = "setSelectedEntry", at = @At("HEAD"))
    private void onSetSelectedEntry(PokedexEntry newSelectedEntry, CallbackInfo ci) {
        if (tabInfoIndex == 5) {
            PokedexGUI self = (PokedexGUI)(Object)this;
            if (tabInfoElement instanceof PokespawnWidget w) {
                ((ScreenAccessor)(Object)self).invokeRemoveWidget(w.getLeftButton());
                ((ScreenAccessor)(Object)self).invokeRemoveWidget(w.getRightButton());
            }
            tabInfoIndex = 0;
        }
    }

    /**
     * Cancels the default update logic for tab index 5, as it is handled by the custom widget.
     */
    @Inject(method = "updateTabInfoElement", at = @At("HEAD"), cancellable = true)
    private void injectUpdateTab(CallbackInfo ci) {
        if (tabInfoIndex == 5) ci.cancel();
    }
}