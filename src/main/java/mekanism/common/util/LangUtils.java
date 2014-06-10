package mekanism.common.util;

public final class LangUtils 
{
	public static String transOnOff(boolean b)
	{
		return MekanismUtils.localize("gui." + (b ? "on" : "off"));
	}
	
	public static String transYesNo(boolean b)
	{
		return MekanismUtils.localize("tooltip." + (b ? "yes" : "no"));
	}
}
