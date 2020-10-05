package mekanism.common.util;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.EnumSet;
import java.util.Set;
import mekanism.api.Action;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.common.content.network.distribution.EnergyAcceptorTarget;
import mekanism.common.integration.energy.EnergyCompatUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;

public final class CableUtils {

    private CableUtils() {
    }

    public static void emit(IEnergyContainer energyContainer, TileEntity from) {
        emit(EnumSet.allOf(Direction.class), energyContainer, from);
    }

    public static void emit(Set<Direction> outputSides, IEnergyContainer energyContainer, TileEntity from) {
        emit(outputSides, energyContainer, from, energyContainer.getMaxEnergy());
    }

    public static void emit(Set<Direction> outputSides, IEnergyContainer energyContainer, TileEntity from, FloatingLong maxOutput) {
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
    public static FloatingLong emit(Set<Direction> sides, FloatingLong energyToSend, TileEntity from) {
        if (energyToSend.isZero() || sides.isEmpty()) {
            return FloatingLong.ZERO;
        }
        //Fake that we have one target given we know that no sides will overlap This allows us to have slightly better performance
        EnergyAcceptorTarget target = new EnergyAcceptorTarget();
        EmitUtils.forEachSide(from.getWorld(), from.getPos(), sides, (acceptor, side) -> {
            //Insert to access side
            Direction accessSide = side.getOpposite();
            //Collect cap
            EnergyCompatUtils.getLazyStrictEnergyHandler(acceptor, accessSide).ifPresent(strictEnergyHandler -> target.addHandler(accessSide, strictEnergyHandler));
        });

        int curHandlers = target.getHandlers().size();
        if (curHandlers > 0) {
            Set<EnergyAcceptorTarget> targets = new ObjectOpenHashSet<>();
            targets.add(target);
            return EmitUtils.sendToAcceptors(targets, curHandlers, energyToSend);
        }
        return FloatingLong.ZERO;
    }
}