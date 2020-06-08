package mekanism.api.recipes.cache.chemical;

import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.item.ItemStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemStackToChemicalCachedRecipe<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      RECIPE extends ItemStackToChemicalRecipe<CHEMICAL, STACK>> extends CachedRecipe<RECIPE> {

    private final IOutputHandler<@NonNull STACK> outputHandler;
    private final IInputHandler<@NonNull ItemStack> inputHandler;

    public ItemStackToChemicalCachedRecipe(RECIPE recipe, IInputHandler<@NonNull ItemStack> inputHandler, IOutputHandler<@NonNull STACK> outputHandler) {
        super(recipe);
        this.inputHandler = inputHandler;
        this.outputHandler = outputHandler;
    }

    @Override
    protected int getOperationsThisTick(int currentMax) {
        currentMax = super.getOperationsThisTick(currentMax);
        if (currentMax <= 0) {
            //If our parent checks show we can't operate then return so
            return currentMax;
        }
        ItemStack recipeItem = inputHandler.getRecipeInput(recipe.getInput());
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputItem)
        if (recipeItem.isEmpty()) {
            return -1;
        }
        //Calculate the current max based on the input
        currentMax = inputHandler.operationsCanSupport(recipe.getInput(), currentMax);
        if (currentMax <= 0) {
            //If our input can't handle it return that we should be resetting
            return -1;
        }
        //Calculate the max based on the space in the output
        return outputHandler.operationsRoomFor(recipe.getOutput(recipeItem), currentMax);
    }

    @Override
    public boolean isInputValid() {
        return recipe.test(inputHandler.getInput());
    }

    @Override
    protected void finishProcessing(int operations) {
        //TODO - Performance: Eventually we should look into caching this stuff from when getOperationsThisTick was called?
        ItemStack recipeItem = inputHandler.getRecipeInput(recipe.getInput());
        if (recipeItem.isEmpty()) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        inputHandler.use(recipeItem, operations);
        outputHandler.handleOutput(recipe.getOutput(recipeItem), operations);
    }
}