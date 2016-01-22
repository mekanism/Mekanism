package mekanism.common.base;

import mekanism.api.EnumColor;

import net.minecraft.util.EnumFacing;

public interface IEjector
{
	public void outputItems();

	public EnumColor getOutputColor();

	public void setOutputColor(EnumColor color);

	public EnumColor getInputColor(EnumFacing side);

	public void setInputColor(EnumFacing side, EnumColor color);

	public boolean hasStrictInput();

	public void setStrictInput(boolean strict);
}
