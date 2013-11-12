package mekanism.common.item;

import mekanism.common.Mekanism;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemProxy extends Item
{
	public ItemProxy(int id)
	{
		super(id);
	}
	
	@Override
	public ItemStack getContainerItemStack(ItemStack stack)
	{
		return getSavedItem(stack) != null ? getSavedItem(stack) : new ItemStack(Mekanism.ItemProxy);
	}
	
	@Override
	public boolean doesContainerItemLeaveCraftingGrid(ItemStack stack)
	{
		return stack.stackTagCompound != null && stack.stackTagCompound.getBoolean("hasStack");
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
	
	@Override
	public void registerIcons(IconRegister register) {}
}
