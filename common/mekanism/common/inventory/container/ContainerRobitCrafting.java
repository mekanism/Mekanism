package mekanism.common.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.world.World;

public class ContainerRobitCrafting extends ContainerWorkbench
{
	public ContainerRobitCrafting(InventoryPlayer inventory, World world)
	{
		super(inventory, world, 0, 0, 0);
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
	{
		return true;
	}
}
