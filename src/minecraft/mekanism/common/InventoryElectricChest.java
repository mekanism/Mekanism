package mekanism.common;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class InventoryElectricChest extends InventoryBasic
{
	public EntityPlayer entityPlayer;
	public int size;
	public boolean reading;
	
	public InventoryElectricChest(EntityPlayer player)
	{
		super("Electric Chest", false, 55);
		entityPlayer = player;
		
		read();
	}
	
	@Override
    public void onInventoryChanged()
    {
        super.onInventoryChanged();
        
        if(!reading)
        {
        	write();
        }
    }
	
	@Override
    public void openChest()
    {
        read();
        ((IElectricChest)getItemStack().getItem()).setOpen(getItemStack(), true);
    }

	@Override
    public void closeChest()
    {
        write();
        ((IElectricChest)getItemStack().getItem()).setOpen(getItemStack(), false);
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
        
        ((ISustainedInventory)getItemStack().getItem()).setInventory(tagList, getItemStack());
	}
	
	public void read()
	{
		reading = true;
		
        NBTTagList tagList = ((ISustainedInventory)getItemStack().getItem()).getInventory(getItemStack());

        for(int tagCount = 0; tagCount < tagList.tagCount(); tagCount++)
        {
            NBTTagCompound tagCompound = (NBTTagCompound)tagList.tagAt(tagCount);
            byte slotID = tagCompound.getByte("Slot");

            if(slotID >= 0 && slotID < getSizeInventory())
            {
                setInventorySlotContents(slotID, ItemStack.loadItemStackFromNBT(tagCompound));
            }
        }
        
        reading = false;
	}
	
	public ItemStack getItemStack()
	{
		return entityPlayer.getCurrentEquippedItem();
	}
}
