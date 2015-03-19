package mekanism.client.render.block;

import net.minecraft.util.IIcon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Texture component class adapted from Chisel
 * Code licensed under GPLv2
 * @author AUTOMATIC_MAIDEN, asie, pokefenn, unpairedbracket
 */
public class TextureVirtual implements IIcon
{
	int ox, oy;
	float u0, u1, v0, v1;
	String name;
	IIcon icon;

	TextureVirtual(IIcon parent, int w, int h, int x, int y)
	{
		icon = parent;

		u0 = icon.getInterpolatedU(16.0 * (x) / w);
		u1 = icon.getInterpolatedU(16.0 * (x + 1) / w);
		v0 = icon.getInterpolatedV(16.0 * (y) / h);
		v1 = icon.getInterpolatedV(16.0 * (y + 1) / h);

		name = icon.getIconName() + "|" + x + "." + y;

		ox = icon.getIconWidth();
		oy = icon.getIconHeight();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getMinU()
	{
		return u0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getMaxU()
	{
		return u1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getInterpolatedU(double d0)
	{
		return (float) (u0 + (u1 - u0) * d0 / 16.0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getMinV()
	{
		return v0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getMaxV()
	{
		return v1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getInterpolatedV(double d0)
	{
		return (float) (v0 + (v1 - v0) * d0 / 16.0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getIconName()
	{
		return name;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getIconWidth()
	{
		return ox;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getIconHeight()
	{
		return oy;
	}
}
