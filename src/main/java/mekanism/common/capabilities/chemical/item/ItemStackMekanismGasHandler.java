package mekanism.common.capabilities.chemical.item;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.DataHandlerUtils;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.IMekanismGasHandler;
import mekanism.common.capabilities.CapabilityCache;
import mekanism.common.capabilities.resolver.basic.PersistentCapabilityResolver;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;

/**
 * Helper class for implementing gas handlers for items
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ItemStackMekanismGasHandler extends ItemStackMekanismChemicalHandler<Gas, GasStack, IGasTank> implements IMekanismGasHandler {

    @Override
    protected void load() {
        ItemStack stack = getStack();
        if (!stack.isEmpty()) {
            DataHandlerUtils.readTanks(getGasTanks(null), ItemDataUtils.getList(stack, NBTConstants.GAS_TANKS));
        }
    }

    @Nonnull
    @Override
    public List<IGasTank> getGasTanks(@Nullable Direction side) {
        return tanks;
    }

    @Override
    public void onContentsChanged() {
        ItemStack stack = getStack();
        if (!stack.isEmpty()) {
            ItemDataUtils.setList(stack, NBTConstants.GAS_TANKS, DataHandlerUtils.writeTanks(getGasTanks(null)));
        }
    }

    @Override
    protected void addCapabilityResolvers(CapabilityCache capabilityCache) {
        capabilityCache.addCapabilityResolver(PersistentCapabilityResolver.gasHandler(() -> this));
    }
}