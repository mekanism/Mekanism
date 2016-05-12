package mekanism.common.multipart;

import java.util.List;

/*
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.HollowMicroblock;
import codechicken.multipart.JItemMultiPart;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
*/
import mcmultipart.item.ItemMultiPart;
import mcmultipart.multipart.IMultipart;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.util.LangUtils;
//import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class ItemGlowPanel extends ItemMultiPart
{
	public ItemGlowPanel()
	{
		super();
		setHasSubtypes(true);
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	public IMultipart createPart(World world, BlockPos pos, EnumFacing orientation, Vec3 vHit, ItemStack item, EntityPlayer player)
	{
		EnumColor col = EnumColor.DYES[item.getItemDamage()];

		if(pos != null && orientation != null)
		{
			BlockPos pos1 = pos.offset(orientation);
			
			if(world.isSideSolid(pos1, orientation.getOpposite()))
			{
				return new PartGlowPanel(col, orientation);
			}
			
/*
			if(world.getTileEntity(pos.x, pos.y, pos.z) instanceof IMultipartContainer && ((TileMultipart) world.getTileEntity(pos.x, pos.y, pos.z)).partMap(orientation.ordinal()) instanceof HollowMicroblock)
			{
				return new PartGlowPanel(col, orientation);
			}
*/
		}

		return null;
	}

/*
	public EnumFacing getSideFromVector3(Vec3 vector)
	{
		if(Math.abs(vector.xCoord) > Math.abs(vector.yCoord) && Math.abs(vector.xCoord) > Math.abs(vector.zCoord))
		{
			if((vector.xCoord < 0.5 && vector.xCoord > 0) || vector.xCoord == -0.5)
			{
				return EnumFacing.EAST;
			}
			
			return EnumFacing.WEST;
		}
		else if(Math.abs(vector.y) > Math.abs(vector.x) && Math.abs(vector.y) > Math.abs(vector.z))
		{
			if((vector.y < 0.5 && vector.y > 0) || vector.y == -0.5)
			{
				return EnumFacing.UP;
			}
			
			return EnumFacing.DOWN;
		}
		else if(Math.abs(vector.z) > Math.abs(vector.x) && Math.abs(vector.z) > Math.abs(vector.y))
		{
			if((vector.z < 0.5 && vector.z > 0) || vector.z == -0.5)
			{
				return EnumFacing.SOUTH;
			}
			
			return EnumFacing.NORTH;
		}
		
		return null;
	}
*/

	@Override
	public void getSubItems(Item item, CreativeTabs tab, List listToAddTo)
	{
		for(EnumColor color : EnumColor.DYES)
		{
			listToAddTo.add(new ItemStack(item, 1, color.getMetaValue()));
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack)
	{
		EnumColor colour = EnumColor.DYES[stack.getItemDamage()];
		String colourName;

        if(StatCollector.canTranslate(getUnlocalizedName(stack) + "." + colour.dyeName))
        {
            return LangUtils.localize(getUnlocalizedName(stack) + "." + colour.dyeName);
        }
		
		if(colour == EnumColor.BLACK)
		{
			colourName = EnumColor.DARK_GREY + colour.getDyeName();
		}
		else {
			colourName = colour.getDyedName();
		}

		return colourName + " " + super.getItemStackDisplayName(stack);
	}

	@Override
	public boolean shouldRotateAroundWhenRendering()
	{
		return true;
	}
}
