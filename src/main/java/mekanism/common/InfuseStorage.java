package mekanism.common;

import mekanism.api.infuse.InfuseType;

public class InfuseStorage
{
	public InfuseType type;

	public int amount;

	public InfuseStorage() {}

	public InfuseStorage(InfuseType infuseType, int infuseAmount)
	{
		type = infuseType;
		amount = infuseAmount;
	}

	public boolean contains(InfuseStorage storage)
	{
		return type == storage.type && amount >= storage.amount;
	}

	public void subtract(InfuseStorage storage)
	{
		if(contains(storage))
		{
			amount -= storage.amount;
		} 
		else if(type == storage.type)
		{
			amount = 0;
		}
	}
}
