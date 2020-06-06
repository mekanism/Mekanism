package mekanism.api.recipes;

import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Contract;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ChemicalInfuserRecipe extends MekanismRecipe implements BiPredicate<@NonNull GasStack, @NonNull GasStack> {

    private final GasStackIngredient leftInput;
    private final GasStackIngredient rightInput;
    private final GasStack output;

    public ChemicalInfuserRecipe(ResourceLocation id, GasStackIngredient leftInput, GasStackIngredient rightInput, GasStack output) {
        super(id);
        this.leftInput = leftInput;
        this.rightInput = rightInput;
        this.output = output;
    }

    @Override
    public boolean test(GasStack input1, GasStack input2) {
        return (leftInput.test(input1) && rightInput.test(input2)) || (rightInput.test(input1) && leftInput.test(input2));
    }

    @Contract(value = "_, _ -> new", pure = true)
    public GasStack getOutput(GasStack input1, GasStack input2) {
        return output.copy();
    }

    public GasStackIngredient getLeftInput() {
        return leftInput;
    }

    public GasStackIngredient getRightInput() {
        return rightInput;
    }

    public List<GasStack> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    @Override
    public void write(PacketBuffer buffer) {
        leftInput.write(buffer);
        rightInput.write(buffer);
        output.writeToPacket(buffer);
    }
}