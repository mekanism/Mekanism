package mekanism.common.item;

import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.Pos3D;
import mekanism.common.EntityBalloon;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBalloon extends ItemMekanism
{
	public ItemBalloon(int id)
	{
		super(id);
		setHasSubtypes(true);
	}
	
	public EnumColor getColor(ItemStack stack)
	{
		return EnumColor.DYES[stack.getItemDamage()];
	}
	
	@Override
	public void getSubItems(int id, CreativeTabs tabs, List list)
	{
		for(int i = 0; i < EnumColor.DYES.length; i++)
		{
			EnumColor color = EnumColor.DYES[i];
			
			if(color != null)
			{
				ItemStack stack = new ItemStack(this);
				stack.setItemDamage(i);
				list.add(stack);
			}
		}
	}
	
	@Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
	{
		if(!world.isRemote)
		{
			Pos3D pos = new Pos3D();
			pos.zPos += 0.3;
			pos.xPos -= 0.4;
			pos.rotateYaw(entityplayer.renderYawOffset);
			pos.translate(new Pos3D(entityplayer));
			
			world.spawnEntityInWorld(new EntityBalloon(world, pos.xPos-0.5, pos.yPos-0.25, pos.zPos-0.5, getColor(itemstack)));
		}
		
		itemstack.stackSize--;
		
		return itemstack;
	}
	
	@Override
	public String getItemDisplayName(ItemStack stack)
	{
		String color = getColor(stack).getName();
		
		if(getColor(stack) == EnumColor.BLACK)
		{
			color = EnumColor.DARK_GREY + getColor(stack).getLocalizedName();
		}
		
		return color + " " + MekanismUtils.localize("tooltip.balloon");
	}
	
	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if(player.isSneaking())
		{
			Coord4D obj = new Coord4D(x, y, z, world.provider.dimensionId);
			
			if(Block.blocksList[obj.getBlockId(world)].isBlockReplaceable(world, x, y, z))
			{
				obj.yCoord--;
			}
			
			if(canReplace(world, obj.xCoord, obj.yCoord+1, obj.zCoord) && canReplace(world, obj.xCoord, obj.yCoord+2, obj.zCoord))
			{
				world.setBlockToAir(obj.xCoord, obj.yCoord+1, obj.zCoord);
				world.setBlockToAir(obj.xCoord, obj.yCoord+2, obj.zCoord);
				
				if(!world.isRemote)
				{
					world.spawnEntityInWorld(new EntityBalloon(world, obj, getColor(stack)));
				}
				
				stack.stackSize--;
			}
			
			return true;
		}
		
		return false;
	}
	
	private boolean canReplace(World world, int x, int y, int z)
	{
		return world.isAirBlock(x, y, z) || Block.blocksList[world.getBlockId(x, y, z)].isBlockReplaceable(world, x, y, z);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register) {}
}
