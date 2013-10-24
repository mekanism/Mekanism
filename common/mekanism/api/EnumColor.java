package mekanism.api;

/**
 * Simple color enum for adding colors to in-game GUI strings of text.
 * @author AidanBrady
 *
 */
public enum EnumColor
{
	BLACK("\u00a70", "Black", new int[] {0, 0, 0}),
	DARK_BLUE("\u00a71", "Dark Blue", new int[] {0, 0, 170}),
	DARK_GREEN("\u00a72", "Dark Green", new int[] {0, 170, 0}),
	DARK_AQUA("\u00a73", "Dark Aqua", new int[] {0, 170, 170}),
	DARK_RED("\u00a74", "Dark Red", new int[] {170, 0, 0}),
	PURPLE("\u00a75", "Purple", new int[] {170, 0, 170}),
	ORANGE("\u00a76", "Orange", new int[] {255, 170, 0}),
	GREY("\u00a77", "Grey", new int[] {170, 170, 170}),
	DARK_GREY("\u00a78", "Dark Grey", new int[] {85, 85, 85}),
	INDIGO("\u00a79", "Indigo", new int[] {85, 85, 255}),
	BRIGHT_GREEN("\u00a7a", "Bright Green", new int[] {85, 255, 85}),
	AQUA("\u00a7b", "Aqua", new int[] {85, 255, 255}),
	RED("\u00a7c", "Red", new int[] {255, 85, 85}),
	PINK("\u00a7d", "Pink", new int[] {255, 85, 255}),
	YELLOW("\u00a7e", "Yellow", new int[] {255, 255, 85}),
	WHITE("\u00a7f", "White", new int[] {255, 255, 255});
	
	/** The color code that will be displayed */
	public final String code;
	
	public final int[] rgbCode;
	
	/** A friendly name of the color. */
	public String friendlyName;
	
	private EnumColor(String s, String n, int[] rgb)
	{
		code = s;
		friendlyName = n;
		rgbCode = rgb;
	}
	
	public String getName()
	{
		return code + friendlyName;
	}
	
	public float getColor(int index)
	{
		return (float)rgbCode[index]/255F;
	}
	
	@Override
	public String toString()
	{
		return code;
	}
}
