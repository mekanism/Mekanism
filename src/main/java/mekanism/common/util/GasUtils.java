package mekanism.common.util;

import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.DataHandlerUtils;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.providers.IGasProvider;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tier.ChemicalTankTier;
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

    /**
     * Creates and returns a full gas tank with the specified gas type.
     *
     * @param gas - gas to fill the tank with
     *
     * @return filled gas tank
     */
    public static ItemStack getFullChemicalTank(ChemicalTankTier tier, @Nonnull Gas gas) {
        return getFilledVariant(getEmptyChemicalTank(tier), tier.getStorage(), gas);
    }

    /**
     * Retrieves an empty Gas Tank.
     *
     * @return empty gas tank
     */
    private static ItemStack getEmptyChemicalTank(ChemicalTankTier tier) {
        switch (tier) {
            case BASIC:
                return MekanismBlocks.BASIC_GAS_TANK.getItemStack();
            case ADVANCED:
                return MekanismBlocks.ADVANCED_GAS_TANK.getItemStack();
            case ELITE:
                return MekanismBlocks.ELITE_GAS_TANK.getItemStack();
            case ULTIMATE:
                return MekanismBlocks.ULTIMATE_GAS_TANK.getItemStack();
            case CREATIVE:
                return MekanismBlocks.CREATIVE_GAS_TANK.getItemStack();
        }
        return ItemStack.EMPTY;
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

    public static boolean hasGas(ItemStack stack, Gas type) {
        return ChemicalUtil.hasChemical(stack, s -> s.isTypeEqual(type), Capabilities.GAS_HANDLER_CAPABILITY);
    }

    public static boolean hasGas(ItemStack stack) {
        return ChemicalUtil.hasChemical(stack, s -> true, Capabilities.GAS_HANDLER_CAPABILITY);
    }
}