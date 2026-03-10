package io.github.lizzyapp.dynamic_ie.api;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * An object which holds the player's additional inventory slots.
 */
public class ExtendedInventoryHolder {
    private final NonNullList<ItemStack> extendedItems;
    public NonNullList<ItemStack> get() {
        return extendedItems;
    }
    public ExtendedInventoryHolder(int capacity) {
        extendedItems = NonNullList.withSize(capacity, ItemStack.EMPTY);
    }

    private List<NonNullList<ItemStack>> extendedCompartmentList;
    public List<NonNullList<ItemStack>> extendedCompartments(List<NonNullList<ItemStack>> original) {
        if (extendedCompartmentList == null) {
            extendedCompartmentList = new ArrayList<>(original);
            extendedCompartmentList.add(extendedItems);
        }
        return extendedCompartmentList;
    }

    public int getAbsoluteIndex(int index) {
        return (index + Inventory.SLOT_OFFHAND) + 1;
    }

    public boolean isEmpty() {
        return extendedItems.isEmpty();
    }

    public int size() {
        return extendedItems.size();
    }
}
