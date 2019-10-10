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
    private GasInventorySlot(GasTank gasTank, Predicate<Gas> isValidGas) {
        //TODO: Do we want to make any of the ones that implement this not support conversion and just support grabbing from tanks
        super(extractPredicate, true, item -> !GasConversionHandler.getItemGas(item, gasTank, isValidGas).isEmpty());
    }

    public static GasInventorySlot input(GasTank gasTank, Predicate<Gas> isValidGas) {
        return new GasInventorySlot(gasTank, isValidGas);
    }

    public static GasInventorySlot output(GasTank gasTank) {
        //TODO: We should probably check that it is an IGasItem and it has a matching gas?
        return new GasInventorySlot(gasTank, gas -> true);
    }
}