package mekanism.common;

import mekanism.common.util.MekanismUtils;

public enum Upgrade
{
	SPEED("speed", 16),
	ENERGY("energy", 16);
	
	private String name;
	private int maxStack;
	
	private Upgrade(String s, int max)
	{
		name = s;
		maxStack = max;
	}
	
	public String getName()
	{
		return MekanismUtils.localize("upgrade." + name);
	}
	
	public String getDescription()
	{
		return MekanismUtils.localize("upgrade." + name + ".desc");
	}
	
	public int getMax()
	{
		return maxStack;
	}
}