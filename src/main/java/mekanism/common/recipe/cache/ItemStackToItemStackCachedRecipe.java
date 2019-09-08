package mekanism.common.recipe.cache;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.common.util.FieldsAreNonnullByDefault;
import net.minecraft.item.ItemStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemStackToItemStackCachedRecipe extends CachedRecipe<ItemStackToItemStackRecipe> {

    private final Supplier<ItemStack> inputStack;

    public ItemStackToItemStackCachedRecipe(ItemStackToItemStackRecipe recipe, BooleanSupplier canTileFunction, DoubleSupplier perTickEnergy, DoubleSupplier storedEnergy,
          IntSupplier requiredTicks, Consumer<Boolean> setActive, Consumer<Double> useEnergy, Runnable onFinish, Supplier<ItemStack> inputStack) {
        super(recipe, canTileFunction, perTickEnergy, storedEnergy, requiredTicks, setActive, useEnergy, onFinish);
        this.inputStack = inputStack;

    }

    @Override
    public boolean hasResourcesForTick() {
        return recipe.getInput().apply(inputStack.get());
    }

    @Override
    public boolean hasRoomForOutput() {
        //TODO: implement
        return false;
    }

    @Override
    protected void finishProcessing() {
        //TODO: add the output to the output slot
    }
}