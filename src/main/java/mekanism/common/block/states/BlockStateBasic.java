package mekanism.common.block.states;

import mekanism.common.MekanismBlocks;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;

public class BlockStateBasic extends BlockStateFacing
{
	public static PropertyEnum typeProperty = PropertyEnum.create("type", BasicBlockType.class);

	public BlockStateBasic()
	{
		super(MekanismBlocks.BasicBlock, typeProperty);
	}

	public static enum BasicBlockType
	{
		OSMIUM_BLOCK,
		BRONZE_BLOCK,
		REFINED_OBSIDIAN,
		COAL_BLOCK,
		REFINED_GLOWSTONE,
		STEEL_BLOCK,
		BIN,
		TELEPORTER_FRAME,
		STEEL_CASING,
		DYNAMIC_TANK,
		DYNAMIC_GLASS,
		DYNAMIC_VALVE,
		COPPER_BLOCK,
		TIN_BLOCK,
		SALINATION_CONTROLLER,
		SALINATION_VALVE,
		SALINATION_BLOCK
	}
}
