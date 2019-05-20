package mekanism.common.recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import javax.annotation.Nonnull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import mekanism.common.MekanismFluids;
import mekanism.common.recipe.ingredients.IMekanismIngredient;
import mekanism.common.recipe.ingredients.ItemStackMekIngredient;
import mekanism.common.recipe.ingredients.OredictMekIngredient;
import mekanism.common.tier.GasTankTier;
import mekanism.common.util.MekanismUtils;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class GasConversionHandler {

    //TODO: Show uses in JEI for fuels that can be turned to gas??
    private final static Map<Gas, List<IMekanismIngredient<ItemStack>>> gasToIngredients = new HashMap<>();
    private final static Map<IMekanismIngredient<ItemStack>, GasStack> ingredientToGas = new HashMap<>();

    public static void addDefaultGasMappings() {
        addGasMapping(new ItemStack(Items.FLINT), MekanismFluids.Oxygen, 10);
        addGasMapping("dustSulfur", MekanismFluids.SulfuricAcid, 2);
        addGasMapping("dustSalt", MekanismFluids.HydrogenChloride, 2);
        addGasMapping("ingotOsmium", MekanismFluids.LiquidOsmium, 200);
        addGasMapping("blockOsmium", MekanismFluids.LiquidOsmium, 1800);
    }

    public static boolean addGasMapping(@Nonnull ItemStack stack, @Nonnull Gas gas, int amount) {
        return addGasMapping(new ItemStackMekIngredient(stack), new GasStack(gas, amount));
    }

    public static boolean addGasMapping(@Nonnull String oreDict, @Nonnull Gas gas, int amount) {
        return addGasMapping(new OredictMekIngredient(oreDict), new GasStack(gas, amount));
    }

    public static boolean addGasMapping(@Nonnull IMekanismIngredient<ItemStack> ingredient, @Nonnull GasStack gasStack) {
        Gas gas = gasStack.getGas();
        if (gas == null || gasStack.amount <= 0) {
            return false;
        }
        List<IMekanismIngredient<ItemStack>> ingredients = gasToIngredients.computeIfAbsent(gas, k -> new ArrayList<>());
        //TODO: Better checking at some point if the ingredient is already in there? Should partial checking happen as well
        ingredients.add(ingredient);
        return ingredientToGas.put(ingredient, gasStack) == null;
    }

    public static int removeGasMapping(@Nonnull IMekanismIngredient<ItemStack> ingredient, @Nonnull GasStack gasStack) {
        Gas gas = gasStack.getGas();
        if (gas != null && gasStack.amount > 0 && gasToIngredients.containsKey(gas)) {
            List<IMekanismIngredient<ItemStack>> ingredients = gasToIngredients.get(gas);
            List<IMekanismIngredient<ItemStack>> toRemove = new ArrayList<>();
            for (IMekanismIngredient<ItemStack> stored : ingredients) {
                if (stored.equals(ingredient)) {
                    //TODO: Better comparision??? Doesn't really matter until we have better duplication handling
                    // or have proper handling for if something is registered as an ore dict and as an item
                    toRemove.add(stored);
                    ingredientToGas.remove(stored);
                }
            }
            if (ingredients.size() == toRemove.size()) {
                //If we are removing all for that gas type then remove the list as well
                gasToIngredients.remove(gas);
            } else {
                ingredients.removeAll(toRemove);
            }
            return toRemove.size();
        }
        return 0;
    }

    public static void removeAllGasMappings() {
        gasToIngredients.clear();
        ingredientToGas.clear();
    }

    /**
     * Gets the amount of ticks the declared itemstack can fuel this machine.
     *
     * @param itemStack - itemstack to check with
     *
     * @return fuel ticks
     */
    public static GasStack getItemGas(ItemStack itemStack, BiFunction<Gas, Integer, GasStack> getIfValid) {
        if (itemStack.getItem() instanceof IGasItem) {
            IGasItem item = (IGasItem) itemStack.getItem();
            GasStack gas = item.getGas(itemStack);
            //Check to make sure it can provide the gas it contains
            if (gas != null && item.canProvideGas(itemStack, gas.getGas())) {
                GasStack gasStack = getIfValid.apply(gas.getGas(), 1);
                if (gasStack != null) {
                    return gasStack;
                }
            }
        }
        for (Entry<IMekanismIngredient<ItemStack>, GasStack> entry : ingredientToGas.entrySet()) {
            if (entry.getKey().contains(itemStack)) {
                GasStack gasStack = getIfValid.apply(entry.getValue().getGas(), entry.getValue().amount);
                if (gasStack != null) {
                    return gasStack;
                }
            }
        }
        return null;
    }

    public static List<ItemStack> getStacksForGas(Gas type) {
        if (type == null) {
            return Collections.emptyList();
        }
        List<ItemStack> stacks = new ArrayList<>();
        //Always include the gas tank of the type
        stacks.add(MekanismUtils.getFullGasTank(GasTankTier.getDefault(), type));
        //See if there are any gas to item mappings
        List<IMekanismIngredient<ItemStack>> ingredients = gasToIngredients.get(type);
        if (ingredients == null) {
            return stacks;
        }
        //TODO: Maybe check for duplicates if things are in oredict and not? For the most part things assume there are no duplication at the moment
        for (IMekanismIngredient<ItemStack> ingredient : ingredients) {
            stacks.addAll(ingredient.getMatching());
        }
        return stacks;
    }
}