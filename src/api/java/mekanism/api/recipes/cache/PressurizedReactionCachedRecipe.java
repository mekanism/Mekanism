package mekanism.api.recipes.cache;

import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Base class to help implement handling of reaction recipes.
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class PressurizedReactionCachedRecipe extends CachedRecipe<PressurizedReactionRecipe> {

    private final IOutputHandler<@NonNull Pair<@NonNull ItemStack, @NonNull GasStack>> outputHandler;
    private final IInputHandler<@NonNull ItemStack> itemInputHandler;
    private final IInputHandler<@NonNull FluidStack> fluidInputHandler;
    private final IInputHandler<@NonNull GasStack> gasInputHandler;

    private ItemStack recipeItem = ItemStack.EMPTY;
    private FluidStack recipeFluid = FluidStack.EMPTY;
    private GasStack recipeGas = GasStack.EMPTY;

    /**
     * @param recipe            Recipe.
     * @param itemInputHandler  Item input handler.
     * @param fluidInputHandler Fluid input handler.
     * @param gasInputHandler   Gas input handler.
     * @param outputHandler     Output handler, handles both the item and gas outputs.
     */
    public PressurizedReactionCachedRecipe(PressurizedReactionRecipe recipe, IInputHandler<@NonNull ItemStack> itemInputHandler,
          IInputHandler<@NonNull FluidStack> fluidInputHandler, IInputHandler<@NonNull GasStack> gasInputHandler,
          IOutputHandler<@NonNull Pair<@NonNull ItemStack, @NonNull GasStack>> outputHandler) {
        super(recipe);
        this.itemInputHandler = Objects.requireNonNull(itemInputHandler, "Item input handler cannot be null.");
        this.fluidInputHandler = Objects.requireNonNull(fluidInputHandler, "Fluid input handler cannot be null.");
        this.gasInputHandler = Objects.requireNonNull(gasInputHandler, "Gas input handler cannot be null.");
        this.outputHandler = Objects.requireNonNull(outputHandler, "Output handler cannot be null.");
    }

    @Override
    protected int getOperationsThisTick(int currentMax) {
        currentMax = super.getOperationsThisTick(currentMax);
        if (currentMax <= 0) {
            //If our parent checks show we can't operate then return so
            return currentMax;
        }
        recipeItem = itemInputHandler.getRecipeInput(recipe.getInputSolid());
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputItem)
        if (recipeItem.isEmpty()) {
            return -1;
        }
        recipeFluid = fluidInputHandler.getRecipeInput(recipe.getInputFluid());
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputFluid)
        if (recipeFluid.isEmpty()) {
            return -1;
        }
        recipeGas = gasInputHandler.getRecipeInput(recipe.getInputGas());
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputGas)
        if (recipeGas.isEmpty()) {
            return -1;
        }
        //Calculate the current max based on the item input
        currentMax = itemInputHandler.operationsCanSupport(recipeItem, currentMax);
        //Calculate the current max based on the fluid input
        currentMax = fluidInputHandler.operationsCanSupport(recipeFluid, currentMax);
        //Calculate the current max based on the gas input
        currentMax = gasInputHandler.operationsCanSupport(recipeGas, currentMax);
        if (currentMax <= 0) {
            //If our input can't handle it return that we should be resetting
            return -1;
        }
        //Calculate the max based on the space in the output
        return outputHandler.operationsRoomFor(recipe.getOutput(recipeItem, recipeFluid, recipeGas), currentMax);
    }

    @Override
    public boolean isInputValid() {
        GasStack gas = gasInputHandler.getInput();
        if (gas.isEmpty()) {
            return false;
        }
        FluidStack fluid = fluidInputHandler.getInput();
        if (fluid.isEmpty()) {
            return false;
        }
        return recipe.test(itemInputHandler.getInput(), fluid, gas);
    }

    @Override
    protected void finishProcessing(int operations) {
        if (recipeItem.isEmpty() || recipeFluid.isEmpty() || recipeGas.isEmpty()) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        itemInputHandler.use(recipeItem, operations);
        fluidInputHandler.use(recipeFluid, operations);
        gasInputHandler.use(recipeGas, operations);
        outputHandler.handleOutput(recipe.getOutput(recipeItem, recipeFluid, recipeGas), operations);
    }
}