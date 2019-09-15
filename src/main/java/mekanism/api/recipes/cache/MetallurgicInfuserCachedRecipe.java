package mekanism.api.recipes.cache;

import java.util.function.Supplier;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.infuse.InfuseObject;
import mekanism.api.infuse.InfusionContainer;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.common.util.FieldsAreNonnullByDefault;
import net.minecraft.item.ItemStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class MetallurgicInfuserCachedRecipe extends CachedRecipe<MetallurgicInfuserRecipe> {

    private final IOutputHandler<@NonNull ItemStack> outputHandler;
    private final Supplier<@NonNull InfusionContainer> infusionContainer;
    private final Supplier<@NonNull ItemStack> inputStack;

    public MetallurgicInfuserCachedRecipe(MetallurgicInfuserRecipe recipe, Supplier<@NonNull InfusionContainer> infusionContainer, Supplier<@NonNull ItemStack> inputStack,
          IOutputHandler<@NonNull ItemStack> outputHandler) {
        super(recipe);
        this.infusionContainer = infusionContainer;
        this.inputStack = inputStack;
        this.outputHandler = outputHandler;
    }

    private ItemStack getItemInput() {
        return inputStack.get();
    }

    private InfusionContainer getInfusionContainer() {
        return infusionContainer.get();
    }

    @Override
    protected int getOperationsThisTick(int currentMax) {
        currentMax = super.getOperationsThisTick(currentMax);
        if (currentMax == 0) {
            //If our parent checks show we can't operate then return so
            return 0;
        }
        ItemStack inputItem = getItemInput();
        if (inputItem.isEmpty()) {
            return 0;
        }
        ItemStack recipeItem = recipe.getItemInput().getMatchingInstance(inputItem);
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputItem)
        if (recipeItem.isEmpty()) {
            return 0;
        }

        //Now check the infusion input
        InfusionContainer inputInfusion = getInfusionContainer();
        if (inputInfusion.isEmpty()) {
            return 0;
        }
        InfuseObject inputInfuseObject = new InfuseObject(inputInfusion.getType(), inputInfusion.getAmount());
        InfuseObject recipeInfuseObject = recipe.getInfusionInput().getMatchingInstance(inputInfuseObject);
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputInfuseObject)
        if (recipeInfuseObject == null || recipeInfuseObject.isEmpty()) {
            //TODO: 1.14 have there be an "EMPTY" object instance so that it can never be null
            return 0;
        }

        //Calculate the current max based on how much item input we have to what is needed, capping at what we are told to use as a max
        currentMax = Math.min(inputItem.getCount() / recipeItem.getCount(), currentMax);

        //Calculate the current max based on how much infusion input we have to what is needed, capping at what we are told to use as a max
        currentMax = Math.min(inputInfusion.getAmount() / recipeInfuseObject.getAmount(), currentMax);

        //Calculate the max based on the space in the output
        return outputHandler.operationsRoomFor(recipe.getOutput(recipeInfuseObject, recipeItem), currentMax);
    }

    @Override
    public boolean isInputValid() {
        return recipe.test(getInfusionContainer(), getItemInput());
    }

    @Override
    protected void finishProcessing(int operations) {
        //TODO: Cache this stuff from when getOperationsThisTick was called?
        ItemStack inputItem = getItemInput();
        if (inputItem.isEmpty()) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        ItemStack recipeItem = recipe.getItemInput().getMatchingInstance(inputItem);
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputItem)
        if (recipeItem.isEmpty()) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }

        //Now check the infusion input
        InfusionContainer inputInfusion = getInfusionContainer();
        if (inputInfusion.isEmpty()) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        InfuseObject inputInfuseObject = new InfuseObject(inputInfusion.getType(), inputInfusion.getAmount());
        InfuseObject recipeInfuseObject = recipe.getInfusionInput().getMatchingInstance(inputInfuseObject);
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputInfuseObject)
        if (recipeInfuseObject == null || recipeInfuseObject.isEmpty()) {
            //TODO: 1.14 have there be an "EMPTY" object instance so that it can never be null
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        outputHandler.handleOutput(recipe.getOutput(recipeInfuseObject, recipeItem), operations);
    }
}