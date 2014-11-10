package mekanism.common.item;

import java.util.List;

import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.inventory.InventoryBin;
import mekanism.common.tile.TileEntityBin;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Item class for handling multiple metal block IDs.
 * 0:0: Osmium Block
 * 0:1: Bronze Block
 * 0:2: Refined Obsidian
 * 0:3: Charcoal Block
 * 0:4: Refined Glowstone
 * 0:5: Steel Block
 * 0:6: Bin
 * 0:7: Teleporter Frame
 * 0:8: Steel Casing
 * 0:9: Dynamic Tank
 * 0:10: Dynamic Glass
 * 0:11: Dynamic Valve
 * 0:12: Copper Block
 * 0:13: Tin Block
 * 0:14: Salination Controller
 * 0:15: Salination Valve
 * 1:0: Salination Block
 * @author AidanBrady
 *
 */
public class ItemBlockBasic extends ItemBlock
{
	public Block metaBlock;

	public ItemBlockBasic(Block block)
	{
		super(block);
		metaBlock = block;
		setHasSubtypes(true);
	}

	@Override
	public int getItemStackLimit(ItemStack stack)
	{
		if(Block.getBlockFromItem(this) == Mekanism.BasicBlock)
		{
			if(stack.getItemDamage() == 6)
			{
				return 1;
			}
		}

		return 64;
	}

	@Override
	public int getMetadata(int i)
	{
		return i;
	}

	@Override
	public IIcon getIconFromDamage(int i)
	{
		return metaBlock.getIcon(2, i);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		if(Block.getBlockFromItem(this) == Mekanism.BasicBlock && itemstack.getItemDamage() == 6)
		{
			InventoryBin inv = new InventoryBin(itemstack);

			if(inv.getItemCount() > 0)
			{
				list.add(EnumColor.BRIGHT_GREEN + inv.getItemType().getDisplayName());
				list.add(EnumColor.INDIGO + "Item amount: " + EnumColor.GREY + inv.getItemCount());
			}
			else {
				list.add(EnumColor.DARK_RED + "Empty");
			}
		}
	}

	@Override
	public boolean hasContainerItem(ItemStack stack)
	{
		return stack.getItemDamage() == 6 && stack.stackTagCompound != null && stack.stackTagCompound.hasKey("newCount");
	}

	@Override
	public boolean doesContainerItemLeaveCraftingGrid(ItemStack stack)
	{
		if(Block.getBlockFromItem(this) == Mekanism.BasicBlock)
		{
			if(stack.getItemDamage() != 6)
			{
				return true;
			}
		}

		if(stack.stackTagCompound == null || !stack.stackTagCompound.hasKey("newCount"))
		{
			return true;
		}

		return false;
	}

	@Override
	public ItemStack getContainerItem(ItemStack stack)
	{
		if(Block.getBlockFromItem(this) == Mekanism.BasicBlock)
		{
			if(stack.getItemDamage() != 6 || stack.stackTagCompound == null || !stack.stackTagCompound.hasKey("newCount"))
			{
				return null;
			}
		}

		ItemStack ret = stack.copy();
		ret.stackTagCompound.setInteger("itemCount", stack.stackTagCompound.getInteger("newCount"));

		return ret;
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
	{
		boolean place = super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata);

		if(place)
		{
			if(Block.getBlockFromItem(this) == Mekanism.BasicBlock)
			{
				if(stack.getItemDamage() == 6 && stack.stackTagCompound != null)
				{
					TileEntityBin tileEntity = (TileEntityBin)world.getTileEntity(x, y, z);
					InventoryBin inv = new InventoryBin(stack);

					if(inv.getItemType() != null)
					{
						tileEntity.setItemType(inv.getItemType());
					}

					tileEntity.setItemCount(inv.getItemCount());
				}
			}
		}

		return place;
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack)
	{
		String name = "";

		if(Block.getBlockFromItem(this) == Mekanism.BasicBlock)
		{
			switch(itemstack.getItemDamage())
			{
				case 0:
					name = "OsmiumBlock";
					break;
				case 1:
					name = "BronzeBlock";
					break;
				case 2:
					name = "RefinedObsidian";
					break;
				case 3:
					name = "CharcoalBlock";
					break;
				case 4:
					name = "RefinedGlowstone";
					break;
				case 5:
					name = "SteelBlock";
					break;
				case 6:
					name = "Bin";
					break;
				case 7:
					name = "TeleporterFrame";
					break;
				case 8:
					name = "SteelCasing";
					break;
				case 9:
					name = "DynamicTank";
					break;
				case 10:
					name = "DynamicGlass";
					break;
				case 11:
					name = "DynamicValve";
					break;
				case 12:
					name = "CopperBlock";
					break;
				case 13:
					name = "TinBlock";
					break;
				case 14:
					name = "SalinationController";
					break;
				case 15:
					name = "SalinationValve";
					break;
				default:
					name = "Unknown";
					break;
			}
		}
		else if(Block.getBlockFromItem(this) == Mekanism.BasicBlock2)
		{
			switch(itemstack.getItemDamage())
			{
				case 0:
					name = "SalinationBlock";
					break;
			}
		}

		return getUnlocalizedName() + "." + name;
	}
}
