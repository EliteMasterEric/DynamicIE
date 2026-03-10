package io.github.lizzyapp.dynamic_ie.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.lizzyapp.dynamic_ie.DynamicInventoryExtender;
import io.github.lizzyapp.dynamic_ie.api.ExtendedInventoryHolder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

/**
 * A mixin injected into the player's Inventory, to store the additional inventory items
 * and redirect inventory access to them when necessary.
 */
@Mixin(Inventory.class)
public class InventoryMixin {

    @Final @Shadow private List<NonNullList<ItemStack>> compartments;

    @Unique Optional<ExtendedInventoryHolder> dynamicIE$extendedHolder;

    /**
     * When the player's inventory is initialized, instantiate a new ExtendedInventoryHolder to hold the additional slots.
     */
    @Inject(method = "<init>", at = @At("TAIL"))
    public void dynamicIE$extendItemList(Player player, CallbackInfo ci) {
        dynamicIE$extendedHolder = Optional.of(new ExtendedInventoryHolder(DynamicInventoryExtender.getExtraSlotCount()));
    }

    /**
     * Extends `Inventory.compartments` to include the extended inventory as an additional compartment.
     * @param original The base list of compartments, each a list of ItemStacks.
     * @return The modified list of compartments, with the extended inventory added as the last compartment.
     */
    @Unique private List<NonNullList<ItemStack>> dynamicIE$redirectHelper(List<NonNullList<ItemStack>> original) {
        if (dynamicIE$extendedHolder.isPresent())
            return dynamicIE$extendedHolder.get().extendedCompartments(original);
        return original;
    }


    /**
     * If the ExtendedInventoryHolder is initialized, check it for free slots if the base inventory is full when trying to add an item.
     */
    @Inject(method = "getFreeSlot", at = @At("RETURN"), cancellable = true)
    public void dynamicIE$getFreeSlotExtended(CallbackInfoReturnable<Integer> cir) {
        boolean isBaseInventoryFull = cir.getReturnValue() == -1;

        if (isBaseInventoryFull) {
            dynamicIE$extendedHolder.ifPresent((holder) -> {
                for (int i = 0; i < holder.size(); i++) {
                    if (holder.get().get(i).isEmpty())
                        cir.setReturnValue(holder.getAbsoluteIndex(i));
                }
            });
        }
    }

    /**
     * If the ExtendedInventoryHolder is initialized, account its items in the StackedContents for crafting recipe matching.
     */
    @Inject(method = "fillStackedContents", at = @At("TAIL"))
    public void dynamicIE$fillStackedContents(StackedContents stackedContent, CallbackInfo ci) {
        dynamicIE$extendedHolder.ifPresent(
            (holder) -> {
                for (ItemStack itemstack : holder.get())
                    stackedContent.accountSimpleStack(itemstack);
            }
        );
    }

    /**
     * If the ExtendedInventoryHolder is initialized, add its size to the container size.
     */
    @Inject(method = "getContainerSize", at = @At("RETURN"), cancellable = true)
    void dynamicIE$extendContainerSize(CallbackInfoReturnable<Integer> cir) {
        dynamicIE$extendedHolder.ifPresent((holder) -> cir.setReturnValue(cir.getReturnValue() + holder.size()));
    }

    /**
     * If the ExtendedInventoryHolder is initialized, check if it is empty.
     */
    @Inject(method = "isEmpty", at = @At("RETURN"), cancellable = true)
    void dynamicIE$extendedEmpty(CallbackInfoReturnable<Boolean> cir) {
        // We only have to check the extended inventory if the base inventory is empty, otherwise we can just return false.
        boolean isBaseInventoryEmpty = cir.getReturnValue();
        if (isBaseInventoryEmpty) {
            dynamicIE$extendedHolder.ifPresent((holder) -> cir.setReturnValue(holder.isEmpty()));
        }
    }

