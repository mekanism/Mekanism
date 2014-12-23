package mekanism.common.block.states;

import mekanism.common.block.BlockBasic;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Plane;
import net.minecraft.util.IStringSerializable;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public class BlockStateBasic extends BlockStateFacing
{
	public static final PropertyEnum typeProperty = PropertyEnum.create("type", BasicBlockType.class);

	public BlockStateBasic(BlockBasic block)
	{
		super(block, typeProperty);
	}

	public static enum BasicBlockType implements IStringSerializable
	{
		OSMIUM_BLOCK(Predicates.<EnumFacing>alwaysTrue()),
		BRONZE_BLOCK(Predicates.<EnumFacing>alwaysTrue()),
		REFINED_OBSIDIAN(Predicates.<EnumFacing>alwaysTrue()),
		COAL_BLOCK(Predicates.<EnumFacing>alwaysTrue()),
		REFINED_GLOWSTONE(Predicates.<EnumFacing>alwaysTrue()),
		STEEL_BLOCK(Predicates.<EnumFacing>alwaysTrue()),
		BIN(Plane.HORIZONTAL),
		TELEPORTER_FRAME(Predicates.<EnumFacing>alwaysTrue()),
		STEEL_CASING(Predicates.<EnumFacing>alwaysTrue()),
		DYNAMIC_TANK(Predicates.<EnumFacing>alwaysTrue()),
		DYNAMIC_GLASS(Predicates.<EnumFacing>alwaysTrue()),
		DYNAMIC_VALVE(Predicates.<EnumFacing>alwaysTrue()),
		COPPER_BLOCK(Predicates.<EnumFacing>alwaysTrue()),
		TIN_BLOCK(Predicates.<EnumFacing>alwaysTrue()),
		SALINATION_CONTROLLER(Plane.HORIZONTAL),
		SALINATION_VALVE(Predicates.<EnumFacing>alwaysTrue()),
		SALINATION_BLOCK(Predicates.<EnumFacing>alwaysTrue());

		Predicate<EnumFacing> facingPredicate;

		private BasicBlockType(Predicate<EnumFacing> facingAllowed)
		{
			facingPredicate = facingAllowed;
		}

		public boolean canRotateTo(EnumFacing side)
		{
			return facingPredicate.apply(side);
		}

		@Override
		public String getName()
		{
			return name().toLowerCase();
		}
	}
}
