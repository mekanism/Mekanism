package mekanism.common.block.states;

import mekanism.common.MekanismBlocks;
import mekanism.common.block.BlockBasic;
import mekanism.common.block.BlockMachine;

import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Plane;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public class BlockStateBasic extends BlockStateFacing
{
	public static final PropertyBool activeProperty = PropertyBool.create("active");

	public BlockStateBasic(BlockBasic block, PropertyEnum typeProperty)
	{
		super(block, typeProperty);
	}

	public static enum BasicBlock
	{
		BASIC_BLOCK_1,
		BASIC_BLOCK_2;

		public BlockBasic implBlock;
		private PropertyEnum<BasicBlockType> predicatedProperty;

		public PropertyEnum<BasicBlockType> getProperty()
		{
			if(predicatedProperty == null)
			{
				predicatedProperty = PropertyEnum.create("type", BasicBlockType.class, new BasicBlockPredicate(this));
			}
			return predicatedProperty;
		}

		public Block getBlock()
		{
			switch(this)
			{
				case BASIC_BLOCK_1:
					return MekanismBlocks.BasicBlock;
				case BASIC_BLOCK_2:
					return MekanismBlocks.BasicBlock2;
				default:
					return null;
			}
		}
	}

	public static enum BasicBlockType implements IStringSerializable
	{
		OSMIUM_BLOCK(BasicBlock.BASIC_BLOCK_1, 0, Predicates.<EnumFacing>alwaysFalse(), false),
		BRONZE_BLOCK(BasicBlock.BASIC_BLOCK_1, 1, Predicates.<EnumFacing>alwaysFalse(), false),
		REFINED_OBSIDIAN(BasicBlock.BASIC_BLOCK_1, 2, Predicates.<EnumFacing>alwaysFalse(), false),
		COAL_BLOCK(BasicBlock.BASIC_BLOCK_1, 3, Predicates.<EnumFacing>alwaysFalse(), false),
		REFINED_GLOWSTONE(BasicBlock.BASIC_BLOCK_1, 4, Predicates.<EnumFacing>alwaysFalse(), false),
		STEEL_BLOCK(BasicBlock.BASIC_BLOCK_1, 5, Predicates.<EnumFacing>alwaysFalse(), false),
		BIN(BasicBlock.BASIC_BLOCK_1, 6, Plane.HORIZONTAL, false),
		TELEPORTER_FRAME(BasicBlock.BASIC_BLOCK_1, 7, Predicates.<EnumFacing>alwaysFalse(), false),
		STEEL_CASING(BasicBlock.BASIC_BLOCK_1, 8, Predicates.<EnumFacing>alwaysFalse(), false),
		DYNAMIC_TANK(BasicBlock.BASIC_BLOCK_1, 9, Predicates.<EnumFacing>alwaysFalse(), false),
		STRUCTURAL_GLASS(BasicBlock.BASIC_BLOCK_1, 10, Predicates.<EnumFacing>alwaysFalse(), false),
		DYNAMIC_VALVE(BasicBlock.BASIC_BLOCK_1, 11, Predicates.<EnumFacing>alwaysFalse(), false),
		COPPER_BLOCK(BasicBlock.BASIC_BLOCK_1, 12, Predicates.<EnumFacing>alwaysFalse(), false),
		TIN_BLOCK(BasicBlock.BASIC_BLOCK_1, 13, Predicates.<EnumFacing>alwaysFalse(), false),
		SOLAR_EVAPORATION_CONTROLLER(BasicBlock.BASIC_BLOCK_1, 14, Plane.HORIZONTAL, false),
		SOLAR_EVAPORATION_VALVE(BasicBlock.BASIC_BLOCK_1, 15, Predicates.<EnumFacing>alwaysFalse(), false),
		SOLAR_EVAPORATION_BLOCK(BasicBlock.BASIC_BLOCK_2, 0, Predicates.<EnumFacing>alwaysFalse(), false),
		INDUCTION_CASING(BasicBlock.BASIC_BLOCK_2, 1, Predicates.<EnumFacing>alwaysFalse(), false),
		INDUCTION_PORT(BasicBlock.BASIC_BLOCK_2, 2, Predicates.<EnumFacing>alwaysFalse(), false),
		INDUCTION_CELL(BasicBlock.BASIC_BLOCK_2, 3, Predicates.<EnumFacing>alwaysFalse(), false),
		INDUCTION_PROVIDER(BasicBlock.BASIC_BLOCK_2, 4, Predicates.<EnumFacing>alwaysFalse(), false);

		public BasicBlock blockType;
		public int meta;
		public Predicate<EnumFacing> facingPredicate;
		public boolean activable;

		private BasicBlockType(BasicBlock block, int metadata, Predicate<EnumFacing> facingAllowed, boolean activeState)
		{
			blockType = block;
			meta = metadata;
			facingPredicate = facingAllowed;
			activable = activeState;
		}

		public static  BasicBlockType getBlockType(BasicBlock blockID, int metadata)
		{
			BasicBlockType firstTry = values()[blockID.ordinal() << 4 | metadata];
			if(firstTry.blockType == blockID && firstTry.meta == metadata)
				return firstTry;
			for(BasicBlockType type : values())
			{
				if(type.blockType == blockID && type.meta == metadata)
					return type;
			}
			return null;
		}

		@Override
		public String getName()
		{
			return name().toLowerCase();
		}

		public ItemStack getStack(int amount)
		{
			return new ItemStack(blockType.implBlock, amount, meta);
		}

		public boolean canRotateTo(EnumFacing side)
		{
			return facingPredicate.apply(side);
		}

		public boolean hasRotations()
		{
			return !facingPredicate.equals(Predicates.alwaysFalse());
		}

		public boolean hasActiveTexture()
		{
			return activable;
		}
	}

	public static class BasicBlockPredicate implements Predicate<BasicBlockType>
	{
		public BasicBlock basicBlock;

		public BasicBlockPredicate(BasicBlock type)
		{
			basicBlock = type;
		}

		@Override
		public boolean apply(BasicBlockType input)
		{
			return input.blockType == basicBlock;
		}
	}

	public static class BasicBlockStateMapper extends StateMapperBase
	{
		@Override
		protected ModelResourceLocation getModelResourceLocation(IBlockState state)
		{
			BlockBasic block = (BlockBasic)state.getBlock();
			BasicBlockType type = state.getValue(block.getProperty());
			StringBuilder builder = new StringBuilder();
			if(type.hasRotations())
			{
				EnumFacing facing = state.getValue(facingProperty);
				if(type.canRotateTo(facing))
				{
					builder.append(facingProperty.getName());
					builder.append("=");
					builder.append(facing.getName());
				}
				else
				{
					return new ModelResourceLocation("builtin/missing", "missing");
				}
			}
			if(type.hasActiveTexture())
			{
				String active = state.getValue(activeProperty) ? "active" : "inactive";
				if(builder.length() > 0)
				{
					builder.append(",");
				}
				builder.append(active);
			}

			if(builder.length() == 0)
			{
				builder.append("normal");
			}

			ResourceLocation baseLocation = new ResourceLocation("mekanism", type.getName());
			return new ModelResourceLocation(baseLocation, builder.toString());
		}
	}

}
