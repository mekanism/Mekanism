package mekanism.common.util;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.IGasItem;
import mekanism.common.base.target.GasHandlerTarget;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
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
        final IGasHandler[] acceptors = new IGasHandler[]{null, null, null, null, null, null};
        EmitUtils.forEachSide(world, pos, sides, (tile, side) ->
              acceptors[side.ordinal()] = CapabilityUtils.getCapabilityHelper(tile, Capabilities.GAS_HANDLER_CAPABILITY, side.getOpposite()).getValue());
        return acceptors;
    }

    /**
     * Gets all the acceptors around a tile entity.
     *
     * @return array of IGasAcceptors
     */
    public static IGasHandler[] getConnectedAcceptors(BlockPos pos, World world) {
        return getConnectedAcceptors(pos, world, EnumSet.allOf(Direction.class));
    }

    public static boolean isValidAcceptorOnSide(TileEntity tile, Direction side) {
        if (CapabilityUtils.getCapabilityHelper(tile, Capabilities.GRID_TRANSMITTER_CAPABILITY, side.getOpposite()).isPresent()) {
            return false;
        }
        return CapabilityUtils.getCapabilityHelper(tile, Capabilities.GAS_HANDLER_CAPABILITY, side.getOpposite()).isPresent();
    }

    public static void clearIfInvalid(GasTank tank, Predicate<Gas> isValid) {
        if (MekanismConfig.general.voidInvalidGases.get()) {
            Gas gas = tank.getGasType();
            if (gas != null && !isValid.test(gas)) {
                tank.setGas(null);
            }
        }
    }

    /**
     * Removes a specified amount of gas from an IGasItem.
     *
     * @param itemStack - ItemStack of the IGasItem
     * @param type      - type of gas to remove from the IGasItem, null if it doesn't matter
     * @param amount    - amount of gas to remove from the ItemStack
     *
     * @return the GasStack removed by the IGasItem
     */
    public static GasStack removeGas(ItemStack itemStack, Gas type, int amount) {
        if (!itemStack.isEmpty() && itemStack.getItem() instanceof IGasItem) {
            IGasItem item = (IGasItem) itemStack.getItem();
            if (type != null && item.getGas(itemStack) != null && item.getGas(itemStack).getGas() != type || !item.canProvideGas(itemStack, type)) {
                return null;
            }
            return item.removeGas(itemStack, amount);
        }
        return null;
    }

    /**
     * Adds a specified amount of gas to an IGasItem.
     *
     * @param itemStack - ItemStack of the IGasItem
     * @param stack     - stack to add to the IGasItem
     *
     * @return amount of gas accepted by the IGasItem
     */
    public static int addGas(ItemStack itemStack, GasStack stack) {
        if (!itemStack.isEmpty() && itemStack.getItem() instanceof IGasItem && ((IGasItem) itemStack.getItem()).canReceiveGas(itemStack, stack.getGas())) {
            return ((IGasItem) itemStack.getItem()).addGas(itemStack, stack.copy());
        }
        return 0;
    }

    /**
     * Emits gas from a central block by splitting the received stack among the sides given.
     *
     * @param stack - the stack to output
     * @param from  - the TileEntity to output from
     * @param sides - the list of sides to output from
     *
     * @return the amount of gas emitted
     */
    public static int emit(GasStack stack, TileEntity from, Set<Direction> sides) {
        if (stack == null || stack.amount == 0) {
            return 0;
        }

        //Fake that we have one target given we know that no sides will overlap
        // This allows us to have slightly better performance
        final GasHandlerTarget target = new GasHandlerTarget(stack);
        EmitUtils.forEachSide(from.getWorld(), from.getPos(), sides, (acceptor, side) -> {

            //Invert to get access side
            final Direction accessSide = side.getOpposite();

            //Collect cap
            CapabilityUtils.getCapabilityHelper(acceptor, Capabilities.GAS_HANDLER_CAPABILITY, accessSide).ifPresent(handler -> {
                if (handler.canReceiveGas(accessSide, stack.getGas())) {
                    target.addHandler(accessSide, handler);
                }
            });
        });

        int curHandlers = target.getHandlers().size();
        if (curHandlers > 0) {
            Set<GasHandlerTarget> targets = new HashSet<>();
            targets.add(target);
            return EmitUtils.sendToAcceptors(targets, curHandlers, stack.amount, stack);
        }
        return 0;
    }

    public static void writeSustainedData(GasTank gasTank, ItemStack itemStack) {
        if (gasTank.stored != null && gasTank.stored.getGas() != null) {
            ItemDataUtils.setCompound(itemStack, "gasStored", gasTank.stored.write(new CompoundNBT()));
        }
    }

    public static void readSustainedData(GasTank gasTank, ItemStack itemStack) {
        if (ItemDataUtils.hasData(itemStack, "gasStored")) {
            gasTank.stored = GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "gasStored"));
        } else {
            gasTank.stored = null;
        }
    }
}