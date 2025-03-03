package mekanism.api.recipes.cache;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.ChemicalToChemicalRecipe;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.ElectrolysisRecipe.ElectrolysisRecipeOutput;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.ItemStackToChemicalRecipe;
import mekanism.api.recipes.ItemStackToFluidOptionalItemRecipe;
import mekanism.api.recipes.ItemStackToFluidOptionalItemRecipe.FluidOptionalItemOutput;
import mekanism.api.recipes.ItemStackToFluidRecipe;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.SawmillRecipe.ChanceOutput;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base class to help implement handling of recipes with one input.
 */
@NothingNullByDefault
public class OneInputCachedRecipe<INPUT, OUTPUT, RECIPE extends MekanismRecipe<?> & Predicate<INPUT>> extends CachedRecipe<RECIPE> {

    private static final Predicate<ElectrolysisRecipeOutput> SEPARATOR_OUTPUT_EMPTY = output -> output.left().isEmpty() || output.right().isEmpty();
    private static final Predicate<FluidOptionalItemOutput> FLUID_OPTIONAL_ITEM_OUTPUT_EMPTY = output -> output.fluid().isEmpty();

    private final IInputHandler<INPUT> inputHandler;
    private final IOutputHandler<OUTPUT> outputHandler;
    private final Predicate<INPUT> inputEmptyCheck;
    private final Supplier<? extends InputIngredient<INPUT>> inputSupplier;
    private final Function<INPUT, OUTPUT> outputGetter;
    private final Predicate<OUTPUT> outputEmptyCheck;
    private final Consumer<INPUT> inputSetter;
    private final Consumer<OUTPUT> outputSetter;

    //Note: Our input and output shouldn't be null in places they are actually used, but we mark them as nullable, so we don't have to initialize them
    @Nullable
    private INPUT input;
    @Nullable
    private OUTPUT output;

    /**
     * @param recipe           Recipe.
     * @param recheckAllErrors Returns {@code true} if processing should be continued even if an error is hit in order to gather all the errors. It is recommended to not
     *                         do this every tick or if there is no one viewing recipes.
     * @param inputHandler     Input handler.
     * @param outputHandler    Output handler.
     * @param inputSupplier    Supplier of the recipe's input ingredient.
     * @param outputGetter     Gets the recipe's output when given the corresponding input.
     * @param inputEmptyCheck  Checks if the input is empty.
     * @param outputEmptyCheck Checks if the output is empty (indicating something went horribly wrong).
     */
    protected OneInputCachedRecipe(RECIPE recipe, BooleanSupplier recheckAllErrors, IInputHandler<INPUT> inputHandler, IOutputHandler<OUTPUT> outputHandler,
          Supplier<? extends InputIngredient<INPUT>> inputSupplier, Function<INPUT, OUTPUT> outputGetter, Predicate<INPUT> inputEmptyCheck,
          Predicate<OUTPUT> outputEmptyCheck) {
        super(recipe, recheckAllErrors);
        this.inputHandler = Objects.requireNonNull(inputHandler, "Input handler cannot be null.");
        this.outputHandler = Objects.requireNonNull(outputHandler, "Output handler cannot be null.");
        this.inputSupplier = Objects.requireNonNull(inputSupplier, "Input ingredient supplier cannot be null.");
        this.outputGetter = Objects.requireNonNull(outputGetter, "Output getter cannot be null.");
        this.inputEmptyCheck = Objects.requireNonNull(inputEmptyCheck, "Input empty check cannot be null.");
        this.outputEmptyCheck = Objects.requireNonNull(outputEmptyCheck, "Output empty check cannot be null.");
        this.inputSetter = input -> this.input = input;
        this.outputSetter = output -> this.output = output;
    }

    @Override
    protected void calculateOperationsThisTick(OperationTracker tracker) {
        super.calculateOperationsThisTick(tracker);
        CachedRecipeHelper.oneInputCalculateOperationsThisTick(tracker, inputHandler, inputSupplier, inputSetter, outputHandler, outputGetter, outputSetter, inputEmptyCheck);
    }

    @Override
    public boolean isInputValid() {
        INPUT input = inputHandler.getInput();
        return !inputEmptyCheck.test(input) && recipe.test(input);
    }

    @Override
    protected void finishProcessing(int operations) {
        //Validate something didn't go horribly wrong
        if (input != null && output != null && !inputEmptyCheck.test(input) && !outputEmptyCheck.test(output)) {
            inputHandler.use(input, operations);
            outputHandler.handleOutput(output, operations);
        }
    }

