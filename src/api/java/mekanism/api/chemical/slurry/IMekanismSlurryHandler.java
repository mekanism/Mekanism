package mekanism.api.chemical.slurry;

import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.inventory.AutomationType;
import net.minecraft.util.Direction;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IMekanismSlurryHandler extends ISidedSlurryHandler {

    /**
     * Used to check if an instance of {@link IMekanismSlurryHandler} actually has the ability to handle slurries.
     *
     * @return True if we are actually capable of handling slurries.
     *
     * @apiNote If for some reason you are comparing to {@link IMekanismSlurryHandler} without having gotten the object via the slurry handler capability, then you
     * must call this method to make sure that it really can handle slurries. As most mekanism tiles have this class in their hierarchy.
     * @implNote If this returns false the capability should not be exposed AND methods should turn reasonable defaults for not doing anything.
     */
    default boolean canHandleSlurry() {
        return true;
    }

    /**
     * Returns the list of ISlurryTanks that this slurry handler exposes on the given side.
     *
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The list of all ISlurryTanks that this {@link IMekanismSlurryHandler} contains for the given side. If there are no tanks for the side or {@link
     * #canHandleSlurry()} is false then it returns an empty list.
     *
     * @implNote When side is null (an internal request), this method <em>MUST</em> return all tanks in the handler. Additionally, if {@link #canHandleSlurry()} is
     * false, this <em>MUST</em> return an empty list.
     */
    List<ISlurryTank> getSlurryTanks(@Nullable Direction side);

    /**
     * Called when the contents of this slurry handler change.
     */
    void onContentsChanged();

    /**
     * Returns the {@link ISlurryTank} that has the given index from the list of tanks on the given side.
     *
     * @param tank The index of the tank to retrieve.
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The {@link ISlurryTank} that has the given index from the list of tanks on the given side.
     */
    @Nullable
    default ISlurryTank getSlurryTank(int tank, @Nullable Direction side) {
        List<ISlurryTank> tanks = getSlurryTanks(side);
        return tank >= 0 && tank < tanks.size() ? tanks.get(tank) : null;
    }

    @Override
    default int getSlurryTankCount(@Nullable Direction side) {
        return getSlurryTanks(side).size();
    }

    @Override
    default SlurryStack getSlurryInTank(int tank, @Nullable Direction side) {
        ISlurryTank slurryTank = getSlurryTank(tank, side);
        return slurryTank == null ? SlurryStack.EMPTY : slurryTank.getStack();
    }

    @Override
    default void setSlurryInTank(int tank, SlurryStack stack, @Nullable Direction side) {
        ISlurryTank slurryTank = getSlurryTank(tank, side);
        if (slurryTank != null) {
            slurryTank.setStack(stack);
        }
    }

    @Override
    default long getSlurryTankCapacity(int tank, @Nullable Direction side) {
        ISlurryTank slurryTank = getSlurryTank(tank, side);
        return slurryTank == null ? 0 : slurryTank.getCapacity();
    }

    @Override
    default boolean isSlurryValid(int tank, SlurryStack stack, @Nullable Direction side) {
        ISlurryTank slurryTank = getSlurryTank(tank, side);
        return slurryTank != null && slurryTank.isValid(stack);
    }

    @Override
    default SlurryStack insertSlurry(int tank, SlurryStack stack, @Nullable Direction side, Action action) {
        ISlurryTank slurryTank = getSlurryTank(tank, side);
        return slurryTank == null ? stack : slurryTank.insert(stack, action, side == null ? AutomationType.INTERNAL : AutomationType.EXTERNAL);
    }

    @Override
    default SlurryStack extractSlurry(int tank, long amount, @Nullable Direction side, Action action) {
        ISlurryTank slurryTank = getSlurryTank(tank, side);
        return slurryTank == null ? SlurryStack.EMPTY : slurryTank.extract(amount, action, side == null ? AutomationType.INTERNAL : AutomationType.EXTERNAL);
    }
}