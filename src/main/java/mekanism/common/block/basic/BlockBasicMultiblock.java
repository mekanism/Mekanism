package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.block.BlockTileDrops;
import mekanism.common.multiblock.IMultiblock;
import mekanism.common.tile.TileEntityMultiblock;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.thread.EffectiveSide;

public class BlockBasicMultiblock extends BlockTileDrops {

    public BlockBasicMultiblock() {
        this(Block.Properties.create(Material.IRON).hardnessAndResistance(5F, 10F));
    }

    public BlockBasicMultiblock(Block.Properties properties) {
        super(properties);
    }

    @Override
    @Deprecated
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (!world.isRemote) {
            TileEntity tileEntity = MekanismUtils.getTileEntity(world, pos);
            if (tileEntity instanceof IMultiblock) {
                ((IMultiblock<?>) tileEntity).doUpdate();
            }
            if (tileEntity instanceof TileEntityMekanism) {
                ((TileEntityMekanism) tileEntity).onNeighborChange(neighborBlock);
            }
        }
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos, PlacementType type, @Nullable EntityType<?> entityType) {
        TileEntityMultiblock<?> tileEntity = MekanismUtils.getTileEntity(TileEntityMultiblock.class, world, pos);
        if (tileEntity != null) {
            if (world instanceof IWorldReader ? !((IWorldReader) world).isRemote() : EffectiveSide.get() == LogicalSide.SERVER) {
                if (tileEntity.structure != null) {
                    return false;
                }
            } else if (tileEntity.clientHasStructure) {
                return false;
            }
        }
        return super.canCreatureSpawn(state, world, pos, type, entityType);
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        TileEntityMultiblock<?> tileEntity = MekanismUtils.getTileEntity(TileEntityMultiblock.class, world, pos);
        if (tileEntity != null) {
            if (world.isRemote) {
                return true;
            }
            if (tileEntity.tryWrench(state, player, hand, hit) != WrenchResult.PASS) {
                return true;
            }
            return tileEntity.onActivate(player, hand, player.getHeldItem(hand));
        }
        return false;
    }
}