    /**
     * Base implementation for handling Crystallizing Recipes.
     *
     * @param recipe           Recipe.
     * @param recheckAllErrors Returns {@code true} if processing should be continued even if an error is hit in order to gather all the errors. It is recommended to not
     *                         do this every tick or if there is no one viewing recipes.
     * @param inputHandler     Input handler.
     * @param outputHandler    Output handler, handles both the left and right outputs.
     *
     * @since 10.7.0
     */
    public static OneInputCachedRecipe<@NotNull ChemicalStack, @NotNull ItemStack, ChemicalCrystallizerRecipe> crystallizing(ChemicalCrystallizerRecipe recipe,
          BooleanSupplier recheckAllErrors, IInputHandler<@NotNull ChemicalStack> inputHandler, IOutputHandler<@NotNull ItemStack> outputHandler) {
        return new OneInputCachedRecipe<>(recipe, recheckAllErrors, inputHandler, outputHandler, recipe::getInput, recipe::getOutput, ConstantPredicates.CHEMICAL_EMPTY,
              ConstantPredicates.ITEM_EMPTY);
    }

    /**
     * Base implementation for handling Electrolytic Separating Recipes.
     *
     * @param recipe           Recipe.
     * @param recheckAllErrors Returns {@code true} if processing should be continued even if an error is hit in order to gather all the errors. It is recommended to not
     *                         do this every tick or if there is no one viewing recipes.
     * @param inputHandler     Input handler.
     * @param outputHandler    Output handler, handles both the left and right outputs.
     */
    public static OneInputCachedRecipe<@NotNull FluidStack, @NotNull ElectrolysisRecipeOutput, ElectrolysisRecipe> separating(ElectrolysisRecipe recipe,
          BooleanSupplier recheckAllErrors, IInputHandler<@NotNull FluidStack> inputHandler, IOutputHandler<@NotNull ElectrolysisRecipeOutput> outputHandler) {
        return new OneInputCachedRecipe<>(recipe, recheckAllErrors, inputHandler, outputHandler, recipe::getInput, recipe::getOutput, ConstantPredicates.FLUID_EMPTY,
              SEPARATOR_OUTPUT_EMPTY);
    }

    /**
     * Base implementation for handling Fluid to Fluid Recipes.
     *
     * @param recipe           Recipe.
     * @param recheckAllErrors Returns {@code true} if processing should be continued even if an error is hit in order to gather all the errors. It is recommended to not
     *                         do this every tick or if there is no one viewing recipes.
     * @param inputHandler     Input handler.
     * @param outputHandler    Output handler.
     */
    public static OneInputCachedRecipe<@NotNull FluidStack, @NotNull FluidStack, FluidToFluidRecipe> fluidToFluid(FluidToFluidRecipe recipe,
          BooleanSupplier recheckAllErrors, IInputHandler<@NotNull FluidStack> inputHandler, IOutputHandler<@NotNull FluidStack> outputHandler) {
        return new OneInputCachedRecipe<>(recipe, recheckAllErrors, inputHandler, outputHandler, recipe::getInput, recipe::getOutput, ConstantPredicates.FLUID_EMPTY,
              ConstantPredicates.FLUID_EMPTY);
    }

    /**
     * Base implementation for handling ItemStack to ItemStack Recipes.
     *
     * @param recipe           Recipe.
     * @param recheckAllErrors Returns {@code true} if processing should be continued even if an error is hit in order to gather all the errors. It is recommended to not
     *                         do this every tick or if there is no one viewing recipes.
     * @param inputHandler     Input handler.
     * @param outputHandler    Output handler.
     */
    public static OneInputCachedRecipe<@NotNull ItemStack, @NotNull ItemStack, ItemStackToItemStackRecipe> itemToItem(ItemStackToItemStackRecipe recipe,
          BooleanSupplier recheckAllErrors, IInputHandler<@NotNull ItemStack> inputHandler, IOutputHandler<@NotNull ItemStack> outputHandler) {
        return new OneInputCachedRecipe<>(recipe, recheckAllErrors, inputHandler, outputHandler, recipe::getInput, recipe::getOutput, ConstantPredicates.ITEM_EMPTY,
              ConstantPredicates.ITEM_EMPTY);
    }

    /**
     * Base implementation for handling ItemStack to Fluid Recipes.
     *
     * @param recipe           Recipe.
     * @param recheckAllErrors Returns {@code true} if processing should be continued even if an error is hit in order to gather all the errors. It is recommended to not
     *                         do this every tick or if there is no one viewing recipes.
     * @param inputHandler     Input handler.
     * @param outputHandler    Output handler.
     */
    public static OneInputCachedRecipe<@NotNull ItemStack, @NotNull FluidStack, ItemStackToFluidRecipe> itemToFluid(ItemStackToFluidRecipe recipe,
          BooleanSupplier recheckAllErrors, IInputHandler<@NotNull ItemStack> inputHandler, IOutputHandler<@NotNull FluidStack> outputHandler) {
        return new OneInputCachedRecipe<>(recipe, recheckAllErrors, inputHandler, outputHandler, recipe::getInput, recipe::getOutput, ConstantPredicates.ITEM_EMPTY,
              ConstantPredicates.FLUID_EMPTY);
    }

