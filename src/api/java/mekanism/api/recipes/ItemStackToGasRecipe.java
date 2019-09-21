package mekanism.api.recipes;

import java.util.function.Predicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import net.minecraft.item.ItemStack;

/**
 * Created by Thiakil on 14/07/2019.
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemStackToGasRecipe implements IMekanismRecipe, Predicate<@NonNull ItemStack> {

    private final ItemStackIngredient input;
    private final Gas outputGas;
    private final int outputGasAmount;

    public ItemStackToGasRecipe(ItemStackIngredient input, Gas outputGas, int outputGasAmount) {
        this.input = input;
        this.outputGas = outputGas;
        this.outputGasAmount = outputGasAmount;
    }

    public ItemStackToGasRecipe(ItemStackIngredient input, GasStack output) {
        this(input, output.getGas(), output.getAmount());
    }

    @Override
    public boolean test(@NonNull ItemStack itemStack) {
        return input.test(itemStack);
    }

    public ItemStackIngredient getInput() {
        return input;
    }

    public GasStack getOutput(ItemStack input) {
        return new GasStack(this.outputGas, this.outputGasAmount);
    }

    public GasStack getOutputDefinition() {
        return new GasStack(this.outputGas, this.outputGasAmount);
    }
}
