package mekanism.generators.common.block.reactor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.block.IHasTileEntity;
import mekanism.common.base.ILangEntry;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.registries.GeneratorsTileEntityTypes;
import mekanism.generators.common.tile.reactor.TileEntityReactorLaserFocusMatrix;
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
import net.minecraftforge.common.ToolType;

public class BlockLaserFocusMatrix extends BlockMekanism implements IHasTileEntity<TileEntityReactorLaserFocusMatrix>, IHasDescription {

    public BlockLaserFocusMatrix() {
        super(Block.Properties.create(Material.GLASS).hardnessAndResistance(3.5F, 8F).notSolid().harvestTool(ToolType.PICKAXE));
    }

    @Override
    @Deprecated
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (!world.isRemote) {
            TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
            if (tile != null) {
                tile.onNeighborChange(neighborBlock);
            }
        }
    }

    @Override
    public boolean shouldDisplayFluidOverlay(BlockState state, ILightReader world, BlockPos pos, IFluidState fluidState) {
        return true;
    }

    @Nonnull
    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
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
    public boolean isSideInvisible(BlockState state, BlockState adjacentBlockState, Direction side) {
        Block blockOffset = adjacentBlockState.getBlock();
        if (blockOffset instanceof BlockReactorGlass || blockOffset instanceof BlockLaserFocusMatrix) {
            return true;
        }
        return super.isSideInvisible(state, adjacentBlockState, side);
    }

    @Override
    public TileEntityType<TileEntityReactorLaserFocusMatrix> getTileType() {
        return GeneratorsTileEntityTypes.LASER_FOCUS_MATRIX.getTileEntityType();
    }

    @Nonnull
    @Override
    public ILangEntry getDescription() {
        return GeneratorsLang.DESCRIPTION_LASER_FOCUS_MATRIX;
    }

    @Override
    @Deprecated
    public float getAmbientOcclusionLightValue(BlockState state, @Nonnull IBlockReader worldIn, @Nonnull BlockPos pos) {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, @Nonnull IBlockReader reader, @Nonnull BlockPos pos) {
        return true;
    }

    @Override
    @Deprecated
    public boolean causesSuffocation(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos) {
        return false;
    }

    @Override
    @Deprecated
    public boolean isNormalCube(BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos) {
        return false;
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos, PlacementType type, @Nullable EntityType<?> entityType) {
        return false;
    }
}