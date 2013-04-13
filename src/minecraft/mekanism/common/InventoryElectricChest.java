package mekanism.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class InventoryElectricChest extends InventoryBasic
{
	public ItemStack itemStack;
	public int size;
	
	public InventoryElectricChest(ItemStack itemstack)
	{
		super("Electric Chest", false, 55);
		itemStack = itemstack;
		
		read();
	}
	
	@Override
    public void onInventoryChanged()
    {
        super.onInventoryChanged();
        write();
    }
	
	@Override
    public void openChest()
    {
        read();
    }

	@Override
    public void closeChest()
    {
        write();
    }
	
	public void write()
	{
        NBTTagList tagList = new NBTTagList();

        for(int slotCount = 0; slotCount < getSizeInventory(); slotCount++)
        {
            if(getStackInSlot(slotCount) != null)
            {
                NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.setByte("Slot", (byte)slotCount);
                getStackInSlot(slotCount).writeToNBT(tagCompound);
                tagList.appendTag(tagCompound);
            }
        }

        itemStack.stackTagCompound.setTag("Items", tagList);
	}
	
	public void read()
	{
        NBTTagList tagList = itemStack.stackTagCompound.getTagList("Items");

        for(int tagCount = 0; tagCount < tagList.tagCount(); tagCount++)
        {
            NBTTagCompound tagCompound = (NBTTagCompound)tagList.tagAt(tagCount);
            byte slotID = tagCompound.getByte("Slot");

            if(slotID >= 0 && slotID < getSizeInventory())
            {
                setInventorySlotContents(slotID, ItemStack.loadItemStackFromNBT(tagCompound));
            }
        }
	}
}
