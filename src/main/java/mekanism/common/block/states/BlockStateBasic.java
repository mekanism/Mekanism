package mekanism.common.block.states;

import mekanism.common.block.BlockBasic;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.item.ItemStack;
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

	public static enum BasicBlock
	{
		BASIC_BLOCK_1,
		BASIC_BLOCK_2;

		public BlockBasic implBlock;

		public void setImplBlock(BlockBasic block)
		{
			implBlock = block;
		}
	}

	public static enum BasicBlockType implements IStringSerializable
	{
		OSMIUM_BLOCK(BasicBlock.BASIC_BLOCK_1, 0, Predicates.<EnumFacing>alwaysTrue()),
		BRONZE_BLOCK(BasicBlock.BASIC_BLOCK_1, 1, Predicates.<EnumFacing>alwaysTrue()),
		REFINED_OBSIDIAN(BasicBlock.BASIC_BLOCK_1, 2, Predicates.<EnumFacing>alwaysTrue()),
		COAL_BLOCK(BasicBlock.BASIC_BLOCK_1, 3, Predicates.<EnumFacing>alwaysTrue()),
		REFINED_GLOWSTONE(BasicBlock.BASIC_BLOCK_1, 4, Predicates.<EnumFacing>alwaysTrue()),
		STEEL_BLOCK(BasicBlock.BASIC_BLOCK_1, 5, Predicates.<EnumFacing>alwaysTrue()),
		BIN(BasicBlock.BASIC_BLOCK_1, 6, Plane.HORIZONTAL),
		TELEPORTER_FRAME(BasicBlock.BASIC_BLOCK_1, 7, Predicates.<EnumFacing>alwaysTrue()),
		STEEL_CASING(BasicBlock.BASIC_BLOCK_1, 8, Predicates.<EnumFacing>alwaysTrue()),
		DYNAMIC_TANK(BasicBlock.BASIC_BLOCK_1, 9, Predicates.<EnumFacing>alwaysTrue()),
		DYNAMIC_GLASS(BasicBlock.BASIC_BLOCK_1, 10, Predicates.<EnumFacing>alwaysTrue()),
		DYNAMIC_VALVE(BasicBlock.BASIC_BLOCK_1, 11, Predicates.<EnumFacing>alwaysTrue()),
		COPPER_BLOCK(BasicBlock.BASIC_BLOCK_1, 12, Predicates.<EnumFacing>alwaysTrue()),
		TIN_BLOCK(BasicBlock.BASIC_BLOCK_1, 13, Predicates.<EnumFacing>alwaysTrue()),
		SALINATION_CONTROLLER(BasicBlock.BASIC_BLOCK_1, 14, Plane.HORIZONTAL),
		SALINATION_VALVE(BasicBlock.BASIC_BLOCK_1, 15, Predicates.<EnumFacing>alwaysTrue()),
		SALINATION_BLOCK(BasicBlock.BASIC_BLOCK_2, 0, Predicates.<EnumFacing>alwaysTrue());

		public BasicBlock blockType;
		public int meta;
		public Predicate<EnumFacing> facingPredicate;

		private BasicBlockType(BasicBlock block, int metadata, Predicate<EnumFacing> facingAllowed)
		{
			blockType = block;
			meta = metadata;
			facingPredicate = facingAllowed;
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
	}
}
