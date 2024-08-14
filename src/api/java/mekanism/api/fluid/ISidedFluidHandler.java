package mekanism.api.fluid;

import mekanism.api.Action;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

/**
 * A sided variant of {@link IExtendedFluidHandler}
 */
@NothingNullByDefault
public interface ISidedFluidHandler extends IExtendedFluidHandler {

    /**
     * The side this {@link ISidedFluidHandler} is for. This defaults to null, which is for internal use.
     *
     * @return The default side to use for the normal {@link IExtendedFluidHandler} methods when wrapping them into {@link ISidedFluidHandler} methods.
     */
    @Nullable
    default Direction getFluidSideFor() {
        return null;
    }

    /**
     * A sided variant of {@link IFluidHandler#getTanks()}, docs copied for convenience.
     * <p>
     * Returns the number of fluid storage units ("tanks") available
     *
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The number of tanks available
     */
    int getTanks(@Nullable Direction side);

    @Override
    default int getTanks() {
        return getTanks(getFluidSideFor());
    }

    /**
     * A sided variant of {@link IFluidHandler#getFluidInTank(int)}, docs copied for convenience.
     * <p>
     * Returns the {@link FluidStack} in a given tank.
     *
     * <p>
     * <strong>IMPORTANT:</strong> This {@link FluidStack} <em>MUST NOT</em> be modified. This method is not for altering internal contents. Any implementers who are
     * able to detect modification via this method should throw an exception. It is ENTIRELY reasonable and likely that the stack returned here will be a copy.
     * </p>
     *
     * <p>
     * <strong><em>SERIOUSLY: DO NOT MODIFY THE RETURNED FLUID STACK</em></strong>
     * </p>
     *
     * @param tank Tank to query.
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return {@link FluidStack} in a given tank. {@link FluidStack#EMPTY} if the tank is empty.
     */
    FluidStack getFluidInTank(int tank, @Nullable Direction side);

    @Override
    default FluidStack getFluidInTank(int tank) {
        return getFluidInTank(tank, getFluidSideFor());
    }

    /**
     * A sided variant of {@link IExtendedFluidHandler#setFluidInTank(int, FluidStack)}, docs copied for convenience.
     * <p>
     * Overrides the stack in the given tank. This method may throw an error if it is called unexpectedly.
     *
     * @param tank  Tank to modify
     * @param stack {@link FluidStack} to set tank to (may be empty).
     * @param side  The side we are interacting with the handler from (null for internal).
     *
     * @throws RuntimeException if the handler is called in a way that the handler was not expecting.
     **/
    void setFluidInTank(int tank, FluidStack stack, @Nullable Direction side);

    @Override
    default void setFluidInTank(int tank, FluidStack stack) {
        setFluidInTank(tank, stack, getFluidSideFor());
    }

    /**
     * A sided variant of {@link IExtendedFluidHandler#getTankCapacity(int)}, docs copied for convenience.
     * <p>
     * Retrieves the maximum amount of fluid that can be stored in a given tank.
     *
     * @param tank Tank to query.
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The maximum fluid amount held by the tank.
     */
    int getTankCapacity(int tank, @Nullable Direction side);

    @Override
    default int getTankCapacity(int tank) {
        return getTankCapacity(tank, getFluidSideFor());
    }

    /**
     * A sided variant of {@link IFluidHandler#isFluidValid(int, FluidStack)}, docs copied for convenience.
     *
     * <p>
     * This function should be used instead of simulated insertions in cases where the contents and state of the tank are irrelevant, mainly for the purpose of automation
     * and logic.
     * </p>
     * <ul>
     * <li>isFluidValid is false when insertion of the fluid is never valid.</li>
     * <li>When isFluidValid is true, no assumptions can be made and insertion must be simulated case-by-case.</li>
     * <li>The actual fluid in the tank, its fullness, or any other state are <strong>not</strong> considered by isFluidValid.</li>
     * </ul>
     *
     * @param tank  Tank to query.
     * @param stack Stack to test with for validity @param side   The side we are interacting with the handler from (null for internal).
     *
     * @return true if the tank can accept the {@link FluidStack}, not considering the current state of the tank. false if the tank can never support the given
     * {@link FluidStack} in any situation.
     */
    boolean isFluidValid(int tank, FluidStack stack, @Nullable Direction side);

    @Override
    default boolean isFluidValid(int tank, FluidStack stack) {
        return isFluidValid(tank, stack, getFluidSideFor());
    }

    /**
     * A sided variant of {@link IExtendedFluidHandler#insertFluid(int, FluidStack, Action)}, docs copied for convenience.
     *
     * <p>
     * Inserts a {@link FluidStack} into a given tank and return the remainder. The {@link FluidStack} <em>should not</em> be modified in this function!
     * </p>
     * Note: This behaviour is subtly <strong>different</strong> from {@link IFluidHandler#fill(FluidStack, IFluidHandler.FluidAction)}
     *
     * @param tank   Tank to insert to.
     * @param stack  {@link FluidStack} to insert. This must not be modified by the tank.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param side   The side we are interacting with the handler from (null for internal).
     *
     * @return The remaining {@link FluidStack} that was not inserted (if the entire stack is accepted, then return an empty {@link FluidStack}). May be the same as the
     * input {@link FluidStack} if unchanged, otherwise a new {@link FluidStack}. The returned {@link FluidStack} can be safely modified after
     */
    FluidStack insertFluid(int tank, FluidStack stack, @Nullable Direction side, Action action);

