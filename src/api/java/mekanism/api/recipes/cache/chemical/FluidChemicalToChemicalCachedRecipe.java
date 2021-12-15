package mekanism.api.recipes.cache.chemical;

import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.chemical.FluidChemicalToChemicalRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.chemical.IChemicalStackIngredient;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraftforge.fluids.FluidStack;

/**
 * Base class to help implement handling of fluid chemical to chemical recipes.
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class FluidChemicalToChemicalCachedRecipe<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends IChemicalStackIngredient<CHEMICAL, STACK>, RECIPE extends FluidChemicalToChemicalRecipe<CHEMICAL, STACK, INGREDIENT>>
      extends CachedRecipe<RECIPE> {

    private final IOutputHandler<@NonNull STACK> outputHandler;
    private final IInputHandler<@NonNull FluidStack> fluidInputHandler;
    private final IInputHandler<@NonNull STACK> chemicalInputHandler;

    private FluidStack recipeFluid = FluidStack.EMPTY;
    @Nullable//Note: Shouldn't be null in places it is actually used, but we mark it as nullable, so we don't have to initialize it
    private STACK recipeChemical;

    /**
     * @param recipe               Recipe.
     * @param fluidInputHandler    Fluid input handler.
     * @param chemicalInputHandler Chemical input handler.
     * @param outputHandler        Output handler.
     */
    public FluidChemicalToChemicalCachedRecipe(RECIPE recipe, IInputHandler<@NonNull FluidStack> fluidInputHandler, IInputHandler<@NonNull STACK> chemicalInputHandler,
          IOutputHandler<@NonNull STACK> outputHandler) {
        super(recipe);
        this.fluidInputHandler = Objects.requireNonNull(fluidInputHandler, "Fluid input handler cannot be null.");
        this.chemicalInputHandler = Objects.requireNonNull(chemicalInputHandler, "Chemical input handler cannot be null.");
        this.outputHandler = Objects.requireNonNull(outputHandler, "Output handler cannot be null.");
    }

    @Override
    protected int getOperationsThisTick(int currentMax) {
        currentMax = super.getOperationsThisTick(currentMax);
        if (currentMax <= 0) {
            //If our parent checks show we can't operate then return so
            return currentMax;
        }
        recipeFluid = fluidInputHandler.getRecipeInput(recipe.getFluidInput());
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputFluid)
        if (recipeFluid.isEmpty()) {
            return -1;
        }
        recipeChemical = chemicalInputHandler.getRecipeInput(recipe.getChemicalInput());
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputChemical)
        if (recipeChemical.isEmpty()) {
            return -1;
        }
        //Calculate the current max based on the fluid input
        currentMax = fluidInputHandler.operationsCanSupport(recipeFluid, currentMax);
        //Calculate the current max based on the chemical input
        currentMax = chemicalInputHandler.operationsCanSupport(recipeChemical, currentMax);
        if (currentMax <= 0) {
            //If our input can't handle it return that we should be resetting
            return -1;
        }
        //Calculate the max based on the space in the output
        return outputHandler.operationsRoomFor(recipe.getOutput(recipeFluid, recipeChemical), currentMax);
    }

    @Override
    public boolean isInputValid() {
        FluidStack fluidStack = fluidInputHandler.getInput();
        if (fluidStack.isEmpty()) {
            return false;
        }
        STACK chemicalInput = chemicalInputHandler.getInput();
        if (chemicalInput.isEmpty()) {
            return false;
        }
        return recipe.test(fluidStack, chemicalInput);
    }

    @Override
    protected void finishProcessing(int operations) {
        if (recipeFluid.isEmpty() || recipeChemical == null || recipeChemical.isEmpty()) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        fluidInputHandler.use(recipeFluid, operations);
        chemicalInputHandler.use(recipeChemical, operations);
        outputHandler.handleOutput(recipe.getOutput(recipeFluid, recipeChemical), operations);
    }
}