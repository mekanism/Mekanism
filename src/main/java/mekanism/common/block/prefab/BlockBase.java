package mekanism.common.block.prefab;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.text.ILangEntry;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.attribute.AttributeCustomShape;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.attribute.Attributes.AttributeCustomResistance;
import mekanism.common.block.attribute.Attributes.AttributeNoMobSpawn;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.interfaces.ITypeBlock;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.content.blocktype.BlockType;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType;
import net.minecraft.entity.EntityType;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;

public class BlockBase<TYPE extends BlockType> extends BlockMekanism implements IHasDescription, ITypeBlock {

    protected final TYPE type;

    public BlockBase(TYPE type) {
        this(type, AbstractBlock.Properties.create(Material.IRON).hardnessAndResistance(3.5F, 16F).setRequiresTool());
    }

    public BlockBase(TYPE type, AbstractBlock.Properties properties) {
        super(hack(type, properties));
        this.type = type;
    }

    // ugly hack but required to have a reference to our block type before setting state info; assumes single-threaded startup
    private static BlockType cacheType;

    private static <TYPE extends BlockType> AbstractBlock.Properties hack(TYPE type, AbstractBlock.Properties props) {
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
    public float getExplosionResistance(BlockState state, IBlockReader world, BlockPos pos, Explosion explosion) {
        return type.has(AttributeCustomResistance.class) ? type.get(AttributeCustomResistance.class).getResistance()
                                                         : super.getExplosionResistance(state, world, pos, explosion);
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos, PlacementType placement, @Nullable EntityType<?> entityType) {
        return !type.has(AttributeNoMobSpawn.class) && super.canCreatureSpawn(state, world, pos, placement, entityType);
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos, @Nonnull ISelectionContext context) {
        if (type.has(AttributeCustomShape.class)) {
            AttributeStateFacing attr = type.get(AttributeStateFacing.class);
            int index = attr == null ? 0 : (attr.getDirection(state).ordinal() - (attr.getFacingProperty() == BlockStateProperties.FACING ? 0 : 2));
            return type.get(AttributeCustomShape.class).getBounds()[index];
        }
        return super.getShape(state, world, pos, context);
    }

    public static class BlockBaseModel<BLOCK extends BlockType> extends BlockBase<BLOCK> implements IStateFluidLoggable {

        public BlockBaseModel(BLOCK blockType) {
            super(blockType);
        }

        public BlockBaseModel(BLOCK blockType, AbstractBlock.Properties properties) {
            super(blockType, properties);
        }
    }
}
