package mekanism.api;

/**
 * The types of infuse currently available in Mekanism.
 * @author AidanBrady
 *
 */
public final class InfuseType 
{
	/** The name of this infusion */
	public String name;
	
	/** The location of this infuse's GUI texture */
	public String texture;
	
	/** The infuse's GUI texture X offset. */
	public int texX;
	
	/** The infuse's GUI texture Y offset. */
	public int texY;
	
	public InfuseType(String s, String s1, int x, int y)
	{
		name = s;
		texture = s1;
		texX = x;
		texY = y;
	}
}
