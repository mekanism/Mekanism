package mekanism.common.recipe.lookup.cache.type;

import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.lib.collection.ItemHashStrategy;
import mekanism.common.recipe.ingredient.creator.ItemStackIngredientCreator.MultiItemStackIngredient;
import mekanism.common.recipe.ingredient.creator.ItemStackIngredientCreator.SingleItemStackIngredient;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;

public class ItemInputCache<RECIPE extends MekanismRecipe> extends ComponentSensitiveInputCache<Item, ItemStack, ItemStackIngredient, RECIPE> {

    public ItemInputCache() {
        super(ItemHashStrategy.INSTANCE);
    }

    @Override
    public boolean mapInputs(RECIPE recipe, ItemStackIngredient inputIngredient) {
        if (inputIngredient instanceof SingleItemStackIngredient single) {
            return mapIngredient(recipe, single.getInputRaw().ingredient());
        } else if (inputIngredient instanceof MultiItemStackIngredient multi) {
            return mapMultiInputs(recipe, multi);
        }
        //This should never really happen as we don't really allow for custom ingredients especially for networking,
        // but if it does add it as a fallback
        return true;
    }

    private boolean mapIngredient(RECIPE recipe, Ingredient input) {
        if (input.isSimple()) {
            //Simple ingredients don't actually check anything related to NBT,
            // so we can add the items to our base/raw input cache directly
            for (ItemStack item : input.getItems()) {
                if (!item.isEmpty()) {
                    //Ignore empty stacks as some mods have ingredients that some stacks are empty
                    addInputCache(item.getItem(), recipe);
                }
            }
        } else if (input.getCustomIngredient() instanceof CompoundIngredient compoundIngredient) {
            //Special handling for neo's compound ingredient to map all children as best as we can
            // as maybe some of them are simple
            boolean result = false;
            for (Ingredient child : compoundIngredient.children()) {
                result |= mapIngredient(recipe, child);
            }
            return result;
        } else if (input.getCustomIngredient() instanceof DataComponentIngredient componentIngredient && componentIngredient.isStrict()) {
            //Special handling for neo's NBT Ingredient as it requires an exact component match
            for (ItemStack item : input.getItems()) {
                //Note: We copy it with a count of one, as we need to copy it anyway to ensure nothing somehow causes our backing map to mutate it,
                // so while we are at it, we just set the size to one, as we don't care about the size
                addNbtInputCache(item.copyWithCount(1), recipe);
            }
        } else {
            //Else it is a custom ingredient, so we don't have a great way of handling it using the normal extraction checks
            // and instead have to just mark it as complex and test as needed
            return true;
        }
        return false;
    }

    @Override
    protected Item createKey(ItemStack stack) {
        return stack.getItem();
    }

    @Override
    public boolean isEmpty(ItemStack input) {
        return input.isEmpty();
    }
}