package mekanism.common;

/**
 * Simple color enum for adding colors to in-game GUI strings of text.
 * @author AidanBrady
 *
 */
public enum EnumColor
{
	BLACK("\u00a70"),
	DARK_BLUE("\u00a71"),
	DARK_GREEN("\u00a72"),
	DARK_AQUA("\u00a73"),
	DARK_RED("\u00a74"),
	PURPLE("\u00a75"),
	ORANGE("\u00a76"),
	GREY("\u00a77"),
	DARK_GREY("\u00a78"),
	INDIGO("\u00a79"),
	BRIGHT_GREEN("\u00a7a"),
	AQUA("\u00a7b"),
	RED("\u00a7c"),
	PINK("\u00a7d"),
	YELLOW("\u00a7e"),
	WHITE("\u00a7f");
	
	/** The color code that will be displayed */
	public final String code;
	
	private EnumColor(String s)
	{
		code = s;
	}
	
	@Override
	public String toString()
	{
		return code;
	}
}
