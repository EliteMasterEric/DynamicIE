package io.github.lizzyapp.dynamic_ie.util;

import java.util.ArrayList;
import java.util.stream.Stream;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

/**
 * A class containing utility functions for working with inventory slots.
 */
public class InventorySlotUtil {

    /**
     * The number of slots in one row of the inventory.
     */
    public static final int DEFAULT_ROW_SIZE = 9;

    /**
     * The size of one inventory slot, in pixels. Used to determine how much to offset the hotbar.
     */
    public static final int DEFAULT_SLOT_SEPARATION = 18;

    /**
     * Gather the slots of an AbstractContainerMenu as a series of rows,
     * filtered just to the slots for the player's inventory (including the hotbar).
     * 
     * @param menu The menu to gather the slots from
     * @return A stream of rows of inventory slots, where each row is an ArrayList of Slots
     */
    public static <T extends AbstractContainerMenu> Stream<ArrayList<Slot>> gatherInventorySlots(T menu) {
        // We can assume that a row of inventory slots is always 9 slots,
        // and any row of slots that isn'''
        return gatherSlotsAsRows(menu).filter((predicate)
            -> (predicate.size() == InventorySlotUtil.DEFAULT_ROW_SIZE));
    }

    /**
     * Gathers the slots of an AbstractContainerMenu into a stream of rows.
     * 
     * @param menu The menu to gather the slots from
     * @return A stream of rows of container slots, where each row is an ArrayList of Slots
     */
    public static <T extends AbstractContainerMenu> Stream<ArrayList<Slot>> gatherSlotsAsRows(T menu) {
        ArrayList<ArrayList<Slot>> nestedSlotArray = new ArrayList<>();
        nestedSlotArray.add(new ArrayList<>());

        int rowList = -1;
        Slot lastSlot = null;
        for (int k = 0; k < menu.slots.size(); k++) {
            Slot slot = menu.slots.get(k);
            if (isInventorySlot(slot)) {
                // Move to a new row if the slots aren't for the same inventory,
                // or if the slots aren't directly horizontally adjavent.
                if (!areSlotsAdjacent(slot, lastSlot) || !slot.isSameInventory(lastSlot))
                    rowList++;

                // Create a new ArrayList if we've moved to a new row.
                if ((nestedSlotArray.size() - 1) < rowList) {
                    nestedSlotArray.add(new ArrayList<>());
                }

                nestedSlotArray.get(rowList).add(slot);
                lastSlot = slot;
            }
        }
        return nestedSlotArray.stream();
    }

    /**
     * Deduces whether a given slot in the container is one of the slots for the player's inventory (including the hotbar).
     * 
     * @param slot The slot to check
     * @return Whether the slot is an inventory slot
     */
    public static boolean isInventorySlot(Slot slot) {
        if (slot.getContainerSlot() >= Inventory.INVENTORY_SIZE && slot.getContainerSlot() <= Inventory.SLOT_OFFHAND)
            return false;
        return (slot.container instanceof Inventory);
    }

    /**
     * Deduces whether a given slot in the container is one of the slots for the player's hotbar,
     * assuming it is one of the slots for the player's inventory.
     * 
     * @param slot The slot to check
     * @return Whether the slot is a hotbar slot
     */
    public static boolean isHotbarSlot(Slot slot) {
        return slot.getSlotIndex() < DEFAULT_ROW_SIZE;
    }

    /**
     * @param slot The first slot to compare
     * @param lastSlot The last slot to compare
     * @return Whether the two slots are horizontally adjacent.
     */
    public static boolean areSlotsAdjacent(Slot slot, Slot lastSlot) {
        return (lastSlot != null && ((lastSlot.y == slot.y)
            && ((slot.x - lastSlot.x) <= DEFAULT_SLOT_SEPARATION)));
    }
}
