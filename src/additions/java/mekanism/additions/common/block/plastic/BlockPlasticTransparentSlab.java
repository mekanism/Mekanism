package mekanism.additions.common.block.plastic;

import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.common.block.interfaces.IColoredBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ToolType;

public class BlockPlasticTransparentSlab extends SlabBlock implements IColoredBlock {

    private final EnumColor color;

    public BlockPlasticTransparentSlab(EnumColor color) {
        super(Block.Properties.create(BlockPlastic.PLASTIC, color.getMapColor()).hardnessAndResistance(5F, 10F).notSolid().harvestTool(ToolType.PICKAXE));
        this.color = color;
    }

    @Override
    public EnumColor getColor() {
        return color;
    }

    @Override
    @Deprecated
    public float getAmbientOcclusionLightValue(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos) {
        return 0.8F;
    }

    @Override
    @Deprecated
    public boolean isTransparent(@Nonnull BlockState state) {
        return true;
    }

    @Override
    public boolean propagatesSkylightDown(@Nonnull BlockState state, @Nonnull IBlockReader reader, @Nonnull BlockPos pos) {
        return true;
    }

    @Override
    public boolean canCreatureSpawn(BlockState state, IBlockReader world, BlockPos pos, PlacementType type, EntityType<?> entityType) {
        return false;
    }

    @Override
    public boolean isSideInvisible(@Nonnull BlockState state, @Nonnull BlockState adjacentBlockState, @Nonnull Direction side) {
        final Block adjacentBlock = adjacentBlockState.getBlock();
        if (adjacentBlock instanceof BlockPlasticTransparent || adjacentBlock instanceof BlockPlasticTransparentSlab
            || adjacentBlock instanceof BlockPlasticTransparentStairs) {
            IColoredBlock plastic = ((IColoredBlock) adjacentBlock);
            if (plastic.getColor() == getColor()) {
                try {
                    VoxelShape shape = state.getShape(null, null);
                    VoxelShape adjacentShape = adjacentBlockState.getShape(null, null);

                    VoxelShape faceShape = shape.project(side);
                    VoxelShape adjacentFaceShape = adjacentShape.project(side.getOpposite());
                    return !VoxelShapes.compare(faceShape, adjacentFaceShape, IBooleanFunction.ONLY_FIRST);
                } catch (Exception ignored) {
                    //Something might have errored due to the null world and position
                }
            }
        }
        return false;
    }
}