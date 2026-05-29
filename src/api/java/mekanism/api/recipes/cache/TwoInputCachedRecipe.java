package mekanism.api.recipes.cache;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.TwoInputMekRecipe;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.core.TypedInstance;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

/**
 * Base class to help implement handling of recipes with two inputs.
 */
@NothingNullByDefault
public class TwoInputCachedRecipe<HOLDER_A, INPUT_A extends TypedInstance<HOLDER_A>, INGREDIENT_A extends InputIngredient<HOLDER_A, INPUT_A>,
      HOLDER_B, INPUT_B extends TypedInstance<HOLDER_B>, INGREDIENT_B extends InputIngredient<HOLDER_B, INPUT_B>,
      OUTPUT, RECIPE extends TwoInputMekRecipe<HOLDER_A, INPUT_A, INGREDIENT_A, HOLDER_B, INPUT_B, INGREDIENT_B, ?, OUTPUT>> extends CachedRecipe<RECIPE> {

    protected final IInputHandler<HOLDER_A, INPUT_A> inputHandler;
    protected final IInputHandler<HOLDER_B, INPUT_B> secondaryInputHandler;
    protected final IOutputHandler<OUTPUT> outputHandler;
    protected final BiConsumer<INPUT_A, INPUT_B> inputsSetter;
    protected final Consumer<OUTPUT> outputSetter;
    protected final Supplier<INGREDIENT_A> inputASupplier;
    protected final Supplier<INGREDIENT_B> inputBSupplier;
    protected final BiFunction<INPUT_A, INPUT_B, OUTPUT> outputGetter;

    //Note: Our inputs and outputs shouldn't be null in places they are actually used, but we mark them as nullable, so we don't have to initialize them
    @Nullable
    private INPUT_A input;
    @Nullable
    private INPUT_B secondaryInput;
    @Nullable
    private OUTPUT output;

    /**
     * @param recipe                   Recipe.
     * @param recheckAllErrors         Returns {@code true} if processing should be continued even if an error is hit in order to gather all the errors. It is recommended
     *                                 to not do this every tick or if there is no one viewing recipes.
     * @param inputHandler             Main input handler.
     * @param secondaryInputHandler    Secondary input handler.
     * @param outputHandler            Output handler.
     */
    public TwoInputCachedRecipe(RECIPE recipe, BooleanSupplier recheckAllErrors, IInputHandler<HOLDER_A, INPUT_A> inputHandler,
          IInputHandler<HOLDER_B, INPUT_B> secondaryInputHandler, IOutputHandler<OUTPUT> outputHandler) {
        super(recipe, recheckAllErrors);
        this.inputHandler = Objects.requireNonNull(inputHandler, "Input handler cannot be null.");
        this.secondaryInputHandler = Objects.requireNonNull(secondaryInputHandler, "Secondary input handler cannot be null.");
        this.outputHandler = Objects.requireNonNull(outputHandler, "Output handler cannot be null.");
        this.inputASupplier = recipe::getInputA;
        this.inputBSupplier = recipe::getInputB;
        this.outputGetter = recipe::getOutput;
        this.inputsSetter = (input, secondary) -> {
            this.input = input;
            this.secondaryInput = secondary;
        };
        this.outputSetter = output -> this.output = output;
    }

    @Override
    protected final void calculateOperationsThisTick(OperationTracker tracker) {
        super.calculateOperationsThisTick(tracker);
        calculateOperations(tracker);
    }

    protected void calculateOperations(OperationTracker tracker) {
        CachedRecipeHelper.twoInputCalculateOperationsThisTick(tracker, inputHandler, inputASupplier, secondaryInputHandler, inputBSupplier, inputsSetter,
              outputHandler, this.outputGetter, outputSetter);
    }

    @Override
    public boolean isInputValid() {
        INPUT_A input = inputHandler.getInput();
        if (inputHandler.isEmpty(input)) {
            return false;
        }
        INPUT_B secondaryInput = secondaryInputHandler.getInput();
        return !secondaryInputHandler.isEmpty(secondaryInput) && recipe.test(input, secondaryInput);
    }

    @Override
    protected boolean finishProcessing(int operations, TransactionContext transaction) {
        return inputHandler.use(input, operations, transaction) &&
               secondaryInputHandler.use(secondaryInput, operations, transaction) &&
               outputHandler.handleOutput(output, operations, transaction);
    }
}