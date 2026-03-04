package io.github.lizzyapp.dynamic_ie.mixin.integration;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import io.github.lizzyapp.dynamic_ie.api.client.MenuRenderContext;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* This extends AbstractContainer, so there's really no need, but regardless this is cleaner */
@IfModLoaded("create")
@Mixin(AbstractSimiContainerScreen.class)
public class AbstractSimiContainerScreenMixin<T extends AbstractContainerMenu> {

    @Inject(method="renderPlayerInventory", at = @At("HEAD"))
    void dynamicIE$preRender(GuiGraphics graphics, int x, int y, CallbackInfo ci) {
        MenuRenderContext.setContext((AbstractContainerScreen) (Object) this);
    }

    @Inject(method="renderPlayerInventory", at = @At("TAIL"))
    void dynamicIE$render(GuiGraphics graphics, int x, int y, CallbackInfo ci) {
        MenuRenderContext.clearContext();
    }
}