    @Override
    default FluidStack insertFluid(int tank, FluidStack stack, Action action) {
        return insertFluid(tank, stack, getFluidSideFor(), action);
    }

    /**
     * A sided variant of {@link IExtendedFluidHandler#extractFluid(int, int, Action)}, docs copied for convenience.
     * <p>
     * Extracts a {@link FluidStack} from a specific tank in this handler.
     * <p>
     * The returned value must be empty if nothing is extracted, otherwise its stack size must be less than or equal to {@code amount}.
     * </p>
     *
     * @param tank   Tank to extract from.
     * @param amount Amount to extract (may be greater than the current stack's amount or the tank's capacity)
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param side   The side we are interacting with the handler from (null for internal).
     *
     * @return {@link FluidStack} extracted from the tank, must be empty if nothing can be extracted. The returned {@link FluidStack} can be safely modified after, so the
     * tank should return a new or copied stack.
     */
    FluidStack extractFluid(int tank, int amount, @Nullable Direction side, Action action);

    @Override
    default FluidStack extractFluid(int tank, int amount, Action action) {
        return extractFluid(tank, amount, getFluidSideFor(), action);
    }

    /**
     * A sided variant of {@link IExtendedFluidHandler#insertFluid(FluidStack, Action)}, docs copied for convenience.
     *
     * <p>
     * Inserts a {@link FluidStack} into this handler, distribution is left <strong>entirely</strong> to this {@link IExtendedFluidHandler}. The {@link FluidStack}
     * <em>should not</em> be modified in this function!
     * </p>
     * Note: This behaviour is subtly <strong>different</strong> from {@link IFluidHandler#fill(FluidStack, IFluidHandler.FluidAction)}
     *
     * @param stack  {@link FluidStack} to insert. This must not be modified by the handler.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param side   The side we are interacting with the handler from (null for internal).
     *
     * @return The remaining {@link FluidStack} that was not inserted (if the entire stack is accepted, then return an empty {@link FluidStack}). May be the same as the
     * input {@link FluidStack} if unchanged, otherwise a new {@link FluidStack}. The returned {@link FluidStack} can be safely modified after
     *
     * @implNote The default implementation of this method, attempts to insert into tanks that contain the same type of fluid as the supplied type, and if it will not all
     * fit, falls back to inserting into any empty tanks.
     * @apiNote It is not guaranteed that the default implementation will be how this {@link IExtendedFluidHandler} ends up distributing the insertion.
     */
    default FluidStack insertFluid(FluidStack stack, @Nullable Direction side, Action action) {
        return ExtendedFluidHandlerUtils.insert(stack, side, action, this::getTanks, this::getFluidInTank, this::insertFluid);
    }

    /**
     * A sided variant of {@link IExtendedFluidHandler#extractFluid(int, Action)}, docs copied for convenience.
     * <p>
     * Extracts a {@link FluidStack} from this handler, distribution is left <strong>entirely</strong> to this {@link IExtendedFluidHandler}.
     * <p>
     * The returned value must be empty if nothing is extracted, otherwise its stack size must be less than or equal to {@code amount}.
     * </p>
     *
     * @param amount Amount to extract (may be greater than the current stack's amount or the tank's capacity)
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param side   The side we are interacting with the handler from (null for internal).
     *
     * @return {@link FluidStack} extracted from the tank, must be empty if nothing can be extracted. The returned {@link FluidStack} can be safely modified after, so the
     * tank should return a new or copied stack.
     *
     * @implNote The default implementation of this method, extracts across all tanks to try and reach the desired amount to extract. Once the first fluid that can be
     * extracted is found, all future extractions will make sure to also make sure they are for the same type of fluid.
     * @apiNote It is not guaranteed that the default implementation will be how this {@link IExtendedFluidHandler} ends up distributing the extraction.
     */
    default FluidStack extractFluid(int amount, @Nullable Direction side, Action action) {
        return ExtendedFluidHandlerUtils.extract(amount, side, action, this::getTanks, this::getFluidInTank, this::extractFluid);
    }

    /**
     * A sided variant of {@link IExtendedFluidHandler#extractFluid(FluidStack, Action)}, docs copied for convenience.
     * <p>
     * Extracts a {@link FluidStack} from this handler, distribution is left <strong>entirely</strong> to this {@link IExtendedFluidHandler}.
     * <p>
     * The returned value must be empty if nothing is extracted, otherwise its stack size must be less than or equal to {@code amount}.
     * </p>
     *
     * @param stack  {@link FluidStack} representing the {@link net.minecraft.world.level.material.Fluid} and maximum amount to be drained.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param side   The side we are interacting with the handler from (null for internal).
     *
     * @return {@link FluidStack} extracted from the tank, must be empty if nothing can be extracted. The returned {@link FluidStack} can be safely modified after, so the
     * tank should return a new or copied stack.
     *
     * @implNote The default implementation of this method, extracts across all tanks that contents match the type of fluid passed into this method.
     * @apiNote It is not guaranteed that the default implementation will be how this {@link IExtendedFluidHandler} ends up distributing the extraction.
     */
    default FluidStack extractFluid(FluidStack stack, @Nullable Direction side, Action action) {
        return ExtendedFluidHandlerUtils.extract(stack, side, action, this::getTanks, this::getFluidInTank, this::extractFluid);
    }
}