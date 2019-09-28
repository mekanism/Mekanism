package mekanism.api.recipes;

import java.util.function.BiPredicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

/**
 * Inputs: ItemStack + GasStack Output: GasStack
 *
 * Chemical Dissolution Chamber
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
//TODO: Make a note of the fact this recipe uses the size of the gas input as a base, but still for the most part will end up multiplying it
// by a per tick usage
public abstract class ItemStackGasToGasRecipe extends MekanismRecipe implements BiPredicate<@NonNull ItemStack, @NonNull GasStack> {

    private final ItemStackIngredient itemInput;
    private final GasStackIngredient gasInput;
    private final Gas outputGas;
    private final int outputGasAmount;

    public ItemStackGasToGasRecipe(ResourceLocation id, ItemStackIngredient itemInput, GasStackIngredient gasInput, Gas outputGas, int outputGasAmount) {
        super(id);
        this.itemInput = itemInput;
        this.gasInput = gasInput;
        this.outputGas = outputGas;
        this.outputGasAmount = outputGasAmount;
    }

    public ItemStackGasToGasRecipe(ResourceLocation id, ItemStackIngredient itemInput, GasStackIngredient gasInput, GasStack output) {
        this(id, itemInput, gasInput, output.getType(), output.getAmount());
    }

    public ItemStackIngredient getItemInput() {
        return itemInput;
    }

    public GasStackIngredient getGasInput() {
        return gasInput;
    }

    public GasStack getOutput(ItemStack inputItem, GasStack inputGas) {
        return new GasStack(outputGas, outputGasAmount);
    }

    @Override
    public boolean test(@NonNull ItemStack itemStack, @NonNull GasStack gasStack) {
        return itemInput.test(itemStack) && gasInput.test(gasStack);
    }

    public GasStack getOutputDefinition() {
        return new GasStack(outputGas, outputGasAmount);
    }

    @Override
    public void write(PacketBuffer buffer) {
        itemInput.write(buffer);
        gasInput.write(buffer);
        buffer.writeRegistryId(outputGas);
        buffer.writeInt(outputGasAmount);
    }
}