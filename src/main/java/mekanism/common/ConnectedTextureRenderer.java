package mekanism.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mekanism.api.Coord4D;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ConnectedTextureRenderer
{
	public static final byte[][] sideEdges = {{2, 5, 3, 4}, {2, 5, 3, 4}, {1, 4, 0, 5}, {1, 5, 0, 4}, {1, 3, 0, 2}, {1, 2, 0, 3}};

	public Block block;
	public List<Integer> metadata;

	public String iconTitle;

	public Map<Integer, IIcon> glassMap = new HashMap<Integer, IIcon>();

	public ConnectedTextureRenderer(String title, Block b, List<Integer> meta)
	{
		iconTitle = title;
		block = b;
		metadata = meta;
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register)
	{
		glassMap.put(0, register.registerIcon("mekanism:" + iconTitle + "_13"));
		glassMap.put(1, register.registerIcon("mekanism:" + iconTitle + "_20"));
		glassMap.put(4, register.registerIcon("mekanism:" + iconTitle + "_19"));
		glassMap.put(5, register.registerIcon("mekanism:" + iconTitle + "_23"));
		glassMap.put(7, register.registerIcon("mekanism:" + iconTitle + "_1"));
		glassMap.put(16, register.registerIcon("mekanism:" + iconTitle + "_7"));
		glassMap.put(17, register.registerIcon("mekanism:" + iconTitle + "_35"));
		glassMap.put(20, register.registerIcon("mekanism:" + iconTitle + "_40"));
		glassMap.put(21, register.registerIcon("mekanism:" + iconTitle + "_21"));
		glassMap.put(23, register.registerIcon("mekanism:" + iconTitle + "_31"));
		glassMap.put(28, register.registerIcon("mekanism:" + iconTitle + "_14"));
		glassMap.put(29, register.registerIcon("mekanism:" + iconTitle + "_45"));
		glassMap.put(31, register.registerIcon("mekanism:" + iconTitle + "_2"));
		glassMap.put(64, register.registerIcon("mekanism:" + iconTitle + "_8"));
		glassMap.put(65, register.registerIcon("mekanism:" + iconTitle + "_41"));
		glassMap.put(68, register.registerIcon("mekanism:" + iconTitle + "_46"));
		glassMap.put(69, register.registerIcon("mekanism:" + iconTitle + "_22"));
		glassMap.put(71, register.registerIcon("mekanism:" + iconTitle + "_32"));
		glassMap.put(80, register.registerIcon("mekanism:" + iconTitle + "_11"));
		glassMap.put(81, register.registerIcon("mekanism:" + iconTitle + "_10"));
		glassMap.put(84, register.registerIcon("mekanism:" + iconTitle + "_9"));
		glassMap.put(85, register.registerIcon("mekanism:" + iconTitle + "_17"));
		glassMap.put(87, register.registerIcon("mekanism:" + iconTitle + "_5"));
		glassMap.put(92, register.registerIcon("mekanism:" + iconTitle + "_34"));
		glassMap.put(93, register.registerIcon("mekanism:" + iconTitle + "_18"));
		glassMap.put(95, register.registerIcon("mekanism:" + iconTitle + "_6"));
		glassMap.put(112, register.registerIcon("mekanism:" + iconTitle + "_25"));
		glassMap.put(113, register.registerIcon("mekanism:" + iconTitle + "_43"));
		glassMap.put(116, register.registerIcon("mekanism:" + iconTitle + "_42"));
		glassMap.put(117, register.registerIcon("mekanism:" + iconTitle + "_29"));
		glassMap.put(119, register.registerIcon("mekanism:" + iconTitle + "_37"));
		glassMap.put(124, register.registerIcon("mekanism:" + iconTitle + "_26"));
		glassMap.put(125, register.registerIcon("mekanism:" + iconTitle + "_30"));
		glassMap.put(127, register.registerIcon("mekanism:" + iconTitle + "_38"));
		glassMap.put(193, register.registerIcon("mekanism:" + iconTitle + "_12"));
		glassMap.put(197, register.registerIcon("mekanism:" + iconTitle + "_44"));
		glassMap.put(199, register.registerIcon("mekanism:" + iconTitle + "_0"));
		glassMap.put(209, register.registerIcon("mekanism:" + iconTitle + "_33"));
		glassMap.put(213, register.registerIcon("mekanism:" + iconTitle + "_16"));
		glassMap.put(215, register.registerIcon("mekanism:" + iconTitle + "_4"));
		glassMap.put(221, register.registerIcon("mekanism:" + iconTitle + "_15"));
		glassMap.put(223, register.registerIcon("mekanism:" + iconTitle + "_3"));
		glassMap.put(241, register.registerIcon("mekanism:" + iconTitle + "_24"));
		glassMap.put(245, register.registerIcon("mekanism:" + iconTitle + "_28"));
		glassMap.put(247, register.registerIcon("mekanism:" + iconTitle + "_36"));
		glassMap.put(253, register.registerIcon("mekanism:" + iconTitle + "_27"));
		glassMap.put(255, register.registerIcon("mekanism:" + iconTitle + "_39"));
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side)
	{
		int map = 0;

		for(int face = 0; face < 4; face++)
		{
			int side0 = sideEdges[side][((face + 3) % 4)];
			int side1 = sideEdges[side][face];

			if(!canConnect(world, new Coord4D(x, y, z), sideEdges[side][face], side))
			{
				map |= (7 << face * 2) % 256 | 7 >>> 8 - face * 2;
			}
			else if(!canConnect(world, new Coord4D(x, y, z).getFromSide(ForgeDirection.getOrientation(side0)), side1, side))
			{
				map |= 1 << face * 2;
			}
			else if(!canConnect(world, new Coord4D(x, y, z).getFromSide(ForgeDirection.getOrientation(side1)), side0, side))
			{
				map |= 1 << face * 2;
			}
		}

		return glassMap.get(map);
	}

	private boolean canConnect(IBlockAccess access, Coord4D obj, int side, int face)
	{
		Coord4D coord = obj.getFromSide(ForgeDirection.getOrientation(side));
		Coord4D coordAbove = obj.getFromSide(ForgeDirection.getOrientation(face));

		return (coord.getBlock(access) == block && metadata.contains(coord.getMetadata(access))) && (coordAbove.getBlock(access) != block || metadata.contains(coordAbove.getMetadata(access)));
	}

	@SideOnly(Side.CLIENT)
	public boolean shouldRenderSide(IBlockAccess world, int x, int y, int z, int side)
	{
		Coord4D obj = new Coord4D(x, y, z).getFromSide(ForgeDirection.getOrientation(side).getOpposite());
		return obj.getBlock(world) != block || metadata.contains(obj.getMetadata(world));
	}
}