    /**
     * Base implementation for handling ItemStack to Fluid with optional Item Recipes.
     *
     * @param recipe           Recipe.
     * @param recheckAllErrors Returns {@code true} if processing should be continued even if an error is hit in order to gather all the errors. It is recommended to not
     *                         do this every tick or if there is no one viewing recipes.
     * @param inputHandler     Input handler.
     * @param outputHandler    Output handler.
     *
     * @since 10.6.3
     */
    public static <RECIPE extends ItemStackToFluidOptionalItemRecipe> OneInputCachedRecipe<@NotNull ItemStack, @NotNull FluidOptionalItemOutput, RECIPE> itemToFluidOptionalItem(
          RECIPE recipe, BooleanSupplier recheckAllErrors, IInputHandler<@NotNull ItemStack> inputHandler, IOutputHandler<@NotNull FluidOptionalItemOutput> outputHandler) {
        return new OneInputCachedRecipe<>(recipe, recheckAllErrors, inputHandler, outputHandler, recipe::getInput, recipe::getOutput, ConstantPredicates.ITEM_EMPTY,
              FLUID_OPTIONAL_ITEM_OUTPUT_EMPTY);
    }

    /**
     * Base implementation for handling ItemStack to Chemical Recipes.
     *
     * @param recipe           Recipe.
     * @param recheckAllErrors Returns {@code true} if processing should be continued even if an error is hit in order to gather all the errors. It is recommended to not
     *                         do this every tick or if there is no one viewing recipes.
     * @param inputHandler     Input handler.
     * @param outputHandler    Output handler.
     */
    public static <RECIPE extends ItemStackToChemicalRecipe>
    OneInputCachedRecipe<@NotNull ItemStack, @NotNull ChemicalStack, RECIPE> itemToChemical(RECIPE recipe, BooleanSupplier recheckAllErrors,
          IInputHandler<@NotNull ItemStack> inputHandler, IOutputHandler<@NotNull ChemicalStack> outputHandler) {
        return new OneInputCachedRecipe<>(recipe, recheckAllErrors, inputHandler, outputHandler, recipe::getInput, recipe::getOutput, ConstantPredicates.ITEM_EMPTY,
              ConstantPredicates.CHEMICAL_EMPTY);
    }

    /**
     * Base implementation for handling Chemical to Chemical Recipes.
     *
     * @param recipe           Recipe.
     * @param recheckAllErrors Returns {@code true} if processing should be continued even if an error is hit in order to gather all the errors. It is recommended to not
     *                         do this every tick or if there is no one viewing recipes.
     * @param inputHandler     Input handler.
     * @param outputHandler    Output handler.
     */
    public static <RECIPE extends ChemicalToChemicalRecipe> OneInputCachedRecipe<@NotNull ChemicalStack, @NotNull ChemicalStack, RECIPE> chemicalToChemical(
          RECIPE recipe, BooleanSupplier recheckAllErrors, IInputHandler<@NotNull ChemicalStack> inputHandler, IOutputHandler<@NotNull ChemicalStack> outputHandler) {
        return new OneInputCachedRecipe<>(recipe, recheckAllErrors, inputHandler, outputHandler, recipe::getInput, recipe::getOutput, ConstantPredicates.CHEMICAL_EMPTY,
              ConstantPredicates.CHEMICAL_EMPTY);
    }

    /**
     * Base implementation for handling Sawing Recipes.
     *
     * @param recipe           Recipe.
     * @param recheckAllErrors Returns {@code true} if processing should be continued even if an error is hit in order to gather all the errors. It is recommended to not
     *                         do this every tick or if there is no one viewing recipes.
     * @param inputHandler     Input handler.
     * @param outputHandler    Output handler.
     */
    public static OneInputCachedRecipe<@NotNull ItemStack, @NotNull ChanceOutput, SawmillRecipe> sawing(SawmillRecipe recipe, BooleanSupplier recheckAllErrors,
          IInputHandler<@NotNull ItemStack> inputHandler, IOutputHandler<@NotNull ChanceOutput> outputHandler) {
        return new OneInputCachedRecipe<>(recipe, recheckAllErrors, inputHandler, outputHandler, recipe::getInput, recipe::getOutput, ConstantPredicates.ITEM_EMPTY,
              ConstantPredicates.alwaysFalse());
    }
}