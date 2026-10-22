package mekanism.api.recipes.display.recipe;

/// Represents a recipe display where two inputs are combined in a specific order to create a result, with the secondary input potentially being consumed each tick.
///
/// @since 10.8.0
public interface TwoInputPerTickRecipeDisplay extends TwoInputRecipeDisplay {

    /// {@return whether the recipe consumes the secondary display each tick}
    boolean perTickSecondary();
}