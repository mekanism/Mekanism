package mekanism.common.util;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import mekanism.common.content.network.distribution.EnergyAcceptorTarget;
import mekanism.common.integration.energy.EnergyCompatUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class CableUtils {

    private CableUtils() {
    }

    //TODO: Move these emit methods to using capability caches. Will require a custom cache that interacts with EnergyCompatUtils
    public static void emit(IEnergyContainer energyContainer, BlockEntity from) {
        emit(EnumSet.allOf(Direction.class), energyContainer, from);
    }

    public static void emit(Set<Direction> outputSides, IEnergyContainer energyContainer, BlockEntity from) {
        emit(outputSides, energyContainer, from, energyContainer.getMaxEnergy());
    }

    public static void emit(Set<Direction> outputSides, IEnergyContainer energyContainer, BlockEntity from, FloatingLong maxOutput) {
        if (!energyContainer.isEmpty() && !maxOutput.isZero()) {
            energyContainer.extract(emit(outputSides, energyContainer.extract(maxOutput, Action.SIMULATE, AutomationType.INTERNAL), from), Action.EXECUTE, AutomationType.INTERNAL);
        }
    }

    /**
     * Emits energy from a central block by splitting the received stack among the sides given.
     *
     * @param sides        - the list of sides to output from
     * @param energyToSend - the energy to output
     * @param from         - the TileEntity to output from
     *
     * @return the amount of energy emitted
     */
    public static FloatingLong emit(Set<Direction> sides, FloatingLong energyToSend, BlockEntity from) {
        if (energyToSend.isZero() || sides.isEmpty()) {
            return FloatingLong.ZERO;
        }
        EnergyAcceptorTarget target = new EnergyAcceptorTarget(6);
        Level level = from.getLevel();
        BlockPos center = from.getBlockPos();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (Direction side : sides) {
            pos.setWithOffset(center, side);
            //Insert to access side and collect the cap if it is present
            Optional<BlockState> blockState = WorldUtils.getBlockState(level, pos);
            if (blockState.isPresent()) {
                BlockState state = blockState.get();
                if (!state.isAir()) {
                    //Note: We already know the position is loaded from our get blockstate call, so we can just directly query the level
                    BlockEntity tile = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
                    IStrictEnergyHandler handler = EnergyCompatUtils.getStrictEnergyHandler(level, pos, state, tile, side.getOpposite());
                    if (handler != null) {
                        target.addHandler(handler);
                    }
                }
            }
        }
        if (target.getHandlerCount() > 0) {
            return EmitUtils.sendToAcceptors(target, energyToSend);
        }
        return FloatingLong.ZERO;
    }
}