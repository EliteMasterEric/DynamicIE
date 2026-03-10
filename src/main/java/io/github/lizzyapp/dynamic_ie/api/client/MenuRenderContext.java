package io.github.lizzyapp.dynamic_ie.api.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

/**
 * A static container which provides a reference to the current ContainerScreen being rendered.
 * This allows the GuiGraphicsMixin to access the current container while rendering.
 */
@SuppressWarnings("rawtypes")
public class MenuRenderContext {
    private static AbstractContainerScreen context;
    public static AbstractContainerScreen getContext() {
        return context;
    }
    public static void setContext(AbstractContainerScreen newContext) {
        context = newContext;
    }
    public static void clearContext() {
        context = null;
    }
}
