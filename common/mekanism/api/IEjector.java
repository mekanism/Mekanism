package mekanism.api;

public interface IEjector 
{
	public void onOutput();
	
	public boolean isEjecting();
	
	public void setEjecting(boolean eject);
}
