package mekanism.common.block;

import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.Mekanism;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.util.WorldUtils;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.IBlockRenderProperties;

//TODO: Extend MekanismBlock. Not done yet as checking is needed to ensure how drops happen still happens correctly and things in the super class don't mess this up
public class BlockBounding extends Block implements IHasTileEntity<TileEntityBoundingBlock>, IStateFluidLoggable {

    @Nullable
    public static BlockPos getMainBlockPos(BlockGetter world, BlockPos thisPos) {
        TileEntityBoundingBlock te = WorldUtils.getTileEntity(TileEntityBoundingBlock.class, world, thisPos);
        if (te != null && te.hasReceivedCoords() && !thisPos.equals(te.getMainPos())) {
            return te.getMainPos();
        }
        return null;
    }

    public BlockBounding() {
        //Note: We require setting variable opacity so that the block state does not cache the ability of if blocks can be placed on top of the bounding block
        // Torches cannot be placed on the sides due to vanilla checking the incorrect shape
        //Note: We mark it as not having occlusion as our occlusion shape is not quite right in that it goes past a single block size which confuses MC
        // Eventually we may want to try cropping it but for now this works better
        super(BlockStateHelper.applyLightLevelAdjustments(BlockBehaviour.Properties.of(Material.METAL).strength(3.5F, 4.8F)
              .requiresCorrectToolForDrops().dynamicShape().noOcclusion().isViewBlocking(BlockStateHelper.NEVER_PREDICATE)));
        registerDefaultState(BlockStateHelper.getDefaultState(stateDefinition.any()));
    }

    @Override
    public void initializeClient(Consumer<IBlockRenderProperties> consumer) {
        consumer.accept(RenderPropertiesProvider.boundingParticles());
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        BlockStateHelper.fillBlockStateContainer(this, builder);
    }

