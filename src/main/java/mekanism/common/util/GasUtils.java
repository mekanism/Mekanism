package mekanism.common.util;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mekanism.api.Action;
import mekanism.api.DataHandlerUtils;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.inventory.AutomationType;
import mekanism.api.providers.IGasProvider;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.distribution.target.GasHandlerTarget;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A handy class containing several utilities for efficient gas transfer.
 *
 * @author AidanBrady
 */
public final class GasUtils {

    public static IGasHandler[] getConnectedAcceptors(BlockPos pos, World world, Set<Direction> sides) {
        IGasHandler[] acceptors = new IGasHandler[EnumUtils.DIRECTIONS.length];
        EmitUtils.forEachSide(world, pos, sides, (tile, side) ->
              CapabilityUtils.getCapability(tile, Capabilities.GAS_HANDLER_CAPABILITY, side.getOpposite()).ifPresent(handler -> acceptors[side.ordinal()] = handler));
        return acceptors;
    }

    public static ItemStack getFilledVariant(ItemStack toFill, long capacity, IGasProvider gasProvider) {
        //Manually handle this as capabilities are not necessarily loaded yet
        // (at least not on the first call to this, which is made via fillItemGroup)
        BasicGasTank tank = BasicGasTank.createDummy(capacity);
        tank.setStack(gasProvider.getGasStack(tank.getCapacity()));
        ItemDataUtils.setList(toFill, NBTConstants.GAS_TANKS, DataHandlerUtils.writeContainers(Collections.singletonList(tank)));
        //The item is now filled return it for convenience
        return toFill;
    }

    public static boolean hasGas(ItemStack stack, Predicate<GasStack> validityCheck) {
        Optional<IGasHandler> capability = MekanismUtils.toOptional(stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
        if (capability.isPresent()) {
            IGasHandler gasHandlerItem = capability.get();
            for (int tank = 0; tank < gasHandlerItem.getGasTankCount(); tank++) {
                GasStack gasStack = gasHandlerItem.getGasInTank(tank);
                if (!gasStack.isEmpty() && validityCheck.test(gasStack)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasGas(ItemStack stack, Gas type) {
        return hasGas(stack, (s) -> s.getType() == type);
    }

    public static boolean hasGas(ItemStack stack) {
        return hasGas(stack, (s) -> true);
    }

    public static void emit(IGasTank tank, TileEntity from) {
        emit(EnumSet.allOf(Direction.class), tank, from);
    }

    public static void emit(Set<Direction> outputSides, IGasTank tank, TileEntity from) {
        emit(outputSides, tank, from, tank.getCapacity());
    }

    public static void emit(Set<Direction> outputSides, IGasTank tank, TileEntity from, long maxOutput) {
        if (!tank.isEmpty() && maxOutput > 0) {
            tank.extract(emit(outputSides, tank.extract(maxOutput, Action.SIMULATE, AutomationType.INTERNAL), from), Action.EXECUTE, AutomationType.INTERNAL);
        }
    }

    /**
     * Emits gas from a central block by splitting the received stack among the sides given.
     *
     * @param sides - the list of sides to output from
     * @param stack - the stack to output
     * @param from  - the TileEntity to output from
     *
     * @return the amount of gas emitted
     */
    public static long emit(Set<Direction> sides, @Nonnull GasStack stack, TileEntity from) {
        if (stack.isEmpty() || sides.isEmpty()) {
            return 0;
        }
        //Fake that we have one target given we know that no sides will overlap
        // This allows us to have slightly better performance
        GasHandlerTarget target = new GasHandlerTarget(stack);
        GasStack unitStack = new GasStack(stack, 1);
        EmitUtils.forEachSide(from.getWorld(), from.getPos(), sides, (acceptor, side) -> {
            //Insert to access side
            Direction accessSide = side.getOpposite();
            //Collect cap
            CapabilityUtils.getCapability(acceptor, Capabilities.GAS_HANDLER_CAPABILITY, accessSide).ifPresent(handler -> {
                if (canInsert(handler, unitStack)) {
                    target.addHandler(accessSide, handler);
                }
            });
        });
        int curHandlers = target.getHandlers().size();
        if (curHandlers > 0) {
            Set<GasHandlerTarget> targets = new ObjectOpenHashSet<>();
            targets.add(target);
            return EmitUtils.sendToAcceptors(targets, curHandlers, stack.getAmount(), stack.copy());
        }
        return 0;
    }

    public static boolean canInsert(IGasHandler handler, @Nonnull GasStack unitStack) {
        return handler.insertGas(unitStack, Action.SIMULATE).isEmpty();
    }
}