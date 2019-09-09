package mekanism.api.recipes.cache;

import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
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

    public ItemStackToItemStackCachedRecipe(ItemStackToItemStackRecipe recipe, BooleanSupplier canTileFunction, DoubleSupplier perTickEnergy, DoubleSupplier storedEnergy,
          IntSupplier requiredTicks, Consumer<Boolean> setActive, DoubleConsumer useEnergy, Runnable onFinish, Supplier<@NonNull ItemStack> inputStack,
          BiFunction<@NonNull ItemStack, Boolean, Boolean> addToOutput) {
        super(recipe, canTileFunction, perTickEnergy, storedEnergy, requiredTicks, setActive, useEnergy, onFinish);
        this.inputStack = inputStack;
        this.addToOutput = addToOutput;
    }

    private ItemStack getInput() {
        return inputStack.get();
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
    protected void finishProcessing() {
        addToOutput.apply(recipe.getOutput(getInput()), false);
    }
}