    /**
     * DEBUG: Replace the name of the inventory container to verify that the mixin is being applied.
     */
    @Inject(method = "getName", at = @At("RETURN"), cancellable = true)
    public void dynamicIE$getName(CallbackInfoReturnable<Component> cir) {
        cir.setReturnValue(Component.literal("Inventorrgy"));
    }

    // Modify the access to `Inventory.compartments` in all methods that access it, to redirect to the extended inventory.

    @ModifyExpressionValue(method = "getItem", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Inventory;compartments:Ljava/util/List;", opcode = Opcodes.GETFIELD))
    private List<NonNullList<ItemStack>> dynamicIE$redirectGetItem(List<NonNullList<ItemStack>> original) { return dynamicIE$redirectHelper(original); }

    @ModifyExpressionValue(method = "setItem", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Inventory;compartments:Ljava/util/List;", opcode = Opcodes.GETFIELD))
    private List<NonNullList<ItemStack>> dynamicIE$redirectSetItem(List<NonNullList<ItemStack>> original) { return dynamicIE$redirectHelper(original); }

    @ModifyExpressionValue(method = "removeItem(Lnet/minecraft/world/item/ItemStack;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Inventory;compartments:Ljava/util/List;", opcode = Opcodes.GETFIELD))
    private List<NonNullList<ItemStack>> dynamicIE$redirectRemoveItem(List<NonNullList<ItemStack>> original) { return dynamicIE$redirectHelper(original); }

    @ModifyExpressionValue(method = "removeItem(II)Lnet/minecraft/world/item/ItemStack;", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Inventory;compartments:Ljava/util/List;", opcode = Opcodes.GETFIELD))
    private List<NonNullList<ItemStack>> dynamicIE$redirectRemoveIndexedItem(List<NonNullList<ItemStack>> original) { return dynamicIE$redirectHelper(original); }

    @ModifyExpressionValue(method = "removeItemNoUpdate", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Inventory;compartments:Ljava/util/List;", opcode = Opcodes.GETFIELD))
    private List<NonNullList<ItemStack>> dynamicIE$redirectRemoveItemNoUpdate(List<NonNullList<ItemStack>> original) { return dynamicIE$redirectHelper(original); }

    @ModifyExpressionValue(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Inventory;compartments:Ljava/util/List;", opcode = Opcodes.GETFIELD))
    private List<NonNullList<ItemStack>> dynamicIE$redirectTick(List<NonNullList<ItemStack>> original) { return dynamicIE$redirectHelper(original); }

    @ModifyExpressionValue(method = "dropAll", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Inventory;compartments:Ljava/util/List;", opcode = Opcodes.GETFIELD))
    private List<NonNullList<ItemStack>> dynamicIE$redirectDropAll(List<NonNullList<ItemStack>> original) { return dynamicIE$redirectHelper(original); }

    @ModifyExpressionValue(method = "clearContent", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Inventory;compartments:Ljava/util/List;", opcode = Opcodes.GETFIELD))
    private List<NonNullList<ItemStack>> dynamicIE$redirectClearContent(List<NonNullList<ItemStack>> original) { return dynamicIE$redirectHelper(original); }

    @ModifyExpressionValue(method = "contains(Lnet/minecraft/world/item/ItemStack;)Z", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Inventory;compartments:Ljava/util/List;"))
    private List<NonNullList<ItemStack>> dynamicIE$redirectContainsItem(List<NonNullList<ItemStack>> original) { return dynamicIE$redirectHelper(original); }

    @ModifyExpressionValue(method = "contains(Lnet/minecraft/tags/TagKey;)Z", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Inventory;compartments:Ljava/util/List;"))
    private List<NonNullList<ItemStack>> dynamicIE$redirectContainsItemTag(List<NonNullList<ItemStack>> original) { return dynamicIE$redirectHelper(original); }

    @ModifyExpressionValue(method = "contains(Ljava/util/function/Predicate;)Z", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Inventory;compartments:Ljava/util/List;"))
    private List<NonNullList<ItemStack>> dynamicIE$redirectContainsPredicate(List<NonNullList<ItemStack>> original) { return dynamicIE$redirectHelper(original); }
}
