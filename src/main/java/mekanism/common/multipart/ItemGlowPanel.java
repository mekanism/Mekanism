package mekanism.common.multipart;

import java.util.List;

import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.HollowMicroblock;
import codechicken.multipart.JItemMultiPart;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemGlowPanel extends JItemMultiPart
{
	public ItemGlowPanel()
	{
		super();
		setHasSubtypes(true);
		setCreativeTab(Mekanism.tabMekanism);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {}

	@Override
	public TMultiPart newPart(ItemStack item, EntityPlayer player, World world, BlockCoord pos, int side, Vector3 vHit)
	{
		EnumColor col = EnumColor.DYES[item.getItemDamage()];
		ForgeDirection orientation = getSideFromVector3(vHit.subtract(Vector3.center));
		
		if(pos != null && orientation != null)
		{
			BlockCoord pos1 = pos.copy().inset(orientation.getOpposite().ordinal());
			
			if(world.isSideSolid(pos1.x, pos1.y, pos1.z, orientation.getOpposite()))
			{
				return new PartGlowPanel(col, orientation);
			}
			
			if(world.getTileEntity(pos.x, pos.y, pos.z) instanceof TileMultipart && ((TileMultipart) world.getTileEntity(pos.x, pos.y, pos.z)).partMap(orientation.ordinal()) instanceof HollowMicroblock)
			{
				return new PartGlowPanel(col, orientation);
			}
		}

		return null;
	}

	public ForgeDirection getSideFromVector3(Vector3 vector)
	{
		if(Math.abs(vector.x) > Math.abs(vector.y) && Math.abs(vector.x) > Math.abs(vector.z))
		{
			if((vector.x < 0.5 && vector.x > 0) || vector.x == -0.5)
			{
				return ForgeDirection.EAST;
			}
			
			return ForgeDirection.WEST;
		}
		else if(Math.abs(vector.y) > Math.abs(vector.x) && Math.abs(vector.y) > Math.abs(vector.z))
		{
			if((vector.y < 0.5 && vector.y > 0) || vector.y == -0.5)
			{
				return ForgeDirection.UP;
			}
			
			return ForgeDirection.DOWN;
		}
		else if(Math.abs(vector.z) > Math.abs(vector.x) && Math.abs(vector.z) > Math.abs(vector.y))
		{
			if((vector.z < 0.5 && vector.z > 0) || vector.z == -0.5)
			{
				return ForgeDirection.SOUTH;
			}
			
			return ForgeDirection.NORTH;
		}
		
		return null;
	}

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
