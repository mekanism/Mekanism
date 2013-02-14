package mekanism.api;

public class InfuseObject 
{
	public InfusionType type;
	public int stored;
	
	public InfuseObject(InfusionType infusion, int i)
	{
		type = infusion;
		stored = i;
	}
}
