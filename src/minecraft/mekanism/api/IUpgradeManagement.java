package mekanism.api;

public interface IUpgradeManagement 
{
	public int getEnergyMultiplier(Object... data);
	
	public void setEnergyMultiplier(int multiplier, Object... data);
	
	public int getSpeedMultiplier(Object... data);
	
	public void setSpeedMultiplier(int multiplier, Object... data);
}
