/**
 * 
 */
package mekanism.induction.common.multimeter;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

/**
 * @author Calclavia
 * 
 */
public class ContainerMultimeter extends Container
{
	private final int yDisplacement = 51;
	private TileEntityMultimeter tileEntity;

	public ContainerMultimeter(InventoryPlayer inventoryPlayer, TileEntityMultimeter tileEntity)
	{
		this.tileEntity = tileEntity;
		int i;

		for (i = 0; i < 3; ++i)
		{
			for (int j = 0; j < 9; ++j)
			{
				this.addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18 + yDisplacement));
			}
		}

		for (i = 0; i < 9; ++i)
		{
			this.addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 142 + yDisplacement));
		}

		this.tileEntity.playersUsing.add(inventoryPlayer.player);
	}

	@Override
	public void onContainerClosed(EntityPlayer entityPlayer)
	{
		this.tileEntity.playersUsing.remove(entityPlayer);
		super.onContainerClosed(entityPlayer);
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
	{
		return true;
	}

}
