package mekanism.common.multipart;

import mekanism.api.EnumColor;
import net.minecraftforge.common.property.IUnlistedProperty;

public class ColorProperty implements IUnlistedProperty<ColorProperty>
{
	public static ColorProperty INSTANCE = new ColorProperty();
	
	public EnumColor color;
	
	public ColorProperty() {}
	
	public ColorProperty(EnumColor c)
	{
		color = c;
	}
	
	@Override
	public String getName() 
	{
		return "color";
	}

	@Override
	public boolean isValid(ColorProperty value) 
	{
		return true;
	}

	@Override
	public Class getType() 
	{
		return getClass();
	}

	@Override
	public String valueToString(ColorProperty value) 
	{
		return color.getName();
	}
}