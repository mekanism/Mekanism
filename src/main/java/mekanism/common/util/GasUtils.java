package mekanism.common.util;

import java.util.Collections;
import java.util.Set;
import mekanism.api.DataHandlerUtils;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.providers.IGasProvider;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A handy class containing several utilities for efficient gas transfer.
 *
 * @author AidanBrady
 */
public final class GasUtils {//TODO - V10: Move various methods from here to ChemicalUtil

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
}