package mekanism.common.block;

import mekanism.api.security.IBlockSecurityUtils;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeGui;
import mekanism.common.block.attribute.AttributeHasBounding;
import mekanism.common.block.attribute.AttributeMultiblock;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.attribute.Attributes.AttributeComparator;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.lib.radiation.Meltdown.MeltdownExplosion;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.lib.security.ISecurityTile;
import mekanism.common.network.to_client.security.PacketSyncSecurity;
import mekanism.common.registries.MekanismParticleTypes;
import mekanism.common.tile.base.TileEntityUpdateable;
import mekanism.common.tile.interfaces.IComparatorSupport;
import mekanism.common.tile.interfaces.ITileRadioactive;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.redstone.Redstone;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BlockMekanism extends Block {

    protected BlockMekanism(BlockBehaviour.Properties properties) {
        super(BlockStateHelper.applyLightLevelAdjustments(properties));
        registerDefaultState(BlockStateHelper.getDefaultState(stateDefinition.any()));
    }

    @Nullable
    @Override
    public PushReaction getPistonPushReaction(@NotNull BlockState state) {
        if (state.hasBlockEntity()) {
            //Protect against mods like Quark that allow blocks with TEs to be moved
            //TODO: Eventually it would be nice to go through this and maybe even allow some TEs to be moved if they don't strongly
            // care about the world, but for now it is safer to just block them from being moved
            return PushReaction.BLOCK;
        }
        return super.getPistonPushReaction(state);
    }

    @Override
    protected boolean canBeReplaced(@NotNull BlockState state, @NotNull Fluid fluid) {
        return false;
    }

    @NotNull
    @Override
    public ItemStack getCloneItemStack(@NotNull BlockState state, @NotNull HitResult target, @NotNull LevelReader world, @NotNull BlockPos pos, @NotNull Player player) {
        ItemStack stack = super.getCloneItemStack(state, target, world, pos, player);
        if (MekanismConfig.common.copyBlockData.get()) {
            TileEntityUpdateable tile = WorldUtils.getTileEntity(TileEntityUpdateable.class, world, pos);
            if (tile != null) {
                stack.applyComponents(tile.collectComponents());
            }
        }
        return stack;
    }

    @Override
    protected boolean triggerEvent(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, int id, int param) {
        boolean triggered = super.triggerEvent(state, level, pos, id, param);
        if (this instanceof IHasTileEntity<?> hasTileEntity) {
            return hasTileEntity.triggerBlockEntityEvent(state, level, pos, id, param);
        }
        return triggered;
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

    @NotNull
    @Override
    protected FluidState getFluidState(BlockState state) {
        if (state.getBlock() instanceof IStateFluidLoggable fluidLoggable) {
            return fluidLoggable.getFluid(state);
        }
        return super.getFluidState(state);
    }

    @NotNull
    @Override
    protected BlockState updateShape(BlockState state, @NotNull Direction facing, @NotNull BlockState facingState, @NotNull LevelAccessor world, @NotNull BlockPos currentPos,
          @NotNull BlockPos facingPos) {
        if (state.getBlock() instanceof IStateFluidLoggable fluidLoggable) {
            fluidLoggable.updateFluids(state, world, currentPos);
        }
        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    protected void onRemove(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            AttributeHasBounding hasBounding = Attribute.get(state, AttributeHasBounding.class);
            if (hasBounding != null) {
                hasBounding.removeBoundingBlocks(world, pos, state);
            }
        }
        if (state.hasBlockEntity() && !state.is(newState.getBlock())) {
            TileEntityUpdateable tile = WorldUtils.getTileEntity(TileEntityUpdateable.class, world, pos);
            if (tile != null) {
                tile.blockRemoved();
            }
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }

    @Override
    public void setPlacedBy(@NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);
        AttributeHasBounding hasBounding = Attribute.get(state, AttributeHasBounding.class);
        if (hasBounding != null) {
            hasBounding.placeBoundingBlocks(world, pos, state);
        }
        TileEntityUpdateable tile = WorldUtils.getTileEntity(TileEntityUpdateable.class, world, pos);
        if (tile != null) {
            //Note: We call onAdded here rather than in onPlace so that we make sure we can run any client side code and that the
            // tile is present
            tile.onAdded();
            if (tile instanceof ISecurityTile securityTile && securityTile.getOwnerUUID() == null && placer != null) {
                //There was no stored owner that got set, use the placer's id
                securityTile.setOwnerUUID(placer.getUUID());
                if (!world.isClientSide) {
                    //If the machine doesn't already have an owner, make sure we portray this
                    PacketDistributor.sendToAllPlayers(new PacketSyncSecurity(placer.getUUID()));
                }
            }
        }
    }

    @Override
    public void onBlockExploded(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Explosion explosion) {
        if (!world.isClientSide) {
            AttributeMultiblock multiblockAttribute = Attribute.get(state, AttributeMultiblock.class);
            if (multiblockAttribute != null && explosion instanceof MeltdownExplosion meltdown) {
                MultiblockData multiblock = multiblockAttribute.getMultiblock(world, pos, meltdown.getMultiblockID());
                if (multiblock != null) {
                    multiblock.meltdownHappened(world);
                }
            }
        }
        super.onBlockExploded(state, world, pos, explosion);
    }

    @NotNull
    @Override
    public BlockState rotate(@NotNull BlockState state, @NotNull LevelAccessor world, @NotNull BlockPos pos, @NotNull Rotation rotation) {
        return AttributeStateFacing.rotate(state, world, pos, rotation);
    }

    @NotNull
    @Override
    protected BlockState rotate(@NotNull BlockState state, @NotNull Rotation rotation) {
        return AttributeStateFacing.rotate(state, rotation);
    }

    @NotNull
    @Override
    protected BlockState mirror(@NotNull BlockState state, @NotNull Mirror mirror) {
        return AttributeStateFacing.mirror(state, mirror);
    }

    @Override
    protected boolean hasAnalogOutputSignal(@NotNull BlockState blockState) {
        return Attribute.has(blockState.getBlockHolder(), AttributeComparator.class);
    }

    @Override
    protected int getAnalogOutputSignal(@NotNull BlockState blockState, @NotNull Level world, @NotNull BlockPos pos) {
        if (hasAnalogOutputSignal(blockState)) {
            BlockEntity tile = WorldUtils.getTileEntity(world, pos);
            //Double-check the tile actually has comparator support
            if (tile instanceof IComparatorSupport comparatorTile && comparatorTile.supportsComparator()) {
                return comparatorTile.getCurrentRedstoneLevel();
            }
        }
        return Redstone.SIGNAL_NONE;
    }

    @Override
    protected float getDestroyProgress(@NotNull BlockState state, @NotNull Player player, @NotNull BlockGetter blockGetter, @NotNull BlockPos pos) {
        return getDestroyProgress(state, player, blockGetter, pos, state.hasBlockEntity() ? WorldUtils.getTileEntity(blockGetter, pos) : null);
    }

    /**
     * Like {@link BlockBehaviour#getDestroyProgress(BlockState, Player, BlockGetter, BlockPos)} except also passes the tile to only have to get it once.
     */
    protected float getDestroyProgress(@NotNull BlockState state, @NotNull Player player, @NotNull BlockGetter blockGetter, @NotNull BlockPos pos,
          @Nullable BlockEntity tile) {
        Level level = tile == null ? null : tile.getLevel();
        //Do our best effort to see if we can figure out a corresponding level to look up the security from
        if (level == null && blockGetter instanceof Level) {
            level = (Level) blockGetter;
        }
        if (level != null && !IBlockSecurityUtils.INSTANCE.canAccess(player, level, pos, state, tile)) {
            //If we have a level and the player cannot access the block, don't allow the player to break the block
            return 0.0F;
        }
        //Call super variant of player relative hardness to get default
        float speed = super.getDestroyProgress(state, player, blockGetter, pos);
        if (RadiationManager.isGlobalRadiationEnabled() && tile instanceof ITileRadioactive radioactiveTile && radioactiveTile.getRadiationScale() > 0) {
            //Our tile has some radioactive substance in it; slow down breaking it
            //Note: Technically our getRadiationScale impls validate that radiation is enabled, but we do so here as well
            // to make intentions clearer
            return speed / 5F;
        }
        return speed;
    }

    @Override
    public void animateTick(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull RandomSource random) {
        super.animateTick(state, world, pos, random);
        if (RadiationManager.isGlobalRadiationEnabled()) {//Skip getting the tile if radiation is disabled in the config
            BlockEntity tile = WorldUtils.getTileEntity(world, pos);
            if (tile instanceof ITileRadioactive radioactiveTile) {
                int count = radioactiveTile.getRadiationParticleCount();
                if (count > 0) {
                    //Update count to be randomized but store it instead of calculating our max number each time we loop
                    count = random.nextInt(count);
                    for (int i = 0; i < count; i++) {
                        double randX = pos.getX() - 0.1 + random.nextDouble() * 1.2;
                        double randY = pos.getY() - 0.1 + random.nextDouble() * 1.2;
                        double randZ = pos.getZ() - 0.1 + random.nextDouble() * 1.2;
                        world.addParticle(MekanismParticleTypes.RADIATION.get(), randX, randY, randZ, 0, 0, 0);
                    }
                }
            }
        }
    }

    protected ItemInteractionResult genericClientActivated(ItemStack stack, BlockEntity blockEntity) {
        if (!Attribute.has(this, AttributeGui.class) && MekanismUtils.canUseAsWrench(stack)) {
            if (blockEntity instanceof ITileRadioactive tileRadioactive && tileRadioactive.getRadiationScale() > 0) {
                return ItemInteractionResult.FAIL;
            }
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}