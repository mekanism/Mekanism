package mekanism.common.block.states;

import mekanism.common.MekanismBlocks;
import mekanism.common.Tier;
import mekanism.common.block.BlockEnergyCube;

import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyEnum;

/**
 * Created by ben on 23/12/14.
 */
public class BlockStateEnergyCube extends BlockStateFacing
{
	public static final PropertyEnum typeProperty = PropertyEnum.create("tier", Tier.EnergyCubeTier.class);

	public BlockStateEnergyCube(BlockEnergyCube block)
	{
		super(block, typeProperty);
	}
}
