package mekanism.additions.common.block.plastic;

import java.util.Optional;
import java.util.function.UnaryOperator;
import mekanism.additions.common.block.IStateExtendedFluidLoggable;
import mekanism.api.text.EnumColor;
import mekanism.common.block.interfaces.IColoredBlock;
import mekanism.common.block.states.BlockStateHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;

public class BlockPlasticSlab extends SlabBlock implements IColoredBlock, IStateExtendedFluidLoggable {

    private final EnumColor color;

    public BlockPlasticSlab(EnumColor color, UnaryOperator<Properties> propertyModifier) {
        super(BlockStateHelper.applyLightLevelAdjustments(propertyModifier.apply(BlockBehaviour.Properties.of().mapColor(color.getMapColor())
              .strength(5, 6))));
        this.color = color;
        //Uses getDefaultState as starting state to take into account the stuff from super
        registerDefaultState(BlockStateHelper.getDefaultState(defaultBlockState()));
    }

    @Override
    public EnumColor getColor() {
        return color;
    }

    @Override
    protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        BlockStateHelper.fillBlockStateContainer(this, builder);
    }

    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return BlockStateHelper.getStateForPlacement(super.getStateForPlacement(context), context);
    }

    @NotNull
    @Override
    protected FluidState getFluidState(@NotNull BlockState state) {
        return getFluid(state);
    }

    @Override
    public boolean placeLiquid(@NotNull LevelAccessor world, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull FluidState fluidState) {
        return state.getValue(TYPE) != SlabType.DOUBLE && IStateExtendedFluidLoggable.super.placeLiquid(world, pos, state, fluidState);
    }

    @Override
    public boolean canPlaceLiquid(Player player, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Fluid fluid) {
        return state.getValue(TYPE) != SlabType.DOUBLE && IStateExtendedFluidLoggable.super.canPlaceLiquid(player, world, pos, state, fluid);
    }

    @NotNull
    @Override
    protected BlockState updateShape(@NotNull BlockState state, @NotNull Direction facing, @NotNull BlockState facingState, @NotNull LevelAccessor world,
          @NotNull BlockPos currentPos, @NotNull BlockPos facingPos) {
        updateFluids(state, world, currentPos);
        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    @NotNull
    @Override
    public ItemStack pickupBlock(Player player, @NotNull LevelAccessor world, @NotNull BlockPos pos, @NotNull BlockState state) {
        //Manually declare which pickupBlock we want to be using
        return IStateExtendedFluidLoggable.super.pickupBlock(player, world, pos, state);
    }

    @NotNull
    @Override
    public Optional<SoundEvent> getPickupSound() {
        //Manually declare which getPickupSound we want to be using
        return IStateExtendedFluidLoggable.super.getPickupSound();
    }

    @NotNull
    @Override
    public Optional<SoundEvent> getPickupSound(@NotNull BlockState state) {
        //Manually declare which getPickupSound we want to be using
        return IStateExtendedFluidLoggable.super.getPickupSound(state);
    }
}