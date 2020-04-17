package mekanism.api.recipes.cache;

import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.item.ItemStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class MetallurgicInfuserCachedRecipe extends CachedRecipe<MetallurgicInfuserRecipe> {

    private final IOutputHandler<@NonNull ItemStack> outputHandler;
    private final IInputHandler<@NonNull InfusionStack> infusionInputHandler;
    private final IInputHandler<@NonNull ItemStack> itemInputHandler;

    public MetallurgicInfuserCachedRecipe(MetallurgicInfuserRecipe recipe, IInputHandler<@NonNull InfusionStack> infusionInputHandler,
          IInputHandler<@NonNull ItemStack> itemInputHandler, IOutputHandler<@NonNull ItemStack> outputHandler) {
        super(recipe);
        this.infusionInputHandler = infusionInputHandler;
        this.itemInputHandler = itemInputHandler;
        this.outputHandler = outputHandler;
    }

    @Override
    protected int getOperationsThisTick(int currentMax) {
        currentMax = super.getOperationsThisTick(currentMax);
        if (currentMax <= 0) {
            //If our parent checks show we can't operate then return so
            return currentMax;
        }
        ItemStack recipeItem = itemInputHandler.getRecipeInput(recipe.getItemInput());
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputItem)
        if (recipeItem.isEmpty()) {
            return -1;
        }
        //Now check the infusion input
        InfusionStack recipeInfuseObject = infusionInputHandler.getRecipeInput(recipe.getInfusionInput());
        if (recipeInfuseObject.isEmpty()) {
            return -1;
        }
        //Calculate the current max based on the item input
        currentMax = itemInputHandler.operationsCanSupport(recipe.getItemInput(), currentMax);
        //Calculate the current max based on the infusion input
        currentMax = infusionInputHandler.operationsCanSupport(recipe.getInfusionInput(), currentMax);
        if (currentMax <= 0) {
            //If our input can't handle it return that we should be resetting
            return -1;
        }
        //Calculate the max based on the space in the output
        return outputHandler.operationsRoomFor(recipe.getOutput(recipeInfuseObject, recipeItem), currentMax);
    }

    @Override
    public boolean isInputValid() {
        return recipe.test(infusionInputHandler.getInput(), itemInputHandler.getInput());
    }

    @Override
    protected void finishProcessing(int operations) {
        //TODO - Performance: Eventually we should look into caching this stuff from when getOperationsThisTick was called?
        ItemStack recipeItem = itemInputHandler.getRecipeInput(recipe.getItemInput());
        if (recipeItem.isEmpty()) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }

        InfusionStack recipeInfuseObject = infusionInputHandler.getRecipeInput(recipe.getInfusionInput());
        if (recipeInfuseObject.isEmpty()) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        infusionInputHandler.use(recipeInfuseObject, operations);
        itemInputHandler.use(recipeItem, operations);
        outputHandler.handleOutput(recipe.getOutput(recipeInfuseObject, recipeItem), operations);
    }
}