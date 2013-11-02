package mekanism.api;

public interface IEjector 
{
	public void onOutput();
	
	public EnumColor getColor();
	
	public void setColor(EnumColor color);
	
	public boolean isEjecting();
	
	public void setEjecting(boolean eject);
}
