package mekanism.api.recipes;

import java.util.List;
import java.util.function.BiPredicate;
import mekanism.api.recipes.ingredients.InputIngredient;
import net.minecraft.core.TypedInstance;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.Contract;

/// Base class to make implementing two input recipes easier
///
/// @since 10.8.0
public abstract class TwoInputMekRecipe<HOLDER_A, STACK_A extends TypedInstance<HOLDER_A>, INPUT_A extends InputIngredient<HOLDER_A, STACK_A>,
      HOLDER_B, STACK_B extends TypedInstance<HOLDER_B>, INPUT_B extends InputIngredient<HOLDER_B, STACK_B>, VANILLA_INPUT extends RecipeInput, OUTPUT>
      extends MekanismRecipe<VANILLA_INPUT> implements BiPredicate<STACK_A, STACK_B> {

    /// Gets the first input ingredient.
    public abstract INPUT_A getInputA();

    /// Gets the second input ingredient.
    public abstract INPUT_B getInputB();

    @Override
    public final boolean test(STACK_A stackA, STACK_B stackB) {
        return getInputA().test(stackA) && getInputB().test(stackB);
    }

    /// Gets a new output based on the given input.
    ///
    /// @param inputA First specific input.
    /// @param inputB Second specific input.
    ///
    /// @return New output.
    ///
    /// @apiNote While Mekanism does not currently make use of the input, it is important to support it and pass the proper value in case any addons define input based
    /// outputs where things like NBT may be different
    @Contract(pure = true)
    public abstract OUTPUT getOutput(TypedInstance<HOLDER_A> inputA, TypedInstance<HOLDER_B> inputB);

    /// For JEI, gets the output representations to display.
    ///
    /// @return Representation of the output, **MUST NOT** be modified.
    public abstract List<OUTPUT> getOutputDefinition();

    @Override
    public boolean isIncomplete() {
        return getInputA().hasNoMatchingInstances() || getInputB().hasNoMatchingInstances();
    }

    @Override
    public void logMissingTags() {
        getInputA().logMissingTags();
        getInputB().logMissingTags();
    }
}