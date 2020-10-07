package mekanism.additions.common.block.plastic;

import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.common.block.interfaces.IColoredBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.common.ToolType;

public class BlockPlasticTransparentSlab extends SlabBlock implements IColoredBlock {

    private final EnumColor color;

    public BlockPlasticTransparentSlab(EnumColor color) {
        super(AbstractBlock.Properties.create(BlockPlastic.PLASTIC, color.getMapColor()).hardnessAndResistance(5F, 10F).notSolid().harvestTool(ToolType.PICKAXE));
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
    @Deprecated
    public boolean isSideInvisible(@Nonnull BlockState state, @Nonnull BlockState adjacentBlockState, @Nonnull Direction side) {
        return BlockPlasticTransparent.isSideInvisible(this, state, adjacentBlockState, side);
    }

    @Override
    public float[] getBeaconColorMultiplier(BlockState state, IWorldReader world, BlockPos pos, BlockPos beaconPos) {
        return getColor().getRgbCodeFloat();
    }
}