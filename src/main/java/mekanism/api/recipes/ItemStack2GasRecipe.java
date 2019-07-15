package mekanism.api.recipes;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.common.util.FieldsAreNonnullByDefault;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

/**
 * Created by Thiakil on 14/07/2019.
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemStack2GasRecipe implements Predicate<@NonNull ItemStack> {

    private final Ingredient input;

    private final Gas outputGas;
    private final int outputGasAmount;

    public ItemStack2GasRecipe(Ingredient input, Gas outputGas, int outputGasAmount) {
        this.input = input;
        this.outputGas = outputGas;
        this.outputGasAmount = outputGasAmount;
    }

    @Override
    public boolean test(@NonNull ItemStack itemStack) {
        return this.input.apply(itemStack);
    }

    public Ingredient getInput() {
        return input;
    }

    public GasStack getOutput(ItemStack input) {
        return new GasStack(this.outputGas, this.outputGasAmount);
    }

    public GasStack getOutputDefinition() {
        return new GasStack(this.outputGas, this.outputGasAmount);
    }
}
