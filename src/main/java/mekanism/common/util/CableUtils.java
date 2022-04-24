package mekanism.common.util;

import java.util.EnumSet;
import java.util.Set;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.common.content.network.distribution.EnergyAcceptorTarget;
import mekanism.common.integration.energy.EnergyCompatUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class CableUtils {

    private CableUtils() {
    }

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
        EmitUtils.forEachSide(from.getLevel(), from.getBlockPos(), sides, (acceptor, side) -> {
            //Insert to access side and collect the cap if it is present
            EnergyCompatUtils.getLazyStrictEnergyHandler(acceptor, side.getOpposite()).ifPresent(target::addHandler);
        });
        if (target.getHandlerCount() > 0) {
            return EmitUtils.sendToAcceptors(target, energyToSend);
        }
        return FloatingLong.ZERO;
    }
}