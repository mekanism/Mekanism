package mekanism.api.recipes.ingredients;

import com.google.gson.JsonElement;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Base implementation for how Mekanism handle's ItemStack Ingredients.
 * <p>
 * Create instances of this using {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#item()}.
 */
public abstract class ItemStackIngredient implements InputIngredient<@NotNull ItemStack> {

    @NotNull
    @Override
    public final JsonElement serialize() {
        return IngredientCreatorAccess.item().serialize(this);
    }
}