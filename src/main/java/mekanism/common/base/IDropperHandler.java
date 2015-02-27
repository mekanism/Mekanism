package mekanism.common.base;

import net.minecraft.entity.player.EntityPlayer;

public interface IDropperHandler 
{
	public int getTankId(Object tank);
	
	public void useDropper(EntityPlayer player, int tankId);
}
