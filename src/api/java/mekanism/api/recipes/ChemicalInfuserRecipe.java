package mekanism.api.recipes;

import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.inputs.GasStackIngredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

/**
 * Created by Thiakil on 13/07/2019.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@FieldsAreNonnullByDefault
public abstract class ChemicalInfuserRecipe extends MekanismRecipe implements BiPredicate<@NonNull GasStack, @NonNull GasStack> {

    private final GasStackIngredient leftInput;
    private final GasStackIngredient rightInput;
    private final Gas outputGas;
    private final int outputGasAmount;

    public ChemicalInfuserRecipe(ResourceLocation id, GasStackIngredient leftInput, GasStackIngredient rightInput, Gas outputGas, int outputGasAmount) {
        super(id);
        this.leftInput = leftInput;
        this.rightInput = rightInput;
        this.outputGas = outputGas;
        this.outputGasAmount = outputGasAmount;
    }

    public ChemicalInfuserRecipe(ResourceLocation id, GasStackIngredient leftInput, GasStackIngredient rightInput, GasStack output) {
        this(id, leftInput, rightInput, output.getType(), output.getAmount());
    }

    @Override
    public boolean test(GasStack input1, GasStack input2) {
        return (leftInput.test(input1) && rightInput.test(input2)) || (rightInput.test(input1) && leftInput.test(input2));
    }

    public GasStack getOutput(GasStack input1, GasStack input2) {
        return new GasStack(outputGas, outputGasAmount);
    }

    public GasStackIngredient getLeftInput() {
        return leftInput;
    }

    public GasStackIngredient getRightInput() {
        return rightInput;
    }

    public List<GasStack> getOutputDefinition() {
        return Collections.singletonList(new GasStack(outputGas, outputGasAmount));
    }

    @Override
    public void write(PacketBuffer buffer) {
        leftInput.write(buffer);
        rightInput.write(buffer);
        buffer.writeRegistryId(outputGas);
        buffer.writeInt(outputGasAmount);
    }
}