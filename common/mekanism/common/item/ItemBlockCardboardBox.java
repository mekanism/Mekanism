package mekanism.common.item;

import java.util.List;

import mekanism.api.EnumColor;
import mekanism.api.MekanismAPI;
import mekanism.api.MekanismAPI.BlockInfo;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockCardboardBox.BlockData;
import mekanism.common.tile.TileEntityCardboardBox;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import powercrystals.minefactoryreloaded.api.IDeepStorageUnit;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBlockCardboardBox extends ItemBlock
{
	public Block metaBlock;
	
	public ItemBlockCardboardBox(int id, Block block)
	{
		super(id);
		setMaxStackSize(1);
		metaBlock = block;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		list.add(EnumColor.INDIGO + "Block data: " + (getBlockData(itemstack) != null ? "Yes" : "No"));
		
		if(getBlockData(itemstack) != null)
		{
			list.add("Block ID: " + getBlockData(itemstack).id);
			list.add("Metadata: " + getBlockData(itemstack).meta);
			
			if(getBlockData(itemstack).tileTag != null)
			{
				list.add("Tile: " + getBlockData(itemstack).tileTag.getString("id"));
			}
		}
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
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
		if(!player.isSneaking() && !world.isAirBlock(x, y, z) && stack.getItemDamage() == 0)
		{
			int id = world.getBlockId(x, y, z);
			int meta = world.getBlockMetadata(x, y, z);
			
	    	if(!world.isRemote && !MekanismAPI.cardboardBoxIgnore.contains(new BlockInfo(id, meta)))
	    	{
    			BlockData data = new BlockData();
    			data.id = id;
    			data.meta = meta;
    			
    			if(world.getBlockTileEntity(x, y, z) != null)
    			{
    				TileEntity tile = world.getBlockTileEntity(x, y, z);
    				NBTTagCompound tag = new NBTTagCompound();
    				
    				tile.writeToNBT(tag);
    				data.tileTag = tag;
    				
    				if(tile instanceof IInventory)
    				{
    					IInventory inv = (IInventory)tile;
    					
    					for(int i = 0; i < inv.getSizeInventory(); i++)
    					{
    						inv.setInventorySlotContents(i, null);
    					}
    				}
    				
    				if(tile instanceof IDeepStorageUnit)
    				{
    					((IDeepStorageUnit)tile).setStoredItemCount(0);
    				}
    			}
    			
    			if(!player.capabilities.isCreativeMode)
    			{
    				stack.stackSize--;
    			}
    			
    			world.setBlock(x, y, z, Mekanism.cardboardBoxID, 1, 3);
    			
    			TileEntityCardboardBox tileEntity = (TileEntityCardboardBox)world.getBlockTileEntity(x, y, z);
    			
    			if(tileEntity != null)
    			{
    				tileEntity.storedData = data;
    			}
    			
    			return true;
	    	}
		}
    	
    	return false;
    }
	
	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
    {
		if(world.isRemote)
		{
			return true;
		}
		
    	boolean place = super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata);
    	
    	if(place)
    	{
    		TileEntityCardboardBox tileEntity = (TileEntityCardboardBox)world.getBlockTileEntity(x, y, z);
			
			if(tileEntity != null)
			{
				tileEntity.storedData = getBlockData(stack);
			}
    	}
    	
    	return place;
    }

    public void setBlockData(ItemStack itemstack, BlockData data)
    {
		if(itemstack.stackTagCompound == null)
		{
			itemstack.setTagCompound(new NBTTagCompound());
		}
		
		itemstack.stackTagCompound.setCompoundTag("blockData", data.write(new NBTTagCompound()));
    }

    public BlockData getBlockData(ItemStack itemstack)
    {
		if(itemstack.stackTagCompound == null || !itemstack.stackTagCompound.hasKey("blockData"))
		{
			return null;
		}
		
		return BlockData.read(itemstack.stackTagCompound.getCompoundTag("blockData"));
    }
}
