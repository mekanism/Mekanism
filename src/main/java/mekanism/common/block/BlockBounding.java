package mekanism.common.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.Mekanism;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.PushReaction;
import net.minecraft.client.particle.DiggingParticle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.chunk.ChunkRenderCache;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

//TODO: Extend MekanismBlock. Not done yet as checking is needed to ensure how drops happen still happens correctly and things in the super class don't mess this up
public class BlockBounding extends Block implements IHasTileEntity<TileEntityBoundingBlock>, IStateFluidLoggable {

    @Nullable
    public static BlockPos getMainBlockPos(IBlockReader world, BlockPos thisPos) {
        TileEntityBoundingBlock te = MekanismUtils.getTileEntity(TileEntityBoundingBlock.class, world, thisPos);
        if (te != null && te.receivedCoords && !thisPos.equals(te.getMainPos())) {
            return te.getMainPos();
        }
        return null;
    }

    //Note: This is not a block state so that we can have it easily create the correct TileEntity.
    // If we end up merging some logic from the TileEntities then this can become a property
    private final boolean advanced;

    public BlockBounding(boolean advanced) {
        //Note: We require setting variable opacity so that the block state does not cache the ability of if blocks can be placed on top of the bounding block
        // Torches cannot be placed on the sides due to vanilla checking the incorrect shape
        super(BlockStateHelper.applyLightLevelAdjustments(AbstractBlock.Properties.create(Material.IRON).hardnessAndResistance(3.5F, 8F)
              .setRequiresTool().variableOpacity()));
        this.advanced = advanced;
        setDefaultState(BlockStateHelper.getDefaultState(stateContainer.getBaseState()));
    }

    @Override
    protected void fillStateContainer(@Nonnull StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        BlockStateHelper.fillBlockStateContainer(this, builder);
    }

