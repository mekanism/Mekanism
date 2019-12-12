package mekanism.generators.common.block.turbine;

import javax.annotation.Nonnull;
import mekanism.api.block.IHasTileEntity;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.states.IStateWaterLogged;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.generators.common.GeneratorsItem;
import mekanism.generators.common.item.ItemTurbineBlade;
import mekanism.generators.common.tile.GeneratorsTileEntityTypes;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockTurbineRotor extends BlockMekanism implements IHasTileEntity<TileEntityTurbineRotor>, IStateWaterLogged {

    private static final VoxelShape bounds = makeCuboidShape(6, 0, 6, 10, 16, 10);

    public BlockTurbineRotor() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(3.5F, 8F));
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
    @Deprecated
    public float getPlayerRelativeBlockHardness(BlockState state, @Nonnull PlayerEntity player, @Nonnull IBlockReader world, @Nonnull BlockPos pos) {
        return SecurityUtils.canAccess(player, MekanismUtils.getTileEntity(world, pos)) ? super.getPlayerRelativeBlockHardness(state, player, world, pos) : 0.0F;
    }

    @Override
    public void onReplaced(BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
        if (!world.isRemote && state.hasTileEntity() && state.getBlock() != newState.getBlock()) {
            TileEntityTurbineRotor tile = MekanismUtils.getTileEntity(TileEntityTurbineRotor.class, world, pos);
            if (tile != null) {
                //TODO: Evaluate
                int amount = tile.getHousedBlades();
                if (amount > 0) {
                    spawnAsEntity(world, pos, GeneratorsItem.TURBINE_BLADE.getItemStack(amount));
                }
            }
        }
        super.onReplaced(state, world, pos, newState, isMoving);
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (world.isRemote) {
            return true;
        }
        TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
        if (tile == null) {
            return false;
        }
        if (tile.tryWrench(state, player, hand, hit) != WrenchResult.PASS) {
            return true;
        }

        ItemStack stack = player.getHeldItem(hand);
        TileEntityTurbineRotor rod = (TileEntityTurbineRotor) tile;
        if (!player.isSneaking()) {
            if (!stack.isEmpty() && stack.getItem() instanceof ItemTurbineBlade) {
                if (rod.addBlade()) {
                    if (!player.isCreative()) {
                        stack.shrink(1);
                        if (stack.getCount() == 0) {
                            player.setHeldItem(hand, ItemStack.EMPTY);
                        }
                    }
                }
                return true;
            }
        } else if (stack.isEmpty()) {
            if (rod.removeBlade()) {
                if (!player.isCreative()) {
                    player.setHeldItem(hand, GeneratorsItem.TURBINE_BLADE.getItemStack());
                    player.inventory.markDirty();
                }
            }
        } else if (stack.getItem() instanceof ItemTurbineBlade) {
            if (stack.getCount() < stack.getMaxStackSize()) {
                if (rod.removeBlade()) {
                    if (!player.isCreative()) {
                        stack.grow(1);
                        player.inventory.markDirty();
                    }
                }
            }
        }
        return true;
    }

    @Nonnull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return bounds;
    }

    @Override
    public TileEntityType<TileEntityTurbineRotor> getTileType() {
        return GeneratorsTileEntityTypes.TURBINE_ROTOR.getTileEntityType();
    }
}