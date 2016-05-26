package mekanism.common.multiblock;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public interface IMultiblock<T extends SynchronizedData<T>>
{
	public T getSynchronizedData();
	
	public boolean onActivate(EntityPlayer player, EnumHand hand, ItemStack stack);
	
	public void doUpdate();
}
