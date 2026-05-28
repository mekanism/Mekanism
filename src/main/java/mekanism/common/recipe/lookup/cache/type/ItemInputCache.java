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
import org.jetbrains.annotations.UnknownNullability;

//TODO - 26.1 - should it still use Item or should we use holders?
public class ItemInputCache<RECIPE extends MekanismRecipe<?>> extends ComponentSensitiveInputCache<Item, ItemStack, ItemStackIngredient, RECIPE> {

    public ItemInputCache() {
        super();
    }

    @Override
    public boolean mapInputs(RECIPE recipe, ItemStackIngredient inputIngredient) {
        return mapIngredient(recipe, inputIngredient.ingredient().ingredient());
    }

    private boolean mapIngredient(RECIPE recipe, Ingredient input) {
        if (input.getCustomIngredient() instanceof CompoundIngredient(List<Ingredient> children)) {
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
                addNbtInputCache(holder, components, recipe);
            }
        } else if (input.getCustomIngredient() != null) {
            //Other custom ingredients cannot be expanded via Ingredient#getValues in 26.1,
            // so we fall back to the complex recipe path and test them on demand.
            return true;
        } else if (input.isSimple()) {
            //Simple ingredients don't actually check anything related to NBT,
            // so we can add the items to our base/raw input cache directly
            for (Holder<Item> item : input.getValues()) {
                addInputCache(item.value(), recipe);
            }
        } else {
            //Non-simple ingredients without a dedicated mapping path are treated as complex and tested on demand.
            return true;
        }
        return false;
    }

    @Override
    public boolean isEmpty(@UnknownNullability TypedInstance<Item> input) {
        return switch (input) {
            case ItemStack stack -> stack.isEmpty();
            case ItemResource resource -> resource.isEmpty();
            case null -> true;
            default -> false;
        };
    }
}
