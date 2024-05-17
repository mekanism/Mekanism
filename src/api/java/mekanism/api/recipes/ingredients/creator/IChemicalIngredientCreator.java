package mekanism.api.recipes.ingredients.creator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.recipes.ingredients.chemical.IChemicalIngredient;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * @since 10.6.0
 */
@NothingNullByDefault
public interface IChemicalIngredientCreator<CHEMICAL extends Chemical<CHEMICAL>, INGREDIENT extends IChemicalIngredient<CHEMICAL, INGREDIENT>> {

    /**
     * A codec that is used to represent basic "single chemical" or "tag" chemical ingredients directly, similar to {@link Ingredient.Value#CODEC}, except not using value
     * subclasses and instead directly providing the corresponding {@link IChemicalIngredient}.
     */
    MapCodec<INGREDIENT> singleOrTagCodec();

    /**
     * A codec that represents a single {@code IChemicalIngredient} in map form; either dispatched by type or falling back to {@link #singleOrTagCodec} if no type is
     * specified.
     *
     * @see Ingredient#MAP_CODEC_NONEMPTY
     */
    MapCodec<INGREDIENT> mapCodecNonEmpty();

    /**
     * Simple codec representing a list of {@code IChemicalIngredient}s.
     *
     * @see #listCodecNonEmpty()
     * @see #listCodecMultipleElements()
     */
    Codec<List<INGREDIENT>> listCodec();

    /**
     * Simple codec representing a list of {@code IChemicalIngredient}s, that requires at least one element.
     *
     * @see #listCodec()
     * @see #listCodecMultipleElements()
     */
    Codec<List<INGREDIENT>> listCodecNonEmpty();

    /**
     * Simple codec representing a list of {@code IChemicalIngredient}s, that requires at least two element.
     *
     * @see #listCodec()
     * @see #listCodecNonEmpty()
     */
    Codec<List<INGREDIENT>> listCodecMultipleElements();

    /**
     * Full codec representing a chemical ingredient in all possible forms.
     * <p>
     * Allows for arrays of chemical ingredients to be read as a {@link mekanism.api.recipes.ingredients.chemical.CompoundChemicalIngredient}, as well as for the
     * {@code type} field to be left out in case of a single chemical or tag ingredient.
     *
     * @see #mapCodecNonEmpty
     */
    Codec<INGREDIENT> codec();

    /**
     * Same as {@link #codec}, except does not allow empty ingredients ({@code []}) to be specified.
     */
    Codec<INGREDIENT> codecNonEmpty();

    /**
     * Stream codec for syncing ingredients over the network.
     *
     * @implNote As all chemical ingredients are simple, it gets synced to the client as a list of supported chemicals.
     */
    StreamCodec<RegistryFriendlyByteBuf, INGREDIENT> streamCodec();

    /**
     * Retrieves the explicit empty instance ingredient for this chemical type.
     *
     * @see mekanism.api.recipes.ingredients.chemical.EmptyChemicalIngredient
     */
    INGREDIENT empty();

    /**
     * Retrieves the explicit empty instance ingredient for this chemical type.
     * <p>
     * Overload for {@link #empty()}
     *
     * @see mekanism.api.recipes.ingredients.chemical.EmptyChemicalIngredient
     */
    default INGREDIENT of() {
        return empty();
    }

    /**
     * Creates a {@link mekanism.api.recipes.ingredients.chemical.SingleChemicalIngredient} matching the chemical for the given stack.
     *
     * @param stack Chemical to match
     */
    default INGREDIENT of(ChemicalStack<CHEMICAL> stack) {
        return of(stack.getChemical());
    }

    /**
     * Creates a {@link mekanism.api.recipes.ingredients.chemical.SingleChemicalIngredient} matching the chemical for the given provider.
     *
     * @param chemicalProvider Chemical to match
     */
    default INGREDIENT of(IChemicalProvider<CHEMICAL> chemicalProvider) {
        return of(chemicalProvider.getChemical().getAsHolder());
    }

    /**
     * Creates a {@link mekanism.api.recipes.ingredients.chemical.SingleChemicalIngredient} matching the chemical for the given holder.
     *
     * @param holder Chemical to match
     */
    INGREDIENT of(Holder<CHEMICAL> holder);

    /**
     * Creates a {@link mekanism.api.recipes.ingredients.chemical.TagChemicalIngredient} matching the chemicals in the given tag.
     *
     * @param tag Chemical tag to match
     */
    INGREDIENT tag(TagKey<CHEMICAL> tag);

    /**
     * Creates a {@link mekanism.api.recipes.ingredients.chemical.CompoundChemicalIngredient} matching the chemicals for the given stacks.
     *
     * @param chemicals Chemicals to match
     *
     * @implNote This method is subtly different from {@link #compound(List)} as if there is no elements this method will return {@link #empty()}, and if there is one
     * element, this will return a {@link mekanism.api.recipes.ingredients.chemical.SingleChemicalIngredient}.
     */
    default INGREDIENT of(ChemicalStack<CHEMICAL>... chemicals) {
        return of(Arrays.stream(chemicals).map(ChemicalStack::getChemical));
    }

