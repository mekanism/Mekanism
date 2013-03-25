package mekanism.api;

import net.minecraftforge.common.ForgeDirection;

public interface ICableOutputter 
{
	public boolean canOutputTo(ForgeDirection side);
}
