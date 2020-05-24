package mekanism.api.chemical.slurry;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.chemical.ChemicalUtils;
import net.minecraft.util.Direction;

/**
 * A sided variant of {@link ISlurryHandler}
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ISidedSlurryHandler extends ISlurryHandler {

    /**
     * The side this {@link ISidedSlurryHandler} is for. This defaults to null, which is for internal use.
     *
     * @return The default side to use for the normal {@link ISlurryHandler} methods when wrapping them into {@link ISidedSlurryHandler} methods.
     */
    @Nullable
    default Direction getSlurrySideFor() {
        return null;
    }

    /**
     * A sided variant of {@link ISlurryHandler#getSlurryTankCount()}, docs copied for convenience.
     *
     * Returns the number of slurry storage units ("tanks") available
     *
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The number of tanks available
     */
    int getSlurryTankCount(@Nullable Direction side);

    @Override
    default int getSlurryTankCount() {
        return getSlurryTankCount(getSlurrySideFor());
    }

    /**
     * A sided variant of {@link ISlurryHandler#getSlurryInTank(int)}, docs copied for convenience.
     *
     * Returns the {@link SlurryStack} in a given tank.
     *
     * <p>
     * <strong>IMPORTANT:</strong> This {@link SlurryStack} <em>MUST NOT</em> be modified. This method is not for altering internal contents. Any implementers who are
     * able to detect modification via this method should throw an exception. It is ENTIRELY reasonable and likely that the stack returned here will be a copy.
     * </p>
     *
     * <p>
     * <strong><em>SERIOUSLY: DO NOT MODIFY THE RETURNED SLURRY STACK</em></strong>
     * </p>
     *
     * @param tank Tank to query.
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return {@link SlurryStack} in a given tank. {@link SlurryStack#EMPTY} if the tank is empty.
     */
    SlurryStack getSlurryInTank(int tank, @Nullable Direction side);

    @Override
    default SlurryStack getSlurryInTank(int tank) {
        return getSlurryInTank(tank, getSlurrySideFor());
    }

    /**
     * A sided variant of {@link ISlurryHandler#setSlurryInTank(int, SlurryStack)}, docs copied for convenience.
     *
     * Overrides the stack in the given tank. This method may throw an error if it is called unexpectedly.
     *
     * @param tank  Tank to modify
     * @param stack {@link SlurryStack} to set tank to (may be empty).
     * @param side  The side we are interacting with the handler from (null for internal).
     *
     * @throws RuntimeException if the handler is called in a way that the handler was not expecting.
     **/
    void setSlurryInTank(int tank, SlurryStack stack, @Nullable Direction side);

    @Override
    default void setSlurryInTank(int tank, SlurryStack stack) {
        setSlurryInTank(tank, stack, getSlurrySideFor());
    }

    /**
     * A sided variant of {@link ISlurryHandler#getSlurryTankCapacity(int)}, docs copied for convenience.
     *
     * Retrieves the maximum amount of slurry that can be stored in a given tank.
     *
     * @param tank Tank to query.
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The maximum slurry amount held by the tank.
     */
    long getSlurryTankCapacity(int tank, @Nullable Direction side);

    @Override
    default long getSlurryTankCapacity(int tank) {
        return getSlurryTankCapacity(tank, getSlurrySideFor());
    }

    /**
     * A sided variant of {@link ISlurryHandler#isSlurryValid(int, SlurryStack)}, docs copied for convenience.
     *
     * <p>
     * This function should be used instead of simulated insertions in cases where the contents and state of the tank are irrelevant, mainly for the purpose of automation
     * and logic.
     * </p>
     * <ul>
     * <li>isSlurryValid is false when insertion of the slurry type is never valid.</li>
     * <li>When isSlurryValid is true, no assumptions can be made and insertion must be simulated case-by-case.</li>
     * <li>The actual slurry in the tank, its fullness, or any other state are <strong>not</strong> considered by isSlurryValid.</li>
     * </ul>
     *
     * @param tank  Tank to query.
     * @param stack Stack to test with for validity @param side   The side we are interacting with the handler from (null for internal).
     *
     * @return true if the tank can accept the {@link SlurryStack}, not considering the current state of the tank. false if the tank can never support the given {@link
     * SlurryStack} in any situation.
     */
    boolean isSlurryValid(int tank, SlurryStack stack, @Nullable Direction side);

    @Override
    default boolean isSlurryValid(int tank, SlurryStack stack) {
        return isSlurryValid(tank, stack, getSlurrySideFor());
    }

    /**
     * A sided variant of {@link ISlurryHandler#insertSlurry(int, SlurryStack, Action)}, docs copied for convenience.
     *
     * <p>
     * Inserts a {@link SlurryStack} into a given tank and return the remainder. The {@link SlurryStack} <em>should not</em> be modified in this function!
     * </p>
     * Note: This behaviour is subtly different from {@link net.minecraftforge.fluids.capability.IFluidHandler#fill(net.minecraftforge.fluids.FluidStack,
     * net.minecraftforge.fluids.capability.IFluidHandler.FluidAction)}
     *
     * @param tank   Tank to insert to.
     * @param stack  {@link SlurryStack} to insert. This must not be modified by the tank.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param side   The side we are interacting with the handler from (null for internal).
     *
     * @return The remaining {@link SlurryStack} that was not inserted (if the entire stack is accepted, then return an empty {@link SlurryStack}). May be the same as
     * the input {@link SlurryStack} if unchanged, otherwise a new {@link SlurryStack}. The returned {@link SlurryStack} can be safely modified after
     */
    SlurryStack insertSlurry(int tank, SlurryStack stack, @Nullable Direction side, Action action);

    @Override
    default SlurryStack insertSlurry(int tank, SlurryStack stack, Action action) {
        return insertSlurry(tank, stack, getSlurrySideFor(), action);
    }

    /**
     * A sided variant of {@link ISlurryHandler#extractSlurry(int, long, Action)}, docs copied for convenience.
     *
     * Extracts a {@link SlurryStack} from a specific tank in this handler.
     * <p>
     * The returned value must be empty if nothing is extracted, otherwise its stack size must be less than or equal to {@code amount}.
     * </p>
     *
     * @param tank   Tank to extract from.
     * @param amount Amount to extract (may be greater than the current stack's amount or the tank's capacity)
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param side   The side we are interacting with the handler from (null for internal).
     *
     * @return {@link SlurryStack} extracted from the tank, must be empty if nothing can be extracted. The returned {@link SlurryStack} can be safely modified after,
     * so the tank should return a new or copied stack.
     */
    SlurryStack extractSlurry(int tank, long amount, @Nullable Direction side, Action action);

    @Override
    default SlurryStack extractSlurry(int tank, long amount, Action action) {
        return extractSlurry(tank, amount, getSlurrySideFor(), action);
    }

    /**
     * A sided variant of {@link ISlurryHandler#insertSlurry(SlurryStack, Action)}, docs copied for convenience.
     *
     * <p>
     * Inserts a {@link SlurryStack} into this handler, distribution is left <strong>entirely</strong> to this {@link ISlurryHandler}. The {@link SlurryStack}
     * <em>should not</em> be modified in this function!
     * </p>
     * Note: This behaviour is subtly different from {@link net.minecraftforge.fluids.capability.IFluidHandler#fill(net.minecraftforge.fluids.FluidStack,
     * net.minecraftforge.fluids.capability.IFluidHandler.FluidAction)}
     *
     * @param stack  {@link SlurryStack} to insert. This must not be modified by the handler.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param side   The side we are interacting with the handler from (null for internal).
     *
     * @return The remaining {@link SlurryStack} that was not inserted (if the entire stack is accepted, then return an empty {@link SlurryStack}). May be the same as
     * the input {@link SlurryStack} if unchanged, otherwise a new {@link SlurryStack}. The returned {@link SlurryStack} can be safely modified after
     *
     * @implNote The default implementation of this method, attempts to insert into tanks that contain the same type of slurry as the supplied type, and if it will
     * not all fit, falls back to inserting into any empty tanks.
     * @apiNote It is not guaranteed that the default implementation will be how this {@link ISlurryHandler} ends up distributing the insertion.
     */
    default SlurryStack insertSlurry(SlurryStack stack, @Nullable Direction side, Action action) {
        return ChemicalUtils.insert(stack, action, SlurryStack.EMPTY, () -> getSlurryTankCount(side), tank -> getSlurryInTank(tank, side),
              (tank, s, a) -> insertSlurry(tank, s, side, a));
    }

    /**
     * A sided variant of {@link ISlurryHandler#extractSlurry(long, Action)}, docs copied for convenience.
     *
     * Extracts a {@link SlurryStack} from this handler, distribution is left <strong>entirely</strong> to this {@link ISlurryHandler}.
     * <p>
     * The returned value must be empty if nothing is extracted, otherwise its stack size must be less than or equal to {@code amount}.
     * </p>
     *
     * @param amount Amount to extract (may be greater than the current stack's amount or the tank's capacity)
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param side   The side we are interacting with the handler from (null for internal).
     *
     * @return {@link SlurryStack} extracted from the tank, must be empty if nothing can be extracted. The returned {@link SlurryStack} can be safely modified after,
     * so the tank should return a new or copied stack.
     *
     * @implNote The default implementation of this method, extracts across all tanks to try and reach the desired amount to extract. Once the first slurry that can
     * be extracted is found, all future extractions will make sure to also make sure they are for the same type of slurry.
     * @apiNote It is not guaranteed that the default implementation will be how this {@link ISlurryHandler} ends up distributing the extraction.
     */
    default SlurryStack extractSlurry(long amount, @Nullable Direction side, Action action) {
        return ChemicalUtils.extract(amount, action, SlurryStack.EMPTY, () -> getSlurryTankCount(side), tank -> getSlurryInTank(tank, side),
              (tank, a, act) -> extractSlurry(tank, a, side, act));
    }

    /**
     * A sided variant of {@link ISlurryHandler#extractSlurry(SlurryStack, Action)}, docs copied for convenience.
     *
     * Extracts a {@link SlurryStack} from this handler, distribution is left <strong>entirely</strong> to this {@link ISlurryHandler}.
     * <p>
     * The returned value must be empty if nothing is extracted, otherwise its stack size must be less than or equal to {@code amount}.
     * </p>
     *
     * @param stack  {@link SlurryStack} representing the {@link Slurry} and maximum amount to be drained.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param side   The side we are interacting with the handler from (null for internal).
     *
     * @return {@link SlurryStack} extracted from the tank, must be empty if nothing can be extracted. The returned {@link SlurryStack} can be safely modified after,
     * so the tank should return a new or copied stack.
     *
     * @implNote The default implementation of this method, extracts across all tanks that contents match the type of slurry passed into this method.
     * @apiNote It is not guaranteed that the default implementation will be how this {@link ISlurryHandler} ends up distributing the extraction.
     */
    default SlurryStack extractSlurry(SlurryStack stack, @Nullable Direction side, Action action) {
        return ChemicalUtils.extract(stack, action, SlurryStack.EMPTY, () -> getSlurryTankCount(side), tank -> getSlurryInTank(tank, side),
              (tank, a, act) -> extractSlurry(tank, a, side, act));
    }
}