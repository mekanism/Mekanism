package mekanism.common;

import mekanism.common.util.MekanismUtils;

public enum Upgrade
{
	SPEED("speed"),
	ENERGY("energy");
	
	private String name;
	
	private Upgrade(String s)
	{
		name = s;
	}
	
	public String getName()
	{
		return MekanismUtils.localize("upgrade." + name);
	}
	
	public String getDescription()
	{
		return MekanismUtils.localize("upgrade." + name + ".desc");
	}
}