package mekanism.common.multipart;

import java.util.List;

import mekanism.api.EnumColor;
import mekanism.common.Mekanism;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import net.minecraftforge.common.ForgeDirection;

import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.JItemMultiPart;
import codechicken.multipart.TMultiPart;

public class ItemGlowPanel extends JItemMultiPart
{
	public ItemGlowPanel(int id)
	{
		super(id);
		setHasSubtypes(true);
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	public TMultiPart newPart(ItemStack item, EntityPlayer player, World world, BlockCoord pos, int side, Vector3 vHit)
	{
		EnumColor col = EnumColor.DYES[item.getItemDamage()];
		ForgeDirection orientation = getSideFromVector3(vHit.subtract(Vector3.center));
		return new PartGlowPanel(col, orientation);
	}

	public ForgeDirection getSideFromVector3(Vector3 vector)
	{
		if(Math.abs(vector.x) > Math.abs(vector.y) && Math.abs(vector.x) > Math.abs(vector.z))
		{
			if((vector.x < 0.5 &&vector.x > 0) || vector.x == -0.5)
			{
				return ForgeDirection.EAST;
			}
			return ForgeDirection.WEST;
		}
		else if(Math.abs(vector.y) > Math.abs(vector.x) && Math.abs(vector.y) > Math.abs(vector.z))
		{
			if((vector.y < 0.5 &&vector.y > 0) || vector.y == -0.5)
			{
				return ForgeDirection.UP;
			}
			return ForgeDirection.DOWN;
		}
		else if(Math.abs(vector.z) > Math.abs(vector.x) && Math.abs(vector.z) > Math.abs(vector.y))
		{
			if((vector.z < 0.5 &&vector.z > 0) || vector.z == -0.5)
			{
				return ForgeDirection.SOUTH;
			}
			return ForgeDirection.NORTH;
		}
		return null;
	}

	@Override
	public void getSubItems(int itemID, CreativeTabs tab, List listToAddTo)
	{
		for(EnumColor color : EnumColor.DYES)
		{
			listToAddTo.add(new ItemStack(itemID, 1, color.getMetaValue()));
		}
	}

	@Override
	public String getItemDisplayName(ItemStack stack)
	{
		EnumColor colour = EnumColor.DYES[stack.getItemDamage()];
		String colourName;
		if(colour == EnumColor.BLACK)
		{
			colourName = EnumColor.DARK_GREY + colour.getDyeName();
		}
		else {
			colourName = colour.getDyedName();
		}

		return colourName + " " + super.getItemDisplayName(stack);
	}

	@Override
	public boolean shouldRotateAroundWhenRendering()
	{
		return true;
	}
}
