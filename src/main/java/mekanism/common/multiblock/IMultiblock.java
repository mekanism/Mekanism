package mekanism.common.multiblock;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public interface IMultiblock<T extends SynchronizedData<T>> {

    T getSynchronizedData();

    boolean onActivate(EntityPlayer player, EnumHand hand, ItemStack stack);

    void doUpdate();
}
