package io.github.lizzyapp.dynamic_ie.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.lizzyapp.dynamic_ie.DynamicInventoryExtender;
import io.github.lizzyapp.dynamic_ie.accessor.IExtendedInventoryHolderAccessor;
import io.github.lizzyapp.dynamic_ie.api.ExtendedInventoryHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

/**
 * A mixin injected into the Player, to handle storing and loading the Extended Inventory.
 */
@Mixin(Player.class)
public class PlayerMixin {
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	public void dynamicIE$readAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        DynamicInventoryExtender.LOGGER.info("Reading additional inventory data from NBT for player {}", ((Player) (Object) this).getName().getString());

        Inventory inventory = ((Player) (Object) this).getInventory();

        CompoundTag extendedInventoryTag = compound.getCompound("ExtendedInventory");

        Optional<ExtendedInventoryHolder> holder = ((IExtendedInventoryHolderAccessor) inventory).dynamicIE$getExtendedInventoryHolder();

        if (holder.isPresent()) {
            holder.get().load(extendedInventoryTag);
        }
    }

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	public void dynamicIE$addAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        Inventory inventory = ((Player) (Object) this).getInventory();

        DynamicInventoryExtender.LOGGER.info("Writing additional inventory data to NBT for player {}", inventory.player.getName().getString());

        Optional<ExtendedInventoryHolder> holder = ((IExtendedInventoryHolderAccessor) inventory).dynamicIE$getExtendedInventoryHolder();

        if (holder.isPresent()) {
            CompoundTag extendedInventoryTag = holder.get().save();
            compound.put("ExtendedInventory", extendedInventoryTag);
        }
    }
}