    /**
     * Creates a {@link mekanism.api.recipes.ingredients.chemical.CompoundChemicalIngredient} matching the chemicals for the given providers.
     *
     * @param chemicalProviders Chemicals to match
     *
     * @implNote This method is subtly different from {@link #compound(List)} as if there is no elements this method will return {@link #empty()}, and if there is one
     * element, this will return a {@link mekanism.api.recipes.ingredients.chemical.SingleChemicalIngredient}.
     */
    default INGREDIENT of(IChemicalProvider<CHEMICAL>... chemicalProviders) {
        return of(Arrays.stream(chemicalProviders));
    }

    /**
     * Creates a {@link mekanism.api.recipes.ingredients.chemical.CompoundChemicalIngredient} matching the chemicals for the given providers.
     *
     * @param chemicalProviders Chemicals to match
     *
     * @implNote This method is subtly different from {@link #compound(List)} as if there is no elements this method will return {@link #empty()}, and if there is one
     * element, this will return a {@link mekanism.api.recipes.ingredients.chemical.SingleChemicalIngredient}.
     */
    default INGREDIENT of(Stream<? extends IChemicalProvider<CHEMICAL>> chemicalProviders) {
        return ofIngredients(chemicalProviders.map(this::of));
    }

    /**
     * Creates a {@link mekanism.api.recipes.ingredients.chemical.CompoundChemicalIngredient} matching the chemicals representing the union of the given ingredients.
     *
     * @param children Ingredients to union
     *
     * @implNote This method is subtly different from {@link #compound(List)} as if there is no elements this method will return {@link #empty()}, and if there is one
     * element, this will return the element.
     */
    default INGREDIENT ofIngredients(INGREDIENT... children) {
        if (children.length == 0) {
            return empty();
        } else if (children.length == 1) {
            return children[0];
        }
        return compound(List.of(children));
    }

    /**
     * Creates a {@link mekanism.api.recipes.ingredients.chemical.CompoundChemicalIngredient} matching the chemicals representing the union of the given ingredients.
     *
     * @param children Ingredients to union
     *
     * @implNote This method is subtly different from {@link #compound(List)} as if there is no elements this method will return {@link #empty()}, and if there is one
     * element, this will return the element.
     */
    default INGREDIENT ofIngredients(List<? extends INGREDIENT> children) {
        if (children.isEmpty()) {
            return empty();
        } else if (children.size() == 1) {
            return children.getFirst();
        }
        return compound(List.copyOf(children));
    }

    /**
     * Creates a {@link mekanism.api.recipes.ingredients.chemical.CompoundChemicalIngredient} matching the chemicals representing the union of the given ingredients.
     *
     * @param children Ingredients to union
     *
     * @implNote This method is subtly different from {@link #compound(List)} as if there is no elements this method will return {@link #empty()}, and if there is one
     * element, this will return the element.
     */
    default INGREDIENT ofIngredients(Stream<? extends INGREDIENT> children) {
        return ofIngredients(children.toList());
    }

    /**
     * Creates a {@link mekanism.api.recipes.ingredients.chemical.CompoundChemicalIngredient} matching the chemicals representing the union of the given ingredients.
     *
     * @param children Ingredients to union
     *
     * @throws IllegalArgumentException If children is empty or contains only a single element.
     */
    INGREDIENT compound(List<INGREDIENT> children);

    /**
     * Gets the difference of the two chemical ingredients
     *
     * @param base       Chemical ingredient that must be matched
     * @param subtracted Chemical ingredient that must not be matched
     *
     * @return A {@link mekanism.api.recipes.ingredients.chemical.DifferenceChemicalIngredient} that matches anything contained in {@code base} that is not in
     * {@code subtracted}
     */
    INGREDIENT difference(INGREDIENT base, INGREDIENT subtracted);

    /**
     * Gets an intersection chemical ingredient
     *
     * @param ingredients List of chemical ingredients to match
     *
     * @return ChemicalIngredient that only matches if all the passed ingredients match
     *
     * @throws IllegalArgumentException If ingredients is empty.
     */
    INGREDIENT intersection(INGREDIENT... ingredients);

    /**
     * Gets an intersection chemical ingredient
     *
     * @param ingredients List of chemical ingredients to match
     *
     * @return ChemicalIngredient that only matches if all the passed ingredients match
     *
     * @throws IllegalArgumentException If ingredients is empty.
     */
    INGREDIENT intersection(List<? extends INGREDIENT> ingredients);

    /**
     * Gets an intersection chemical ingredient
     *
     * @param ingredients List of chemical ingredients to match
     *
     * @return ChemicalIngredient that only matches if all the passed ingredients match
     *
     * @throws IllegalArgumentException If ingredients is empty.
     */
    default INGREDIENT intersection(Stream<? extends INGREDIENT> ingredients) {
        return intersection(ingredients.toList());
    }
}