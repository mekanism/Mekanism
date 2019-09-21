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
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.IGasItem;
import mekanism.api.providers.IGasProvider;
import mekanism.common.MekanismGases;
import mekanism.common.recipe.ingredients.IMekanismIngredient;
import mekanism.common.recipe.ingredients.ItemStackMekIngredient;
import mekanism.common.recipe.ingredients.TagMekIngredient;
import mekanism.common.tier.GasTankTier;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

//TODO: Move the conversion handler to API??
public class GasConversionHandler {

    //TODO: Show uses in JEI for fuels that can be turned to gas??
    private final static Map<@NonNull Gas, List<IMekanismIngredient<ItemStack>>> gasToIngredients = new HashMap<>();
    private final static Map<IMekanismIngredient<ItemStack>, GasStack> ingredientToGas = new HashMap<>();

    public static void addDefaultGasMappings() {
        ItemTags.Wrapper sulfur = new ItemTags.Wrapper(new ResourceLocation("forge", "dusts/sulfur"));
        ItemTags.Wrapper salt = new ItemTags.Wrapper(new ResourceLocation("forge", "dusts/salt"));
        ItemTags.Wrapper osmiumIngot = new ItemTags.Wrapper(new ResourceLocation("forge", "ingots/osmium"));
        ItemTags.Wrapper osmiumBlock = new ItemTags.Wrapper(new ResourceLocation("forge", "storage_blocks/osmium"));
        addGasMapping(new ItemStack(Items.FLINT), MekanismGases.OXYGEN, 10);
        addGasMapping(sulfur, MekanismGases.SULFURIC_ACID, 2);
        addGasMapping(salt, MekanismGases.HYDROGEN_CHLORIDE, 2);
        addGasMapping(osmiumIngot, MekanismGases.LIQUID_OSMIUM, 200);
        addGasMapping(osmiumBlock, MekanismGases.LIQUID_OSMIUM, 1_800);
    }

    public static boolean addGasMapping(@Nonnull ItemStack stack, @Nonnull IGasProvider gasProvider, int amount) {
        return addGasMapping(new ItemStackMekIngredient(stack), new GasStack(gasProvider, amount));
    }

    public static boolean addGasMapping(@Nonnull Tag<Item> tag, @Nonnull IGasProvider gasProvider, int amount) {
        return addGasMapping(new TagMekIngredient(tag), new GasStack(gasProvider, amount));
    }

    public static boolean addGasMapping(@Nonnull IMekanismIngredient<ItemStack> ingredient, @Nonnull GasStack gasStack) {
        if (gasStack.isEmpty()) {
            return false;
        }
        List<IMekanismIngredient<ItemStack>> ingredients = gasToIngredients.computeIfAbsent(gasStack.getGas(), k -> new ArrayList<>());
        //TODO: Better checking at some point if the ingredient is already in there? Should partial checking happen as well
        ingredients.add(ingredient);
        return ingredientToGas.put(ingredient, gasStack) == null;
    }

    public static int removeGasMapping(@Nonnull IMekanismIngredient<ItemStack> ingredient, @Nonnull GasStack gasStack) {
        if (gasStack.isEmpty()) {
            return 0;
        }
        Gas gas = gasStack.getGas();
        if (gasToIngredients.containsKey(gas)) {
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
     * Gets an item gas checking if it will be valid for a specific tank and if the type is also valid.
     */
    @Nonnull
    public static GasStack getItemGas(ItemStack itemStack, GasTank gasTank, Predicate<@NonNull Gas> isValidGas) {
        return getItemGas(itemStack, gasTank.getNeeded(), (gas, quantity) -> {
            if (gas != MekanismAPI.EMPTY_GAS && gasTank.canReceive(gas) && isValidGas.test(gas)) {
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
            if (!gas.isEmpty() && item.canProvideGas(itemStack, gas.getGas())) {
                int amount = Math.min(needed, Math.min(gas.getAmount(), item.getRate(itemStack)));
                if (amount > 0) {
                    GasStack gasStack = getIfValid.apply(gas.getGas(), amount);
                    if (!gasStack.isEmpty()) {
                        return gasStack;
                    }
                }
            }
        }
        for (Entry<IMekanismIngredient<ItemStack>, GasStack> entry : ingredientToGas.entrySet()) {
            if (entry.getKey().contains(itemStack)) {
                GasStack gasStack = getIfValid.apply(entry.getValue().getGas(), entry.getValue().getAmount());
                if (!gasStack.isEmpty()) {
                    return gasStack;
                }
            }
        }
        return GasStack.EMPTY;
    }

    public static List<ItemStack> getStacksForGas(@Nonnull Gas type) {
        if (type == MekanismAPI.EMPTY_GAS) {
            return Collections.emptyList();
        }
        List<ItemStack> stacks = new ArrayList<>();
        //Always include the gas tank of the type
        stacks.add(MekanismUtils.getFullGasTank(GasTankTier.BASIC, type));
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