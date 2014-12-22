package mekanism.common.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class ContainerRobitRepair extends ContainerRepair
{
	public ContainerRobitRepair(InventoryPlayer inventory, World world)
	{
		super(inventory, world, new BlockPos(0, 0, 0), inventory.player);
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
	{
		return true;
	}
}
