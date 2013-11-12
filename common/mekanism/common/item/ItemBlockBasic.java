package mekanism.common.item;

import java.util.List;

import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.inventory.InventoryBin;
import mekanism.common.tileentity.TileEntityBin;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Item class for handling multiple metal block IDs.
 * 0: Osmium Block
 * 1: Bronze Block
 * 2: Refined Obsidian
 * 3: Coal Block
 * 4: Refined Glowstone
 * 5: Steel Block
 * 6: Bin
 * 7: Teleporter Frame
 * 8: Steel Casing
 * 9: Dynamic Tank
 * 10: Dynamic Glass
 * 11: Dynamic Valve
 * @author AidanBrady
 *
 */
public class ItemBlockBasic extends ItemBlock
{
	public Block metaBlock;
	
	public ItemBlockBasic(int id, Block block)
	{
		super(id);
		metaBlock = block;
		setHasSubtypes(true);
	}
	
	@Override
	public int getMetadata(int i)
	{
		return i;
	}
	
	@Override
	public Icon getIconFromDamage(int i)
	{
		return metaBlock.getIcon(2, i);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		if(itemstack.getItemDamage() == 6)
		{
			if(!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
			{
				list.add("Hold " + EnumColor.AQUA + "shift" + EnumColor.GREY + " for more details.");
			}
			else {
				InventoryBin inv = new InventoryBin(itemstack);
				
				list.add(EnumColor.INDIGO + "Item color: " + EnumColor.GREY + inv.getItemCount());
				
				if(inv.getItemCount() > 0)
				{
					list.add(EnumColor.BRIGHT_GREEN + "Item type: " + inv.getItemType().getDisplayName());
				}
			}
		}
	}
	
	@Override
	public boolean hasContainerItem()
	{
		return true; //TODO forge PR
	}
	
	@Override
	public boolean doesContainerItemLeaveCraftingGrid(ItemStack stack)
	{
		if(stack.getItemDamage() != 6)
		{
			return false;
		}
		
		if(stack.stackTagCompound == null || !stack.stackTagCompound.hasKey("newCount"))
		{
			return false;
		}
		
		return true;
	}
	
	@Override
	public ItemStack getContainerItemStack(ItemStack stack)
	{
		if(stack.getItemDamage() != 6 || stack.stackTagCompound == null || !stack.stackTagCompound.hasKey("newCount"))
		{
			return new ItemStack(Mekanism.ItemProxy);
		}
		
		ItemStack ret = stack.copy();
		ret.stackSize = stack.stackTagCompound.getInteger("newCount");
		return ret;
	}
	
	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
    {
    	boolean place = super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata);
    	
    	if(place)
    	{
    		if(stack.getItemDamage() == 6 && stack.stackTagCompound != null)
    		{
    			TileEntityBin tileEntity = (TileEntityBin)world.getBlockTileEntity(x, y, z);
    			InventoryBin inv = new InventoryBin(stack);
    			
    			tileEntity.itemCount = inv.getItemCount();
    			
    			if(inv.getItemCount() > 0)
    			{
    				tileEntity.setItemType(inv.getItemType());
    			}
    		}
    	}
    	
    	return place;
    }
	
	@Override
	public String getUnlocalizedName(ItemStack itemstack)
	{
		String name = "";
		
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
				name = "CoalBlock";
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
			default:
				name = "Unknown";
				break;
		}
		
		return getUnlocalizedName() + "." + name;
	}
}
