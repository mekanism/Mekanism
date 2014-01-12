package mekanism.common.inventory.container;

import mekanism.common.tile.TileEntitySalinationController;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;


public class ContainerSalinationController extends Container
{
	private TileEntitySalinationController tileEntity;

	public ContainerSalinationController(InventoryPlayer inventory, TileEntitySalinationController tile)
	{
		tileEntity = tile;

		int slotX;

		for(slotX = 0; slotX < 3; slotX++)
		{
			for(int slotY = 0; slotY < 9; slotY++)
			{
				addSlotToContainer(new Slot(inventory, slotY + slotX * 9 + 9, 8 + slotY * 18, 84 + slotX * 18));
			}
		}

		for(slotX = 0; slotX < 9; ++slotX)
		{
			addSlotToContainer(new Slot(inventory, slotX, 8 + slotX * 18, 142));
		}

		tileEntity.open(inventory.player);
		tileEntity.openChest();
	}


	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
	{
		return tileEntity.isUseableByPlayer(entityplayer);
	}
}
