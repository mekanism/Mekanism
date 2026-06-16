package mekanism.common.recipe.lookup.cache.type;

import java.util.List;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

//TODO - 26.2 - should it still use Item or should we use holders?
public class ItemInputCache<RECIPE extends MekanismRecipe<?>> extends ComponentSensitiveInputCache<Item, ItemStack, ItemStackIngredient, RECIPE> {

    @Override
    public boolean mapInputs(RECIPE recipe, ItemStackIngredient inputIngredient) {
        return mapIngredient(recipe, inputIngredient.ingredient().ingredient());
    }

    private boolean mapIngredient(RECIPE recipe, Ingredient input) {
        if (input.isSimple()) {
            //Simple ingredients don't actually check anything related to NBT,
            // so we can add the items to our base/raw input cache directly
            for (Holder<Item> item : input.getValues()) {
                addInputCache(item.value(), recipe);
            }
        } else if (input.getCustomIngredient() instanceof CompoundIngredient(List<Ingredient> children)) {
            //Special handling for neo's compound ingredient to map all children as best as we can
            // as maybe some of them are simple
            boolean result = false;
            for (Ingredient child : children) {
                result |= mapIngredient(recipe, child);
            }
            return result;
        } else if (input.getCustomIngredient() instanceof DataComponentIngredient componentIngredient && componentIngredient.componentsExhaustive()) {
            //Special handling for neo's NBT Ingredient as it requires an exact component match
            DataComponentPatch components = componentIngredient.components();
            for (Holder<Item> holder : componentIngredient.itemSet()) {
                addComponentInputCache(holder, components, recipe);
            }
        } else {
            //Else it is a custom ingredient, so we don't have a great way of handling it using the normal extraction checks
            // and instead have to just mark it as complex and test as needed
            return true;
        }
        return false;
    }

    @Override
    public boolean isEmpty(@Nullable TypedInstance<Item> input) {
        return switch (input) {
            case ItemStack stack -> stack.isEmpty();
            case ItemResource resource -> resource.isEmpty();
            case null -> true;
            default -> false;
        };
    }
}