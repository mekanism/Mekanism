package mekanism.common.security;

import mekanism.api.EnumColor;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.util.LangUtils;

public interface ISecurity 
{
	public TileComponentSecurity getSecurity();
	
	public enum SecurityMode
	{
		PUBLIC(EnumColor.BRIGHT_GREEN + "security.public"),
		PRIVATE(EnumColor.RED + "security.private"),
		TRUSTED(EnumColor.ORANGE + "security.trusted");
		
		private String display;
		private EnumColor color;

		public String getDisplay()
		{
			return color + LangUtils.localize(display);
		}

		private SecurityMode(String s)
		{
			display = s;
		}
	}
}
