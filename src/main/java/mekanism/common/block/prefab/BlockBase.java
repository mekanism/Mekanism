package mekanism.common.block.prefab;

import java.util.function.UnaryOperator;
import javax.annotation.Nonnull;
import mekanism.api.text.ILangEntry;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.attribute.AttributeCustomShape;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.attribute.Attributes.AttributeCustomResistance;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.interfaces.ITypeBlock;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.content.blocktype.BlockType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockBase<TYPE extends BlockType> extends BlockMekanism implements IHasDescription, ITypeBlock {

    protected final TYPE type;

    public BlockBase(TYPE type, UnaryOperator<BlockBehaviour.Properties> propertyModifier) {
        this(type, propertyModifier.apply(BlockBehaviour.Properties.of(Material.METAL).requiresCorrectToolForDrops()));
    }

    public BlockBase(TYPE type, BlockBehaviour.Properties properties) {
        super(hack(type, properties));
        this.type = type;
    }

    // ugly hack but required to have a reference to our block type before setting state info; assumes single-threaded startup
    private static BlockType cacheType;

    private static <TYPE extends BlockType> BlockBehaviour.Properties hack(TYPE type, BlockBehaviour.Properties props) {
        cacheType = type;
        type.getAll().forEach(a -> a.adjustProperties(props));
        return props;
    }

    @Override
    public BlockType getType() {
        return type == null ? cacheType : type;
    }

    @Nonnull
    @Override
    public ILangEntry getDescription() {
        return type.getDescription();
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter world, BlockPos pos, Explosion explosion) {
        return type.has(AttributeCustomResistance.class) ? type.get(AttributeCustomResistance.class).getResistance()
                                                         : super.getExplosionResistance(state, world, pos, explosion);
    }

    @Override
    @Deprecated
    public boolean isPathfindable(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos, @Nonnull PathComputationType pathType) {
        //If we have a custom shape which means we are not a full block then mark that movement is not
        // allowed through this block it is not a full block. Otherwise, use the normal handling for if movement is allowed
        return !type.has(AttributeCustomShape.class) && super.isPathfindable(state, world, pos, pathType);
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
        if (type.has(AttributeCustomShape.class)) {
            AttributeStateFacing attr = type.get(AttributeStateFacing.class);
            int index = attr == null ? 0 : (attr.getDirection(state).ordinal() - (attr.getFacingProperty() == BlockStateProperties.FACING ? 0 : 2));
            return type.get(AttributeCustomShape.class).bounds()[index];
        }
        return super.getShape(state, world, pos, context);
    }

    public static class BlockBaseModel<BLOCK extends BlockType> extends BlockBase<BLOCK> implements IStateFluidLoggable {

        public BlockBaseModel(BLOCK blockType, UnaryOperator<BlockBehaviour.Properties> propertyModifier) {
            super(blockType, propertyModifier);
        }

        public BlockBaseModel(BLOCK blockType, BlockBehaviour.Properties properties) {
            super(blockType, properties);
        }
    }
}
