package mekanism.api.recipes.ingredients.creator;

import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.providers.IFluidProvider;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.DataComponentFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

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
        return from(SizedFluidIngredient.of(instance, amount));
    }

    @Override
    default FluidStackIngredient from(FluidStack instance) {
        //TODO - 1.20.5: Helper for this and item stack ingredient creator to create compound ingredients?
        // Maybe by having a var-arg variant of this?
        Objects.requireNonNull(instance, "FluidStackIngredients cannot be created from a null FluidStack.");
        if (instance.isEmpty()) {
            throw new IllegalArgumentException("FluidStackIngredients cannot be created using the empty stack.");
        }
        //Copy the stack to ensure it doesn't get modified afterward
        instance = instance.copy();
        //Support Components that are on the stack in case it matters
        // Note: Only bother making it a data component ingredient if the stack has data, otherwise there is no point in doing the extra checks
        DataComponentPredicate predicate = IngredientCreatorAccess.getComponentPatchPredicate(instance.getComponentsPatch());
        if (predicate != null) {
            return from(DataComponentFluidIngredient.of(false, predicate, instance.getFluidHolder()), instance.getAmount());
        }
        return from(SizedFluidIngredient.of(instance));
    }

    @Override
    default FluidStackIngredient from(TagKey<Fluid> tag, int amount) {
        Objects.requireNonNull(tag, "FluidStackIngredients cannot be created from a null tag.");
        return from(SizedFluidIngredient.of(tag, amount));
    }

    /**
     * Creates a Fluid Stack Ingredient that matches a given ingredient and amount.
     *
     * @param ingredient Ingredient to match.
     * @param amount     Amount needed.
     *
     * @throws NullPointerException     if the given instance is null.
     * @throws IllegalArgumentException if the given instance is empty or an amount smaller than one.
     * @since 10.6.0
     */
    default FluidStackIngredient from(FluidIngredient ingredient, int amount) {
        Objects.requireNonNull(ingredient, "FluidStackIngredients cannot be created from a null fluid ingredient.");
        return from(new SizedFluidIngredient(ingredient, amount));
    }

    /**
     * Creates a Fluid Stack Ingredient that matches a given ingredient and amount.
     *
     * @param ingredient Sized ingredient to match.
     *
     * @throws NullPointerException     if the given instance is null.
     * @throws IllegalArgumentException if the given instance is empty.
     * @since 10.6.0
     */
    default FluidStackIngredient from(SizedFluidIngredient ingredient) {
        return FluidStackIngredient.of(ingredient);
    }
}