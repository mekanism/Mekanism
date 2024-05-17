package mekanism.api.recipes.ingredients.chemical;

import java.util.List;
import javax.annotation.Nullable;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * This class serves as the chemical analogue of an item {@link Ingredient}, that is, a representation of both a {@linkplain #test predicate} to test {@link Chemical}s
 * against, and a {@linkplain #getChemicals list} of matching chemicals for e.g. display purposes.
 *
 * @see mekanism.api.recipes.ingredients.ChemicalStackIngredient
 * @since 10.6.0
 */
@NothingNullByDefault
abstract sealed class ChemicalIngredient<CHEMICAL extends Chemical<CHEMICAL>, INGREDIENT extends IChemicalIngredient<CHEMICAL, INGREDIENT>>
      implements IChemicalIngredient<CHEMICAL, INGREDIENT> permits CompoundChemicalIngredient, DifferenceChemicalIngredient, EmptyChemicalIngredient,
      IntersectionChemicalIngredient, SingleChemicalIngredient, TagChemicalIngredient, GasIngredient, InfusionIngredient, PigmentIngredient, SlurryIngredient {

    @Nullable
    private List<CHEMICAL> chemicals;

    @Override
    public final List<CHEMICAL> getChemicals() {
        if (chemicals == null) {
            chemicals = generateChemicals().toList();
        }
        return chemicals;
    }

    @Override
    public final boolean isEmpty() {
        return this == ingredientCreator().empty();
    }

    @Override
    public final boolean hasNoChemicals() {
        return getChemicals().isEmpty();
    }

    //Force overriding
    @Override
    public abstract int hashCode();

    //Force overriding
    @Override
    public abstract boolean equals(Object obj);
}