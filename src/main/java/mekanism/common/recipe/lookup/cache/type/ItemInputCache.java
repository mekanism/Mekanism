package mekanism.common.recipe.lookup.cache.type;

import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.recipe.ingredient.creator.ItemStackIngredientCreator.MultiItemStackIngredient;
import mekanism.common.recipe.ingredient.creator.ItemStackIngredientCreator.SingleItemStackIngredient;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CompoundIngredient;
import net.minecraftforge.common.crafting.NBTIngredient;

public class ItemInputCache<RECIPE extends MekanismRecipe> extends NBTSensitiveInputCache<Item, HashedItem, ItemStack, ItemStackIngredient, RECIPE> {

    @Override
    public boolean mapInputs(RECIPE recipe, ItemStackIngredient inputIngredient) {
        if (inputIngredient instanceof SingleItemStackIngredient single) {
            return mapIngredient(recipe, single.getInputRaw());
        } else if (inputIngredient instanceof MultiItemStackIngredient multi) {
            return mapMultiInputs(recipe, multi);
        }
        //This should never really happen as we don't really allow for custom ingredients especially for networking,
        // but if it does add it as a fallback
        return true;
    }

    private boolean mapIngredient(RECIPE recipe, Ingredient input) {
        if (input.isVanilla() || input.isSimple()) {
            //Vanilla ingredients and simple ingredients don't actually check anything related to NBT,
            // so we can add the items to our base/raw input cache directly
            for (ItemStack item : input.getItems()) {
                addInputCache(item.getItem(), recipe);
            }
        } else if (input instanceof CompoundIngredient compoundIngredient) {
            //Special handling for forge's compound ingredient to map all children
            boolean result = false;
            for (Ingredient child : compoundIngredient.getChildren()) {
                result |= mapIngredient(recipe, child);
            }
            return result;
        } else if (input instanceof NBTIngredient) {
            //Special handling for forge's NBT Ingredient as it requires an exact NBT match
            addNbtInputCache(HashedItem.create(input.getItems()[0]), recipe);
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
    protected HashedItem createNbtKey(ItemStack stack) {
        //TODO: I don't think this is an issue currently but if it ever comes up we may need to create a
        // version of HashedItem for input cache purposes that ignores cap data
        return HashedItem.raw(stack);
    }

    @Override
    public boolean isEmpty(ItemStack input) {
        return input.isEmpty();
    }
}