package dev.darcosse.forge.justenoughcobblemon.mixin;

import com.cobblemon.mod.common.api.pokedex.entry.PokedexEntry;
import com.cobblemon.mod.common.api.pokedex.entry.PokedexForm;
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUI;
import com.cobblemon.mod.common.client.gui.pokedex.ScaledButton;
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants;
import dev.darcosse.common.justenoughcobblemon.client.gui.PokespawnWidget;
import dev.darcosse.common.justenoughcobblemon.util.SpawnDataExtractor;
import dev.darcosse.common.justenoughcobblemon.util.SpawnInfo;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Mixin for the Pokédex GUI to inject custom tabs and handle rendering logic.
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
    @Shadow public void setSelectedEntry(PokedexEntry newSelectedEntry) {}

    private int savedTabInfoIndex = -1;

    /**
     * Temporarily hides the original selection arrow before the main render call.
     */
    @Inject(method = "render", at = @At("HEAD"))
    private void hideOriginalArrow(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        savedTabInfoIndex = tabInfoIndex;
        tabInfoIndex = -100;
    }

    /**
     * Re-renders the selection arrow at the correct position, including support for custom tabs.
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
     * Injects the custom "Drops" tab into the Pokédex tab list and re-aligns existing buttons.
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
                    if (canSelectTab(5)) displayTabInject(5);
                }
        );
        tabButtons.add(myTab);
        ((ScreenAccessor)(Object)self).invokeAddRenderableWidget(myTab);
    }

    /**
     * Handles the display logic for the custom tab content when selected.
     */
    @Inject(method = "displaytabInfoElement", at = @At("HEAD"), cancellable = true)
    private void injectDisplayTab(int tabIndex, boolean update, CallbackInfo ci) {
        if (tabIndex != 5) {
            if (tabInfoIndex == 5 && tabInfoElement instanceof PokespawnWidget) {
                PokedexGUI self = (PokedexGUI)(Object)this;
                ((ScreenAccessor)(Object)self).invokeRemoveWidget(((PokespawnWidget) tabInfoElement).getLeftButton());
                ((ScreenAccessor)(Object)self).invokeRemoveWidget(((PokespawnWidget) tabInfoElement).getRightButton());
            }
            return;
        }

        PokedexGUI self = (PokedexGUI)(Object)this;
        int x = (self.width - PokedexGUIConstants.BASE_WIDTH) / 2;
        int y = (self.height - PokedexGUIConstants.BASE_HEIGHT) / 2;

        for (int i = 0; i < tabButtons.size(); i++) {
            tabButtons.get(i).setWidgetActive(i == 5);
        }

        if (tabInfoElement instanceof AbstractWidget) {
            ((ScreenAccessor)(Object)self).invokeRemoveWidget(tabInfoElement);
        }

        tabInfoIndex = 5;
        PokespawnWidget widget = new PokespawnWidget(x + 180, y + 135);

        List<SpawnInfo> spawns = SpawnDataExtractor.INSTANCE.getSpawnsForSpecies(selectedEntry.getSpeciesId());
        widget.setSpawns(spawns);

        tabInfoElement = widget;
        ((ScreenAccessor)(Object)self).invokeAddRenderableWidget(widget);

        if (spawns.size() > 1) {
            ((ScreenAccessor)(Object)self).invokeAddRenderableWidget(widget.getLeftButton());
            ((ScreenAccessor)(Object)self).invokeAddRenderableWidget(widget.getRightButton());
        }

        ci.cancel();
    }

    /**
     * Resets the tab selection to the default view (index 0) when a new Pokémon entry is selected
     * while on the custom spawn tab. This ensures the UI doesn't break and cleans up custom buttons.
     */
    @Inject(method = "setSelectedEntry", at = @At("HEAD"))
    private void onSetSelectedEntry(PokedexEntry newSelectedEntry, CallbackInfo ci) {
        if (tabInfoIndex == 5) {
            PokedexGUI self = (PokedexGUI)(Object)this;
            if (tabInfoElement instanceof PokespawnWidget) {
                ((ScreenAccessor)(Object)self).invokeRemoveWidget(((PokespawnWidget) tabInfoElement).getLeftButton());
                ((ScreenAccessor)(Object)self).invokeRemoveWidget(((PokespawnWidget) tabInfoElement).getRightButton());
            }
            displayTabInject(0);
        }
    }

    /**
     * Prevents the original update logic from overwriting the custom tab state.
     */
    @Inject(method = "updateTabInfoElement", at = @At("HEAD"), cancellable = true)
    private void injectUpdateTab(CallbackInfo ci) {
        if (tabInfoIndex == 5) ci.cancel();
    }

    /**
     * Checks if a specific tab index can be selected.
     */
    private boolean canSelectTabInject(int tabIndex) {
        return tabIndex != tabInfoIndex;
    }

    /**
     * Triggers the Pokédex display element update for the injected tab.
     */
    private void displayTabInject(int tabIndex) {
        ((PokedexGUI)(Object)this).displaytabInfoElement(tabIndex, true);
    }
}