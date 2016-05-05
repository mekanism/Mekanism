package mekanism.common.block.states;

import mekanism.common.Tier;
import mekanism.common.block.BlockGasTank;
import net.minecraft.block.properties.PropertyEnum;

/**
 * Created by ben on 23/12/14.
 */
public class BlockStateGasTank extends BlockStateFacing
{
	public static final PropertyEnum<Tier.GasTankTier> typeProperty = PropertyEnum.create("tier", Tier.GasTankTier.class);
	
	public BlockStateGasTank(BlockGasTank block)
	{
		super(block, typeProperty);
	}
}
