package mekanism.api.recipes.cache;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.PressurizedReactionRecipe.PressurizedReactionRecipeOutput;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base class to help implement handling of reaction recipes.
 */
@NothingNullByDefault
public class PressurizedReactionCachedRecipe extends CachedRecipe<PressurizedReactionRecipe> {

    private final IOutputHandler<@NotNull PressurizedReactionRecipeOutput> outputHandler;
    private final IInputHandler<@NotNull ItemStack> itemInputHandler;
    private final IInputHandler<@NotNull FluidStack> fluidInputHandler;
    private final IInputHandler<@NotNull ChemicalStack> chemicalInputHandler;

    private ItemStack recipeItem = ItemStack.EMPTY;
    private FluidStack recipeFluid = FluidStack.EMPTY;
    private ChemicalStack recipeChemical = ChemicalStack.EMPTY;
    //Note: Our output shouldn't be null in places it is actually used, but we mark it as nullable, so we don't have to initialize it
    @Nullable
    private PressurizedReactionRecipeOutput output;

    /**
     * @param recipe               Recipe.
     * @param recheckAllErrors     Returns {@code true} if processing should be continued even if an error is hit in order to gather all the errors. It is recommended to
     *                             not do this every tick or if there is no one viewing recipes.
     * @param itemInputHandler     Item input handler.
     * @param fluidInputHandler    Fluid input handler.
     * @param chemicalInputHandler Chemical input handler.
     * @param outputHandler        Output handler, handles both the item and chemical outputs.
     */
    public PressurizedReactionCachedRecipe(PressurizedReactionRecipe recipe, BooleanSupplier recheckAllErrors, IInputHandler<@NotNull ItemStack> itemInputHandler,
          IInputHandler<@NotNull FluidStack> fluidInputHandler, IInputHandler<@NotNull ChemicalStack> chemicalInputHandler,
          IOutputHandler<@NotNull PressurizedReactionRecipeOutput> outputHandler) {
        super(recipe, recheckAllErrors);
        this.itemInputHandler = Objects.requireNonNull(itemInputHandler, "Item input handler cannot be null.");
        this.fluidInputHandler = Objects.requireNonNull(fluidInputHandler, "Fluid input handler cannot be null.");
        this.chemicalInputHandler = Objects.requireNonNull(chemicalInputHandler, "Chemical input handler cannot be null.");
        this.outputHandler = Objects.requireNonNull(outputHandler, "Output handler cannot be null.");
    }

    @Override
    protected void calculateOperationsThisTick(OperationTracker tracker) {
        super.calculateOperationsThisTick(tracker);
        if (tracker.shouldContinueChecking()) {
            //TODO - 1.18: If they are empty that means that we either don't have any or don't have enough of the input (or it doesn't match)
            // how do we want to best check if we have some but just not enough for the recipe?
            // Also figure this out for our other recipes that have multiple inputs
            recipeItem = itemInputHandler.getRecipeInput(recipe.getInputSolid());
            //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputItem)
            if (recipeItem.isEmpty()) {
                //No input, we don't know if the recipe matches or not so treat it as not matching
                tracker.mismatchedRecipe();
            } else {
                recipeFluid = fluidInputHandler.getRecipeInput(recipe.getInputFluid());
                //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputFluid)
                if (recipeFluid.isEmpty()) {
                    //No input, we don't know if the recipe matches or not so treat it as not matching
                    tracker.mismatchedRecipe();
                } else {
                    recipeChemical = chemicalInputHandler.getRecipeInput(recipe.getInputChemical());
                    //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputChemical)
                    if (recipeChemical.isEmpty()) {
                        //No input, we don't know if the recipe matches or not so treat it as not matching
                        tracker.mismatchedRecipe();
                    } else {
                        //Calculate the current max based on the item input
                        itemInputHandler.calculateOperationsCanSupport(tracker, recipeItem);
                        if (tracker.shouldContinueChecking()) {
                            //Calculate the current max based on the fluid input
                            fluidInputHandler.calculateOperationsCanSupport(tracker, recipeFluid);
                            if (tracker.shouldContinueChecking()) {
                                //Calculate the current max based on the chemical input
                                chemicalInputHandler.calculateOperationsCanSupport(tracker, recipeChemical);
                                if (tracker.shouldContinueChecking()) {
                                    output = recipe.getOutput(recipeItem, recipeFluid, recipeChemical);
                                    //Calculate the max based on the space in the output
                                    outputHandler.calculateOperationsCanSupport(tracker, output);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean isInputValid() {
        ItemStack item = itemInputHandler.getInput();
        if (item.isEmpty()) {
            return false;
        }
        ChemicalStack chemical = chemicalInputHandler.getInput();
        if (chemical.isEmpty()) {
            return false;
        }
        FluidStack fluid = fluidInputHandler.getInput();
        return !fluid.isEmpty() && recipe.test(item, fluid, chemical);
    }

    @Override
    protected void finishProcessing(int operations) {
        //Validate something didn't go horribly wrong
        if (output != null && !recipeItem.isEmpty() && !recipeFluid.isEmpty() && !recipeChemical.isEmpty()) {
            itemInputHandler.use(recipeItem, operations);
            fluidInputHandler.use(recipeFluid, operations);
            chemicalInputHandler.use(recipeChemical, operations);
            outputHandler.handleOutput(output, operations);
        }
    }
}