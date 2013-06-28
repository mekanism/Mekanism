package mekanism.api;

/**
 * Simple color enum for adding colors to in-game GUI strings of text.
 * @author AidanBrady
 *
 */
public enum EnumColor
{
	BLACK("\u00a70", "Black"),
	DARK_BLUE("\u00a71", "Dark Blue"),
	DARK_GREEN("\u00a72", "Dark Green"),
	DARK_AQUA("\u00a73", "Dark Aqua"),
	DARK_RED("\u00a74", "Dark Red"),
	PURPLE("\u00a75", "Purple"),
	ORANGE("\u00a76", "Orange"),
	GREY("\u00a77", "Grey"),
	DARK_GREY("\u00a78", "Dark Grey"),
	INDIGO("\u00a79", "Indigo"),
	BRIGHT_GREEN("\u00a7a", "Bright Green"),
	AQUA("\u00a7b", "Aqua"),
	RED("\u00a7c", "Red"),
	PINK("\u00a7d", "Pink"),
	YELLOW("\u00a7e", "Yellow"),
	WHITE("\u00a7f", "White");
	
	/** The color code that will be displayed */
	public final String code;
	
	/** A friendly name of the color. */
	public String friendlyName;
	
	private EnumColor(String s, String n)
	{
		code = s;
		friendlyName = n;
	}
	
	public String getName()
	{
		return code + friendlyName;
	}
	
	@Override
	public String toString()
	{
		return code;
	}
}
