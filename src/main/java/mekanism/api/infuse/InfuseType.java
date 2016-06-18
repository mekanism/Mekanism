package mekanism.api.infuse;

import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;

/**
 * The types of infuse currently available in Mekanism.
 * @author AidanBrady
 *
 */
public final class InfuseType
{
	/** The name of this infusion */
	public String name;

	/** This infuse GUI's icon */
	public IIcon icon;
	
	/** The location of this infuse GUI's icon */
	public String textureLocation;

	/** The unlocalized name of this type. */
	public String unlocalizedName;

	public InfuseType(String s, String tex)
	{
		name = s;
		textureLocation = tex;
	}
	
	public void setIcon(IIcon i)
	{
		icon = i;
	}

	public InfuseType setUnlocalizedName(String name)
	{
		unlocalizedName = "infuse." + name;

		return this;
	}

	public String getLocalizedName()
	{
		return StatCollector.translateToLocal(unlocalizedName);
	}
}
