package mekanism.common.multipart;

import mekanism.common.block.states.BlockStateFacing;
import net.minecraft.block.properties.IProperty;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class GlowPanelBlockState extends ExtendedBlockState
{
	public GlowPanelBlockState(BlockGlowPanel block)
	{
		super(block, new IProperty[] {BlockStateFacing.facingProperty}, new IUnlistedProperty[] {ColorProperty.INSTANCE});
	}
}
