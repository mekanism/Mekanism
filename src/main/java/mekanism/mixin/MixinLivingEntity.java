package mekanism.mixin;

import mekanism.common.content.gear.IModuleContainerItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {

    @Inject(method = "equipmentHasChanged", at = @At("HEAD"), cancellable = true)
    public void equipmentHasChanged$handleMekGear(ItemStack oldItem, ItemStack newItem, CallbackInfoReturnable<Boolean> ci) {
        if (oldItem.getItem() == newItem.getItem() && newItem.getItem() instanceof IModuleContainerItem containerItem) {
            ci.setReturnValue(containerItem.hasEquipmentChanged(oldItem, newItem));
        }
    }
}