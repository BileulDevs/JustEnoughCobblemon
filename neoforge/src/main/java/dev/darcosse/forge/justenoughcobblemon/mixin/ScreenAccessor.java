package dev.darcosse.forge.justenoughcobblemon.mixin;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Accessor interface for the Minecraft Screen class to expose protected methods.
 *
 * @author Darcosse
 * @version 1.0
 * @since 2026
 */
@Mixin(Screen.class)
public interface ScreenAccessor {

    /**
     * Invokes the protected addRenderableWidget method to register a new widget to the screen.
     */
    @Invoker("addRenderableWidget")
    <T extends GuiEventListener & Renderable & NarratableEntry> T invokeAddRenderableWidget(T widget);

    /**
     * Invokes the protected removeWidget method to unregister an existing widget from the screen.
     */
    @Invoker("removeWidget")
    void invokeRemoveWidget(GuiEventListener widget);
}