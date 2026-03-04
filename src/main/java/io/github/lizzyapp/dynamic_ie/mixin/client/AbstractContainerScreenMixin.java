package io.github.lizzyapp.dynamic_ie.mixin.client;

import io.github.lizzyapp.dynamic_ie.DynamicInventoryExtender;
import io.github.lizzyapp.dynamic_ie.api.client.MenuRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> {

    @Final @Shadow protected T menu;

    @Inject(method="render", at = @At("HEAD"))
    void dynamicIE$preRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        MenuRenderContext.setContext((AbstractContainerScreen) (Object) this);
    }

    @Inject(method="render", at = @At("TAIL"))
    void dynamicIE$render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        // Debug Rendering
        DynamicInventoryExtender.gatherInventorySlots(menu).forEach((row) -> {
            guiGraphics.drawString(Minecraft.getInstance().font,
                row.size() + " : " + row.getFirst().getSlotIndex() + " : " + row.getFirst().getContainerSlot(),
                row.getFirst().x, row.getFirst().y, 255);
        });

        // Clear render context
        MenuRenderContext.clearContext();
    }

}