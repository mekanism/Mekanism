package mekanism.api.recipes.cache;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.common.util.FieldsAreNonnullByDefault;
import net.minecraft.item.ItemStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemStackToGasCachedRecipe extends CachedRecipe<ItemStackToGasRecipe> {

    private final BiFunction<@NonNull GasStack, Boolean, Boolean> addToOutput;
    private final Supplier<@NonNull ItemStack> inputStack;

    public ItemStackToGasCachedRecipe(ItemStackToGasRecipe recipe, Supplier<@NonNull ItemStack> inputStack, BiFunction<@NonNull GasStack, Boolean, Boolean> addToOutput) {
        super(recipe);
        this.inputStack = inputStack;
        this.addToOutput = addToOutput;
    }

    private ItemStack getInput() {
        return inputStack.get();
    }

    @Override
    protected int getOperationsThisTick(int currentMax) {
        //TODO: Move hasResourcesForTick and hasRoomForOutput into this calculation
        return 1;
    }

    @Override
    public boolean hasResourcesForTick() {
        return recipe.test(getInput());
    }

    @Override
    public boolean hasRoomForOutput() {
        return addToOutput.apply(recipe.getOutput(getInput()), true);
    }

    @Override
    protected void finishProcessing(int operations) {
        addToOutput.apply(recipe.getOutput(getInput()), false);
    }
}