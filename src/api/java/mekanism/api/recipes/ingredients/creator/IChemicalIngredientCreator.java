package mekanism.api.recipes.ingredients.creator;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.chemical.ChemicalIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.TypedInstance;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.display.SlotDisplay;

/// @since 10.6.0
public interface IChemicalIngredientCreator {

    /// Full codec representing a chemical ingredient in all possible forms.
    ///
    /// Allows for arrays of chemical ingredients to be read as a [mekanism.api.recipes.ingredients.chemical.CompoundChemicalIngredient], as well as for the `type` field
    /// to be left out in case of a simple precalculated chemical ingredient.
    Codec<ChemicalIngredient> codec();

    /// Stream codec for syncing ingredients over the network.
    ///
    /// @implNote As all chemical ingredients are simple, it gets synced to the client as a list of supported chemicals.
    StreamCodec<RegistryFriendlyByteBuf, ChemicalIngredient> streamCodec();

    /// Stream codec for syncing ingredients over the network.
    ///
    /// @implNote As all chemical ingredients are simple, it gets synced to the client as a list of supported chemicals.
    /// @since 10.8.0
    StreamCodec<RegistryFriendlyByteBuf, Optional<ChemicalIngredient>> optionalStreamCodec();

    /// Creates a [mekanism.api.recipes.ingredients.chemical.CompoundChemicalIngredient] matching the chemicals for the given stacks.
    ///
    /// @param chemicals Chemicals to match
    ///
    /// @throws IllegalArgumentException If children is empty.
    default ChemicalIngredient of(ChemicalStack... chemicals) {
        return of(HolderSet.direct(TypedInstance::typeHolder, chemicals));
    }

    /// Creates a [mekanism.api.recipes.ingredients.chemical.CompoundChemicalIngredient] matching the chemicals for the given providers.
    ///
    /// @param chemicalProviders Chemicals to match
    ///
    /// @throws IllegalArgumentException If children is empty.
    /// @since 10.7.11
    @SuppressWarnings("unchecked")
    default ChemicalIngredient of(Holder<Chemical>... chemicalProviders) {
        return of(HolderSet.direct(chemicalProviders));
    }

    ChemicalIngredient of(HolderSet<Chemical> chemicals);

    /// Creates a [mekanism.api.recipes.ingredients.chemical.CompoundChemicalIngredient] matching the chemicals representing the union of the given ingredients.
    ///
    /// @param children Ingredients to union
    ///
    /// @throws IllegalArgumentException If children is empty.
    default ChemicalIngredient ofIngredients(ChemicalIngredient... children) {
        return ofIngredients(List.of(children));
    }

    /// Creates a [mekanism.api.recipes.ingredients.chemical.CompoundChemicalIngredient] matching the chemicals representing the union of the given ingredients.
    ///
    /// @param children Ingredients to union
    ///
    /// @throws IllegalArgumentException If children is empty.
    ChemicalIngredient ofIngredients(List<? extends ChemicalIngredient> children);

    /// Creates a [mekanism.api.recipes.ingredients.chemical.CompoundChemicalIngredient] matching the chemicals representing the union of the given ingredients.
    ///
    /// @param children Ingredients to union
    ///
    /// @throws IllegalArgumentException If children is empty.
    default ChemicalIngredient ofIngredients(Stream<? extends ChemicalIngredient> children) {
        return ofIngredients(children.toList());
    }

    /// Gets the difference of the two chemical ingredients
    ///
    /// @param base       Chemical ingredient that must be matched
    /// @param subtracted Chemical ingredient that must not be matched
    ///
    /// @return A [mekanism.api.recipes.ingredients.chemical.DifferenceChemicalIngredient] that matches anything contained in `base` that is not in `subtracted`
    ChemicalIngredient difference(ChemicalIngredient base, ChemicalIngredient subtracted);

    /// Gets an intersection chemical ingredient
    ///
    /// @param ingredients List of chemical ingredients to match
    ///
    /// @return ChemicalIngredient that only matches if all the passed ingredients match
    ///
    /// @throws IllegalArgumentException If ingredients is empty.
    ChemicalIngredient intersection(ChemicalIngredient... ingredients);

    /// Gets an intersection chemical ingredient
    ///
    /// @param ingredients List of chemical ingredients to match
    ///
    /// @return ChemicalIngredient that only matches if all the passed ingredients match
    ///
    /// @throws IllegalArgumentException If ingredients is empty.
    ChemicalIngredient intersection(List<? extends ChemicalIngredient> ingredients);

    /// Gets an intersection chemical ingredient
    ///
    /// @param ingredients List of chemical ingredients to match
    ///
    /// @return ChemicalIngredient that only matches if all the passed ingredients match
    ///
    /// @throws IllegalArgumentException If ingredients is empty.
    default ChemicalIngredient intersection(Stream<? extends ChemicalIngredient> ingredients) {
        return intersection(ingredients.toList());
    }

    /// ChemicalIngredient that wraps another chemical ingredient to override its [SlotDisplay].
    ///
    /// @param base    Chemical ingredient that must be matched
    /// @param display Display to use in place of the `base`'s display
    ///
    /// @return A [mekanism.api.recipes.ingredients.chemical.CustomDisplayChemicalIngredient] that uses `display` for the ingredient's display.
    ///
    /// @since 10.8.0
    ChemicalIngredient customDisplay(ChemicalIngredient base, SlotDisplay display);
}