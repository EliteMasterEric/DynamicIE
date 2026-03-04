package io.github.lizzyapp.dynamic_ie.api.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;

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
