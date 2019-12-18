package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.block.IHasModel;
import mekanism.api.block.IHasTileEntity;
import mekanism.common.block.BlockMekanism;
import mekanism.common.multiblock.IStructuralMultiblock;
import mekanism.common.tile.TileEntityStructuralGlass;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockStructuralGlass extends BlockMekanism implements IHasModel, IHasTileEntity<TileEntityStructuralGlass> {

    public BlockStructuralGlass() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(5F, 10F));
    }

    @Override
    @Deprecated
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (!world.isRemote) {
            TileEntity tile = MekanismUtils.getTileEntity(world, pos);
            if (tile instanceof TileEntityMekanism) {
                ((TileEntityMekanism) tile).onNeighborChange(neighborBlock);
            }
            if (tile instanceof IStructuralMultiblock) {
                ((IStructuralMultiblock) tile).doUpdate();
            }
        }
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos, PlacementType type, @Nullable EntityType<?> entityType) {
        return false;
    }

    @Override
    @Deprecated
    public boolean isSideInvisible(BlockState state, BlockState adjacentBlockState, Direction side) {
        //Not structural glass
        return adjacentBlockState.getBlock() == this;
    }

    @Nonnull
    @Override
    public ActionResultType func_225533_a_(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        TileEntityStructuralGlass tile = MekanismUtils.getTileEntity(TileEntityStructuralGlass.class, world, pos);
        if (tile != null) {
            if (world.isRemote) {
                return ActionResultType.SUCCESS;
            }
            return tile.onActivate(player, hand, player.getHeldItem(hand));
        }
        return ActionResultType.PASS;
    }

    @Override
    public TileEntityType<TileEntityStructuralGlass> getTileType() {
        return MekanismTileEntityTypes.STRUCTURAL_GLASS.getTileEntityType();
    }
}