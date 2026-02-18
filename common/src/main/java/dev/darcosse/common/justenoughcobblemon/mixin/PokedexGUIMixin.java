package dev.darcosse.common.justenoughcobblemon.mixin;

import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUI;
import com.cobblemon.mod.common.client.gui.pokedex.ScaledButton;
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants;
import dev.darcosse.common.justenoughcobblemon.client.gui.MyCustomWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;

@Mixin(value = PokedexGUI.class, remap = false)
public class PokedexGUIMixin {

    @Shadow private List<ScaledButton> tabButtons;
    @Shadow public GuiEventListener tabInfoElement;
    @Shadow public int tabInfoIndex;

    @Inject(method = "setUpTabs", at = @At("TAIL"))
    private void injectMyTab(CallbackInfo ci) {
        PokedexGUI self = (PokedexGUI)(Object)this;
        int x = (self.width - PokedexGUIConstants.BASE_WIDTH) / 2;
        int y = (self.height - PokedexGUIConstants.BASE_HEIGHT) / 2;

        ResourceLocation myIcon = ResourceLocation.fromNamespaceAndPath("cobblemon", "textures/gui/pokedex/tab_drops.png");

        ScaledButton myTab = new ScaledButton(
                x + 197F + (5 * 25F),
                y + 181.5F,
                PokedexGUIConstants.TAB_ICON_SIZE,
                PokedexGUIConstants.TAB_ICON_SIZE,
                myIcon,
                0.5F,
                false,
                (btn) -> {
                    if (canSelectTabInject(5)) displayTabInject(5);
                }
        );
        tabButtons.add(myTab);
        ((ScreenAccessor)(Object)self).invokeAddRenderableWidget(myTab);
    }

    @Inject(method = "displaytabInfoElement", at = @At("HEAD"), cancellable = true)
    private void injectDisplayTab(int tabIndex, boolean update, CallbackInfo ci) {
        if (tabIndex != 5) return;

        PokedexGUI self = (PokedexGUI)(Object)this;
        int x = (self.width - PokedexGUIConstants.BASE_WIDTH) / 2;
        int y = (self.height - PokedexGUIConstants.BASE_HEIGHT) / 2;

        // Désactiver tous les tabs sauf le nôtre
        for (int i = 0; i < tabButtons.size(); i++) {
            tabButtons.get(i).setWidgetActive(i == 5);
        }

        if (tabInfoElement instanceof net.minecraft.client.gui.components.AbstractWidget) {
            ((ScreenAccessor)(Object)self).invokeRemoveWidget(tabInfoElement);
        }

        tabInfoIndex = 5;
        MyCustomWidget widget = new MyCustomWidget(x + 180, y + 135);
        tabInfoElement = widget;
        ((ScreenAccessor)(Object)self).invokeAddRenderableWidget(widget);
        ci.cancel();
    }

    @Inject(method = "updateTabInfoElement", at = @At("HEAD"), cancellable = true)
    private void injectUpdateTab(CallbackInfo ci) {
        if (tabInfoIndex == 5) ci.cancel();
    }

    private boolean canSelectTabInject(int tabIndex) {
        return tabIndex != tabInfoIndex;
    }

    private void displayTabInject(int tabIndex) {
        ((PokedexGUI)(Object)this).displaytabInfoElement(tabIndex, true);
    }
}