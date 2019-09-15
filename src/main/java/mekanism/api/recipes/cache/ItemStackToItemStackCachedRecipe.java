package mekanism.api.recipes.cache;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.common.util.FieldsAreNonnullByDefault;
import net.minecraft.item.ItemStack;

//TODO: Look into making some things have a common super class, such as all the ones that have an ItemStack as an input
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemStackToItemStackCachedRecipe extends CachedRecipe<ItemStackToItemStackRecipe> {

    private final BiFunction<@NonNull ItemStack, Boolean, Boolean> addToOutput;
    private final Supplier<@NonNull ItemStack> inputStack;

    public ItemStackToItemStackCachedRecipe(ItemStackToItemStackRecipe recipe, Supplier<@NonNull ItemStack> inputStack,
          BiFunction<@NonNull ItemStack, Boolean, Boolean> addToOutput) {
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
        //TODO: Should we cache the result of recipe.getOutput, as ItemStack.copy() is a relatively expensive call
        // If we decide to do it also check other cached recipes that end up having copy calls via their getOutput checks
        return addToOutput.apply(recipe.getOutput(getInput()), true);
    }

    @Override
    protected void finishProcessing(int operations) {
        addToOutput.apply(recipe.getOutput(getInput()), false);
    }
}