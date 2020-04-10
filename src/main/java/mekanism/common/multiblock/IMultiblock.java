package mekanism.common.multiblock;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;

public interface IMultiblock<T extends SynchronizedData<T>> {

    T getSynchronizedData();

    ActionResultType onActivate(PlayerEntity player, Hand hand, ItemStack stack);

    void doUpdate();
}