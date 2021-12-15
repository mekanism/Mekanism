package mekanism.api.recipes.cache.chemical;

import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.chemical.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.chemical.IChemicalStackIngredient;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.item.ItemStack;

/**
 * Base class to help implement handling of item chemical to item recipes.
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemStackChemicalToItemStackCachedRecipe<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends IChemicalStackIngredient<CHEMICAL, STACK>, RECIPE extends ItemStackChemicalToItemStackRecipe<CHEMICAL, STACK, INGREDIENT>>
      extends CachedRecipe<RECIPE> {

    private final IOutputHandler<@NonNull ItemStack> outputHandler;
    private final IInputHandler<@NonNull STACK> chemicalInputHandler;
    private final IInputHandler<@NonNull ItemStack> itemInputHandler;

    private ItemStack recipeItem = ItemStack.EMPTY;
    @Nullable//Note: Shouldn't be null in places it is actually used, but we mark it as nullable, so we don't have to initialize it
    private STACK recipeChemical;

    /**
     * @param recipe               Recipe.
     * @param itemInputHandler     Item input handler.
     * @param chemicalInputHandler Chemical input handler.
     * @param outputHandler        Output handler.
     */
    public ItemStackChemicalToItemStackCachedRecipe(RECIPE recipe, IInputHandler<@NonNull ItemStack> itemInputHandler, IInputHandler<@NonNull STACK> chemicalInputHandler,
          IOutputHandler<@NonNull ItemStack> outputHandler) {
        super(recipe);
        this.itemInputHandler = Objects.requireNonNull(itemInputHandler, "Item input handler cannot be null.");
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
        recipeItem = itemInputHandler.getRecipeInput(recipe.getItemInput());
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputItem)
        if (recipeItem.isEmpty()) {
            return -1;
        }
        //Now check the chemical input
        recipeChemical = chemicalInputHandler.getRecipeInput(recipe.getChemicalInput());
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputChemical)
        if (recipeChemical.isEmpty()) {
            return -1;
        }
        //Calculate the current max based on the item input
        currentMax = itemInputHandler.operationsCanSupport(recipeItem, currentMax);
        //Calculate the current max based on the infusion input
        currentMax = chemicalInputHandler.operationsCanSupport(recipeChemical, currentMax);
        if (currentMax <= 0) {
            //If our input can't handle it return that we should be resetting
            return -1;
        }
        //Calculate the max based on the space in the output
        return outputHandler.operationsRoomFor(recipe.getOutput(recipeItem, recipeChemical), currentMax);
    }

    @Override
    public boolean isInputValid() {
        return recipe.test(itemInputHandler.getInput(), chemicalInputHandler.getInput());
    }

    @Override
    protected void finishProcessing(int operations) {
        if (recipeItem.isEmpty() || recipeChemical == null || recipeChemical.isEmpty()) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        itemInputHandler.use(recipeItem, operations);
        chemicalInputHandler.use(recipeChemical, operations);
        outputHandler.handleOutput(recipe.getOutput(recipeItem, recipeChemical), operations);
    }
}