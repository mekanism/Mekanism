package mekanism.induction.common.inventory.container;

import mekanism.induction.common.BatteryManager;
import mekanism.induction.common.BatteryManager.SlotBattery;
import mekanism.induction.common.BatteryManager.SlotOut;
import mekanism.induction.common.tileentity.TileEntityBattery;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import calclavia.lib.gui.ContainerBase;

public class ContainerBattery extends ContainerBase
{
	private TileEntityBattery tileEntity;

	public ContainerBattery(InventoryPlayer inventory, TileEntityBattery unit)
	{
		super(unit);
		tileEntity = unit;
		addSlotToContainer(new SlotBattery(unit, 0, 8, 22));
		addSlotToContainer(new SlotOut(unit, 1, 8, 58));
		addSlotToContainer(new SlotBattery(unit, 2, 31, 22));
		addSlotToContainer(new SlotBattery(unit, 3, 31, 58));

		int slotX;

		for (slotX = 0; slotX < 3; ++slotX)
		{
			for (int slotY = 0; slotY < 9; ++slotY)
			{
				addSlotToContainer(new Slot(inventory, slotY + slotX * 9 + 9, 8 + slotY * 18, 125 + slotX * 18));
			}
		}

		for (slotX = 0; slotX < 9; ++slotX)
		{
			addSlotToContainer(new Slot(inventory, slotX, 8 + slotX * 18, 183));
		}

		tileEntity.openChest();
		tileEntity.playersUsing.add(inventory.player);
	}

	@Override
	public ItemStack slotClick(int slotID, int par2, int par3, EntityPlayer par4EntityPlayer)
	{
		ItemStack stack = super.slotClick(slotID, par2, par3, par4EntityPlayer);

		if (slotID == 1)
		{
			ItemStack itemstack = ((Slot) inventorySlots.get(slotID)).getStack();
			ItemStack itemstack1 = itemstack == null ? null : itemstack.copy();
			inventoryItemStacks.set(slotID, itemstack1);

			for (int j = 0; j < crafters.size(); ++j)
			{
				((ICrafting) crafters.get(j)).sendSlotContents(this, slotID, itemstack1);
			}
		}

		return stack;
	}

	@Override
	public void onContainerClosed(EntityPlayer entityplayer)
	{
		super.onContainerClosed(entityplayer);
		tileEntity.closeChest();
		tileEntity.playersUsing.remove(entityplayer);
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
	{
		return tileEntity.isUseableByPlayer(entityplayer);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int slotID)
	{
		if (slotID != 1)
		{
			return super.transferStackInSlot(par1EntityPlayer, slotID);
		}
		return null;
	}
}
