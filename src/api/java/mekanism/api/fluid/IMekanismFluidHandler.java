package mekanism.api.fluid;

import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public interface IMekanismFluidHandler extends ISidedFluidHandler, IContentsListener {

    /**
     * Used to check if an instance of {@link IMekanismFluidHandler} actually has the ability to handle fluid.
     *
     * @return True if we are actually capable of handling fluid.
     *
     * @apiNote If for some reason you are comparing to {@link IMekanismFluidHandler} without having gotten the object via the fluid handler capability, then you must
     * call this method to make sure that it really can handle fluid. As most mekanism tiles have this class in their hierarchy.
     * @implNote If this returns false the capability should not be exposed AND methods should turn reasonable defaults for not doing anything.
     */
    default boolean canHandleFluid() {
        return true;
    }

    /**
     * Returns the list of IExtendedFluidTanks that this fluid handler exposes on the given side.
     *
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The list of all IExtendedFluidTanks that this {@link IMekanismFluidHandler} contains for the given side. If there are no tanks for the side or
     * {@link #canHandleFluid()} is false then it returns an empty list.
     *
     * @implNote When side is null (an internal request), this method <em>MUST</em> return all tanks in the handler. Additionally, if {@link #canHandleFluid()} is false,
     * this <em>MUST</em> return an empty list.
     */
    List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side);

    /**
     * Returns the {@link IExtendedFluidTank} that has the given index from the list of tanks on the given side.
     *
     * @param tank The index of the tank to retrieve.
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The {@link IExtendedFluidTank} that has the given index from the list of tanks on the given side.
     */
    @Nullable
    default IExtendedFluidTank getFluidTank(int tank, @Nullable Direction side) {
        List<IExtendedFluidTank> tanks = getFluidTanks(side);
        return tank >= 0 && tank < tanks.size() ? tanks.get(tank) : null;
    }

    @Override
    default int getTanks(@Nullable Direction side) {
        return getFluidTanks(side).size();
    }

    @Override
    default FluidStack getFluidInTank(int tank, @Nullable Direction side) {
        IExtendedFluidTank fluidTank = getFluidTank(tank, side);
        return fluidTank == null ? FluidStack.EMPTY : fluidTank.getFluid();
    }

    @Override
    default void setFluidInTank(int tank, FluidStack stack, @Nullable Direction side) {
        IExtendedFluidTank fluidTank = getFluidTank(tank, side);
        if (fluidTank != null) {
            fluidTank.setStack(stack);
        }
    }

    @Override
    default int getTankCapacity(int tank, @Nullable Direction side) {
        IExtendedFluidTank fluidTank = getFluidTank(tank, side);
        return fluidTank == null ? 0 : fluidTank.getCapacity();
    }

    @Override
    default boolean isFluidValid(int tank, FluidStack stack, @Nullable Direction side) {
        IExtendedFluidTank fluidTank = getFluidTank(tank, side);
        return fluidTank != null && fluidTank.isFluidValid(stack);
    }

    /**
     * @implNote Any overrides to this should also override {@link #insertFluid(FluidStack, Direction, Action)} as it bypasses calling this method in order to skip
     * looking up the containers for every sub operation.
     */
    @Override
    default FluidStack insertFluid(int tank, FluidStack stack, @Nullable Direction side, Action action) {
        IExtendedFluidTank fluidTank = getFluidTank(tank, side);
        return fluidTank == null ? stack : fluidTank.insert(stack, action, AutomationType.handler(side));
    }

    /**
     * @implNote Any overrides to this should also override {@link #extractFluid(int, Direction, Action)} and {@link #extractFluid(FluidStack, Direction, Action)} as they
     * bypass calling this method in order to skip looking up the containers for every sub operation.
     */
    @Override
    default FluidStack extractFluid(int tank, int amount, @Nullable Direction side, Action action) {
        IExtendedFluidTank fluidTank = getFluidTank(tank, side);
        return fluidTank == null ? FluidStack.EMPTY : fluidTank.extract(amount, action, AutomationType.handler(side));
    }

    @Override
    default FluidStack insertFluid(FluidStack stack, @Nullable Direction side, Action action) {
        return ExtendedFluidHandlerUtils.insert(stack, side, this::getFluidTanks, action, AutomationType.handler(side));
    }

    @Override
    default FluidStack extractFluid(int amount, @Nullable Direction side, Action action) {
        return ExtendedFluidHandlerUtils.extract(amount, side, this::getFluidTanks, action, AutomationType.handler(side));
    }

    @Override
    default FluidStack extractFluid(FluidStack stack, @Nullable Direction side, Action action) {
        return ExtendedFluidHandlerUtils.extract(stack, side, this::getFluidTanks, action, AutomationType.handler(side));
    }
}