    @Nonnull
    @Override
    @Deprecated
    public PushReaction getPistonPushReaction(@Nonnull BlockState state) {
        //Protect against mods like Quark that allow blocks with TEs to be moved
        return PushReaction.BLOCK;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(@Nonnull BlockPlaceContext context) {
        return BlockStateHelper.getStateForPlacement(this, super.getStateForPlacement(context), context);
    }

    @Nonnull
    @Override
    @Deprecated
    public InteractionResult use(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand,
          @Nonnull BlockHitResult hit) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            return InteractionResult.FAIL;
        }
        BlockState mainState = world.getBlockState(mainPos);
        //TODO: Use proper ray trace result, currently is using the one we got but we probably should make one with correct position information
        return mainState.getBlock().use(mainState, world, mainPos, player, hand, hit);
    }

    @Override
    @Deprecated
    public void onRemove(BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
        //Remove the main block if a bounding block gets broken by being directly replaced
        // Note: We only do this if we don't go from bounding block to bounding block
        if (!state.is(newState.getBlock())) {
            BlockPos mainPos = getMainBlockPos(world, pos);
            if (mainPos != null) {
                BlockState mainState = world.getBlockState(mainPos);
                if (!mainState.isAir()) {
                    //Set the main block to air, which will invalidate the rest of the bounding blocks
                    world.removeBlock(mainPos, false);
                }
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    /**
     * {@inheritDoc} Delegate to main {@link Block#getCloneItemStack(BlockState, HitResult, BlockGetter, BlockPos, Player)}.
     */
    @Nonnull
    @Override
    public ItemStack getCloneItemStack(@Nonnull BlockState state, HitResult target, @Nonnull BlockGetter world, @Nonnull BlockPos pos, Player player) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            return ItemStack.EMPTY;
        }
        BlockState mainState = world.getBlockState(mainPos);
        return mainState.getBlock().getCloneItemStack(mainState, target, world, mainPos, player);
    }

    @Override
    public boolean onDestroyedByPlayer(@Nonnull BlockState state, Level world, @Nonnull BlockPos pos, @Nonnull Player player, boolean willHarvest,
          FluidState fluidState) {
        if (willHarvest) {
            return true;
        }
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos != null) {
            BlockState mainState = world.getBlockState(mainPos);
            if (!mainState.isAir()) {
                //Set the main block to air, which will invalidate the rest of the bounding blocks
                mainState.onDestroyedByPlayer(world, mainPos, player, false, mainState.getFluidState());
            }
        }
        return super.onDestroyedByPlayer(state, world, pos, player, false, fluidState);
    }

    @Override
    public void onBlockExploded(BlockState state, Level world, BlockPos pos, Explosion explosion) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos != null) {
            BlockState mainState = world.getBlockState(mainPos);
            if (!mainState.isAir()) {
                //Proxy the explosion to the main block which, will set it to air causing it to invalidate the rest of the bounding blocks
                LootContext.Builder lootContextBuilder = new LootContext.Builder((ServerLevel) world)
                      .withRandom(world.random)
                      .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(mainPos))
                      .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                      .withOptionalParameter(LootContextParams.BLOCK_ENTITY, mainState.hasBlockEntity() ? WorldUtils.getTileEntity(world, mainPos) : null)
                      .withOptionalParameter(LootContextParams.THIS_ENTITY, explosion.getExploder());
                if (explosion.blockInteraction == Explosion.BlockInteraction.DESTROY) {
                    lootContextBuilder.withParameter(LootContextParams.EXPLOSION_RADIUS, explosion.radius);
                }
                mainState.getDrops(lootContextBuilder).forEach(stack -> Block.popResource(world, mainPos, stack));
                mainState.onBlockExploded(world, mainPos, explosion);
            }
        }
        super.onBlockExploded(state, world, pos, explosion);
    }

    @Override
    public void playerDestroy(@Nonnull Level world, @Nonnull Player player, @Nonnull BlockPos pos, @Nonnull BlockState state, BlockEntity te,
          @Nonnull ItemStack stack) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos != null) {
            BlockState mainState = world.getBlockState(mainPos);
            mainState.getBlock().playerDestroy(world, player, mainPos, mainState, WorldUtils.getTileEntity(world, mainPos), stack);
        } else {
            super.playerDestroy(world, player, pos, state, te, stack);
        }
        world.removeBlock(pos, false);
    }

    @Override
    @Deprecated
    public void neighborChanged(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull Block neighborBlock, @Nonnull BlockPos neighborPos,
          boolean isMoving) {
        if (!world.isClientSide) {
            TileEntityBoundingBlock tile = WorldUtils.getTileEntity(TileEntityBoundingBlock.class, world, pos);
            if (tile != null) {
                tile.onNeighborChange(neighborBlock, neighborPos);
            }
        }
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos != null) {
            world.getBlockState(mainPos).neighborChanged(world, mainPos, neighborBlock, neighborPos, isMoving);
        }
    }

    @Override
    @Deprecated
    public boolean hasAnalogOutputSignal(@Nonnull BlockState blockState) {
        //TODO: Figure out if there is a better way to do this so it doesn't have to return true for all bounding blocks
        return true;
    }

    @Override
    @Deprecated
    public int getAnalogOutputSignal(@Nonnull BlockState blockState, @Nonnull Level world, @Nonnull BlockPos pos) {
        if (!world.isClientSide) {
            TileEntityBoundingBlock tile = WorldUtils.getTileEntity(TileEntityBoundingBlock.class, world, pos);
            if (tile != null) {
                return tile.getComparatorSignal();
            }
        }
        return 0;
    }

    @Override
    @Deprecated
    public float getDestroyProgress(@Nonnull BlockState state, @Nonnull Player player, @Nonnull BlockGetter world, @Nonnull BlockPos pos) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            return super.getDestroyProgress(state, player, world, pos);
        }
        return world.getBlockState(mainPos).getDestroyProgress(player, world, mainPos);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter world, BlockPos pos, Explosion explosion) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            return super.getExplosionResistance(state, world, pos, explosion);
        }
        return world.getBlockState(mainPos).getExplosionResistance(world, mainPos, explosion);
    }

    @Nonnull
    @Override
    @Deprecated
    public RenderShape getRenderShape(@Nonnull BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    @Deprecated
    public boolean triggerEvent(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, int id, int param) {
        super.triggerEvent(state, level, pos, id, param);
        return triggerBlockEntityEvent(state, level, pos, id, param);
    }

    @Override
    public TileEntityTypeRegistryObject<TileEntityBoundingBlock> getTileType() {
        return MekanismTileEntityTypes.BOUNDING_BLOCK;
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
        return proxyShape(world, pos, context, BlockStateBase::getShape);
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getCollisionShape(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
        return proxyShape(world, pos, context, BlockStateBase::getCollisionShape);
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getVisualShape(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
        return proxyShape(world, pos, context, BlockStateBase::getVisualShape);
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getOcclusionShape(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos) {
        return proxyShape(world, pos, null, (s, level, p, ctx) -> s.getOcclusionShape(level, p));
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getBlockSupportShape(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos) {
        return proxyShape(world, pos, null, (s, level, p, ctx) -> s.getBlockSupportShape(level, p));
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getInteractionShape(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos) {
        return proxyShape(world, pos, null, (s, level, p, ctx) -> s.getInteractionShape(level, p));
    }

    //Context should only be null if there is none, and it isn't used in the shape proxy
    private VoxelShape proxyShape(BlockGetter world, BlockPos pos, @Nullable CollisionContext context, ShapeProxy proxy) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            //If we don't have a main pos, then act as if the block is empty so that we can move into it properly
            return Shapes.empty();
        }
        BlockState mainState;
        try {
            mainState = world.getBlockState(mainPos);
        } catch (ArrayIndexOutOfBoundsException e) {
            //Note: ChunkRenderCache is client side only, though it does not seem to have any class loading issues on the server
            // due to this exception not being caught in that specific case
            if (world instanceof RenderChunkRegion region) {
                //Workaround for when the main spot of the miner is out of bounds of the ChunkRenderCache thus causing an
                // ArrayIndexOutOfBoundException on the client as seen by:
                // https://github.com/mekanism/Mekanism/issues/5792
                // https://github.com/mekanism/Mekanism/issues/5844
                world = region.level;
                mainState = world.getBlockState(mainPos);
            } else {
                Mekanism.logger.error("Error getting bounding block shape, for position {}, with main position {}. World of type {}", pos, mainPos,
                      world.getClass().getName());
                return Shapes.empty();
            }
        }
        VoxelShape shape = proxy.getShape(mainState, world, mainPos, context);
        BlockPos offset = pos.subtract(mainPos);
        //TODO: Can we somehow cache the withOffset? It potentially would have to then be moved into the Tile, but that is probably fine
        return shape.move(-offset.getX(), -offset.getY(), -offset.getZ());
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
    public BlockState updateShape(@Nonnull BlockState state, @Nonnull Direction facing, @Nonnull BlockState facingState, @Nonnull LevelAccessor world,
          @Nonnull BlockPos currentPos, @Nonnull BlockPos facingPos) {
        updateFluids(state, world, currentPos);
        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    @Deprecated
    public boolean isPathfindable(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos, @Nonnull PathComputationType type) {
        //Mark that bounding blocks do not allow movement for use by AI pathing
        return false;
    }

    private interface ShapeProxy {

        VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context);
    }
}