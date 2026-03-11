package io.github.lizzyapp.dynamic_ie.api;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import io.github.lizzyapp.dynamic_ie.DynamicInventoryExtender;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * A serializable object which holds the player's additional inventory slots.
 */
public class ExtendedInventoryHolder {
    private final Inventory inventory;
    private NonNullList<ItemStack> extendedItems;

    public ExtendedInventoryHolder(Inventory inventory, int capacity) {
        this.inventory = inventory;
        this.extendedItems = NonNullList.withSize(capacity, ItemStack.EMPTY);
    }

    public NonNullList<ItemStack> get() {
        return extendedItems;
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
        return index + (Inventory.SLOT_OFFHAND + 1);
    }

    public int getRelativeIndex(int absoluteIndex) {
        return absoluteIndex - (Inventory.SLOT_OFFHAND + 1);
    }

    public ItemStack getRelative(int index) {
        return extendedItems.get(index);
    }

    public void setRelative(int index, @NotNull ItemStack stack) {
        extendedItems.set(index, stack);
    }

    public ItemStack getAbsolute(int index) {
        return extendedItems.get(getRelativeIndex(index));
    }

    public void setAbsolute(int index, @NotNull ItemStack stack) {
        extendedItems.set(getRelativeIndex(index), stack);
    }

    public void resize(int capacity) {
        NonNullList<ItemStack> newExtendedItems = NonNullList.withSize(capacity, ItemStack.EMPTY);

        for (int i = 0; i < Math.min(capacity, extendedItems.size()); i++) {
            newExtendedItems.set(i, extendedItems.get(i));
        }

        extendedItems = newExtendedItems;
    }

    public void clear() {
        extendedItems.clear();
    }

    public boolean isEmpty() {
        return extendedItems.isEmpty();
    }

    /**
     * @return The size of the extended inventory.
     */
    public int size() {
        return extendedItems.size();
    }

    /**
	 * Reads from the given NBT tag and builds an Extended Inventory with the correct items.
	 */
	public void load(CompoundTag compoundTag) {
        
        int capacity = compoundTag.getInt("Capacity");

        // DEBUG: Reset the capacity to the configured amount.
        if (capacity <= 0) {
            capacity = DynamicInventoryExtender.getExtraSlotCount();
        }

        this.resize(capacity);
        this.clear();

        ListTag inventoryList = compoundTag.getList("Inventory", 10);

        for (int i = 0; i < capacity; i++) {
            if (i >= inventoryList.size()) {
                DynamicInventoryExtender.LOGGER.warn("Filling remaining slots with empty stacks.");
                break;
            }

            CompoundTag itemCompoundTag = inventoryList.getCompound(i);
            // Slot index as an integer to 
            int slotIndex = itemCompoundTag.getInt("Slot");

            ItemStack itemStack = (ItemStack) ItemStack.parseOptional(inventory.player.registryAccess(), itemCompoundTag);

            this.setRelative(slotIndex, itemStack);
        }
	}

    /**
	 * Writes the current state of the Extended Inventory to an NBT tag, which can be read from later to restore the inventory.
	 */
    public CompoundTag save() {
        CompoundTag compoundTag = new CompoundTag();

        compoundTag.putInt("Capacity", this.size());

        ListTag inventoryList = new ListTag();

        for (int i = 0; i < this.size(); i++) {
            ItemStack itemStack = this.getRelative(i);

            if (itemStack.isEmpty()) continue;

            CompoundTag itemCompoundTag = new CompoundTag();
            
            itemCompoundTag.putInt("Slot", i);

            inventoryList.add(itemStack.save(inventory.player.registryAccess(), itemCompoundTag));
        }

        compoundTag.put("Inventory", inventoryList);

        return compoundTag;
    }
}
