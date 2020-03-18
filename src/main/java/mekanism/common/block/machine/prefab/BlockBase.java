package mekanism.common.block.machine.prefab;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.base.ILangEntry;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.attribute.AttributeCustomShape;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.attribute.Attributes.AttributeCustomResistance;
import mekanism.common.block.attribute.Attributes.AttributeNoMobSpawn;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.interfaces.ITypeBlock;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.content.blocktype.BlockType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;

public class BlockBase<TYPE extends BlockType> extends BlockMekanism implements IHasDescription, ITypeBlock {

    protected TYPE type;

    public BlockBase(TYPE type) {
        this(type, Block.Properties.create(Material.IRON).hardnessAndResistance(3.5F, 16F));
    }

    public BlockBase(TYPE type, Block.Properties properties) {
        super(properties);
        this.type = type;
    }

    @Override
    public BlockType getType() {
        return type;
    }

    @Nonnull
    @Override
    public ILangEntry getDescription() {
        return type.getDescription();
    }

    @Override
    public float getExplosionResistance(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
        return type.has(AttributeCustomResistance.class) ? type.get(AttributeCustomResistance.class).getResistance() : blockResistance;
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos, PlacementType placement, @Nullable EntityType<?> entityType) {
        return type.has(AttributeNoMobSpawn.class) ? false : super.canCreatureSpawn(state, world, pos, placement, entityType);
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        if (type.has(AttributeCustomShape.class)) {
            AttributeStateFacing attr = type.get(AttributeStateFacing.class);
            return type.get(AttributeCustomShape.class).getBounds()[attr.getDirection(state).ordinal() - (attr.getFacingProperty() == BlockStateHelper.facingProperty ? 0 : 2)];
        }
        return super.getShape(state, world, pos, context);
    }

    public static class BlockBaseModel<BLOCK extends BlockType> extends BlockBase<BLOCK> implements IStateFluidLoggable {

        public BlockBaseModel(BLOCK blockType) {
            super(blockType);
        }

        public BlockBaseModel(BLOCK blockType, Block.Properties properties) {
            super(blockType, properties);
        }
    }
}
