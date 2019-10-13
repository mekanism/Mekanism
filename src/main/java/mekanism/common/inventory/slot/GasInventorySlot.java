package mekanism.common.inventory.slot;

import java.util.function.Predicate;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.IGasItem;
import mekanism.common.recipe.GasConversionHandler;
import net.minecraft.item.ItemStack;

public class GasInventorySlot extends BasicInventorySlot {

    private static final Predicate<@NonNull ItemStack> extractPredicate = item -> !item.isEmpty() && item.getItem() instanceof IGasItem &&
                                                                                  ((IGasItem) item.getItem()).getGas(item).isEmpty();

    public static GasInventorySlot input(GasTank gasTank, Predicate<Gas> isValidGas, int x, int y) {
        return new GasInventorySlot(gasTank, isValidGas, x, y);
    }

    public static GasInventorySlot output(GasTank gasTank, int x, int y) {
        //TODO: We should probably check that it is an IGasItem and it has a matching gas?
        return new GasInventorySlot(gasTank, gas -> true, x, y);
    }

    //TODO: Replace GasTank with an IGasHandler??
    private final GasTank gasTank;

    private GasInventorySlot(GasTank gasTank, Predicate<Gas> isValidGas, int x, int y) {
        //TODO: Do we want to make any of the ones that implement this not support conversion and just support grabbing from tanks
        //TODO: FIX THIS, it currently is making it so that it is false for is valid if the tank has a different type than we have stored
        super(extractPredicate, item -> true, item -> !GasConversionHandler.getItemGas(item, gasTank, isValidGas).isEmpty(), x, y);
        this.gasTank = gasTank;
    }

    //TODO: Make it so that the gas tank fills
}