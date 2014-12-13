package mekanism.common.base;

import mekanism.api.EnumColor;

import net.minecraft.util.EnumFacing;

public interface IEjector
{
	public void onOutput();

	public EnumColor getOutputColor();

	public void setOutputColor(EnumColor color);

	public EnumColor getInputColor(EnumFacing side);

	public void setInputColor(EnumFacing side, EnumColor color);

	public boolean isEjecting();

	public void setEjecting(boolean eject);

	public boolean hasStrictInput();

	public void setStrictInput(boolean strict);
}
