package mekanism.api.recipes.ingredients;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import mekanism.api.IMekanismAccess;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

/**
 * Base implementation for how Mekanism handle's FluidStack Ingredients.
 * <p>
 * Create instances of this using {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#fluid()}.
 */
public abstract class FluidStackIngredient implements InputIngredient<@NotNull FluidStack> {

    @Override
    @Deprecated(forRemoval = true)
    public JsonElement serialize() {
        return IMekanismAccess.INSTANCE.fluidStackIngredientCreator().codec().encodeStart(JsonOps.INSTANCE, this).getOrThrow(false, e->{});
    }
}