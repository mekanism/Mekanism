package mekanism.api.recipes;

import java.util.function.BiPredicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.inputs.GasIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.util.FieldsAreNonnullByDefault;
import net.minecraft.item.ItemStack;

/**
 * Inputs: ItemStack + GasStack Output: GasStack
 *
 * Chemical Dissolution Chamber
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemStackGasToGasRecipe implements IMekanismRecipe, BiPredicate<@NonNull ItemStack, @NonNull Gas> {

    private final ItemStackIngredient itemInput;

    private final GasIngredient gasInput;

    private final Gas outputGas;

    private final int outputGasAmount;

    public ItemStackGasToGasRecipe(ItemStackIngredient itemInput, GasIngredient gasInput, Gas outputGas, int outputGasAmount) {
        this.itemInput = itemInput;
        this.gasInput = gasInput;
        this.outputGas = outputGas;
        this.outputGasAmount = outputGasAmount;
    }

    public ItemStackGasToGasRecipe(ItemStackIngredient itemInput, GasIngredient gasInput, GasStack output) {
        this(itemInput, gasInput, output.getGas(), output.amount);
    }

    public ItemStackIngredient getItemInput() {
        return itemInput;
    }

    public GasIngredient getGasInput() {
        return gasInput;
    }

    public GasStack getOutput(ItemStack inputItem, GasStack inputGas) {
        return new GasStack(outputGas, outputGasAmount);
    }

    @Override
    public boolean test(@NonNull ItemStack itemStack, @NonNull Gas gasStack) {
        return itemInput.test(itemStack) && gasInput.test(gasStack);
    }

    public GasStack getOutputDefinition() {
        return new GasStack(outputGas, outputGasAmount);
    }
}