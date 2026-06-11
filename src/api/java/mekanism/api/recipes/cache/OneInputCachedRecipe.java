package mekanism.api.recipes.cache;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.api.recipes.SingleInputRecipe;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.core.TypedInstance;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

/// Base class to help implement handling of recipes with one input.
public class OneInputCachedRecipe<HOLDER, INPUT extends TypedInstance<HOLDER>, INGREDIENT extends InputIngredient<HOLDER, INPUT>, OUTPUT,
      RECIPE extends SingleInputRecipe<HOLDER, INPUT, INGREDIENT, ?, OUTPUT>> extends CachedRecipe<RECIPE> {

    private final IInputHandler<HOLDER, INPUT> inputHandler;
    private final IOutputHandler<OUTPUT> outputHandler;
    private final Supplier<INGREDIENT> inputSupplier;
    private final Function<INPUT, OUTPUT> outputGetter;
    private final Consumer<INPUT> inputSetter;
    private final Consumer<OUTPUT> outputSetter;

    //Note: Our input and output shouldn't be null in places they are actually used, but we mark them as nullable, so we don't have to initialize them
    @Nullable
    private INPUT input;
    @Nullable
    private OUTPUT output;

    /// @param recipe           Recipe.
    /// @param recheckAllErrors Returns `true` if processing should be continued even if an error is hit in order to gather all the errors. It is recommended to not do
    /// do this every tick or if there is no one viewing recipes.
    /// @param inputHandler     Input handler.
    /// @param outputHandler    Output handler.
    public OneInputCachedRecipe(RECIPE recipe, BooleanSupplier recheckAllErrors, IInputHandler<HOLDER, INPUT> inputHandler, IOutputHandler<OUTPUT> outputHandler) {
        super(recipe, recheckAllErrors);
        this.inputHandler = Objects.requireNonNull(inputHandler, "Input handler cannot be null.");
        this.outputHandler = Objects.requireNonNull(outputHandler, "Output handler cannot be null.");
        this.inputSupplier = recipe::getInput;
        this.outputGetter = recipe::getOutput;
        this.inputSetter = input -> this.input = input;
        this.outputSetter = output -> this.output = output;
    }

    @Override
    protected void calculateOperationsThisTick(OperationTracker tracker) {
        super.calculateOperationsThisTick(tracker);
        CachedRecipeHelper.oneInputCalculateOperationsThisTick(tracker, inputHandler, inputSupplier, inputSetter, outputHandler, outputGetter, outputSetter);
    }

    @Override
    public boolean isInputValid() {
        INPUT input = inputHandler.getInput();
        return !inputHandler.isEmpty(input) && recipe.test(input);
    }

    @Override
    protected boolean finishProcessing(int operations, TransactionContext transaction) {
        return inputHandler.use(input, operations, transaction) &&
               outputHandler.handleOutput(output, operations, transaction);
    }
}