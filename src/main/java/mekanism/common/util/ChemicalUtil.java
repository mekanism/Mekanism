package mekanism.common.util;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.EnumSet;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.inventory.AutomationType;
import mekanism.common.distribution.target.ChemicalHandlerTarget;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

/**
 * @apiNote This class is called ChemicalUtil instead of ChemicalUtils so that it does not overlap with {@link mekanism.api.chemical.ChemicalUtils}
 */
public class ChemicalUtil {

    /**
     * Helper to resize a chemical stack when we don't know what implementation it is.
     *
     * @param stack  Stack to copy
     * @param amount Desired size
     *
     * @return Copy of the input stack with the desired size
     *
     * @apiNote Should only be called if we know that copy returns STACK
     */
    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> STACK copyWithAmount(STACK stack, long amount) {
        STACK copy = (STACK) stack.copy();
        copy.setAmount(amount);
        return copy;
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>> void emit(
          Capability<HANDLER> capability, IChemicalTank<CHEMICAL, STACK> tank, TileEntity from) {
        emit(capability, EnumSet.allOf(Direction.class), tank, from);
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>> void emit(
          Capability<HANDLER> capability, Set<Direction> outputSides, IChemicalTank<CHEMICAL, STACK> tank, TileEntity from) {
        emit(capability, outputSides, tank, from, tank.getCapacity());
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>> void emit(
          Capability<HANDLER> capability, Set<Direction> outputSides, IChemicalTank<CHEMICAL, STACK> tank, TileEntity from, long maxOutput) {
        if (!tank.isEmpty() && maxOutput > 0) {
            tank.extract(emit(capability, outputSides, tank.extract(maxOutput, Action.SIMULATE, AutomationType.INTERNAL), from), Action.EXECUTE, AutomationType.INTERNAL);
        }
    }

    /**
     * Emits chemical from a central block by splitting the received stack among the sides given.
     *
     * @param sides - the list of sides to output from
     * @param stack - the stack to output
     * @param from  - the TileEntity to output from
     *
     * @return the amount of chemical emitted
     */
    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>> long emit(
          Capability<HANDLER> capability, Set<Direction> sides, @Nonnull STACK stack, TileEntity from) {
        if (stack.isEmpty() || sides.isEmpty()) {
            return 0;
        }
        //Fake that we have one target given we know that no sides will overlap
        // This allows us to have slightly better performance
        ChemicalHandlerTarget<CHEMICAL, STACK, HANDLER> target = new ChemicalHandlerTarget<>(stack);
        EmitUtils.forEachSide(from.getWorld(), from.getPos(), sides, (acceptor, side) -> {
            //Insert to access side
            Direction accessSide = side.getOpposite();
            //Collect cap
            CapabilityUtils.getCapability(acceptor, capability, accessSide).ifPresent(handler -> {
                if (canInsert(handler, stack)) {
                    target.addHandler(accessSide, handler);
                }
            });
        });
        int curHandlers = target.getHandlers().size();
        if (curHandlers > 0) {
            Set<ChemicalHandlerTarget<CHEMICAL, STACK, HANDLER>> targets = new ObjectOpenHashSet<>();
            targets.add(target);
            return EmitUtils.sendToAcceptors(targets, curHandlers, stack.getAmount(), (STACK) stack.copy());
        }
        return 0;
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>> boolean canInsert(
          HANDLER handler, @Nonnull STACK stack) {
        return handler.insertChemical(stack, Action.SIMULATE).getAmount() < stack.getAmount();
    }
}