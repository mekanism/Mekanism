package mekanism.common;

import java.util.List;

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

	public List<Integer> acceptableMetas;

	public CTMData(String textureName, List<Integer> connectableMeta)
	{
		texture = textureName;
		acceptableMetas = connectableMeta;
	}

	public void registerIcons(IIconRegister register)
	{
		icon = register.registerIcon("mekanism:" + texture);
		submap = new TextureSubmap(register.registerIcon("mekanism:" + texture + "-ctm"), 4, 4);
		submapSmall = new TextureSubmap(icon, 2, 2);
	}

	@SideOnly(Side.CLIENT)
	public boolean shouldRenderSide(IBlockAccess world, int x, int y, int z, int side, Block block)
	{
		Coord4D obj = new Coord4D(x, y, z);
		return !(obj.getBlock(world).equals(block) && acceptableMetas.contains(obj.getMetadata(world)));
	}

}
