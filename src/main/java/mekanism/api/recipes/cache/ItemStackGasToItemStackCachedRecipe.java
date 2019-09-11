package mekanism.api.recipes.cache;

import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.common.util.FieldsAreNonnullByDefault;
import net.minecraft.item.ItemStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemStackGasToItemStackCachedRecipe extends CachedRecipe<ItemStackGasToItemStackRecipe> {

    private final BiFunction<@NonNull ItemStack, Boolean, Boolean> addToOutput;
    private final Supplier<@NonNull ItemStack> inputStack;
    private final Supplier<@NonNull GasTank> inputTank;
    private final IntSupplier gasUsage;

    public ItemStackGasToItemStackCachedRecipe(ItemStackGasToItemStackRecipe recipe, BooleanSupplier canTileFunction, DoubleSupplier perTickEnergy, DoubleSupplier storedEnergy,
          IntSupplier requiredTicks, Consumer<Boolean> setActive, DoubleConsumer useEnergy, Runnable onFinish, Supplier<@NonNull ItemStack> inputStack,
          Supplier<@NonNull GasTank> inputTank, IntSupplier gasUsage, BiFunction<@NonNull ItemStack, Boolean, Boolean> addToOutput) {
        super(recipe, canTileFunction, perTickEnergy, storedEnergy, requiredTicks, setActive, useEnergy, onFinish);
        this.inputStack = inputStack;
        this.inputTank = inputTank;
        this.addToOutput = addToOutput;
        this.gasUsage = gasUsage;
    }

    @Nonnull
    private ItemStack getItemInput() {
        return inputStack.get();
    }

    @Nonnull
    private GasTank getGasTank() {
        return inputTank.get();
    }

    private int getGasUsage() {
        return gasUsage.getAsInt();
    }

    @Override
    public boolean hasResourcesForTick() {
        GasTank gasTank = getGasTank();
        GasStack gas = gasTank.getGas();
        //Ensure that we check that we have enough for that the recipe matches *and* also that we have enough for how much we need to use
        return gas != null && recipe.test(getItemInput(), gas) && gasTank.getStored() >= gas.amount * getGasUsage();
    }

    @Override
    public boolean hasRoomForOutput() {
        GasStack gasStack = getGasTank().getGas();
        //TODO: If the parameters to getOutput ever actually do get used, we may need to pass a more accurate gasStack to it
        // Also we should potentially have a better way of doing this because we may not know our current gas stack
        //TODO: Ideally the getOutput won't have any params because we already have the recipe object, then we won't have null warnings as our gas stack may be null
        return addToOutput.apply(recipe.getOutput(getItemInput(), gasStack), true);
    }

    @Override
    protected void useResources() {
        super.useResources();
        GasStack gas = getGasTank().getGas();
        if (gas != null && gas.amount > 0) {
            getGasTank().draw(gas.amount * getGasUsage(), true);
        }
        //TODO: Else throw some error? It really should already have the needed amount due to the hasResourceForTick call
        // but it may make sense to check anyways
    }

    @Override
    protected void finishProcessing() {
        GasStack gasStack = getGasTank().getGas();
        //TODO: Ideally the getOutput won't have any params because we already have the recipe object, then we won't have null warnings as our gas stack may be null
        addToOutput.apply(recipe.getOutput(getItemInput(), gasStack), false);
    }
}