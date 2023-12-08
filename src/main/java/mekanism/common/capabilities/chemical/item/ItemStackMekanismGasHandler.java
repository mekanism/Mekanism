package mekanism.common.capabilities.chemical.item;

import java.util.function.Function;
import mekanism.api.IContentsListener;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler.IMekanismGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Helper class for implementing gas handlers for items
 */
public abstract class ItemStackMekanismGasHandler extends ItemStackMekanismChemicalHandler<Gas, GasStack, IGasTank> implements IMekanismGasHandler {

    @SafeVarargs
    public ItemStackMekanismGasHandler(ItemStack stack, Function<IContentsListener, IGasTank>... tankProviders) {
        super(stack, tankProviders);
    }

    @NotNull
    @Override
    protected String getNbtKey() {
        return NBTConstants.GAS_TANKS;
    }
}