package mekanism.common.block;

import java.util.function.BiConsumer;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeHasBounding;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.resource.BlockResourceInfo;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Redstone;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//TODO: Extend MekanismBlock. Not done yet as checking is needed to ensure how drops happen still happens correctly and things in the super class don't mess this up
public class BlockBounding extends Block implements IHasTileEntity<TileEntityBoundingBlock>, IStateFluidLoggable {

    @Nullable
    public static BlockPos getMainBlockPos(BlockGetter world, BlockPos thisPos) {
        TileEntityBoundingBlock te = WorldUtils.getTileEntity(TileEntityBoundingBlock.class, world, thisPos);
        if (te != null && te.canRedirectFrom(thisPos)) {
            return te.getMainPos();
        }
        return null;
    }

    public BlockBounding() {
        //Note: We require setting variable opacity so that the block state does not cache the ability of if blocks can be placed on top of the bounding block
        // Torches cannot be placed on the sides due to vanilla checking the incorrect shape
        //Note: We mark it as not having occlusion as our occlusion shape is not quite right in that it goes past a single block size which confuses MC
        // Eventually we may want to try cropping it but for now this works better
        //Note: We explicitly set the push reaction to protect against mods like Quark that allow blocks with TEs to be moved
        super(BlockStateHelper.applyLightLevelAdjustments(BlockBehaviour.Properties.of().mapColor(BlockResourceInfo.STEEL.getMapColor())
              .strength(3.5F, 4.8F).requiresCorrectToolForDrops().dynamicShape().noOcclusion()
              .isViewBlocking(BlockStateHelper.NEVER_PREDICATE).pushReaction(PushReaction.BLOCK)));
        registerDefaultState(BlockStateHelper.getDefaultState(stateDefinition.any()));
    }

