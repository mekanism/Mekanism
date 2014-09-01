package mekanism.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import mekanism.api.Coord4D;
import mekanism.client.render.block.TextureSubmap;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class CTMData
{
	public IIcon icon;

	public TextureSubmap submap;

	public TextureSubmap submapSmall;

	public String texture;

	public HashMap<Block, List<Integer>> acceptableBlockMetas = new HashMap<Block, List<Integer>>();

	public CTMData(String textureName, Block block, List<Integer> connectableMeta)
	{
		texture = textureName;
		acceptableBlockMetas.put(block, connectableMeta);
	}

	public CTMData registerIcons(IIconRegister register)
	{
		icon = register.registerIcon("mekanism:" + texture);
		submap = new TextureSubmap(register.registerIcon("mekanism:" + texture + "-ctm"), 4, 4);
		submapSmall = new TextureSubmap(icon, 2, 2);

		return this;
	}

	public CTMData addOtherBlockConnectivities(Block block, List<Integer> connectableMeta)
	{
		acceptableBlockMetas.put(block, connectableMeta);
		return this;
	}

	@SideOnly(Side.CLIENT)
	public boolean shouldRenderSide(IBlockAccess world, int x, int y, int z, int side)
	{
		Coord4D obj = new Coord4D(x, y, z);
		Block coordBlock = obj.getBlock(world);
		int coordMeta = obj.getMetadata(world);
		boolean valid = false;

		for(Entry<Block, List<Integer>> entry : acceptableBlockMetas.entrySet())
		{
			valid |= entry.getKey().equals(coordBlock) && entry.getValue().contains(coordMeta);
		}
		return !valid;
	}

}
