package mekanism.common.block;

import java.util.Set;
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
import mekanism.common.tile.TileEntityBoundingBlock.ProxiedShapeType;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
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
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.redstone.Redstone;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.BlockRelocability;
import org.jspecify.annotations.Nullable;

//TODO: Extend MekanismBlock. Not done yet as checking is needed to ensure how drops happen still happens correctly and things in the super class don't mess this up
public class BlockBounding extends Block implements IHasTileEntity<TileEntityBoundingBlock>, IStateFluidLoggable {

    @Nullable
    public static BlockPos getMainBlockPos(BlockGetter world, BlockPos thisPos) {
        TileEntityBoundingBlock te = WorldUtils.getTileEntity(TileEntityBoundingBlock.class, world, thisPos);
        return te == null ? null : te.getMainPos();
    }

    //TODO - 26.3: When breaking it doesn't display the break progress outline if a bounding block is the primary target
    public BlockBounding(BlockBehaviour.Properties properties) {
        //Note: We require setting variable opacity so that the block state does not cache the ability of if blocks can be placed on top of the bounding block
        // Torches cannot be placed on the sides due to vanilla checking the incorrect shape
        //Note: We mark it as not having occlusion as our occlusion shape is not quite right in that it goes past a single block size which confuses MC
        // Eventually we may want to try cropping it but for now this works better
        //Note: We explicitly set the push reaction to protect against mods like Quark that allow blocks with TEs to be moved
        super(BlockStateHelper.applyLightLevelAdjustments(properties.mapColor(BlockResourceInfo.STEEL.getMapColor())
              .strength(3.5F, 4.8F).requiresCorrectToolForDrops().dynamicShape().noOcclusion()
              .isViewBlocking(BlockStateHelper.NEVER_VIEW_BLOCKING).pushReaction(PushReaction.IMMOVEABLE)));
        registerDefaultState(BlockStateHelper.getDefaultState(stateDefinition.any()));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        BlockStateHelper.fillBlockStateContainer(this, builder);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return BlockStateHelper.getStateForPlacement(super.getStateForPlacement(context), context);
    }

    @Override
    public BlockRelocability getRelocability(LevelReader level, BlockPos pos, BlockState state) {
        if (state.is(Tags.Blocks.RELOCATION_NOT_SUPPORTED)) {//This is the check that happens in super
            return BlockRelocability.No.INSTANCE;
        }
        BlockPos mainPos = getMainBlockPos(level, pos);
        if (mainPos == null) {
            //The bounding block is effectively air as it has no main position set, so something went wrong.
            // Don't block a mod that is trying to move an AOE from being able to move the bounding block as it won't have a reference regardless of if it is moved
            return BlockRelocability.Yes.INSTANCE;
        }
        BlockState mainState = level.getBlockState(mainPos);
        BlockRelocability relocability = mainState.getRelocability(level, mainPos);
        if (relocability instanceof BlockRelocability.Multiblock(Set<BlockPos> requiredPositions) && !requiredPositions.contains(pos)) {
            //If the main position is a relocatable multiblock, but doesn't contain our position. It isn't valid, so return that we can't be relocated
            return BlockRelocability.No.INSTANCE;
        }
        return relocability;
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos mainPos = getMainBlockPos(level, context.getClickedPos());
        //Allow replacing the bounding block if it doesn't have a main pos or the block at the main position doesn't have any bounding blocks,
        // so this one is likely left over from someone manually removing or placing something with commands
        return mainPos == null || !Attribute.has(level.getBlockState(mainPos), AttributeHasBounding.class);
    }

