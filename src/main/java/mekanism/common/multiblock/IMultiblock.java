package mekanism.common.multiblock;

import net.minecraft.entity.player.EntityPlayer;

public interface IMultiblock<T extends SynchronizedData<T>>
{
	public T getSynchronizedData();
	
	public boolean onActivate(EntityPlayer player);
	
	public void doUpdate();
}
