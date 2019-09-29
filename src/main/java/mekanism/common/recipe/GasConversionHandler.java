package mekanism.common.recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.IGasItem;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.MekanismGases;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tier.GasTankTier;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

//TODO: Move the conversion handler to API??
public class GasConversionHandler {

    //TODO: Show uses in JEI for fuels that can be turned to gas??
    private final static Map<@NonNull Gas, List<ItemStackIngredient>> gasToIngredients = new HashMap<>();
    private final static Map<ItemStackIngredient, GasStack> ingredientToGas = new HashMap<>();

    public static void addDefaultGasMappings() {
        addGasMapping(ItemStackIngredient.from(Items.FLINT), MekanismGases.OXYGEN.getGasStack(10));
        addGasMapping(ItemStackIngredient.from(MekanismTags.DUSTS_SULFUR), MekanismGases.SULFURIC_ACID.getGasStack(2));
        addGasMapping(ItemStackIngredient.from(MekanismTags.DUSTS_SALT), MekanismGases.HYDROGEN_CHLORIDE.getGasStack(2));
        addGasMapping(ItemStackIngredient.from(MekanismTags.INGOTS_OSMIUM), MekanismGases.LIQUID_OSMIUM.getGasStack(200));
        addGasMapping(ItemStackIngredient.from(MekanismTags.STORAGE_BLOCKS_OSMIUM), MekanismGases.LIQUID_OSMIUM.getGasStack(1_800));
    }

    public static boolean addGasMapping(@Nonnull ItemStackIngredient ingredient, @Nonnull GasStack gasStack) {
        if (gasStack.isEmpty()) {
            return false;
        }
        List<ItemStackIngredient> ingredients = gasToIngredients.computeIfAbsent(gasStack.getType(), k -> new ArrayList<>());
        //TODO: Better checking at some point if the ingredient is already in there? Should partial checking happen as well
        ingredients.add(ingredient);
        return ingredientToGas.put(ingredient, gasStack) == null;
    }

    public static int removeGasMapping(@Nonnull ItemStackIngredient ingredient, @Nonnull GasStack gasStack) {
        if (gasStack.isEmpty()) {
            return 0;
        }
        Gas gas = gasStack.getType();
        if (gasToIngredients.containsKey(gas)) {
            List<ItemStackIngredient> ingredients = gasToIngredients.get(gas);
            List<ItemStackIngredient> toRemove = new ArrayList<>();
            for (ItemStackIngredient stored : ingredients) {
                if (stored.equals(ingredient)) {
                    //TODO: Better comparision??? Doesn't really matter until we have better duplication handling
                    // or have proper handling for if something is registered as an ore dict and as an item
                    // Note: I am not even sure it currently properly matches, given I don't think we override equals
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
     * Gets an item gas checking if it will be valid for a specific tank and if the type is also valid.
     */
    @Nonnull
    public static GasStack getItemGas(ItemStack itemStack, GasTank gasTank, Predicate<@NonNull Gas> isValidGas) {
        return getItemGas(itemStack, gasTank.getNeeded(), (gas, quantity) -> {
            if (!gas.isEmptyType() && gasTank.canReceive(gas) && isValidGas.test(gas)) {
                return new GasStack(gas, quantity);
            }
            return GasStack.EMPTY;
        });
    }

    /**
     * Gets the amount of ticks the declared itemstack can fuel this machine.
     *
     * @param itemStack - itemstack to check with.
     * @param needed    The max amount we need for use with IGasItem's so that we do not return a value that is too large, thus making it so it thinks there is no room.
     *
     * @return fuel ticks
     */
    @Nonnull
    public static GasStack getItemGas(ItemStack itemStack, int needed, BiFunction<@NonNull Gas, Integer, @NonNull GasStack> getIfValid) {
        if (itemStack.getItem() instanceof IGasItem) {
            IGasItem item = (IGasItem) itemStack.getItem();
            GasStack gas = item.getGas(itemStack);
            //Check to make sure it can provide the gas it contains
            if (!gas.isEmpty() && item.canProvideGas(itemStack, gas.getType())) {
                int amount = Math.min(needed, Math.min(gas.getAmount(), item.getRate(itemStack)));
                if (amount > 0) {
                    GasStack gasStack = getIfValid.apply(gas.getType(), amount);
                    if (!gasStack.isEmpty()) {
                        return gasStack;
                    }
                }
            }
        }
        for (Entry<ItemStackIngredient, GasStack> entry : ingredientToGas.entrySet()) {
            //TODO: Double check if this should be this or testType
            if (entry.getKey().test(itemStack)) {
                GasStack gasStack = getIfValid.apply(entry.getValue().getType(), entry.getValue().getAmount());
                if (!gasStack.isEmpty()) {
                    return gasStack;
                }
            }
        }
        return GasStack.EMPTY;
    }

    public static List<ItemStack> getStacksForGas(@Nonnull Gas type) {
        if (type.isEmptyType()) {
            return Collections.emptyList();
        }
        List<ItemStack> stacks = new ArrayList<>();
        //Always include the gas tank of the type
        stacks.add(MekanismUtils.getFullGasTank(GasTankTier.BASIC, type));
        //See if there are any gas to item mappings
        List<ItemStackIngredient> ingredients = gasToIngredients.get(type);
        if (ingredients == null) {
            return stacks;
        }
        for (ItemStackIngredient ingredient : ingredients) {
            stacks.addAll(ingredient.getRepresentations());
        }
        return stacks;
    }
}