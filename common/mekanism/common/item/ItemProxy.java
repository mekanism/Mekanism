package mekanism.common.item;

import mekanism.common.Mekanism;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ItemProxy extends Item
{
	public ItemProxy(int id)
	{
		super(id);
		setMaxDamage(1);
	}
	
	@Override
	public ItemStack getContainerItemStack(ItemStack stack)
	{
		return getSavedItem(stack) != null ? getSavedItem(stack) : getDead();
	}
	
	@Override
	public boolean doesContainerItemLeaveCraftingGrid(ItemStack stack)
	{
		return stack.stackTagCompound == null || !stack.stackTagCompound.getBoolean("hasStack");
	}
	
	@Override
	public boolean hasContainerItem()
	{
		return true; //TODO forge PR
	}
	
	public void setSavedItem(ItemStack stack, ItemStack save)
	{
		if(stack.stackTagCompound == null)
		{
			stack.setTagCompound(new NBTTagCompound());
		}
		
		if(save == null)
		{
			stack.stackTagCompound.setBoolean("hasStack", false);
			stack.stackTagCompound.removeTag("savedItem");
		}
		else {
			stack.stackTagCompound.setBoolean("hasStack", true);
			stack.stackTagCompound.setCompoundTag("savedItem", save.writeToNBT(new NBTTagCompound()));
		}
	}
	
	public ItemStack getSavedItem(ItemStack stack)
	{
		if(stack.stackTagCompound == null)
		{
			return null;
		}
		
		if(stack.stackTagCompound.getBoolean("hasStack"))
		{
			return ItemStack.loadItemStackFromNBT(stack.stackTagCompound.getCompoundTag("savedItem"));
		}
		
		return null;
	}
	
	public static ItemStack getDead()
	{
		ItemStack stack = new ItemStack(Mekanism.ItemProxy);
		stack.stackSize = 0;
		stack.setItemDamage(2);
		
		return stack;
	}

	@Override
	public void registerIcons(IconRegister register) {}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean flag)
	{
		if(entity instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer)entity;
			
			for(Object o : player.inventoryContainer.inventorySlots)
			{
				Slot s = (Slot)o;
				
				if(s.getStack() != null && s.getStack().getItem() == this)
				{
					player.inventory.decrStackSize(s.slotNumber, 64);
				}
			}
		}
	}
}
