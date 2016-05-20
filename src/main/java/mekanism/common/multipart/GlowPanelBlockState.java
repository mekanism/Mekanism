package mekanism.common.multipart;

import mcmultipart.MCMultiPartMod;
import mekanism.api.EnumColor;
import mekanism.common.block.states.BlockStateFacing;
import net.minecraft.block.properties.IProperty;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class GlowPanelBlockState extends ExtendedBlockState
{
	public static GlowPanelColorProperty colorState = new GlowPanelColorProperty();
	
	public GlowPanelBlockState()
	{
		super(MCMultiPartMod.multipart, new IProperty[] {BlockStateFacing.facingProperty}, new IUnlistedProperty[] {colorState});
	}
	
	public static class GlowPanelColorProperty implements IUnlistedProperty<GlowPanelColorProperty>
	{
		public EnumColor color;
		
		public GlowPanelColorProperty() {}
		
		public GlowPanelColorProperty(EnumColor c)
		{
			color = c;
		}
		
		@Override
		public String getName() 
		{
			return "color";
		}

		@Override
		public boolean isValid(GlowPanelColorProperty value) 
		{
			return true;
		}

		@Override
		public Class getType() 
		{
			return getClass();
		}

		@Override
		public String valueToString(GlowPanelColorProperty value) 
		{
			return color.getName();
		}
	}
}
