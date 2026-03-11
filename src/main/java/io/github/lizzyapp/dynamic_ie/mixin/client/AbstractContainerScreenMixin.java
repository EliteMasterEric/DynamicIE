package io.github.lizzyapp.dynamic_ie.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.lizzyapp.dynamic_ie.api.client.MenuRenderContext;
import io.github.lizzyapp.dynamic_ie.util.InventorySlotUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;

@SuppressWarnings("rawtypes")
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> {

    @Final @Shadow protected T menu;
    
    /**
     * Before rendering, store a reference to the current AbstractContainerScreen,
     * so we can access it from the GuiGraphics mixin.
     */
    @Inject(method="render", at = @At("HEAD"))
    void dynamicIE$preRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        // Apply render context
        MenuRenderContext.setContext((AbstractContainerScreen) (Object) this);
    }

    /**
     * After rendering, clear the reference to the current AbstractContainerScreen,
     * to prevent it from being accidentally used in future renders.
     */
    @Inject(method="render", at = @At("TAIL"))
    void dynamicIE$render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        // DEBUG: Render the list of inventory rows as text
        InventorySlotUtil.gatherInventorySlots(menu).forEach((row) -> {
            guiGraphics.drawString(Minecraft.getInstance().font,
                row.size() + " : " + row.getFirst().getSlotIndex() + " : " + row.getFirst().getContainerSlot(),
                row.getFirst().x, row.getFirst().y, 255);
        });

        // Clear render context
        MenuRenderContext.clearContext();
    }

}