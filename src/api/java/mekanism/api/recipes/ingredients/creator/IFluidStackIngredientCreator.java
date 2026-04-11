package mekanism.api.recipes.ingredients.creator;

import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.DataComponentFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

@NothingNullByDefault
public interface IFluidStackIngredientCreator extends IIngredientCreator<Fluid, FluidStack, FluidStackIngredient> {

    /**
     * @implNote This wraps via {@link #from(FluidIngredient, int)} so if there are any default components it will <strong>NOT</strong> be included in the
     * ingredient. If this is not desired, manually create the ingredient via {@link DataComponentFluidIngredient} and call {@link #from(FluidIngredient, int)}.
     * @since 10.7.11
     */
    @Override
    @SuppressWarnings("unchecked")
    default FluidStackIngredient fromHolders(int amount, Holder<Fluid>... fluids) {
        if (fluids.length == 0) {
            throw new IllegalArgumentException("Attempted to create a FluidStackIngredient with no fluids.");
        }
        return from(FluidIngredient.of(HolderSet.direct(List.of(fluids))), amount);
    }

    /**
     * @implNote This wraps via {@link #from(FluidIngredient, int)} so if there are any default components it will <strong>NOT</strong> be included in the ingredient. If
     * this is not desired, manually create the ingredient via {@link DataComponentFluidIngredient} and call {@link #from(FluidIngredient, int)}.
     */
    @Override
    default FluidStackIngredient fromHolder(Holder<Fluid> instance, int amount) {
        return from(FluidIngredient.of(instance.value()), amount);
    }

    /**
     * @implNote If the stack has any non-default data components, a non-strict component matching those additions will be used.
     */
    @Override
    default FluidStackIngredient from(FluidStack instance) {
        Objects.requireNonNull(instance, "FluidStackIngredients cannot be created from a null FluidStack.");
        if (instance.isEmpty()) {
            throw new IllegalArgumentException("FluidStackIngredients cannot be created using the empty stack.");
        }
        //Copy the stack to ensure it doesn't get modified afterward
        instance = instance.copy();
        //Support Components that are on the stack in case it matters
        // Note: Only bother making it a data component ingredient if the stack has non-default data, otherwise there is no point in doing the extra checks
        DataComponentExactPredicate predicate = IngredientCreatorAccess.getComponentPatchPredicate(instance.getComponentsPatch());
        if (predicate != null) {
            return from(DataComponentFluidIngredient.of(false, predicate, instance.typeHolder()), instance.getAmount());
        }
        return from(SizedFluidIngredient.of(instance.getFluid(), instance.getAmount()));
    }

    @Override
    default FluidStackIngredient from(HolderGetter<Fluid> holderGetter, TagKey<Fluid> tag, int amount) {
        Objects.requireNonNull(tag, "FluidStackIngredients cannot be created from a null tag.");
        return from(new SizedFluidIngredient(FluidIngredient.of(holderGetter.getOrThrow(tag)), amount));
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