package mekanism.api.recipes.cache;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.chemical.FluidChemicalToChemicalRecipe;
import mekanism.api.recipes.chemical.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/**
 * Base class to help implement handling of recipes with two inputs.
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TwoInputCachedRecipe<INPUT_A, INPUT_B, OUTPUT, RECIPE extends MekanismRecipe & BiPredicate<INPUT_A, INPUT_B>> extends CachedRecipe<RECIPE> {

    private final IInputHandler<INPUT_A> inputHandler;
    private final IInputHandler<INPUT_B> secondaryInputHandler;
    private final IOutputHandler<OUTPUT> outputHandler;
    private final Predicate<INPUT_A> inputEmptyCheck;
    private final Predicate<INPUT_B> secondaryInputEmptyCheck;
    private final Supplier<? extends InputIngredient<INPUT_A>> inputSupplier;
    private final Supplier<? extends InputIngredient<INPUT_B>> secondaryInputSupplier;
    private final BiFunction<INPUT_A, INPUT_B, OUTPUT> outputGetter;
    private final Predicate<OUTPUT> outputEmptyCheck;

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
     * @param inputSupplier            Supplier of the recipe's input ingredient.
     * @param secondaryInputSupplier   Supplier of the recipe's secondary input ingredient.
     * @param outputGetter             Gets the recipe's output when given the corresponding inputs.
     * @param inputEmptyCheck          Checks if the primary input is empty.
     * @param secondaryInputEmptyCheck Checks if the secondary input is empty.
     * @param outputEmptyCheck         Checks if the output is empty (indicating something went horribly wrong).
     */
    protected TwoInputCachedRecipe(RECIPE recipe, BooleanSupplier recheckAllErrors, IInputHandler<INPUT_A> inputHandler, IInputHandler<INPUT_B> secondaryInputHandler,
          IOutputHandler<OUTPUT> outputHandler, Supplier<InputIngredient<INPUT_A>> inputSupplier, Supplier<InputIngredient<INPUT_B>> secondaryInputSupplier,
          BiFunction<INPUT_A, INPUT_B, OUTPUT> outputGetter, Predicate<INPUT_A> inputEmptyCheck, Predicate<INPUT_B> secondaryInputEmptyCheck,
          Predicate<OUTPUT> outputEmptyCheck) {
        super(recipe, recheckAllErrors);
        this.inputHandler = Objects.requireNonNull(inputHandler, "Input handler cannot be null.");
        this.secondaryInputHandler = Objects.requireNonNull(secondaryInputHandler, "Secondary input handler cannot be null.");
        this.outputHandler = Objects.requireNonNull(outputHandler, "Output handler cannot be null.");
        this.inputSupplier = Objects.requireNonNull(inputSupplier, "Input ingredient supplier cannot be null.");
        this.secondaryInputSupplier = Objects.requireNonNull(secondaryInputSupplier, "Secondary input ingredient supplier cannot be null.");
        this.outputGetter = Objects.requireNonNull(outputGetter, "Output getter cannot be null.");
        this.inputEmptyCheck = Objects.requireNonNull(inputEmptyCheck, "Input empty check cannot be null.");
        this.secondaryInputEmptyCheck = Objects.requireNonNull(secondaryInputEmptyCheck, "Secondary input empty check cannot be null.");
        this.outputEmptyCheck = Objects.requireNonNull(outputEmptyCheck, "Output empty check cannot be null.");
    }

    @Override
    protected void calculateOperationsThisTick(OperationTracker tracker) {
        super.calculateOperationsThisTick(tracker);
        CachedRecipeHelper.twoInputCalculateOperationsThisTick(tracker, inputHandler, inputSupplier, secondaryInputHandler, secondaryInputSupplier, (input, secondary) -> {
            this.input = input;
            this.secondaryInput = secondary;
        }, outputHandler, outputGetter, output -> this.output = output, inputEmptyCheck, secondaryInputEmptyCheck);
    }

    @Override
    public boolean isInputValid() {
        INPUT_A input = inputHandler.getInput();
        if (inputEmptyCheck.test(input)) {
            return false;
        }
        INPUT_B secondaryInput = secondaryInputHandler.getInput();
        return !secondaryInputEmptyCheck.test(secondaryInput) && recipe.test(input, secondaryInput);
    }

    @Override
    protected void finishProcessing(int operations) {
        //Validate something didn't go horribly wrong
        if (input != null && secondaryInput != null && output != null && !inputEmptyCheck.test(input) && !secondaryInputEmptyCheck.test(secondaryInput) &&
            !outputEmptyCheck.test(output)) {
            inputHandler.use(input, operations);
            secondaryInputHandler.use(secondaryInput, operations);
            outputHandler.handleOutput(output, operations);
        }
    }

    /**
     * Base implementation for handling Fluid Chemical To Chemical Recipes.
     *
     * @param recipe               Recipe.
     * @param recheckAllErrors     Returns {@code true} if processing should be continued even if an error is hit in order to gather all the errors. It is recommended to
     *                             not do this every tick or if there is no one viewing recipes.
     * @param fluidInputHandler    Fluid input handler.
     * @param chemicalInputHandler Chemical input handler.
     * @param outputHandler        Output handler.
     */
    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>,
          RECIPE extends FluidChemicalToChemicalRecipe<CHEMICAL, STACK, INGREDIENT>>
    TwoInputCachedRecipe<@NonNull FluidStack, @NonNull STACK, @NonNull STACK, RECIPE> fluidChemicalToChemical(RECIPE recipe, BooleanSupplier recheckAllErrors,
          IInputHandler<@NonNull FluidStack> fluidInputHandler, IInputHandler<@NonNull STACK> chemicalInputHandler, IOutputHandler<@NonNull STACK> outputHandler) {
        return new TwoInputCachedRecipe<>(recipe, recheckAllErrors, fluidInputHandler, chemicalInputHandler, outputHandler, recipe::getFluidInput,
              recipe::getChemicalInput, recipe::getOutput, FluidStack::isEmpty, ChemicalStack::isEmpty, ChemicalStack::isEmpty);
    }

    /**
     * Base implementation for handling ItemStack Chemical To ItemStack Recipes.
     *
     * @param recipe               Recipe.
     * @param recheckAllErrors     Returns {@code true} if processing should be continued even if an error is hit in order to gather all the errors. It is recommended to
     *                             not do this every tick or if there is no one viewing recipes.
     * @param itemInputHandler     Item input handler.
     * @param chemicalInputHandler Chemical input handler.
     * @param outputHandler        Output handler.
     */
    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>,
          RECIPE extends ItemStackChemicalToItemStackRecipe<CHEMICAL, STACK, INGREDIENT>>
    TwoInputCachedRecipe<@NonNull ItemStack, @NonNull STACK, @NonNull ItemStack, RECIPE> itemChemicalToItem(RECIPE recipe, BooleanSupplier recheckAllErrors,
          IInputHandler<@NonNull ItemStack> itemInputHandler, IInputHandler<@NonNull STACK> chemicalInputHandler, IOutputHandler<@NonNull ItemStack> outputHandler) {
        return new TwoInputCachedRecipe<>(recipe, recheckAllErrors, itemInputHandler, chemicalInputHandler, outputHandler, recipe::getItemInput, recipe::getChemicalInput,
              recipe::getOutput, ItemStack::isEmpty, ChemicalStack::isEmpty, ItemStack::isEmpty);
    }

    /**
     * Base implementation for handling Combiner Recipes.
     *
     * @param recipe            Recipe.
     * @param recheckAllErrors  Returns {@code true} if processing should be continued even if an error is hit in order to gather all the errors. It is recommended to not
     *                          do this every tick or if there is no one viewing recipes.
     * @param inputHandler      Main input handler.
     * @param extraInputHandler Secondary/Extra input handler.
     * @param outputHandler     Output handler.
     */
    public static TwoInputCachedRecipe<@NonNull ItemStack, @NonNull ItemStack, @NonNull ItemStack, CombinerRecipe> combiner(CombinerRecipe recipe,
          BooleanSupplier recheckAllErrors, IInputHandler<@NonNull ItemStack> inputHandler, IInputHandler<@NonNull ItemStack> extraInputHandler,
          IOutputHandler<@NonNull ItemStack> outputHandler) {
        return new TwoInputCachedRecipe<>(recipe, recheckAllErrors, inputHandler, extraInputHandler, outputHandler, recipe::getMainInput, recipe::getExtraInput,
              recipe::getOutput, ItemStack::isEmpty, ItemStack::isEmpty, ItemStack::isEmpty);
    }
}