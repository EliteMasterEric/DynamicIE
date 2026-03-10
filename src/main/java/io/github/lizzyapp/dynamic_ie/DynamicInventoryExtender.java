package io.github.lizzyapp.dynamic_ie;

import io.github.lizzyapp.dynamic_ie.accessor.IStoredPointAccessor;
import io.github.lizzyapp.dynamic_ie.util.InventorySlotUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(DynamicInventoryExtender.MODID)
public class DynamicInventoryExtender {
    public static final String MODID = "dynamic_inventory_extender";
    public static final Logger LOGGER = LogUtils.getLogger();

    /**
     * The number of extra rows to add to the inventory.
     * The big important magic number that should probably be in a config or tied to a custom item or something.
     */
    public static int rowAmount = 2;

    public DynamicInventoryExtender(IEventBus modEventBus, ModContainer modContainer) {}

    /**
     * @return The total number of extra inventory slots added by this mod.
     */
    public static int getExtraSlotCount() {
        return InventorySlotUtil.DEFAULT_ROW_SIZE * DynamicInventoryExtender.rowAmount;
    }

    /**
     * @return The vertical offset to apply to the hotbar slots, in pixels.
     */
    public static int getHotbarPixelOffset() {
        return InventorySlotUtil.DEFAULT_SLOT_SEPARATION * DynamicInventoryExtender.rowAmount;
    }

    public static boolean debugRender(
        GuiGraphics guiGraphics,
        AbstractContainerScreen<?> contextMenu,
        ResourceLocation atlasLocation,
        int x1, int x2, int y1, int y2,
        int blitOffset, float minU, float maxU,
        float minV, float maxV
    ) {
        // Obtain lowest organic row
        int rowBottom = ((IStoredPointAccessor) (Object) contextMenu.getMenu()).dynamicIE$getLowestPoint();

        int textureHeight = (y2 - y1);
        int relativeTopY = y1 - contextMenu.getGuiTop();

        // Favors divisible by two for some reason uuhh i dont really know or care atp
        // honestly its just good to floor it so its not at some weird 3rd interval or whatever
        // im tired im sorry in advance if this makes no sense
        int cutoffValue = (((rowBottom - relativeTopY) - 1) / 2) * 2;
        float percentage = (float) cutoffValue / (textureHeight);

        // used solely to ensure (guess) this is an inventory texture
        int textureWidth = (x2 - x1);
        if (textureWidth < ((InventorySlotUtil.DEFAULT_SLOT_SEPARATION * InventorySlotUtil.DEFAULT_ROW_SIZE) - 4))
            return false;

        if (rowBottom > relativeTopY && rowBottom <= (relativeTopY + textureHeight) && percentage < 1) {
            float percentageExtension = percentage + ((float) InventorySlotUtil.DEFAULT_SLOT_SEPARATION / textureHeight);
            guiGraphics.innerBlit(atlasLocation,
                x1, x2, y1, y1 + (int) (textureHeight * percentageExtension),
                blitOffset, minU, maxU,
                minV, maxV * percentageExtension
            );

            for (int i = 1; i < rowAmount + 1; i++) {
                int offset = InventorySlotUtil.DEFAULT_SLOT_SEPARATION * i;
                guiGraphics.innerBlit(atlasLocation,
                    x1, x2, (y2 - (int) (textureHeight * (1f - percentage)) + offset), y2 + offset,
                    blitOffset, minU, maxU,
                    (maxV * percentage) , maxV
                );
            }
            return true;
        }
        return false;
    }

}
