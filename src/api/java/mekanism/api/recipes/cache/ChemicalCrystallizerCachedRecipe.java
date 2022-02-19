package mekanism.api.recipes.cache;

import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.inputs.BoxedChemicalInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.item.ItemStack;

/**
 * Base class to help implement handling of crystallizing recipes.
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class ChemicalCrystallizerCachedRecipe extends CachedRecipe<ChemicalCrystallizerRecipe> {

    private final IOutputHandler<@NonNull ItemStack> outputHandler;
    private final BoxedChemicalInputHandler inputHandler;

    private BoxedChemicalStack recipeInput = BoxedChemicalStack.EMPTY;

    /**
     * @param recipe        Recipe.
     * @param inputHandler  Input handler.
     * @param outputHandler Output handler.
     */
    public ChemicalCrystallizerCachedRecipe(ChemicalCrystallizerRecipe recipe, BoxedChemicalInputHandler inputHandler, IOutputHandler<@NonNull ItemStack> outputHandler) {
        super(recipe);
        this.inputHandler = Objects.requireNonNull(inputHandler, "Input handler cannot be null.");
        this.outputHandler = Objects.requireNonNull(outputHandler, "Output handler cannot be null.");
    }

    @Override
    protected int getOperationsThisTick(int currentMax) {
        currentMax = super.getOperationsThisTick(currentMax);
        if (currentMax <= 0) {
            //If our parent checks show we can't operate then return so
            return currentMax;
        }
        recipeInput = inputHandler.getRecipeInput(recipe.getInput());
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputChemical)
        if (recipeInput.isEmpty()) {
            return -1;
        }
        //Calculate the current max based on the input
        currentMax = inputHandler.operationsCanSupport(recipeInput, currentMax);
        if (currentMax <= 0) {
            //If our input can't handle it return that we should be resetting
            return -1;
        }
        //Calculate the max based on the space in the output
        return outputHandler.operationsRoomFor(recipe.getOutput(recipeInput), currentMax);
    }

    @Override
    public boolean isInputValid() {
        return recipe.test(inputHandler.getInput());
    }

    @Override
    protected void finishProcessing(int operations) {
        if (recipeInput.isEmpty()) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        inputHandler.use(recipeInput, operations);
        outputHandler.handleOutput(recipe.getOutput(recipeInput), operations);
    }
}