package io.github.lizzyapp.dynamic_ie.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.lizzyapp.dynamic_ie.DynamicInventoryExtender;
import io.github.lizzyapp.dynamic_ie.api.client.MenuRenderContext;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {

    @Shadow
    abstract void innerBlit(
        ResourceLocation atlasLocation, int x1, int x2,
        int y1, int y2, int blitOffset, float minU,
        float maxU, float minV, float maxV
    );

    @Unique private boolean dynamicIE$recursive = false;
    @Unique private GuiGraphics dynamicIE$self = (GuiGraphics) (Object) this;

    /**
     * Called when GuiGraphics renders an interface to the screen.
     * Generally only takes action if the interface contains the player's inventory.
     */
    @Inject(method = "innerBlit(Lnet/minecraft/resources/ResourceLocation;IIIIIFFFF)V", at = @At("HEAD"), cancellable = true)
    private void dynamicIE$innerBlit(
        ResourceLocation atlasLocation,
        int x1, int x2, int y1, int y2,
        int blitOffset, float minU, float maxU,
        float minV, float maxV, CallbackInfo ci
    ) {
        if (!dynamicIE$recursive) {
            dynamicIE$recursive = true;

            // If we are in an AbstractContainerScreen...
            AbstractContainerScreen<?> contextMenu = MenuRenderContext.getContext();
            if (contextMenu != null) {
                // ...attempt to render the custom inventory slots.
                if (DynamicInventoryExtender.debugRender(
                    (GuiGraphics) (Object) this, contextMenu, atlasLocation, x1, x2, y1, y2, blitOffset, minU, maxU, minV, maxV))
                    ci.cancel();
            }
            dynamicIE$recursive = false;
        }
    }
}
