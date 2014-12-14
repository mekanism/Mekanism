package mekanism.common.item;

import net.minecraft.client.renderer.texture.TextureAtlasSpriteRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ItemProxy extends Item
{
	public ItemProxy()
	{
		super();
		setMaxDamage(1);
	}

	@Override
	public ItemStack getContainerItem(ItemStack stack)
	{
		return getSavedItem(stack);
	}

	@Override
	public boolean doesContainerItemLeaveCraftingGrid(ItemStack stack)
	{
		return stack.getTagCompound() == null || !stack.getTagCompound().getBoolean("hasStack");
	}

	@Override
	public boolean hasContainerItem(ItemStack itemStack)
	{
		return getSavedItem(itemStack) != null;
	}

	public void setSavedItem(ItemStack stack, ItemStack save)
	{
		if(stack.getTagCompound() == null)
		{
			stack.setTagCompound(new NBTTagCompound());
		}

		if(save == null)
		{
			stack.getTagCompound().setBoolean("hasStack", false);
			stack.getTagCompound().removeTag("savedItem");
		}
		else {
			stack.getTagCompound().setBoolean("hasStack", true);
			stack.getTagCompound().setTag("savedItem", save.writeToNBT(new NBTTagCompound()));
		}
	}

	public ItemStack getSavedItem(ItemStack stack)
	{
		if(stack.getTagCompound() == null)
		{
			return null;
		}

		if(stack.getTagCompound().getBoolean("hasStack"))
		{
			return ItemStack.loadItemStackFromNBT(stack.getTagCompound().getCompoundTag("savedItem"));
		}

		return null;
	}

	@Override
	public void registerIcons(TextureAtlasSpriteRegister register) {}

	@Override
	public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int par4, boolean par5)
	{
		if (par3Entity instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) par3Entity;
			for (int i = 0; i < player.inventory.mainInventory.length; i++)
			{
				if (player.inventory.mainInventory[i] != null && player.inventory.mainInventory[i].getItem() == this)
					player.inventory.mainInventory[i] = null;					
			}
		}
	}
}
