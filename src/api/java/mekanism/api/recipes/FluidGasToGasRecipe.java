package mekanism.api.recipes;

import java.util.function.BiPredicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class FluidGasToGasRecipe extends MekanismRecipe implements BiPredicate<@NonNull FluidStack, @NonNull GasStack> {

    private final GasStackIngredient gasInput;
    private final FluidStackIngredient fluidInput;
    private final GasStack output;

    public FluidGasToGasRecipe(ResourceLocation id, FluidStackIngredient fluidInput, GasStackIngredient gasInput, GasStack output) {
        super(id);
        this.fluidInput = fluidInput;
        this.gasInput = gasInput;
        this.output = output;
    }

    @Override
    public boolean test(FluidStack fluidStack, GasStack gasStack) {
        return fluidInput.test(fluidStack) && gasInput.test(gasStack);
    }

    public FluidStackIngredient getFluidInput() {
        return fluidInput;
    }

    public GasStackIngredient getGasInput() {
        return gasInput;
    }

    public GasStack getOutputRepresentation() {
        return output;
    }

    @Contract(value = "_, _ -> new", pure = true)
    public GasStack getOutput(FluidStack fluidStack, GasStack input) {
        return output.copy();
    }

    @Override
    public void write(PacketBuffer buffer) {
        fluidInput.write(buffer);
        gasInput.write(buffer);
        output.writeToPacket(buffer);
    }
}