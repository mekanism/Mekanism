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
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

/**
 * Created by Thiakil on 14/07/2019.
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ItemStackToGasRecipe extends MekanismRecipe implements Predicate<@NonNull ItemStack> {

    private final ItemStackIngredient input;
    private final Gas outputGas;
    private final int outputGasAmount;

    public ItemStackToGasRecipe(ResourceLocation id, ItemStackIngredient input, Gas outputGas, int outputGasAmount) {
        super(id);
        this.input = input;
        this.outputGas = outputGas;
        this.outputGasAmount = outputGasAmount;
    }

    public ItemStackToGasRecipe(ResourceLocation id, ItemStackIngredient input, GasStack output) {
        this(id, input, output.getType(), output.getAmount());
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

    @Override
    public void write(PacketBuffer buffer) {
        input.write(buffer);
        buffer.writeRegistryId(outputGas);
        buffer.writeInt(outputGasAmount);
    }
}