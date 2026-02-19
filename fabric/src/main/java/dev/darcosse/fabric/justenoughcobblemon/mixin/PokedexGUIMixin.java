package dev.darcosse.fabric.justenoughcobblemon.mixin;

import com.cobblemon.mod.common.api.abilities.PotentialAbility;
import com.cobblemon.mod.common.api.pokedex.entry.PokedexEntry;
import com.cobblemon.mod.common.api.pokedex.entry.PokedexForm;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUI;
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants;
import com.cobblemon.mod.common.client.gui.pokedex.ScaledButton;
import com.cobblemon.mod.common.client.gui.pokedex.widgets.*;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Species;
import com.cobblemon.mod.common.pokemon.abilities.HiddenAbility;
import dev.darcosse.common.justenoughcobblemon.client.gui.PokespawnWidget;
import dev.darcosse.common.justenoughcobblemon.util.SpawnDataExtractor;
import dev.darcosse.common.justenoughcobblemon.util.SpawnInfo;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Main Mixin for the Pokédex GUI that handles the integration of custom tabs
 * and extends the UI to support a 6-tab layout including spawn locations.
 *
 * @author Darcosse
 * @version 1.0
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
    private static final ResourceLocation[] NEW_TAB_ICONS = new ResourceLocation[] {
            ResourceLocation.fromNamespaceAndPath("cobblemon", "textures/gui/pokedex/tab_info.png"),
            ResourceLocation.fromNamespaceAndPath("cobblemon", "textures/gui/pokedex/tab_abilities.png"),
            ResourceLocation.fromNamespaceAndPath("cobblemon", "textures/gui/pokedex/tab_size.png"),
            ResourceLocation.fromNamespaceAndPath("cobblemon", "textures/gui/pokedex/tab_stats.png"),
            ResourceLocation.fromNamespaceAndPath("cobblemon", "textures/gui/pokedex/tab_drops.png"),
            ResourceLocation.fromNamespaceAndPath("cobblemon", "textures/gui/pokedex/tab_locations.png")
    };

    /**
     * Helper to cast the current mixin target to PokedexGUI.
     */
    @Unique
    private static PokedexGUI cast(Object object) {
        return (PokedexGUI)(Object) object;
    }

    /**
     * Checks if the current Pokémon entry has been encountered by the player.
     */
    @Unique
    private boolean isEncountered() {
        return selectedEntry != null &&
                !CobblemonClient.INSTANCE.getClientPokedexData().getEncounteredForms(selectedEntry).isEmpty();
    }

    /**
     * Checks if the current Pokémon form has been caught by the player.
     */
    @Unique
    private boolean isCaught() {
        return selectedEntry != null &&
                CobblemonClient.INSTANCE.getClientPokedexData().getCaughtForms(selectedEntry).contains(selectedForm);
    }

    /**
     * Reconstructs the tab layout to accommodate 6 tabs and initializes buttons with custom icons.
     */
    @Overwrite
    public void setUpTabs() {
        PokedexGUI gui = cast(this);
        int x = (gui.width - PokedexGUIConstants.BASE_WIDTH) / 2;
        int y = (gui.height - PokedexGUIConstants.BASE_HEIGHT) / 2;

        if (!tabButtons.isEmpty()) tabButtons.clear();

        for (int i = 0; i < NEW_TAB_ICONS.length; i++) {
            int j = i;
            tabButtons.add(new ScaledButton(
                    x + 190.5F + (i * 22F),
                    y + 181.5F,
                    PokedexGUIConstants.TAB_ICON_SIZE,
                    PokedexGUIConstants.TAB_ICON_SIZE,
                    NEW_TAB_ICONS[i],
                    0.5F,
                    false,
                    btn -> { if (gui.canSelectTab(j)) gui.displaytabInfoElement(j, true); }
            ));
        }

        for (ScaledButton btn : tabButtons) {
            gui.addRenderableWidget(btn);
        }
    }

    /**
     * Validates if a tab can be selected, specifically allowing the spawn tab (index 5)
     * for any encountered Pokémon.
     */
    @Inject(method = "canSelectTab", at = @At("HEAD"), cancellable = true, remap = false)
    private void onCanSelectTab(int tabIndex, CallbackInfoReturnable<Boolean> cir) {
        if (tabIndex == 5) {
            cir.setReturnValue(isEncountered() && tabIndex != tabInfoIndex);
        }
    }

    /**
     * Handles the instantiation and cleanup of the various information widgets,
     * including the custom PokespawnWidget.
     */
    @Overwrite
    public void displaytabInfoElement(int tabIndex, boolean update) {
        PokedexGUI gui = cast(this);

        boolean showActiveForTab;
        if (tabIndex == 5) {
            showActiveForTab = isEncountered();
        } else {
            showActiveForTab = isCaught();
        }

        if (!tabButtons.isEmpty() && tabButtons.size() > tabIndex) {
            for (int i = 0; i < tabButtons.size(); i++) {
                boolean active;
                if (i == 5) {
                    active = isEncountered() && i == tabIndex;
                } else {
                    active = isCaught() && i == tabIndex;
                }
                tabButtons.get(i).setWidgetActive(active);
            }
        }

        if (tabInfoIndex == 1 && tabInfoElement instanceof AbilitiesWidget w) {
            gui.removeWidget(w.getLeftButton());
            gui.removeWidget(w.getRightButton());
        } else if (tabInfoIndex == 3 && tabInfoElement instanceof StatsWidget w) {
            gui.removeWidget(w.getLeftButton());
            gui.removeWidget(w.getRightButton());
            gui.removeWidget(w.getLeftSubButton());
            gui.removeWidget(w.getRightSubButton());
        } else if (tabInfoIndex == 5 && tabInfoElement instanceof PokespawnWidget w) {
            gui.removeWidget(w.getLeftButton());
            gui.removeWidget(w.getRightButton());
        }

        tabInfoIndex = tabIndex;
        if (tabInfoElement != null) gui.removeWidget(tabInfoElement);

        int x = (gui.width - PokedexGUIConstants.BASE_WIDTH) / 2;
        int y = (gui.height - PokedexGUIConstants.BASE_HEIGHT) / 2;

        switch (tabIndex) {
            case 0 -> tabInfoElement = new DescriptionWidget(x + 180, y + 135);
            case 1 -> tabInfoElement = new AbilitiesWidget(x + 180, y + 135);
            case 2 -> tabInfoElement = new SizeWidget(x + 180, y + 135);
            case 3 -> tabInfoElement = new StatsWidget(x + 180, y + 135);
            case 4 -> tabInfoElement = new DropsScrollingWidget(x + 189, y + 135);
            case 5 -> {
                PokespawnWidget widget = new PokespawnWidget(x + 180, y + 135);
                if (isEncountered()) {
                    List<SpawnInfo> spawns = SpawnDataExtractor.INSTANCE.getSpawnsForSpecies(selectedEntry.getSpeciesId());
                    widget.setSpawns(spawns);
                    if (spawns.size() > 1) {
                        gui.addRenderableWidget(widget.getLeftButton());
                        gui.addRenderableWidget(widget.getRightButton());
                    }
                }
                tabInfoElement = widget;
            }
        }

        if (tabInfoElement instanceof Renderable && tabInfoElement instanceof NarratableEntry) {
            gui.addRenderableWidget((GuiEventListener & Renderable & NarratableEntry) tabInfoElement);
        }

        if (update) gui.updateTabInfoElement();
    }

    /**
     * Refreshes the content of the active tab based on the selected Pokémon and form data.
     */
    @Overwrite
    public void updateTabInfoElement() {
        PokedexGUI gui = cast(this);
        Species species = selectedEntry == null ? null : PokemonSpecies.INSTANCE.getByIdentifier(selectedEntry.getSpeciesId());
        String formName = selectedForm != null ? selectedForm.getDisplayForm() : null;
        boolean canDisplay = isCaught() && species != null;
        List<String> description = new ArrayList<>();

        if (canDisplay) {
            FormData form = species.getForms().stream()
                    .filter(f -> f.getName().equals(formName))
                    .findFirst()
                    .orElse(species.getStandardForm());

            switch (tabInfoIndex) {
                case 0 -> {
                    description.addAll(form.getPokedex());
                    if (tabInfoElement instanceof DescriptionWidget w) w.setShowPlaceholder(false);
                }
                case 1 -> {
                    if (tabInfoElement instanceof AbilitiesWidget w) {
                        w.setAbilitiesList(StreamSupport.stream(form.getAbilities().spliterator(), false)
                                .sorted(Comparator.comparing(a -> (a instanceof HiddenAbility) ? 1 : 0))
                                .map(PotentialAbility::getTemplate)
                                .collect(Collectors.toList()));
                        w.setSelectedAbilitiesIndex(0);
                        w.setAbility();
                        w.setScrollAmount(0);
                        if (w.getAbilitiesList().size() > 1) {
                            gui.addRenderableWidget(w.getLeftButton());
                            gui.addRenderableWidget(w.getRightButton());
                        }
                    }
                }
                case 2 -> {
                    if (pokemonInfoWidget != null && pokemonInfoWidget.getRenderablePokemon() != null
                            && tabInfoElement instanceof SizeWidget w) {
                        w.setPokemonHeight(form.getHeight());
                        w.setWeight(form.getWeight());
                        w.setBaseScale(form.getBaseScale());
                        w.setRenderablePokemon(pokemonInfoWidget.getRenderablePokemon());
                    }
                }
                case 3 -> {
                    if (tabInfoElement instanceof StatsWidget w) {
                        w.setBaseStats(form.getBaseStats());
                        w.setRideProperties(form.getRiding());
                        if (form.getRiding().getBehaviours() != null) {
                            gui.addRenderableWidget(w.getLeftButton());
                            gui.addRenderableWidget(w.getRightButton());
                            if (form.getRiding().getBehaviours().size() > 1) {
                                gui.addRenderableWidget(w.getLeftSubButton());
                                gui.addRenderableWidget(w.getRightSubButton());
                            }
                        }
                    }
                }
                case 4 -> {
                    if (tabInfoElement instanceof DropsScrollingWidget w) {
                        w.setDropTable(form.getDrops());
                        w.setEntries();
                    }
                }
                case 5 -> {}
            }
        } else {
            if (tabInfoIndex != 0 && tabInfoIndex != 5) gui.displaytabInfoElement(0, true);
            if (tabInfoElement instanceof DescriptionWidget w) w.setShowPlaceholder(true);
        }

        if (tabInfoIndex == 0 && tabInfoElement instanceof DescriptionWidget w) {
            w.setText(description);
            w.setScrollAmount(0);
        }
    }

    /**
     * Resets the tab state when switching to a different Pokémon entry.
     */
    @Inject(method = "setSelectedEntry", at = @At("HEAD"), remap = false)
    private void onSetSelectedEntry(PokedexEntry newSelectedEntry, CallbackInfo ci) {
        if (tabInfoIndex == 5) {
            PokedexGUI gui = cast(this);
            if (tabInfoElement instanceof PokespawnWidget w) {
                gui.removeWidget(w.getLeftButton());
                gui.removeWidget(w.getRightButton());
            }
            tabInfoIndex = 0;
        }
    }

    /**
     * Dynamically modifies the X coordinate of the selection arrow blit call to align with 6 tabs.
     */
    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/cobblemon/mod/common/api/gui/GuiUtilsKt;blitk$default(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/resources/ResourceLocation;Ljava/lang/Number;Ljava/lang/Number;Ljava/lang/Number;Ljava/lang/Number;Ljava/lang/Number;Ljava/lang/Number;Ljava/lang/Number;Ljava/lang/Number;Ljava/lang/Number;Ljava/lang/Number;Ljava/lang/Number;Ljava/lang/Number;Ljava/lang/Number;ZFILjava/lang/Object;)V",
                    ordinal = 5
            ),
            index = 2,
            remap = true
    )
    private Number modifyArrowX(Number original) {
        PokedexGUI self = cast(this);
        int x = (self.width - PokedexGUIConstants.BASE_WIDTH) / 2;
        return (x + 191.5F + (22 * tabInfoIndex)) / 0.5F;
    }
}