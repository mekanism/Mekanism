package calclavia.lib.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import calclavia.lib.IPlayerUsing;

public class ContainerBase extends Container
{
	protected int slotCount = 0;
	protected int xInventoryDisplacement = 8;
	protected int yInventoryDisplacement = 135;
	protected int yHotBarDisplacement = 193;
	private IInventory inventory;

	public ContainerBase(IInventory inventory)
	{
		this.inventory = inventory;
		this.slotCount = inventory.getSizeInventory();
	}

	@Override
	public void onContainerClosed(EntityPlayer entityplayer)
	{
		super.onContainerClosed(entityplayer);

		if (this.inventory instanceof IPlayerUsing)
		{
			((IPlayerUsing) this.inventory).getPlayersUsing().remove(entityplayer);
		}
	}

	public void addPlayerInventory(EntityPlayer player)
	{
		if (this.inventory instanceof IPlayerUsing)
		{
			((IPlayerUsing) this.inventory).getPlayersUsing().add(player);
		}

		for (int y = 0; y < 3; y++)
		{
			for (int x = 0; x < 9; x++)
			{
				this.addSlotToContainer(new Slot(player.inventory, x + y * 9 + 9, this.xInventoryDisplacement + x * 18, this.yInventoryDisplacement + y * 18));
			}

		}

		for (int x = 0; x < 9; x++)
		{
			this.addSlotToContainer(new Slot(player.inventory, x, this.xInventoryDisplacement + x * 18, this.yHotBarDisplacement));
		}
	}

	/**
	 * Called to transfer a stack from one inventory to the other eg. when shift clicking.
	 */
	@Override
	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int slotID)
	{
		ItemStack var2 = null;

		Slot var3 = (Slot) this.inventorySlots.get(slotID);

		if (var3 != null && var3.getHasStack())
		{
			ItemStack itemStack = var3.getStack();
			var2 = itemStack.copy();

			// A slot ID greater than the slot count means it is inside the TileEntity GUI.
			if (slotID >= this.slotCount)
			{
				// Player Inventory, Try to place into slot.
				boolean didTry = false;

				for (int i = 0; i < this.slotCount; i++)
				{
					if (this.getSlot(i).isItemValid(itemStack))
					{
						didTry = true;

						if (this.mergeItemStack(itemStack, i, i + 1, false))
						{
							break;
						}
					}
				}

				if (!didTry)
				{
					if (slotID < 27 + this.slotCount)
					{
						if (!this.mergeItemStack(itemStack, 27 + this.slotCount, 36 + this.slotCount, false))
						{
							return null;
						}
					}
					else if (slotID >= 27 + this.slotCount && slotID < 36 + this.slotCount && !this.mergeItemStack(itemStack, slotCount, 27 + slotCount, false))
					{
						return null;
					}
				}
			}
			else if (!this.mergeItemStack(itemStack, this.slotCount, 36 + this.slotCount, false))
			{
				return null;
			}

			if (itemStack.stackSize == 0)
			{
				var3.putStack((ItemStack) null);
			}
			else
			{
				var3.onSlotChanged();
			}

			if (itemStack.stackSize == var2.stackSize)
			{
				return null;
			}

			var3.onPickupFromSlot(par1EntityPlayer, itemStack);
		}

		return var2;
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
	{
		return this.inventory.isUseableByPlayer(entityplayer);
	}
}