    @Nonnull
    @Override
    @Deprecated
    public PushReaction getPushReaction(@Nonnull BlockState state) {
        //Protect against mods like Quark that allow blocks with TEs to be moved
        return PushReaction.BLOCK;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(@Nonnull BlockItemUseContext context) {
        return BlockStateHelper.getStateForPlacement(this, super.getStateForPlacement(context), context);
    }

    @Nonnull
    @Override
    @Deprecated
    public ActionResultType onBlockActivated(@Nonnull BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull PlayerEntity player, @Nonnull Hand hand,
          @Nonnull BlockRayTraceResult hit) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            return ActionResultType.FAIL;
        }
        BlockState state1 = world.getBlockState(mainPos);
        //TODO: Use proper ray trace result, currently is using the one we got but we probably should make one with correct position information
        return state1.getBlock().onBlockActivated(state1, world, mainPos, player, hand, hit);
    }

    @Override
    @Deprecated
    public void onReplaced(BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
        //Remove the main block if a bounding block gets broken by being directly replaced
        // Note: We only do this if we don't go from bounding block to bounding block
        if (!state.isIn(newState.getBlock())) {
            BlockPos mainPos = getMainBlockPos(world, pos);
            if (mainPos != null) {
                BlockState mainState = world.getBlockState(mainPos);
                if (!mainState.isAir(world, mainPos)) {
                    //Set the main block to air, which will invalidate the rest of the bounding blocks
                    world.removeBlock(mainPos, false);
                }
            }
            super.onReplaced(state, world, pos, newState, isMoving);
        }
    }

    /**
     * {@inheritDoc} Delegate to main {@link Block#getPickBlock(BlockState, RayTraceResult, IBlockReader, BlockPos, PlayerEntity)}.
     */
    @Nonnull
    @Override
    public ItemStack getPickBlock(@Nonnull BlockState state, RayTraceResult target, @Nonnull IBlockReader world, @Nonnull BlockPos pos, PlayerEntity player) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            return ItemStack.EMPTY;
        }
        BlockState state1 = world.getBlockState(mainPos);
        return state1.getBlock().getPickBlock(state1, target, world, mainPos, player);
    }

    @Override
    public boolean removedByPlayer(@Nonnull BlockState state, World world, @Nonnull BlockPos pos, @Nonnull PlayerEntity player, boolean willHarvest,
          FluidState fluidState) {
        if (willHarvest) {
            return true;
        }
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos != null) {
            BlockState mainState = world.getBlockState(mainPos);
            if (!mainState.isAir(world, mainPos)) {
                //Set the main block to air, which will invalidate the rest of the bounding blocks
                mainState.removedByPlayer(world, mainPos, player, false, world.getFluidState(mainPos));
            }
        }
        return super.removedByPlayer(state, world, pos, player, false, fluidState);
    }

    @Override
    public void onBlockExploded(BlockState state, World world, BlockPos pos, Explosion explosion) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos != null) {
            BlockState mainState = world.getBlockState(mainPos);
            if (!mainState.isAir(world, mainPos)) {
                //Proxy the explosion to the main block which, will set it to air causing it to invalidate the rest of the bounding blocks
                LootContext.Builder lootContextBuilder = new LootContext.Builder((ServerWorld) world)
                      .withRandom(world.rand)
                      .withParameter(LootParameters.field_237457_g_, Vector3d.copyCentered(mainPos))
                      .withParameter(LootParameters.TOOL, ItemStack.EMPTY)
                      .withNullableParameter(LootParameters.BLOCK_ENTITY, mainState.hasTileEntity() ? MekanismUtils.getTileEntity(world, mainPos) : null)
                      .withNullableParameter(LootParameters.THIS_ENTITY, explosion.getExploder());
                if (explosion.mode == Explosion.Mode.DESTROY) {
                    lootContextBuilder.withParameter(LootParameters.EXPLOSION_RADIUS, explosion.size);
                }
                mainState.getDrops(lootContextBuilder).forEach(stack -> Block.spawnAsEntity(world, mainPos, stack));
                mainState.onBlockExploded(world, mainPos, explosion);
            }
        }
        super.onBlockExploded(state, world, pos, explosion);
    }

    @Override
    public void harvestBlock(@Nonnull World world, @Nonnull PlayerEntity player, @Nonnull BlockPos pos, @Nonnull BlockState state, TileEntity te,
          @Nonnull ItemStack stack) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos != null) {
            BlockState mainState = world.getBlockState(mainPos);
            mainState.getBlock().harvestBlock(world, player, mainPos, mainState, MekanismUtils.getTileEntity(world, mainPos), stack);
        } else {
            super.harvestBlock(world, player, pos, state, te, stack);
        }
        world.removeBlock(pos, false);
    }

    @Override
    @Deprecated
    public void neighborChanged(@Nonnull BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull Block neighborBlock, @Nonnull BlockPos neighborPos,
          boolean isMoving) {
        TileEntityBoundingBlock tile = MekanismUtils.getTileEntity(TileEntityBoundingBlock.class, world, pos);
        if (tile != null) {
            tile.onNeighborChange(state);
        }
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos != null) {
            world.getBlockState(mainPos).neighborChanged(world, mainPos, neighborBlock, neighborPos, isMoving);
        }
    }

    @Override
    @Deprecated
    public float getPlayerRelativeBlockHardness(@Nonnull BlockState state, @Nonnull PlayerEntity player, @Nonnull IBlockReader world, @Nonnull BlockPos pos) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            return super.getPlayerRelativeBlockHardness(state, player, world, pos);
        }
        return world.getBlockState(mainPos).getPlayerRelativeBlockHardness(player, world, mainPos);
    }

    @Nonnull
    @Override
    @Deprecated
    public BlockRenderType getRenderType(@Nonnull BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world) {
        return getTileType().create();
    }

    @Override
    public TileEntityType<TileEntityBoundingBlock> getTileType() {
        if (advanced) {
            return MekanismTileEntityTypes.ADVANCED_BOUNDING_BLOCK.getTileEntityType();
        }
        return MekanismTileEntityTypes.BOUNDING_BLOCK.getTileEntityType();
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos, @Nonnull ISelectionContext context) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            //If we don't have a main pos, then act as if the block is empty so that we can move into it properly
            return VoxelShapes.empty();
        }
        BlockState mainState;
        try {
            mainState = world.getBlockState(mainPos);
        } catch (ArrayIndexOutOfBoundsException e) {
            //Note: ChunkRenderCache is client side only, though it does not seem to have any class loading issues on the server
            // due to this exception not being caught in that specific case
            if (world instanceof ChunkRenderCache) {
                //Workaround for when the main spot of the miner is out of bounds of the ChunkRenderCache thus causing an
                // ArrayIndexOutOfBoundException on the client as seen by:
                // https://github.com/mekanism/Mekanism/issues/5792
                // https://github.com/mekanism/Mekanism/issues/5844
                world = ((ChunkRenderCache) world).world;
                mainState = world.getBlockState(mainPos);
            } else {
                Mekanism.logger.error("Error getting bounding block shape, for position {}, with main position {}. World of type {}", pos, mainPos, world.getClass().getName());
                return VoxelShapes.empty();
            }
        }
        VoxelShape shape = mainState.getShape(world, mainPos, context);
        BlockPos offset = pos.subtract(mainPos);
        //TODO: Can we somehow cache the withOffset? It potentially would have to then be moved into the Tile, but that is probably fine
        return shape.withOffset(-offset.getX(), -offset.getY(), -offset.getZ());
    }

    @Nonnull
    @Override
    @Deprecated
    public FluidState getFluidState(@Nonnull BlockState state) {
        return getFluid(state);
    }

    @Nonnull
    @Override
    @Deprecated
    public BlockState updatePostPlacement(@Nonnull BlockState state, @Nonnull Direction facing, @Nonnull BlockState facingState, @Nonnull IWorld world,
          @Nonnull BlockPos currentPos, @Nonnull BlockPos facingPos) {
        updateFluids(state, world, currentPos);
        return super.updatePostPlacement(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    @Deprecated
    public boolean allowsMovement(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos, @Nonnull PathType type) {
        //Mark that bounding blocks do not allow movement for use by AI pathing
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean addHitEffects(BlockState state, World world, RayTraceResult target, ParticleManager manager) {
        if (target.getType() == Type.BLOCK && target instanceof BlockRayTraceResult) {
            BlockRayTraceResult blockTarget = (BlockRayTraceResult) target;
            BlockPos pos = blockTarget.getPos();
            BlockPos mainPos = getMainBlockPos(world, pos);
            if (mainPos != null) {
                BlockState mainState = world.getBlockState(mainPos);
                if (!mainState.isAir(world, mainPos)) {
                    //Copy of ParticleManager#addBlockHitEffects except using the block state for the main position
                    AxisAlignedBB axisalignedbb = state.getShape(world, pos).getBoundingBox();
                    double x = pos.getX() + world.rand.nextDouble() * (axisalignedbb.maxX - axisalignedbb.minX - 0.2) + 0.1 + axisalignedbb.minX;
                    double y = pos.getY() + world.rand.nextDouble() * (axisalignedbb.maxY - axisalignedbb.minY - 0.2) + 0.1 + axisalignedbb.minY;
                    double z = pos.getZ() + world.rand.nextDouble() * (axisalignedbb.maxZ - axisalignedbb.minZ - 0.2) + 0.1 + axisalignedbb.minZ;
                    Direction side = blockTarget.getFace();
                    if (side == Direction.DOWN) {
                        y = pos.getY() + axisalignedbb.minY - 0.1;
                    } else if (side == Direction.UP) {
                        y = pos.getY() + axisalignedbb.maxY + 0.1;
                    } else if (side == Direction.NORTH) {
                        z = pos.getZ() + axisalignedbb.minZ - 0.1;
                    } else if (side == Direction.SOUTH) {
                        z = pos.getZ() + axisalignedbb.maxZ + 0.1;
                    } else if (side == Direction.WEST) {
                        x = pos.getX() + axisalignedbb.minX - 0.1;
                    } else if (side == Direction.EAST) {
                        x = pos.getX() + axisalignedbb.maxX + 0.1;
                    }
                    manager.addEffect(new DiggingParticle((ClientWorld) world, x, y, z, 0, 0, 0, mainState)
                          .setBlockPos(mainPos).multiplyVelocity(0.2F).multiplyParticleScaleBy(0.6F));
                    return true;
                }
            }
        }
        return false;
    }
}