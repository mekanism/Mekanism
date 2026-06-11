package mekanism.api.recipes.ingredients.chemical;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.recipes.ingredients.creator.IChemicalIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.core.Holder;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import org.jspecify.annotations.Nullable;

/// This class serves as the chemical analogue of an item [Ingredient], that is, a representation of both a [predicate][#test] to test [Chemical]s against, and a
/// [list][#getChemicalHolders] of matching chemicals for e.g. display purposes.
///
/// @see mekanism.api.recipes.ingredients.ChemicalStackIngredient
/// @since 10.6.0
public abstract sealed class ChemicalIngredient implements Predicate<Holder<Chemical>> permits CompoundChemicalIngredient, DifferenceChemicalIngredient,
      EmptyChemicalIngredient, IntersectionChemicalIngredient, SingleChemicalIngredient, TagChemicalIngredient {

    //TODO - 26.1: Refactor this to make sure it is like FluidIngredient, and maybe switch from Holder<Chemical> to ChemicalResource in case we at some point let chemicals have data components

    @Nullable
    private List<Holder<Chemical>> chemicalHolders;

    /// Checks if a given chemical matches this ingredient.
    ///
    /// @param chemical the chemical to test
    ///
    /// @return `true` if the chemical matches, `false` otherwise
    ///
    /// @since 10.7.11
    @Override
    public abstract boolean test(Holder<Chemical> chemical);

    /// Generates a stream of all chemicals this ingredient matches against.
    ///
    /// Unlike fluid and item ingredients, as chemicals have no data components, this should be exhaustive and perfectly accurate.
    /// - It is important that the returned chemicals correspond exactly to all the accepted [Chemical]s.
    /// - At least one chemical should always be returned, otherwise the ingredient may be considered [accidentally empty][#hasNoChemicals()].
    ///
    /// @return a stream of all chemicals this ingredient accepts.
    ///
    /// @see ICustomIngredient#items()
    /// @since 10.7.11
    public abstract Stream<Holder<Chemical>> generateChemicals();

    /// {@return a list of chemicals this ingredient accepts}
    ///
    /// @see #generateChemicals()
    /// @since 10.7.11
    public final List<Holder<Chemical>> getChemicalHolders() {
        if (chemicalHolders == null) {
            chemicalHolders = generateChemicals().toList();
        }
        return chemicalHolders;
    }

    /// Checks if this ingredient is **explicitly empty**, i.e. equal to [IChemicalIngredientCreator#empty()].
    ///
    /// Note: This does _not_ return true for "accidentally empty" ingredients, including compound ingredients that are explicitly constructed with no children or
    /// intersection / difference ingredients that resolve to an empty set.
    ///
    /// @return `true` if this ingredient is [IChemicalIngredientCreator#empty()], `false` otherwise
    public final boolean isEmpty() {
        return this == IngredientCreatorAccess.chemical().empty();
    }

    /// Checks if this ingredient matches no chemicals, i.e. if its list of [matching chemicals][#getChemicalHolders()] is empty.
    ///
    /// Note that this method explicitly **resolves** the ingredient; if this is not desired, you will need to check for emptiness another way!
    ///
    /// @return `true` if this ingredient matches no chemicals, `false` otherwise
    ///
    /// @see #isEmpty()
    public final boolean hasNoChemicals() {
        return getChemicalHolders().isEmpty();
    }

    public abstract void logMissingTags();

    /// {@return The type of this chemical ingredient.}
    ///
    /// The type **must** be registered to the corresponding type register.
    ///
    /// @see MekanismAPI#CHEMICAL_INGREDIENT_TYPES
    public abstract MapCodec<? extends ChemicalIngredient> codec();

    //Force overriding
    @Override
    public abstract int hashCode();

    //Force overriding
    @Override
    public abstract boolean equals(@Nullable Object obj);
}