package mekanism.common.item;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.EnumColor;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemCraftingFormula extends ItemMekanism
{
	public static ModelResourceLocation MODEL = new ModelResourceLocation("mekanism:CraftingFormula", "inventory");
	public static ModelResourceLocation INVALID_MODEL = new ModelResourceLocation("mekanism:CraftingFormulaInvalid", "inventory");
	public static ModelResourceLocation ENCODED_MODEL = new ModelResourceLocation("mekanism:CraftingFormulaEncoded", "inventory");

	public ItemCraftingFormula()
	{
		super();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		ItemStack[] inv = getInventory(itemstack);
		
		if(inv != null)
		{
			addIngredientDetails(inv, list);
		}
	}
	
	private void addIngredientDetails(ItemStack[] inv, List list)
	{
		List<ItemStack> stacks = new ArrayList<ItemStack>();
		
		for(ItemStack stack : inv)
		{
			if(stack != null)
			{
				boolean found = false;
				
				for(ItemStack iterStack : stacks)
				{
					if(InventoryUtils.canStack(stack, iterStack))
					{
						iterStack.stackSize += stack.stackSize;
						found = true;
					}
				}
				
				if(!found)
				{
					stacks.add(stack);
				}
			}
		}
		
		list.add(EnumColor.GREY + LangUtils.localize("tooltip.ingredients") + ":");
		
		for(ItemStack stack : stacks)
		{
			list.add(EnumColor.GREY + " - " + stack.getDisplayName() + " (" + stack.stackSize + ")");
		}
	}
	
	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		if(player.isSneaking() && !world.isRemote)
		{
			setInventory(stack, null);
			setInvalid(stack, false);
			
			((EntityPlayerMP)player).sendContainerToPlayer(player.openContainer);
		
			return stack;
		}
		
		return stack;
	}
	
	@Override
	public int getItemStackLimit(ItemStack stack)
	{
		return getInventory(stack) != null ? 1 : 64;
	}
	
	@Override
	public String getItemStackDisplayName(ItemStack stack)
	{
		if(getInventory(stack) == null)
		{
			return super.getItemStackDisplayName(stack);
		}
		
		return super.getItemStackDisplayName(stack) + " " + (isInvalid(stack) ? EnumColor.DARK_RED + "(" + LangUtils.localize("tooltip.invalid")
				: EnumColor.DARK_GREEN + "(" + LangUtils.localize("tooltip.encoded")) + ")";
	}
	
	public boolean isInvalid(ItemStack stack)
	{
		return ItemDataUtils.getBoolean(stack, "invalid");
	}
	
	public void setInvalid(ItemStack stack, boolean invalid)
	{
		ItemDataUtils.setBoolean(stack, "invalid", invalid);
	}
	
	public ItemStack[] getInventory(ItemStack stack)
	{
		if(!ItemDataUtils.hasData(stack, "Items"))
		{
			return null;
		}
		
		NBTTagList tagList = ItemDataUtils.getList(stack, "Items");
		ItemStack[] inventory = new ItemStack[9];

		for(int tagCount = 0; tagCount < tagList.tagCount(); tagCount++)
		{
			NBTTagCompound tagCompound = tagList.getCompoundTagAt(tagCount);
			byte slotID = tagCompound.getByte("Slot");

			if(slotID >= 0 && slotID < 9)
			{
				inventory[slotID] = ItemStack.loadItemStackFromNBT(tagCompound);
			}
		}
		
		return inventory;
	}
	
	public void setInventory(ItemStack stack, ItemStack[] inv)
	{
		if(inv == null)
		{
			ItemDataUtils.removeData(stack, "Items");
			return;
		}
		
		NBTTagList tagList = new NBTTagList();

		for(int slotCount = 0; slotCount < 9; slotCount++)
		{
			if(inv[slotCount] != null)
			{
				NBTTagCompound tagCompound = new NBTTagCompound();
				tagCompound.setByte("Slot", (byte)slotCount);
				inv[slotCount].writeToNBT(tagCompound);
				tagList.appendTag(tagCompound);
			}
		}

		ItemDataUtils.setList(stack, "Items", tagList);
	}
}
