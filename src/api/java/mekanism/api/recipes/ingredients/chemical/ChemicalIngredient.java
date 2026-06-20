package mekanism.api.recipes.ingredients.chemical;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.recipes.ingredients.chemical.display.ChemicalSlotDisplay;
import net.minecraft.core.Holder;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jspecify.annotations.Nullable;

/// This class serves as the chemical analogue of an item [Ingredient], that is, a representation of both a [predicate][#test] to test [Chemical]s against, and a
/// [list][#chemicals] of matching chemicals for e.g. display purposes.
///
/// @see mekanism.api.recipes.ingredients.ChemicalStackIngredient
/// @since 10.6.0
public abstract sealed class ChemicalIngredient implements Predicate<ChemicalResource> permits SimpleChemicalIngredient, CompoundChemicalIngredient,
      DifferenceChemicalIngredient, IntersectionChemicalIngredient, CustomDisplayChemicalIngredient {

    @Nullable
    private List<Holder<Chemical>> chemicals;

    /// Checks if a given chemical matches this ingredient.
    ///
    /// @param chemical the chemical to test
    ///
    /// @return `true` if the chemical matches, `false` otherwise
    ///
    /// @since 10.8.0
    @Override
    public abstract boolean test(ChemicalResource chemical);

    /// Checks if a given chemical matches this ingredient.
    ///
    /// @param chemical the chemical to test
    ///
    /// @return `true` if the chemical matches, `false` otherwise
    ///
    /// @since 10.7.11
    public final boolean test(Holder<Chemical> chemical) {
        return test(ChemicalResource.of(chemical));
    }

    /// Generates a stream of all chemicals this ingredient matches against.
    ///
    /// Unlike fluid and item ingredients, as chemicals have no data components, this should be exhaustive and perfectly accurate.
    /// - It is important that the returned chemicals correspond exactly to all the accepted [Chemical]s.
    /// - At least one chemical should always be returned, otherwise the ingredient may be considered [accidentally empty][#isEmpty()].
    ///
    /// @return a stream of all chemicals this ingredient accepts.
    ///
    /// @see net.neoforged.neoforge.common.crafting.ICustomIngredient#items()
    /// @since 10.7.11
    public abstract Stream<Holder<Chemical>> generateChemicals();

    /// {@return a list of chemicals this ingredient accepts}
    ///
    /// @see #generateChemicals()
    /// @since 10.8.0
    public final List<Holder<Chemical>> chemicals() {
        if (chemicals == null) {
            chemicals = generateChemicals().toList();
        }
        return chemicals;
    }

    /// {@return a slot display for this ingredient, used for display on the client-side}
    ///
    /// @implNote The default implementation just constructs a list of stacks from [#chemicals()]. This is generally suitable for chemical ingredients. If a more accurate
    /// display is desired, ingredients can either override this method to provide a more customized display, or let data pack writers use
    /// [CustomDisplayChemicalIngredient] to override the display of an ingredient.
    /// @see Ingredient#display()
    /// @see net.neoforged.neoforge.fluids.crafting.FluidIngredient#display()
    public SlotDisplay display() {
        return new SlotDisplay.Composite(chemicals()
              .stream()
              .<SlotDisplay>map(ChemicalSlotDisplay::new)
              .toList());
    }

    /// Checks if this ingredient matches no chemicals, i.e. if its list of [matching chemicals][#chemicals()] is empty.
    ///
    /// Note that this method explicitly **resolves** the ingredient; if this is not desired, you will need to check for emptiness another way!
    ///
    /// @return `true` if this ingredient matches no chemicals, `false` otherwise
    ///
    /// @see #isEmpty()
    public final boolean isEmpty() {
        return chemicals().isEmpty();
    }

    public abstract void logMissingTags();

    /// {@return The type of this chemical ingredient.}
    ///
    /// The type **must** be registered to the [MekanismAPI#CHEMICAL_INGREDIENT_TYPES].
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