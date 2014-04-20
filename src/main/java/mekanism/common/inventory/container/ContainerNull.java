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
		tileEntity.openChest();
	}

	@Override
	public void onContainerClosed(EntityPlayer entityplayer)
	{
		super.onContainerClosed(entityplayer);

		tileEntity.close(entityplayer);
		tileEntity.closeChest();
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
	{
		return tileEntity.isUseableByPlayer(entityplayer);
	}
}
