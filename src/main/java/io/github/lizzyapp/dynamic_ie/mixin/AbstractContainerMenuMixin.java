package io.github.lizzyapp.dynamic_ie.mixin;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.lizzyapp.dynamic_ie.DynamicInventoryExtender;
import io.github.lizzyapp.dynamic_ie.accessor.IStoredPointAccessor;
import io.github.lizzyapp.dynamic_ie.util.InventorySlotUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

/**
 * A mixin into every AbstractContainerMenu,
 * which should allow for accessing all vanilla inventories and most standard modded ones.
 * 
 * @see io.github.lizzyapp.dynamic_ie.mixin.integration for reimplementations for modded containers
 */
@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin implements IStoredPointAccessor {

    @Shadow @Final public NonNullList<Slot> slots;

    @Unique AbstractContainerMenu dynamicIE$self = (AbstractContainerMenu) (Object) (this);
    @Shadow protected Slot addSlot(Slot slot) { return null; };

    @Unique int dynamicIE$internalSlotCounter = 0;

    /**
     * Whether the additional inventory slots provided by DynamicIE have been added to the container.
     */
    @Unique boolean dynamicIE$postConstructed = false;

    /**
     * Stores the vertical position of the lowest inventory slot (excluding the hotbar)
     * Used by the GuiGraphicsMixin to help render the extra inventory slots.
     */
    @Override public int dynamicIE$getLowestPoint() {
        return dynamicIE$lowestOrganicY;
    }
    @Unique public int dynamicIE$lowestOrganicY = 0;

    @Unique private void dynamicIE$constructUI(Container container) {
        dynamicIE$postConstructed = true;
        
        int separator = InventorySlotUtil.DEFAULT_SLOT_SEPARATION;

        // Gather Inventory only slots (bottom to top)
        Stream<ArrayList<Slot>> inventorySlots = InventorySlotUtil.gatherInventorySlots(dynamicIE$self);

        // Iterate through each row to get the horizontal position of the leftmost slot,
        // and vertical posiition of the lowest inventory slot (excluding the hotbar)
        AtomicInteger x = new AtomicInteger();
        AtomicInteger lowest_y = new AtomicInteger();
        inventorySlots.forEach((slotRow) -> {
            // Move Hotbar Slots down
            if (InventorySlotUtil.isHotbarSlot(slotRow.getFirst()))
                slotRow.forEach((slot) -> {slot.y += DynamicInventoryExtender.getHotbarPixelOffset();});
            else {
                // Compare to previous positions found
                x.set(slotRow.getFirst().x);
                lowest_y.set(Math.max(lowest_y.get(), slotRow.getFirst().y));
            }
        });

        // Add more inventory rows
        for (int j = 1; j < (DynamicInventoryExtender.rowAmount + 1); j++) {
            for (int i = 0; i < InventorySlotUtil.DEFAULT_ROW_SIZE; i++) {
                int slotIndex = Inventory.SLOT_OFFHAND
                    + ((i + 1) + (InventorySlotUtil.DEFAULT_ROW_SIZE * (j - 1)));
                this.addSlot(
                    new Slot(
                        container, slotIndex,
                        x.get() + (separator * i),
                        (lowest_y.get() + (separator * j))
                    )
                );
            }
        }
        dynamicIE$lowestOrganicY = lowest_y.get();
    }

    @Inject(method = "addSlot", at = @At("TAIL"), cancellable = true)
    protected void addSlotPost(Slot slot, CallbackInfoReturnable<Slot> cir) {
        int totalInventorySize = Inventory.INVENTORY_SIZE;
        if (!dynamicIE$postConstructed && InventorySlotUtil.isInventorySlot(slot)) {
            dynamicIE$internalSlotCounter++;
            if (dynamicIE$internalSlotCounter >= totalInventorySize)
                dynamicIE$constructUI(slot.container);
        }
    }
}