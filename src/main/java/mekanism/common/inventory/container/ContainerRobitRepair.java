package mekanism.common.inventory.container;

import mekanism.common.entity.EntityRobit;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerRepair;

public class ContainerRobitRepair extends ContainerRepair
{
	public EntityRobit robit;
	
	public ContainerRobitRepair(InventoryPlayer inventory, EntityRobit entity)
	{
		super(inventory, entity.worldObj, 0, 0, 0, inventory.player);
		
		robit = entity;
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
	{
		return !robit.isDead;
	}
}
