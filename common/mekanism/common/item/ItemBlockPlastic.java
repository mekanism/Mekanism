package mekanism.common.item;

import java.util.List;

import mekanism.api.EnumColor;
import mekanism.common.tile.TileEntityPlasticBlock;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBlockPlastic extends ItemBlock
{
	public Block metaBlock;

	public ItemBlockPlastic(int id, Block block)
	{
		super(id);
		metaBlock = block;
		setHasSubtypes(true);
	}

	@Override
	public int getMetadata(int i)
	{
		return i >> 4;
	}

	@Override
	public Icon getIconFromDamage(int i)
	{
		return metaBlock.getIcon(2, i >> 4);
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack)
	{
		String name = "";

		switch(itemstack.getItemDamage() >> 4)
		{
			case 0:
				name = "PlasticBlock";
				break;
			case 1:
				name = "SlickPlasticBlock";
				break;
			case 2:
				name = "GlowPlasticBlock";
				break;
			case 3:
				name = "ReinforcedPlasticBlock";
				break;
			default:
				name = "Unknown";
				break;
		}

		return getUnlocalizedName() + "." + name;
	}

	@Override
	public String getItemDisplayName(ItemStack stack)
	{
		EnumColor colour = EnumColor.DYES[stack.getItemDamage()&15];
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
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
	{
		if(super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata))
		{
			TileEntity tile = world.getBlockTileEntity(x, y, z);
			if(tile instanceof TileEntityPlasticBlock)
			{
				((TileEntityPlasticBlock)tile).setColour(stack.getItemDamage() & 15);
			}

			return true;
		}
		return false;
	}
}
