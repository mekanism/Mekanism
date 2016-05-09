package mekanism.common.multiblock;

import mekanism.api.Coord4D;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public interface IStructuralMultiblock 
{
	public boolean onActivate(EntityPlayer player);
	
	public boolean canInterface(TileEntity controller);
	
	public void setController(Coord4D coord);
	
	public void doUpdate();
}
