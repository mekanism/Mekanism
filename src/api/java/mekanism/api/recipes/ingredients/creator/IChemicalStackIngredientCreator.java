package mekanism.api.recipes.ingredients.creator;

import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.tags.TagKey;

@NothingNullByDefault
public interface IChemicalStackIngredientCreator<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>> extends IIngredientCreator<CHEMICAL, STACK, INGREDIENT> {

    @Override
    default INGREDIENT from(STACK instance) {
        Objects.requireNonNull(instance, "ChemicalStackIngredients cannot be created from a null ChemicalStack.");
        return from(instance.getType(), instance.getAmount());
    }

    @Override
    default INGREDIENT from(CHEMICAL instance, int amount) {
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
    INGREDIENT from(IChemicalProvider<CHEMICAL> provider, long amount);

    @Override
    default INGREDIENT from(TagKey<CHEMICAL> tag, int amount) {
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
    INGREDIENT from(TagKey<CHEMICAL> tag, long amount);
}