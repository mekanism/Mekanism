package mekanism.common.inventory.container;

import mekanism.common.EntityRobit;
import mekanism.common.inventory.slot.SlotEnergy;
import mekanism.common.inventory.slot.SlotEnergy.SlotDischarge;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerRobitMain extends Container
{
    private EntityRobit robit;

    public ContainerRobitMain(InventoryPlayer inventory, EntityRobit entity)
    {
        robit = entity;
        addSlotToContainer(new SlotDischarge(entity, 27, 153, 17));
        
        robit.openChest();
        
        int slotX;

        for(slotX = 0; slotX < 3; ++slotX)
        {
            for(int slotY = 0; slotY < 9; ++slotY)
            {
                addSlotToContainer(new Slot(inventory, slotY + slotX * 9 + 9, 8 + slotY * 18, 84 + slotX * 18));
            }
        }

        for(slotX = 0; slotX < 9; ++slotX)
        {
            addSlotToContainer(new Slot(inventory, slotX, 8 + slotX * 18, 142));
        }
    }
    
    @Override
    public void onContainerClosed(EntityPlayer entityplayer)
    {
		super.onContainerClosed(entityplayer);
		robit.closeChest();
    }

	@Override
    public boolean canInteractWith(EntityPlayer entityplayer)
	{
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotID)
    {
    	return null;
    }
}
