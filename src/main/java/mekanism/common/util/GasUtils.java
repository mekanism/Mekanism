package mekanism.common.util;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.DataHandlerUtils;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.providers.IGasProvider;
import mekanism.api.NBTConstants;
import mekanism.common.base.target.GasHandlerTarget;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
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
        final IGasHandler[] acceptors = new IGasHandler[]{null, null, null, null, null, null};
        EmitUtils.forEachSide(world, pos, sides, (tile, side) ->
              CapabilityUtils.getCapability(tile, Capabilities.GAS_HANDLER_CAPABILITY, side.getOpposite()).ifPresent(handler -> acceptors[side.ordinal()] = handler));
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
        if (CapabilityUtils.getCapability(tile, Capabilities.GRID_TRANSMITTER_CAPABILITY, side.getOpposite()).isPresent()) {
            return false;
        }
        return CapabilityUtils.getCapability(tile, Capabilities.GAS_HANDLER_CAPABILITY, side.getOpposite()).isPresent();
    }

    public static void clearIfInvalid(IChemicalTank<Gas, GasStack> tank, Predicate<@NonNull Gas> isValid) {
        //TODO: Hook this back up
        if (MekanismConfig.general.voidInvalidGases.get()) {
            Gas gas = tank.getType();
            if (!gas.isEmptyType() && !isValid.test(gas)) {
                tank.setEmpty();
            }
        }
    }

    public static ItemStack getFilledVariant(ItemStack toFill, int capacity, IGasProvider gasProvider) {
        //Manually handle this as capabilities are not necessarily loaded yet
        // (at least not on the first call to this, which is made via fillItemGroup)
        BasicGasTank tank = BasicGasTank.create(capacity, null);
        tank.setStack(gasProvider.getGasStack(tank.getCapacity()));
        ItemDataUtils.setList(toFill, NBTConstants.GAS_TANKS, DataHandlerUtils.writeTanks(Collections.singletonList(tank)));
        //The item is now filled return it for convenience
        return toFill;
    }

    public static double getDurabilityForDisplay(ItemStack stack) {
        if (Capabilities.GAS_HANDLER_CAPABILITY != null) {
            //Ensure the capability is not null, as the first call to getDurabilityForDisplay happens before capability injection
            Optional<IGasHandler> capability = MekanismUtils.toOptional(stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
            if (capability.isPresent()) {
                IGasHandler gasHandlerItem = capability.get();
                //TODO: Support having multiple tanks at some point, none of our items
                // currently do so, so it doesn't matter that much
                if (gasHandlerItem.getGasTankCount() > 0) {
                    //Validate something didn't go terribly wrong and we actually do have the tank we expect to have
                    return 1D - gasHandlerItem.getGasInTank(0).getAmount() / (double) gasHandlerItem.getGasTankCapacity(0);
                }
            }
        }
        return 1;
    }

    public static boolean hasGas(ItemStack stack) {
        Optional<IGasHandler> capability = MekanismUtils.toOptional(stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
        if (capability.isPresent()) {
            IGasHandler gasHandlerItem = capability.get();
            for (int tank = 0; tank < gasHandlerItem.getGasTankCount(); tank++) {
                if (!gasHandlerItem.getGasInTank(tank).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
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
    public static int emit(@Nonnull GasStack stack, TileEntity from, Set<Direction> sides) {
        if (stack.isEmpty()) {
            return 0;
        }

        //Fake that we have one target given we know that no sides will overlap
        // This allows us to have slightly better performance
        final GasHandlerTarget target = new GasHandlerTarget(stack);
        GasStack unitStack = new GasStack(stack, 1);
        EmitUtils.forEachSide(from.getWorld(), from.getPos(), sides, (acceptor, side) -> {

            //Invert to get access side
            final Direction accessSide = side.getOpposite();

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
            return EmitUtils.sendToAcceptors(targets, curHandlers, stack.getAmount(), stack);
        }
        return 0;
    }

    public static void emitGas(TileEntity tile, IChemicalTank<Gas, GasStack> tank, int gasOutput, Direction side) {
        if (!tank.isEmpty()) {
            GasStack toSend = new GasStack(tank.getStack(), Math.min(tank.getStored(), gasOutput));
            int sent = GasUtils.emit(toSend, tile, EnumSet.of(side));
            if (tank.shrinkStack(sent, Action.EXECUTE) != sent) {
                //TODO: Print warning/error
            }
        }
    }

    public static boolean canInsert(IGasHandler handler, @Nonnull GasStack unitStack) {
        return handler.insertGas(unitStack, Action.SIMULATE).isEmpty();
    }
}