package mekanism.api.recipes;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.TwoInputMekRecipe.SimpleTwoInputRecipe;
import mekanism.api.recipes.ingredients.InputIngredient;
import net.minecraft.core.TypedInstance;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.Contract;

/// Base class to make implementing two input recipes that don't care about the ingredient order easier
///
/// @since 10.8.0
@NothingNullByDefault
public abstract class OrderlessTwoInputRecipe<HOLDER_TYPE, STACK extends TypedInstance<HOLDER_TYPE>, INPUT extends InputIngredient<HOLDER_TYPE, STACK>,
      VANILLA_INPUT extends RecipeInput, OUTPUT> extends SimpleTwoInputRecipe<HOLDER_TYPE, STACK, INPUT, VANILLA_INPUT, OUTPUT> {

    /**
     * Gets a new output based on the given inputs, the order of these inputs which one is {@code input1} and which one is {@code input2} does not matter.
     *
     * @param input1 Specific "left" input.
     * @param input2 Specific "right" input.
     *
     * @return New output.
     *
     * @apiNote While Mekanism does not currently make use of the inputs, it is important to support it and pass the proper value in case any addons define input based
     * outputs where things like NBT may be different.
     * @implNote The passed in inputs should <strong>NOT</strong> be modified.
     */
    @Override
    @Contract(pure = true)
    public abstract OUTPUT getOutput(TypedInstance<HOLDER_TYPE> input1, TypedInstance<HOLDER_TYPE> input2);

    /**
     * Gets the left input ingredient.
     */
    public abstract INPUT getLeftInput();

    @Override
    public final INPUT getInputA() {
        return getLeftInput();
    }

    /**
     * Gets the right input ingredient.
     */
    public abstract INPUT getRightInput();

    @Override
    public final INPUT getInputB() {
        return getRightInput();
    }
}