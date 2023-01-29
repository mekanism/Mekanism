package mekanism.api.gas;

import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class GasifyableItems {

    static HashMap<String, GasStack> gasifyableItems = new HashMap<String, GasStack>();
    static List<Gas> validGases = new ArrayList<>();

    public static void registerGasifyables(String oredict, Gas gas, Integer quantity) {
        System.out.println("oredict = " + oredict);
        System.out.println("gas = " + gas);
        System.out.println("quantity = " + quantity);

        GasStack gasifyEntry = new GasStack(gas, quantity);

        gasifyableItems.put(oredict, gasifyEntry);
        validGases.add(gas);
    }

    public static GasStack getGasFromItem(ItemStack itemstack) {

        List<String> oredict = MekanismUtils.getOreDictName(itemstack);

        for (String s : oredict) {
            if (gasifyableItems.containsKey(s))
                return gasifyableItems.get(s);
        }
        return null;
    }

    public static boolean isGasValidGasifyable(Gas gas) {
        return validGases.contains(gas);
    }
}
