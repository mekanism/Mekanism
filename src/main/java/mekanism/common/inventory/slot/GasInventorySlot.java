package mekanism.common.inventory.slot;

import java.util.function.Predicate;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.IGasItem;
import mekanism.common.recipe.GasConversionHandler;
import net.minecraft.item.ItemStack;

public class GasInventorySlot extends BasicInventorySlot {

    private static final Predicate<@NonNull ItemStack> extractPredicate = item -> !item.isEmpty() && item.getItem() instanceof IGasItem && ((IGasItem) item.getItem()).getGas(item).isEmpty();

    //TODO: Replace GasTank with an IGasHandler??
    public GasInventorySlot(GasTank gasTank, Predicate<Gas> isValidGas) {
        super(extractPredicate, true, item -> !GasConversionHandler.getItemGas(item, gasTank, isValidGas).isEmpty());
    }
}