    @Override
    protected boolean canBeReplaced(BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            return InteractionResult.FAIL;
        }
        BlockState mainState = world.getBlockState(mainPos);
        return mainState.useWithoutItem(world, player, withHitResultForMain(hit, mainPos));
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player,
          InteractionHand hand, BlockHitResult hit) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            return InteractionResult.FAIL;
        }
        BlockState mainState = world.getBlockState(mainPos);
        return mainState.useItemOn(stack, world, player, hand, withHitResultForMain(hit, mainPos));
    }

    private BlockHitResult withHitResultForMain(BlockHitResult hit, BlockPos mainPos) {
        Vec3 location = Vec3.atCenterOf(mainPos).relative(hit.getDirection(), 0.5);
        if (hit.getType() == Type.MISS) {
            //I don't know if this ever can be the case, but if it is, just return a proper one for it
            return BlockHitResult.miss(location, hit.getDirection(), mainPos);
        }
        return new BlockHitResult(location, hit.getDirection(), mainPos, hit.isInside());
    }

    /// {@inheritDoc}
    ///
    /// Delegate to main [Block#getCloneItemStack(LevelReader, BlockPos, BlockState, boolean, Player)].
    @Override
    public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state, boolean includeData, Player player) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            return ItemStack.EMPTY;
        }
        BlockState mainState = world.getBlockState(mainPos);
        return mainState.getCloneItemStack(mainPos, world, includeData, player);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level world, BlockPos pos, Player player, ItemStack toolStack, boolean willHarvest,
          FluidState fluidState) {
        if (willHarvest) {
            return true;
        }
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos != null) {
            BlockState mainState = world.getBlockState(mainPos);
            if (!mainState.isAir()) {
                //Set the main block to air, which will invalidate the rest of the bounding blocks
                mainState.onDestroyedByPlayer(world, mainPos, player, toolStack, false, mainState.getFluidState());
            }
        }
        return super.onDestroyedByPlayer(state, world, pos, player, toolStack, false, fluidState);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
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
    protected void onExplosionHit(BlockState state, ServerLevel level, BlockPos pos, Explosion explosion,
          BiConsumer<ItemStack, BlockPos> dropConsumer) {
        BlockPos mainPos = getMainBlockPos(level, pos);
        if (mainPos == null) {
            super.onExplosionHit(state, level, pos, explosion, dropConsumer);
        } else {
            //Proxy the explosion to the main block which, will set it to air causing it to invalidate the rest of the bounding blocks
            level.getBlockState(mainPos).onExplosionHit(level, mainPos, explosion, dropConsumer);
        }
    }

    @Override
    protected void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack stack, boolean dropExperience) {
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
    public void playerDestroy(ServerLevel world, ServerPlayer player, BlockPos pos, BlockState state, @Nullable BlockEntity te, ItemStack stack) {
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
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        TileEntityBoundingBlock tile = WorldUtils.getTileEntity(TileEntityBoundingBlock.class, level, pos);
        if (tile != null) {
            if (!level.isClientSide()) {
                tile.neighborChanged(level, orientation);
            }
            BlockPos mainPos = tile.getMainPos();
            if (mainPos != null) {
                BlockState mainState = level.getBlockState(mainPos);
                mainState.handleNeighborChanged(level, mainPos, block, orientation, movedByPiston);
            }
        }
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState blockState) {
        //TODO: Figure out if there is a better way to do this so it doesn't have to return true for all bounding blocks
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level world, BlockPos pos, Direction direction) {
        if (!world.isClientSide()) {
            TileEntityBoundingBlock tile = WorldUtils.getTileEntity(TileEntityBoundingBlock.class, world, pos);
            if (tile != null) {
                return tile.getComparatorSignal();
            }
        }
        return Redstone.SIGNAL_NONE;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        level.updateNeighbourForOutputSignal(pos, state.getBlock());
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter world, BlockPos pos) {
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

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        super.triggerEvent(state, level, pos, id, param);
        return triggerBlockEntityEvent(state, level, pos, id, param);
    }

    @Override
    public TileEntityTypeRegistryObject<TileEntityBoundingBlock> getTileType() {
        return MekanismTileEntityTypes.BOUNDING_BLOCK;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return proxyShape(world, pos, ProxiedShapeType.SHAPE, context);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return proxyShape(world, pos, ProxiedShapeType.COLLISION, context);
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return proxyShape(world, pos, ProxiedShapeType.VISUAL, context);
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state) {
        //Note: We just return an empty shape as we mark ourselves as noOcclusion so this method shouldn't really ever be hit,
        // and we don't have enough of a context to be able to proxy the shape to the main block
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter world, BlockPos pos) {
        return proxyShape(world, pos, ProxiedShapeType.BLOCK_SUPPORT, null);
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter world, BlockPos pos) {
        return proxyShape(world, pos, ProxiedShapeType.INTERACTION, null);
    }

    //Context should only be null if there is none, and it isn't used in the shape proxy
    private VoxelShape proxyShape(BlockGetter level, BlockPos pos, ProxiedShapeType proxiedShapeType, @Nullable CollisionContext context) {
        TileEntityBoundingBlock tile = WorldUtils.getTileEntity(TileEntityBoundingBlock.class, level, pos);
        return tile == null ? Shapes.empty() : tile.proxyShape(level, proxiedShapeType, context);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return getFluid(state);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos currentPos,
          Direction facing, BlockPos facingPos, BlockState facingState, RandomSource random) {
        updateFluids(level, currentPos, state, scheduledTickAccess);
        return super.updateShape(state, level, scheduledTickAccess, currentPos, facing, facingPos, facingState, random);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        //Mark that bounding blocks do not allow movement for use by AI pathing
        return false;
    }
}