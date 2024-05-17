package mekanism.api.recipes.ingredients.creator;

import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.chemical.IChemicalIngredient;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;

@NothingNullByDefault
public interface IChemicalStackIngredientCreator<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends IChemicalIngredient<CHEMICAL, INGREDIENT>, STACK_INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK, INGREDIENT>>
      extends IIngredientCreator<CHEMICAL, STACK, STACK_INGREDIENT> {

    /**
     * {@return the basic internal chemical ingredient creator}
     *
     * @since 10.6.0
     */
    IChemicalIngredientCreator<CHEMICAL, INGREDIENT> chemicalCreator();

    @Override
    default STACK_INGREDIENT from(STACK instance) {
        Objects.requireNonNull(instance, "ChemicalStackIngredients cannot be created from a null ChemicalStack.");
        return from(instance.getChemical(), instance.getAmount());
    }

    @Override
    default STACK_INGREDIENT from(CHEMICAL instance, int amount) {
        return from(instance, (long) amount);
    }

    /**
     * Creates a Chemical Stack Ingredient that matches a provided chemical and amount.
     *
     * @param provider Chemical provider that provides the chemical to match.
     * @param amount   Amount needed.
     *
     * @throws NullPointerException     if the given provider is null.
     * @throws IllegalArgumentException if the given provider is empty or an amount smaller than one.
     */
    default STACK_INGREDIENT from(IChemicalProvider<CHEMICAL> provider, long amount) {
        Objects.requireNonNull(provider, "ChemicalStackIngredients cannot be created from a null chemical provider.");
        return from(chemicalCreator().of(provider), amount);
    }

    /**
     * Creates a Chemical Stack Ingredient that matches a provided chemical and amount.
     *
     * @param instance Chemical to match.
     * @param amount   Amount needed.
     *
     * @throws NullPointerException     if the given instance is null.
     * @throws IllegalArgumentException if the given instance is empty or an amount smaller than one.
     * @since 10.5.0
     */
    default STACK_INGREDIENT fromHolder(Holder<CHEMICAL> instance, long amount) {
        return from(instance.value(), amount);
    }

    @Override
    default STACK_INGREDIENT from(TagKey<CHEMICAL> tag, int amount) {
        return from(tag, (long) amount);
    }

    /**
     * Creates a Chemical Stack Ingredient that matches a given chemical tag and amount.
     *
     * @param tag    Tag to match.
     * @param amount Amount needed.
     *
     * @throws NullPointerException     if the given tag is null.
     * @throws IllegalArgumentException if the given amount smaller than one.
     */
    default STACK_INGREDIENT from(TagKey<CHEMICAL> tag, long amount) {
        Objects.requireNonNull(tag, "ChemicalStackIngredients cannot be created from a null tag.");
        return from(chemicalCreator().tag(tag), amount);
    }

    /**
     * Creates a Chemical Stack Ingredient that matches a given chemical ingredient and amount.
     *
     * @param ingredient Ingredient to match.
     * @param amount     Amount needed.
     *
     * @throws NullPointerException     if the given ingredient is null.
     * @throws IllegalArgumentException if the ingredient is explicitly empty or the given amount smaller than one.
     */
    STACK_INGREDIENT from(INGREDIENT ingredient, long amount);
}