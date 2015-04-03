package mekanism.common.base;

import mekanism.api.EnumColor;
import net.minecraftforge.common.util.ForgeDirection;

public interface IEjector
{
	public void outputItems();

	public EnumColor getOutputColor();

	public void setOutputColor(EnumColor color);

	public EnumColor getInputColor(ForgeDirection side);

	public void setInputColor(ForgeDirection side, EnumColor color);

	public boolean hasStrictInput();

	public void setStrictInput(boolean strict);
}
