package mekanism.api;

import net.minecraft.util.StatCollector;

/**
 * Simple color enum for adding colors to in-game GUI strings of text.
 * @author AidanBrady
 *
 */
public enum EnumColor
{
	BLACK("\u00a70", "black", new int[] {0, 0, 0}, 0),
	DARK_BLUE("\u00a71", "darkBlue", new int[] {0, 0, 170}, 4),
	DARK_GREEN("\u00a72", "darkGreen", new int[] {0, 170, 0}, 2),
	DARK_AQUA("\u00a73", "darkAqua", new int[] {0, 170, 170}, 6),
	DARK_RED("\u00a74", "darkRed", new int[] {170, 0, 0}, 1),
	PURPLE("\u00a75", "purple", new int[] {170, 0, 170}, 5),
	ORANGE("\u00a76", "orange", new int[] {255, 170, 0}, 14),
	GREY("\u00a77", "grey", new int[] {170, 170, 170}, 7),
	DARK_GREY("\u00a78", "darkGrey", new int[] {85, 85, 85}, 8),
	INDIGO("\u00a79", "indigo", new int[] {85, 85, 255}, 12),
	BRIGHT_GREEN("\u00a7a", "brightGreen", new int[] {85, 255, 85}, 10),
	AQUA("\u00a7b", "aqua", new int[] {85, 255, 255}, -1),
	RED("\u00a7c", "red", new int[] {255, 85, 85}, 13),
	PINK("\u00a7d", "pink", new int[] {255, 85, 255}, 9),
	YELLOW("\u00a7e", "yellow", new int[] {255, 255, 85}, 11),
	WHITE("\u00a7f", "white", new int[] {255, 255, 255}, 15);
	
	public static EnumColor[] DYES = new EnumColor[] {BLACK, DARK_RED, DARK_GREEN, null, DARK_BLUE, PURPLE, DARK_AQUA, GREY, DARK_GREY, PINK, BRIGHT_GREEN, YELLOW, INDIGO, RED, ORANGE, WHITE};
	
	/** The color code that will be displayed */
	public final String code;
	
	public final int[] rgbCode;
	
	public final int mcMeta;
	
	/** A friendly name of the color. */
	public String unlocalizedName;
	
	private EnumColor(String s, String n, int[] rgb, int meta)
	{
		code = s;
		unlocalizedName = n;
		rgbCode = rgb;
		mcMeta = meta;
	}
	
	public String getLocalizedName()
	{
		return StatCollector.translateToLocal("color." + unlocalizedName);
	}
	
	public String getName()
	{
		return code + getLocalizedName();
	}
	
	public float getColor(int index)
	{
		return (float)rgbCode[index]/255F;
	}
	
	public int getMetaValue()
	{
		return mcMeta;
	}
	
	@Override
	public String toString()
	{
		return code;
	}
}
