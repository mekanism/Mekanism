package mekanism.api.recipes.ingredients;

import com.google.gson.JsonElement;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

/**
 * Base implementation for how Mekanism handle's FluidStack Ingredients.
 * <p>
 * Create instances of this using {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#fluid()}.
 */
public abstract class FluidStackIngredient implements InputIngredient<@NotNull FluidStack> {

    @NotNull
    @Override
    public final JsonElement serialize() {
        return IngredientCreatorAccess.fluid().serialize(this);
    }
}