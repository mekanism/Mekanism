package mekanism.api.recipes.cache;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

/**
 * Base class to help implement handling of rotary recipes.
 */
@NothingNullByDefault
public class RotaryCachedRecipe extends CachedRecipe<RotaryRecipe> {

    private final IOutputHandler<@NotNull ChemicalStack> chemicalOutputHandler;
    private final IOutputHandler<@NotNull FluidStack> fluidOutputHandler;
    private final IInputHandler<@NotNull FluidStack> fluidInputHandler;
    private final IInputHandler<@NotNull ChemicalStack> chemicalInputHandler;
    private final BooleanSupplier modeSupplier;
    private final Consumer<FluidStack> fluidInputSetter;
    private final Consumer<ChemicalStack> chemicalInputSetter;
    private final Consumer<FluidStack> fluidOutputSetter;
    private final Consumer<ChemicalStack> chemicalOutputSetter;
    private final Supplier<FluidStackIngredient> fluidInputGetter;
    private final Supplier<ChemicalStackIngredient> chemicalInputGetter;
    private final Function<ChemicalStack, FluidStack> fluidOutputGetter;
    private final Function<FluidStack, ChemicalStack> chemicalOutputGetter;

    private FluidStack recipeFluid = FluidStack.EMPTY;
    private ChemicalStack recipeChemical = ChemicalStack.EMPTY;
    private FluidStack fluidOutput = FluidStack.EMPTY;
    private ChemicalStack chemicalOutput = ChemicalStack.EMPTY;

    /**
     * @param recipe                Recipe.
     * @param recheckAllErrors      Returns {@code true} if processing should be continued even if an error is hit in order to gather all the errors. It is recommended to
     *                              not do this every tick or if there is no one viewing recipes.
     * @param fluidInputHandler     Fluid input handler.
     * @param chemicalInputHandler  Chemical input handler.
     * @param chemicalOutputHandler Chemical output handler.
     * @param fluidOutputHandler    Fluid output handler.
     * @param modeSupplier          Machine handling mode. Returns {@code true} for fluid to chemical, and {@code false} for chemical to fluid.
     */
    public RotaryCachedRecipe(RotaryRecipe recipe, BooleanSupplier recheckAllErrors, IInputHandler<@NotNull FluidStack> fluidInputHandler,
          IInputHandler<@NotNull ChemicalStack> chemicalInputHandler, IOutputHandler<@NotNull ChemicalStack> chemicalOutputHandler, IOutputHandler<@NotNull FluidStack> fluidOutputHandler,
          BooleanSupplier modeSupplier) {
        super(recipe, recheckAllErrors);
        this.fluidInputHandler = Objects.requireNonNull(fluidInputHandler, "Fluid input handler cannot be null.");
        this.chemicalInputHandler = Objects.requireNonNull(chemicalInputHandler, "Chemical input handler cannot be null.");
        this.chemicalOutputHandler = Objects.requireNonNull(chemicalOutputHandler, "Chemical output handler cannot be null.");
        this.fluidOutputHandler = Objects.requireNonNull(fluidOutputHandler, "Fluid output handler cannot be null.");
        this.modeSupplier = Objects.requireNonNull(modeSupplier, "Mode supplier cannot be null.");
        this.fluidInputSetter = input -> this.recipeFluid = input;
        this.chemicalInputSetter = input -> this.recipeChemical = input;
        this.fluidOutputSetter = output -> this.fluidOutput = output;
        this.chemicalOutputSetter = output -> this.chemicalOutput = output;
        this.fluidInputGetter = this.recipe::getFluidInput;
        this.chemicalInputGetter = this.recipe::getChemicalInput;
        this.fluidOutputGetter = this.recipe::getFluidOutput;
        this.chemicalOutputGetter = this.recipe::getChemicalOutput;
    }

    @Override
    protected void calculateOperationsThisTick(OperationTracker tracker) {
        super.calculateOperationsThisTick(tracker);
        if (tracker.shouldContinueChecking()) {
            //Mode == true if fluid to chemical
            if (modeSupplier.getAsBoolean()) {
                if (!recipe.hasFluidToChemical()) {
                    //If our recipe doesn't have a fluid to chemical version, return that we cannot operate
                    tracker.mismatchedRecipe();
                } else {
                    //Handle fluid to chemical conversion
                    CachedRecipeHelper.oneInputCalculateOperationsThisTick(tracker, fluidInputHandler, fluidInputGetter, fluidInputSetter,
                          chemicalOutputHandler, chemicalOutputGetter, chemicalOutputSetter, ConstantPredicates.FLUID_EMPTY);
                }
            } else if (!recipe.hasChemicalToFluid()) {
                //If our recipe doesn't have a chemical to fluid version, return that we cannot operate
                tracker.mismatchedRecipe();
            } else {
                //Handle chemical to fluid conversion
                CachedRecipeHelper.oneInputCalculateOperationsThisTick(tracker, chemicalInputHandler, chemicalInputGetter, chemicalInputSetter,
                      fluidOutputHandler, fluidOutputGetter, fluidOutputSetter, ConstantPredicates.CHEMICAL_EMPTY);
            }
        }
    }

    @Override
    public boolean isInputValid() {
        //Mode == true if fluid to chemical
        if (modeSupplier.getAsBoolean()) {
            if (!recipe.hasFluidToChemical()) {
                //If our recipe doesn't have a fluid to chemical version, return that we cannot operate
                return false;
            }
            FluidStack fluidStack = fluidInputHandler.getInput();
            return !fluidStack.isEmpty() && recipe.test(fluidStack);
        } else if (!recipe.hasChemicalToFluid()) {
            //If our recipe doesn't have a chemical to fluid version, return that we cannot operate
            return false;
        }
        ChemicalStack chemicalStack = chemicalInputHandler.getInput();
        return !chemicalStack.isEmpty() && recipe.test(chemicalStack);
    }

    @Override
    protected void finishProcessing(int operations) {
        //Mode == true if fluid to chemical
        if (modeSupplier.getAsBoolean()) {
            //Validate something didn't go horribly wrong and the fluid is somehow empty
            if (recipe.hasFluidToChemical() && !recipeFluid.isEmpty() && !chemicalOutput.isEmpty()) {
                fluidInputHandler.use(recipeFluid, operations);
                chemicalOutputHandler.handleOutput(chemicalOutput, operations);
            }
        } else if (recipe.hasChemicalToFluid() && !recipeChemical.isEmpty() && !fluidOutput.isEmpty()) {
            //Validate something didn't go horribly wrong and the chemical is somehow empty
            chemicalInputHandler.use(recipeChemical, operations);
            fluidOutputHandler.handleOutput(fluidOutput, operations);
        }
    }
}