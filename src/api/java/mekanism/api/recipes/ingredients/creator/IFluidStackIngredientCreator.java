package mekanism.api.recipes.ingredients.creator;

import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.providers.IFluidProvider;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

@NothingNullByDefault
public interface IFluidStackIngredientCreator extends IIngredientCreator<Fluid, FluidStack, FluidStackIngredient> {

    /**
     * Creates a Fluid Stack Ingredient that matches a provided fluid and amount.
     *
     * @param provider Fluid provider that provides the fluid to match.
     * @param amount   Amount needed.
     *
     * @throws NullPointerException     if the given instance is null.
     * @throws IllegalArgumentException if the given instance is empty or an amount smaller than one.
     */
    default FluidStackIngredient from(IFluidProvider provider, int amount) {
        Objects.requireNonNull(provider, "FluidStackIngredients cannot be created from a null fluid provider.");
        return from(provider.getFluidStack(amount));
    }

    @Override
    default FluidStackIngredient from(Fluid instance, int amount) {
        return from(new FluidStack(instance, amount));
    }
}