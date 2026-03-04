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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(Inventory.class)
public class InventoryMixin {

    @Final @Shadow private List<NonNullList<ItemStack>> compartments;

    @Unique Optional<ExtendedInventoryHolder> dynamicIE$extendedHolder;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void dynamicIE$extendItemList(Player player, CallbackInfo ci) {
        dynamicIE$extendedHolder = Optional.of(new ExtendedInventoryHolder(DynamicInventoryExtender.getRowSize()));
    }

    @Unique private List<NonNullList<ItemStack>> dynamicIE$redirectHelper(List<NonNullList<ItemStack>> original) {
        if (dynamicIE$extendedHolder.isPresent())
            return dynamicIE$extendedHolder.get().extendedCompartments(original);
        return original;
    }

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

    /* Contains */
    @ModifyExpressionValue(method = "contains(Lnet/minecraft/world/item/ItemStack;)Z", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Inventory;compartments:Ljava/util/List;"))
    private List<NonNullList<ItemStack>> dynamicIE$redirectContainsItem(List<NonNullList<ItemStack>> original) { return dynamicIE$redirectHelper(original); }

    @ModifyExpressionValue(method = "contains(Lnet/minecraft/tags/TagKey;)Z", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Inventory;compartments:Ljava/util/List;"))
    private List<NonNullList<ItemStack>> dynamicIE$redirectContainsItemTag(List<NonNullList<ItemStack>> original) { return dynamicIE$redirectHelper(original); }

    @ModifyExpressionValue(method = "contains(Ljava/util/function/Predicate;)Z", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Inventory;compartments:Ljava/util/List;"))
    private List<NonNullList<ItemStack>> dynamicIE$redirectContainsPredicate(List<NonNullList<ItemStack>> original) { return dynamicIE$redirectHelper(original); }

    @Inject(method = "getFreeSlot", at = @At("RETURN"), cancellable = true)
    public void dynamicIE$getFreeSlotExtended(CallbackInfoReturnable<Integer> cir) {
        if (cir.getReturnValue() == -1) {
            dynamicIE$extendedHolder.ifPresent((holder) -> {
                for (int i = 0; i < holder.size(); i++) {
                    if (holder.get().get(i).isEmpty())
                        cir.setReturnValue(holder.getAbsoluteIndex(i));
                }
            });
        }
    }

    @Inject(method = "fillStackedContents", at = @At("TAIL"))
    public void dynamicIE$fillStackedContents(StackedContents stackedContent, CallbackInfo ci) {
        dynamicIE$extendedHolder.ifPresent(
            (holder) -> {
                for (ItemStack itemstack : holder.get())
                    stackedContent.accountSimpleStack(itemstack);
            }
        );
    }

    @Inject(method = "getName", at = @At("RETURN"), cancellable = true)
    public void dynamicIE$getName(CallbackInfoReturnable<Component> cir) {
        cir.setReturnValue(Component.literal("Inventorrgy"));
    }

    @Inject(method = "getContainerSize", at = @At("RETURN"), cancellable = true)
    void dynamicIE$extendContainerSize(CallbackInfoReturnable<Integer> cir) {
        dynamicIE$extendedHolder.ifPresent((holder) -> cir.setReturnValue(cir.getReturnValue() + holder.size()));
    }

    @Inject(method = "isEmpty", at = @At("RETURN"), cancellable = true)
    void dynamicIE$extendedEmpty(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue())
            dynamicIE$extendedHolder.ifPresent((holder) -> cir.setReturnValue(holder.isEmpty()));
    }
}