    @Override
    protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        BlockStateHelper.fillBlockStateContainer(this, builder);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return BlockStateHelper.getStateForPlacement(super.getStateForPlacement(context), context);
    }

    @Override
    protected boolean canBeReplaced(@NotNull BlockState state, @NotNull BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos mainPos = getMainBlockPos(level, context.getClickedPos());
        //Allow replacing the bounding block if it doesn't have a main pos or the block at the main position doesn't have any bounding blocks,
        // so this one is likely left over from someone manually removing or placing something with commands
        return mainPos == null || !Attribute.has(level.getBlockState(mainPos), AttributeHasBounding.class);
    }

    @Override
    protected boolean canBeReplaced(@NotNull BlockState state, @NotNull Fluid fluid) {
        return false;
    }

    @NotNull
    @Override
    protected InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            return InteractionResult.FAIL;
        }
        BlockState mainState = world.getBlockState(mainPos);
        return mainState.useWithoutItem(world, player, withHitResultForMain(hit, mainPos));
    }

    @NotNull
    @Override
    protected ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Player player,
          @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            return ItemInteractionResult.FAIL;
        }
        BlockState mainState = world.getBlockState(mainPos);
        return mainState.useItemOn(stack, world, player, hand, withHitResultForMain(hit, mainPos));
    }

    private BlockHitResult withHitResultForMain(BlockHitResult hit, BlockPos mainPos) {
        Vec3 location = mainPos.getCenter().relative(hit.getDirection(), 0.5);
        if (hit.getType() == Type.MISS) {
            //I don't know if this ever can be the case, but if it is, just return a proper one for it
            return BlockHitResult.miss(location, hit.getDirection(), mainPos);
        }
        return new BlockHitResult(location, hit.getDirection(), mainPos, hit.isInside());
    }

    @Override
    protected void onRemove(BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
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
     * {@inheritDoc} Delegate to main {@link Block#getCloneItemStack(BlockState, HitResult, LevelReader, BlockPos, Player)}.
     */
    @NotNull
    @Override
    public ItemStack getCloneItemStack(@NotNull BlockState state, @NotNull HitResult target, @NotNull LevelReader world, @NotNull BlockPos pos, @NotNull Player player) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            return ItemStack.EMPTY;
        }
        BlockState mainState = world.getBlockState(mainPos);
        return mainState.getBlock().getCloneItemStack(mainState, target, world, mainPos, player);
    }

    @Override
    public boolean onDestroyedByPlayer(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Player player, boolean willHarvest,
          @NotNull FluidState fluidState) {
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

    @NotNull
    @Override
    public BlockState playerWillDestroy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player) {
        BlockPos mainPos = getMainBlockPos(level, pos);
        if (mainPos != null) {
            BlockState mainState = level.getBlockState(mainPos);
            if (!mainState.isAir()) {
                //Call player will destroy on the main block in case we have any other logic we want to run, but then return ourselves as the state
                // similar to what calling super would do.
                // Note: We don't call super as we don't want to spawn block break particles twice, and it only really makes sense to be firing the
                // game event for the main block position
                mainState.getBlock().playerWillDestroy(level, mainPos, mainState, player);
                return state;
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void onExplosionHit(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Explosion explosion,
          @NotNull BiConsumer<ItemStack, BlockPos> dropConsumer) {
        BlockPos mainPos = getMainBlockPos(level, pos);
        if (mainPos == null) {
            super.onExplosionHit(state, level, pos, explosion, dropConsumer);
        } else {
            //Proxy the explosion to the main block which, will set it to air causing it to invalidate the rest of the bounding blocks
            level.getBlockState(mainPos).onExplosionHit(level, mainPos, explosion, dropConsumer);
        }
    }

    @Override
    protected void spawnAfterBreak(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull ItemStack stack, boolean dropExperience) {
        BlockPos mainPos = getMainBlockPos(level, pos);
        if (mainPos != null) {
            BlockState mainState = level.getBlockState(mainPos);
            if (!mainState.isAir()) {
                //Proxy the call to the main block even though none of our blocks currently make use of this method
                mainState.spawnAfterBreak(level, mainPos, stack, dropExperience);
            }
        }
        super.spawnAfterBreak(state, level, pos, stack, dropExperience);
    }

    @Override
    public void playerDestroy(@NotNull Level world, @NotNull Player player, @NotNull BlockPos pos, @NotNull BlockState state, BlockEntity te,
          @NotNull ItemStack stack) {
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
    protected void neighborChanged(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Block neighborBlock, @NotNull BlockPos neighborPos,
          boolean isMoving) {
        if (!world.isClientSide) {
            TileEntityBoundingBlock tile = WorldUtils.getTileEntity(TileEntityBoundingBlock.class, world, pos);
            if (tile != null) {
                tile.onNeighborChange(world, neighborBlock, neighborPos);
            }
        }
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos != null) {
            world.getBlockState(mainPos).handleNeighborChanged(world, mainPos, neighborBlock, neighborPos, isMoving);
        }
    }

    @Override
    protected boolean hasAnalogOutputSignal(@NotNull BlockState blockState) {
        //TODO: Figure out if there is a better way to do this so it doesn't have to return true for all bounding blocks
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(@NotNull BlockState blockState, @NotNull Level world, @NotNull BlockPos pos) {
        if (!world.isClientSide) {
            TileEntityBoundingBlock tile = WorldUtils.getTileEntity(TileEntityBoundingBlock.class, world, pos);
            if (tile != null) {
                return tile.getComparatorSignal();
            }
        }
        return Redstone.SIGNAL_NONE;
    }

    @Override
    protected float getDestroyProgress(@NotNull BlockState state, @NotNull Player player, @NotNull BlockGetter world, @NotNull BlockPos pos) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            return super.getDestroyProgress(state, player, world, pos);
        }
        return world.getBlockState(mainPos).getDestroyProgress(player, world, mainPos);
    }

    @Override
    public float getExplosionResistance(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull Explosion explosion) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            return super.getExplosionResistance(state, world, pos, explosion);
        }
        return world.getBlockState(mainPos).getExplosionResistance(world, mainPos, explosion);
    }

    @NotNull
    @Override
    protected RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected boolean triggerEvent(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, int id, int param) {
        super.triggerEvent(state, level, pos, id, param);
        return triggerBlockEntityEvent(state, level, pos, id, param);
    }

    @Override
    public TileEntityTypeRegistryObject<TileEntityBoundingBlock> getTileType() {
        return MekanismTileEntityTypes.BOUNDING_BLOCK;
    }

    @NotNull
    @Override
    protected VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return proxyShape(world, pos, context, BlockStateBase::getShape);
    }

    @NotNull
    @Override
    protected VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return proxyShape(world, pos, context, BlockStateBase::getCollisionShape);
    }

    @NotNull
    @Override
    protected VoxelShape getVisualShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return proxyShape(world, pos, context, BlockStateBase::getVisualShape);
    }

    @NotNull
    @Override
    protected VoxelShape getOcclusionShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos) {
        return proxyShape(world, pos, null, (s, level, p, ctx) -> s.getOcclusionShape(level, p));
    }

    @NotNull
    @Override
    protected VoxelShape getBlockSupportShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos) {
        return proxyShape(world, pos, null, (s, level, p, ctx) -> s.getBlockSupportShape(level, p));
    }

    @NotNull
    @Override
    protected VoxelShape getInteractionShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos) {
        return proxyShape(world, pos, null, (s, level, p, ctx) -> s.getInteractionShape(level, p));
    }

    //Context should only be null if there is none, and it isn't used in the shape proxy
    private VoxelShape proxyShape(BlockGetter world, BlockPos pos, @Nullable CollisionContext context, ShapeProxy proxy) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            //If we don't have a main pos, then act as if the block is empty so that we can move into it properly
            return Shapes.empty();
        }
        BlockState mainState = world.getBlockState(mainPos);
        VoxelShape shape = proxy.getShape(mainState, world, mainPos, context);
        BlockPos offset = pos.subtract(mainPos);
        //TODO: Can we somehow cache the withOffset? It potentially would have to then be moved into the Tile, but that is probably fine
        return shape.move(-offset.getX(), -offset.getY(), -offset.getZ());
    }

    @NotNull
    @Override
    protected FluidState getFluidState(@NotNull BlockState state) {
        return getFluid(state);
    }

    @NotNull
    @Override
    protected BlockState updateShape(@NotNull BlockState state, @NotNull Direction facing, @NotNull BlockState facingState, @NotNull LevelAccessor world,
          @NotNull BlockPos currentPos, @NotNull BlockPos facingPos) {
        updateFluids(state, world, currentPos);
        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    protected boolean isPathfindable(@NotNull BlockState state, @NotNull PathComputationType type) {
        //Mark that bounding blocks do not allow movement for use by AI pathing
        return false;
    }

    private interface ShapeProxy {

        VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context);
    }
}