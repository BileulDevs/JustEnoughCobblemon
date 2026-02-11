package dev.darcosse.common.justenoughcobblemon.mixin;

import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUI;
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants;
import com.cobblemon.mod.common.client.gui.pokedex.ScaledButton;
import dev.darcosse.common.justenoughcobblemon.client.gui.pokedex.pages.SpawnPokedexPage;
import dev.darcosse.common.justenoughcobblemon.util.SpawnController;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = PokedexGUI.class, remap = false)
public abstract class PokedexGUIMixin implements SpawnController {

    @Shadow private List<ScaledButton> tabButtons;

    @Shadow public abstract void displaytabInfoElement(int index, boolean update);

    @Unique
    private boolean isSpawnPageOpen = false;

    // --- Implémentation de l'interface SpawnController ---

    @Override
    public void setSpawnPageOpen(boolean open) {
        this.isSpawnPageOpen = open;
    }

    @Override
    public boolean isSpawnPageOpen() {
        return this.isSpawnPageOpen;
    }

    // --- Injections ---

    /**
     * Ajoute notre bouton de spawn à la liste des onglets lors de l'initialisation.
     */
    @Inject(method = "setUpTabs", at = @At("TAIL"))
    private void injectSpawnTab(CallbackInfo ci) {
        PokedexGUI gui = (PokedexGUI) (Object) this;

        float xOffset = (gui.width - PokedexGUIConstants.BASE_WIDTH) / 2f;
        float yOffset = (gui.height - PokedexGUIConstants.BASE_HEIGHT) / 2f;

        // On utilise notre Helper Kotlin pour créer le bouton
        ScaledButton spawnTab = SpawnPokedexPage.createTabButton(
                gui,
                xOffset + 322f,
                yOffset + 181.5f
        );

        this.tabButtons.add(spawnTab);

        // Utilisation de l'invoker pour ajouter le widget au Screen Minecraft
        ((ScreenAccessor) (Object) gui).callAddRenderableWidget(spawnTab);
    }

    /**
     * Si l'utilisateur clique sur un autre onglet (Info, Stats, etc.),
     * on ferme automatiquement notre page de spawn.
     */
    @Inject(method = "displaytabInfoElement", at = @At("HEAD"))
    private void onTabChange(int index, boolean update, CallbackInfo ci) {
        if (index != -1) {
            this.isSpawnPageOpen = false;
        }
    }

    /**
     * Rendu de notre page personnalisée.
     */
    @Inject(method = "render", at = @At("TAIL"))
    private void renderSpawnContent(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.isSpawnPageOpen) {
            PokedexGUI gui = (PokedexGUI) (Object) this;

            // On instancie et on rend notre page
            // Note: On pourrait mettre l'instance en cache si besoin de performances
            new SpawnPokedexPage(gui).render(graphics, mouseX, mouseY, delta);
        }
    }
}