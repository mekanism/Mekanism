package mekanism.api.chemical.gas;

import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.inventory.AutomationType;
import net.minecraft.util.Direction;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IMekanismGasHandler extends ISidedGasHandler {

    /**
     * Used to check if an instance of {@link IMekanismGasHandler} actually has the ability to handle gas.
     *
     * @return True if we are actually capable of handling gas.
     *
     * @apiNote If for some reason you are comparing to {@link IMekanismGasHandler} without having gotten the object via the gas handler capability, then you must call
     * this method to make sure that it really can handle gas. As most mekanism tiles have this class in their hierarchy.
     * @implNote If this returns false the capability should not be exposed AND methods should turn reasonable defaults for not doing anything.
     */
    default boolean canHandleGas() {
        return true;
    }

    /**
     * Returns the list of IGasTanks that this gas handler exposes on the given side.
     *
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The list of all IGasTanks that this {@link IMekanismGasHandler} contains for the given side. If there are no tanks for the side or {@link
     * #canHandleGas()} is false then it returns an empty list.
     *
     * @implNote When side is null (an internal request), this method <em>MUST</em> return all tanks in the handler. Additionally, if {@link #canHandleGas()} is false,
     * this <em>MUST</em> return an empty list.
     */
    List<IGasTank> getGasTanks(@Nullable Direction side);

    /**
     * Called when the contents of this gas handler change.
     */
    void onContentsChanged();

    /**
     * Returns the {@link IGasTank} that has the given index from the list of tanks on the given side.
     *
     * @param tank The index of the tank to retrieve.
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The {@link IGasTank} that has the given index from the list of tanks on the given side.
     */
    @Nullable
    default IGasTank getGasTank(int tank, @Nullable Direction side) {
        List<IGasTank> tanks = getGasTanks(side);
        return tank >= 0 && tank < tanks.size() ? tanks.get(tank) : null;
    }

    @Override
    default int getGasTankCount(@Nullable Direction side) {
        return getGasTanks(side).size();
    }

    @Override
    default GasStack getGasInTank(int tank, @Nullable Direction side) {
        IGasTank gasTank = getGasTank(tank, side);
        return gasTank == null ? GasStack.EMPTY : gasTank.getStack();
    }

    @Override
    default void setGasInTank(int tank, GasStack stack, @Nullable Direction side) {
        IGasTank gasTank = getGasTank(tank, side);
        if (gasTank != null) {
            gasTank.setStack(stack);
        }
    }

    @Override
    default long getGasTankCapacity(int tank, @Nullable Direction side) {
        IGasTank gasTank = getGasTank(tank, side);
        return gasTank == null ? 0 : gasTank.getCapacity();
    }

    @Override
    default boolean isGasValid(int tank, GasStack stack, @Nullable Direction side) {
        IGasTank gasTank = getGasTank(tank, side);
        return gasTank != null && gasTank.isValid(stack);
    }

    @Override
    default GasStack insertGas(int tank, GasStack stack, @Nullable Direction side, Action action) {
        IGasTank gasTank = getGasTank(tank, side);
        return gasTank == null ? stack : gasTank.insert(stack, action, side == null ? AutomationType.INTERNAL : AutomationType.EXTERNAL);
    }

    @Override
    default GasStack extractGas(int tank, long amount, @Nullable Direction side, Action action) {
        IGasTank gasTank = getGasTank(tank, side);
        return gasTank == null ? GasStack.EMPTY : gasTank.extract(amount, action, side == null ? AutomationType.INTERNAL : AutomationType.EXTERNAL);
    }
}