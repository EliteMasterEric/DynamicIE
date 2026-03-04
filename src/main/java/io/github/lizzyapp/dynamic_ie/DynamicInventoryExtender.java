package io.github.lizzyapp.dynamic_ie;

import io.github.lizzyapp.dynamic_ie.accessor.IStoredPointAccessor;
import io.github.lizzyapp.dynamic_ie.api.client.MenuRenderContext;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ArmorSlot;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Mod(DynamicInventoryExtender.MODID)
public class DynamicInventoryExtender {
    public static final String MODID = "dynamic_inventory_extender";
    public static final Logger LOGGER = LogUtils.getLogger();

    public DynamicInventoryExtender(IEventBus modEventBus, ModContainer modContainer) {}

    public static boolean slotsAreAdjacent(Slot slot, Slot lastSlot) {
        return (lastSlot != null && ((lastSlot.y == slot.y)
            && ((slot.x - lastSlot.x) <= DEFAULT_SLOT_SEPARATION)));
    }

    // Can probably be optimized through interface injection of isInventory() to Container that returns true in class of Inventory
    public static boolean isInventorySlot(Slot slot) {
        if (slot.getContainerSlot() >= Inventory.INVENTORY_SIZE && slot.getContainerSlot() <= Inventory.SLOT_OFFHAND)
            return false;
        return (slot.container instanceof Inventory);
    }

    public static final int DEFAULT_ROW_SIZE = 9;
    public static boolean isHotbarSlot(Slot slot) {
        return slot.getSlotIndex() < DEFAULT_ROW_SIZE;
    }

    public static int rowAmount = 2;
    public static final int DEFAULT_SLOT_SEPARATION = 18;
    public static int getRowSize() {
        return DEFAULT_ROW_SIZE * rowAmount;
    }

    public static <T extends AbstractContainerMenu> Stream<ArrayList<Slot>> gatherSlotsAsRows(T menu) {
        ArrayList<ArrayList<Slot>> nestedSlotArray = new ArrayList<>();
        nestedSlotArray.add(new ArrayList<>());

        int rowList = -1;
        Slot lastSlot = null;
        for (int k = 0; k < menu.slots.size(); k++) {
            Slot slot = menu.slots.get(k);
            if (isInventorySlot(slot)) {
                // Increase the row if this slot is incompatible with the last
                if (!slotsAreAdjacent(slot, lastSlot) || !slot.isSameInventory(lastSlot))
                    rowList++;

                // Add to nested row list
                if ((nestedSlotArray.size() - 1) < rowList)
                    nestedSlotArray.add(new ArrayList<>());
                nestedSlotArray.get(rowList).add(slot);
                lastSlot = slot;
            }
        }
        return nestedSlotArray.stream();
    }

    public static int getHotbarOffset() {
        return DEFAULT_SLOT_SEPARATION * rowAmount;
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
        if (textureWidth < ((DEFAULT_SLOT_SEPARATION * DEFAULT_ROW_SIZE) - 4))
            return false;

        if (rowBottom > relativeTopY && rowBottom <= (relativeTopY + textureHeight) && percentage < 1) {
            float percentageExtension = percentage + ((float) DEFAULT_SLOT_SEPARATION / textureHeight);
            guiGraphics.innerBlit(atlasLocation,
                x1, x2, y1, y1 + (int) (textureHeight * percentageExtension),
                blitOffset, minU, maxU,
                minV, maxV * percentageExtension
            );

            for (int i = 1; i < rowAmount + 1; i++) {
                int offset = DEFAULT_SLOT_SEPARATION * i;
                guiGraphics.innerBlit(atlasLocation,
                    x1, x2, (y2 - (int) (textureHeight * (1f - percentage)) + offset), y2 + offset,
                    blitOffset, minU, maxU,
                    (maxV * percentage) , maxV
                );
            }
//            MenuRenderContext.clearContext();
            return true;
        }
        return false;
    }

    public static <T extends AbstractContainerMenu> Stream<ArrayList<Slot>> gatherInventorySlots(T menu) {
        return DynamicInventoryExtender.gatherSlotsAsRows(menu).filter((predicate)
            -> (predicate.size() == DEFAULT_ROW_SIZE));
    }
}
