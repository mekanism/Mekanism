package mekanism.common.inventory.container;

import mekanism.common.tile.TileEntityContainerBlock;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class ContainerNull extends Container
{
	private TileEntityContainerBlock tileEntity;

	public ContainerNull(EntityPlayer player, TileEntityContainerBlock tile)
	{
		tileEntity = tile;

		tileEntity.open(player);
		tileEntity.openInventory();
	}
	
	public ContainerNull() {}

	@Override
	public void onContainerClosed(EntityPlayer entityplayer)
	{
		super.onContainerClosed(entityplayer);

		if(tileEntity != null)
		{
			tileEntity.close(entityplayer);
			tileEntity.closeInventory();
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
	{
		if(tileEntity != null)
		{
			return tileEntity.isUseableByPlayer(entityplayer);
		}
		
		return true;
	}
}
