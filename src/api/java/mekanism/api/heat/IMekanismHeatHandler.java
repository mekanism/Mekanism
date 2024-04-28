package mekanism.api.heat;

import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public interface IMekanismHeatHandler extends ISidedHeatHandler, IContentsListener {

    /**
     * Used to check if an instance of {@link IMekanismHeatHandler} actually has the ability to handle heat.
     *
     * @return True if we are actually capable of handling heat.
     *
     * @apiNote If for some reason you are comparing to {@link IMekanismHeatHandler} without having gotten the object via the heat handler capability, then you must call
     * this method to make sure that it really can handle heat. As most mekanism tiles have this class in their hierarchy.
     * @implNote If this returns false the capability should not be exposed AND methods should turn reasonable defaults for not doing anything.
     */
    default boolean canHandleHeat() {
        return true;
    }

    @Override
    default int getHeatCapacitorCount(@Nullable Direction side) {
        return getHeatCapacitors(side).size();
    }

    /**
     * Returns the list of IHeatCapacitors that this heat handler exposes on the given side.
     *
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The list of all IHeatCapacitors that this {@link IMekanismHeatHandler} contains for the given side. If there are no capacitors for the side or
     * {@link #canHandleHeat()} is false then it returns an empty list.
     *
     * @implNote When side is null (an internal request), this method <em>MUST</em> return all capacitors in the handler. Additionally, if {@link #canHandleHeat()} is
     * false, this <em>MUST</em> return an empty list.
     */
    List<IHeatCapacitor> getHeatCapacitors(@Nullable Direction side);

    /**
     * Returns the {@link IHeatCapacitor} that has the given index from the list of capacitors on the given side.
     *
     * @param capacitor The index of the capacitor to retrieve.
     * @param side      The side we are interacting with the handler from (null for internal).
     *
     * @return The {@link IHeatCapacitor} that has the given index from the list of capacitors on the given side.
     */
    @Nullable
    default IHeatCapacitor getHeatCapacitor(int capacitor, @Nullable Direction side) {
        List<IHeatCapacitor> capacitors = getHeatCapacitors(side);
        return capacitor >= 0 && capacitor < capacitors.size() ? capacitors.get(capacitor) : null;
    }

    @Override
    default double getTemperature(int capacitor, @Nullable Direction side) {
        IHeatCapacitor heatCapacitor = getHeatCapacitor(capacitor, side);
        return heatCapacitor == null ? HeatAPI.AMBIENT_TEMP : heatCapacitor.getTemperature();
    }

    @Override
    default double getInverseConduction(int capacitor, @Nullable Direction side) {
        IHeatCapacitor heatCapacitor = getHeatCapacitor(capacitor, side);
        return heatCapacitor == null ? HeatAPI.DEFAULT_INVERSE_CONDUCTION : heatCapacitor.getInverseConduction();
    }

    @Override
    default double getHeatCapacity(int capacitor, @Nullable Direction side) {
        IHeatCapacitor heatCapacitor = getHeatCapacitor(capacitor, side);
        return heatCapacitor == null ? HeatAPI.DEFAULT_HEAT_CAPACITY : heatCapacitor.getHeatCapacity();
    }

    @Override
    default void handleHeat(int capacitor, double transfer, @Nullable Direction side) {
        IHeatCapacitor heatCapacitor = getHeatCapacitor(capacitor, side);
        if (heatCapacitor != null) {
            heatCapacitor.handleHeat(transfer);
        }
    }

    /**
     * Returns the inverse insulation coefficient of a given capacitor. The larger the value the less heat dissipates into the environment.
     *
     * @param capacitor Capacitor to query.
     * @param side      The side we are interacting with the handler from (null for internal).
     *
     * @return Inverse insulation coefficient of a given capacitor.
     */
    default double getInverseInsulation(int capacitor, @Nullable Direction side) {
        IHeatCapacitor heatCapacitor = getHeatCapacitor(capacitor, side);
        return heatCapacitor == null ? HeatAPI.DEFAULT_INVERSE_INSULATION : heatCapacitor.getInverseInsulation();
    }

    /**
     * Calculates the total inverse insulation coefficient across all capacitors in this handler.
     *
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The total inverse insulation coefficient across all capacitors in this handler.
     */
    default double getTotalInverseInsulation(@Nullable Direction side) {
        List<IHeatCapacitor> heatCapacitors = getHeatCapacitors(side);
        if (heatCapacitors.isEmpty()) {
            return HeatAPI.DEFAULT_INVERSE_INSULATION;
        } else if (heatCapacitors.size() == 1) {
            return heatCapacitors.getFirst().getInverseInsulation();
        }
        double sum = 0;
        double totalCapacity = getTotalHeatCapacity(heatCapacitors);
        for (IHeatCapacitor capacitor : heatCapacitors) {
            sum += capacitor.getInverseInsulation() * (capacitor.getHeatCapacity() / totalCapacity);
        }
        return sum;
    }

    @Override
    default double getTotalTemperature(@Nullable Direction side) {
        List<IHeatCapacitor> heatCapacitors = getHeatCapacitors(side);
        if (heatCapacitors.isEmpty()) {
            return 0;
        } else if (heatCapacitors.size() == 1) {
            return heatCapacitors.getFirst().getTemperature();
        }
        double sum = 0;
        double totalCapacity = getTotalHeatCapacity(heatCapacitors);
        for (IHeatCapacitor capacitor : heatCapacitors) {
            sum += capacitor.getTemperature() * (capacitor.getHeatCapacity() / totalCapacity);
        }
        return sum;
    }

    @Override
    default double getTotalInverseConductionCoefficient(@Nullable Direction side) {
        List<IHeatCapacitor> heatCapacitors = getHeatCapacitors(side);
        if (heatCapacitors.isEmpty()) {
            return HeatAPI.DEFAULT_INVERSE_CONDUCTION;
        } else if (heatCapacitors.size() == 1) {
            return heatCapacitors.getFirst().getInverseConduction();
        }
        double sum = 0;
        double totalCapacity = getTotalHeatCapacity(heatCapacitors);
        for (IHeatCapacitor capacitor : heatCapacitors) {
            sum += capacitor.getInverseConduction() * (capacitor.getHeatCapacity() / totalCapacity);
        }
        return sum;
    }

    @Override
    default double getTotalHeatCapacity(@Nullable Direction side) {
        return getTotalHeatCapacity(getHeatCapacitors(side));
    }

    private double getTotalHeatCapacity(List<IHeatCapacitor> capacitors) {
        if (capacitors.size() == 1) {
            return capacitors.getFirst().getHeatCapacity();
        }
        double sum = 0;
        for (IHeatCapacitor capacitor : capacitors) {
            sum += capacitor.getHeatCapacity();
        }
        return sum;
    }

    @Override
    default void handleHeat(double transfer, @Nullable Direction side) {
        List<IHeatCapacitor> heatCapacitors = getHeatCapacitors(side);
        if (heatCapacitors.size() == 1) {
            heatCapacitors.getFirst().handleHeat(transfer);
        } else if (!heatCapacitors.isEmpty()) {
            double totalHeatCapacity = getTotalHeatCapacity(heatCapacitors);
            for (IHeatCapacitor heatCapacitor : heatCapacitors) {
                heatCapacitor.handleHeat(transfer * (heatCapacitor.getHeatCapacity() / totalHeatCapacity));
            }
        }
    }
}
