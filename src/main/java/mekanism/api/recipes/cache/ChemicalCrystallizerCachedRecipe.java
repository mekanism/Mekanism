package mekanism.api.recipes.cache;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.common.util.FieldsAreNonnullByDefault;
import net.minecraft.item.ItemStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class ChemicalCrystallizerCachedRecipe extends CachedRecipe<ChemicalCrystallizerRecipe> {

    private final Supplier<@NonNull GasTank> inputTank;
    private final BiFunction<@NonNull ItemStack, Boolean, Boolean> addToOutput;

    public ChemicalCrystallizerCachedRecipe(ChemicalCrystallizerRecipe recipe,  Supplier<@NonNull GasTank> inputTank,
          BiFunction<@NonNull ItemStack, Boolean, Boolean> addToOutput) {
        super(recipe);
        this.inputTank = inputTank;
        this.addToOutput = addToOutput;
    }

    @Nonnull
    private GasTank getGasTank() {
        return inputTank.get();
    }

    @Override
    protected int getOperationsThisTick(int currentMax) {
        //TODO: Move hasResourcesForTick and hasRoomForOutput into this calculation
        return 1;
    }

    @Override
    public boolean hasResourcesForTick() {
        GasTank gasTank = getGasTank();
        GasStack gasInput = gasTank.getGas();
        return gasInput != null && recipe.test(gasInput);
    }

    @Override
    public boolean hasRoomForOutput() {
        return addToOutput.apply(recipe.getOutput(getGasTank().getGas()), true);
    }

    @Override
    protected void finishProcessing(int operations) {
        addToOutput.apply(recipe.getOutput(getGasTank().getGas()), false);
    }
}