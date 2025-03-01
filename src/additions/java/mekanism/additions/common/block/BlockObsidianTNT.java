package mekanism.additions.common.block;

import mekanism.additions.common.entity.EntityObsidianTNT;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.util.VoxelShapeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockObsidianTNT extends TntBlock implements IStateFluidLoggable {

    private static final VoxelShape bounds = VoxelShapeUtils.combine(
          box(0, 0, 0, 16, 3, 16),//Wooden1
          box(0, 8, 0, 16, 11, 16),//Wooden2
          box(12.5, 11.8, 12.5, 13.5, 13.8, 13.5),//Wick1
          box(12.5, 11.5, 7.5, 13.5, 13.5, 8.5),//Wick2
          box(12.5, 11.8, 2.5, 13.5, 13.8, 3.5),//Wick3
          box(2.5, 11.8, 12.5, 3.5, 13.8, 13.5),//Wick4
          box(2.5, 11.5, 7.5, 3.5, 13.5, 8.5),//Wick5
          box(2.5, 11.8, 2.5, 3.5, 13.8, 3.5),//Wick6
          box(7.5, 11.5, 12.5, 8.5, 13.5, 13.5),//Wick7
          box(7.5, 11.5, 2.5, 8.5, 13.5, 3.5),//Wick8
          box(7.5, 11.8, 7.5, 8.5, 13.8, 8.5),//Wick9
          box(11, -1, 11, 15, 12, 15),//Rod1
          box(11, -1, 6, 15, 12, 10),//Rod2
          box(11, -1, 1, 15, 12, 5),//Rod3
          box(6, -1, 1, 10, 12, 5),//Rod4
          box(6, -1, 6, 10, 12, 10),//Rod5
          box(6, -1, 11, 10, 12, 15),//Rod6
          box(1, -1, 6, 5, 12, 10),//Rod7
          box(1, -1, 11, 5, 12, 15),//Rod8
          box(1, -1, 1, 5, 12, 5)//Rod9
    );

    public BlockObsidianTNT() {
        super(BlockStateHelper.applyLightLevelAdjustments(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).ignitedByLava()));
        //Uses getDefaultState as starting state to take into account the stuff from super
        registerDefaultState(BlockStateHelper.getDefaultState(defaultBlockState()));
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
    public int getFlammability(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull Direction face) {
        //300 is 100% chance fire will spread to this block, 100 is default for TNT
        // Given we are "obsidian" make ours slightly more stable against fire being spread than vanilla TNT
        return 75;
    }

    @Override
    public void onCaughtFire(@NotNull BlockState state, Level world, @NotNull BlockPos pos, @Nullable Direction side, @Nullable LivingEntity igniter) {
        if (!world.isClientSide && createAndAddEntity(world, pos, igniter)) {
            world.gameEvent(igniter, GameEvent.PRIME_FUSE, pos);
        }
    }

    @Override
    public void wasExploded(Level world, @NotNull BlockPos pos, @NotNull Explosion explosion) {
        if (!world.isClientSide) {
            PrimedTnt tnt = EntityObsidianTNT.create(world, pos.getX() + 0.5F, pos.getY(), pos.getZ() + 0.5F, explosion.getIndirectSourceEntity());
            if (tnt != null) {
                tnt.setFuse((short) (world.random.nextInt(tnt.getFuse() / 4) + tnt.getFuse() / 8));
                world.addFreshEntity(tnt);
            }
        }
    }

    @Override
    protected boolean isPathfindable(@NotNull BlockState state, @NotNull PathComputationType type) {
        return false;
    }

    @NotNull
    @Override
    protected VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return bounds;
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

    public static boolean createAndAddEntity(@NotNull Level world, @NotNull BlockPos pos, @Nullable LivingEntity igniter) {
        PrimedTnt tnt = EntityObsidianTNT.create(world, pos.getX() + 0.5F, pos.getY(), pos.getZ() + 0.5F, igniter);
        if (tnt != null) {
            world.addFreshEntity(tnt);
            world.playSound(null, tnt.getX(), tnt.getY(), tnt.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS);
            return true;
        }
        return false;
    }
}