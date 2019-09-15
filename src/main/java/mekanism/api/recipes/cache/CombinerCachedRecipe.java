package mekanism.api.recipes.cache;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.common.util.FieldsAreNonnullByDefault;
import net.minecraft.item.ItemStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class CombinerCachedRecipe extends CachedRecipe<CombinerRecipe> {

    private final Supplier<@NonNull ItemStack> inputStack;
    private final Supplier<@NonNull ItemStack> extraStack;
    private final BiFunction<@NonNull ItemStack, Boolean, Boolean> addToOutput;

    public CombinerCachedRecipe(CombinerRecipe recipe, Supplier<@NonNull ItemStack> inputStack, Supplier<@NonNull ItemStack> extraStack,
          BiFunction<@NonNull ItemStack, Boolean, Boolean> addToOutput) {
        super(recipe);
        this.inputStack = inputStack;
        this.extraStack = extraStack;
        this.addToOutput = addToOutput;
    }

    @Nonnull
    private ItemStack getMainInput() {
        return inputStack.get();
    }

    @Nonnull
    private ItemStack getExtraInput() {
        return extraStack.get();
    }

    @Override
    protected int getOperationsThisTick(int currentMax) {
        //TODO: Move hasResourcesForTick and hasRoomForOutput into this calculation
        return 1;
    }

    @Override
    public boolean hasResourcesForTick() {
        return recipe.test(getMainInput(), getExtraInput());
    }

    @Override
    public boolean hasRoomForOutput() {
        return addToOutput.apply(recipe.getOutput(getMainInput(), getExtraInput()), true);
    }

    @Override
    protected void finishProcessing(int operations) {
        addToOutput.apply(recipe.getOutput(getMainInput(), getExtraInput()), false);
    }
}