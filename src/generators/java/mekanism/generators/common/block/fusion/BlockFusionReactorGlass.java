package mekanism.generators.common.block.fusion;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.text.ILangEntry;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.registries.GeneratorsTileEntityTypes;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorGlass;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ILightReader;
import net.minecraft.world.World;

public class BlockFusionReactorGlass extends BlockMekanism implements IHasTileEntity<TileEntityFusionReactorGlass>, IHasDescription {

    public BlockFusionReactorGlass() {
        super(Block.Properties.create(Material.GLASS).hardnessAndResistance(3.5F, 8F).notSolid());
    }

    @Override
    @Deprecated
    public void neighborChanged(@Nonnull BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull Block neighborBlock, @Nonnull BlockPos neighborPos,
          boolean isMoving) {
        if (!world.isRemote) {
            TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
            if (tile != null) {
                tile.onNeighborChange(neighborBlock, neighborPos);
            }
        }
    }

    @Override
    public boolean shouldDisplayFluidOverlay(BlockState state, ILightReader world, BlockPos pos, IFluidState fluidState) {
        return true;
    }

    @Nonnull
    @Override
    @Deprecated
    public ActionResultType onBlockActivated(@Nonnull BlockState state, World world, @Nonnull BlockPos pos, @Nonnull PlayerEntity player, @Nonnull Hand hand,
          @Nonnull BlockRayTraceResult hit) {
        if (world.isRemote) {
            return ActionResultType.SUCCESS;
        }
        TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
        if (tile == null) {
            return ActionResultType.PASS;
        }
        if (tile.tryWrench(state, player, hand, hit) != WrenchResult.PASS) {
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    @Override
    @Deprecated
    public boolean isSideInvisible(@Nonnull BlockState state, BlockState adjacentBlockState, @Nonnull Direction side) {
        Block blockOffset = adjacentBlockState.getBlock();
        if (blockOffset instanceof BlockFusionReactorGlass || blockOffset instanceof BlockLaserFocusMatrix) {
            return true;
        }
        return super.isSideInvisible(state, adjacentBlockState, side);
    }

    @Override
    public TileEntityType<TileEntityFusionReactorGlass> getTileType() {
        return GeneratorsTileEntityTypes.FUSION_REACTOR_GLASS.getTileEntityType();
    }

    @Nonnull
    @Override
    public ILangEntry getDescription() {
        return GeneratorsLang.DESCRIPTION_FUSION_REACTOR_GLASS;
    }

    @Override
    @Deprecated
    public float getAmbientOcclusionLightValue(@Nonnull BlockState state, @Nonnull IBlockReader worldIn, @Nonnull BlockPos pos) {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(@Nonnull BlockState state, @Nonnull IBlockReader reader, @Nonnull BlockPos pos) {
        return true;
    }

    @Override
    @Deprecated
    public boolean causesSuffocation(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos) {
        return false;
    }

    @Override
    @Deprecated
    public boolean isNormalCube(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos) {
        return false;
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos, PlacementType type, @Nullable EntityType<?> entityType) {
        return false;
    }
}