package mekanism.api;

import net.minecraftforge.common.ForgeDirection;

public interface IEjector 
{
	public void onOutput();
	
	public EnumColor getOutputColor();
	
	public void setOutputColor(EnumColor color);
	
	public EnumColor getInputColor(ForgeDirection side);
	
	public void setInputColor(ForgeDirection side, EnumColor color);
	
	public boolean isEjecting();
	
	public void setEjecting(boolean eject);
	
	public boolean hasStrictInput();
	
	public void setStrictInput(boolean strict);
}
