package mekanism.api.recipes;

import java.util.Collections;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@FieldsAreNonnullByDefault
//TODO: TriPredicate?
public abstract class PressurizedReactionRecipe extends MekanismRecipe {

    private final ItemStackIngredient inputSolid;
    private final FluidStackIngredient inputFluid;
    private final GasStackIngredient gasInput;
    protected final Gas outputGas;
    protected final int outputGasAmount;
    private final double energyRequired;
    private final int duration;
    private final ItemStack outputDefinition;
    private final GasStack gasOutputDefinition;

    public PressurizedReactionRecipe(ResourceLocation id, ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, GasStackIngredient gasInput, Gas outputGas,
          int outputGasAmount, double energyRequired, int duration, ItemStack outputDefinition) {
        super(id);
        this.inputSolid = inputSolid;
        this.inputFluid = inputFluid;
        this.gasInput = gasInput;
        this.outputGas = outputGas;
        this.outputGasAmount = outputGasAmount;
        this.energyRequired = energyRequired;
        this.duration = duration;
        this.outputDefinition = outputDefinition;
        this.gasOutputDefinition = new GasStack(this.outputGas, this.outputGasAmount);
    }

    public PressurizedReactionRecipe(ResourceLocation id, ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, GasStackIngredient gasInput, GasStack outputGas,
          double energyRequired, int duration, ItemStack outputDefinition) {
        this(id, inputSolid, inputFluid, gasInput, outputGas.getType(), outputGas.getAmount(), energyRequired, duration, outputDefinition);
    }

    public ItemStackIngredient getInputSolid() {
        return inputSolid;
    }

    public FluidStackIngredient getInputFluid() {
        return inputFluid;
    }

    public GasStackIngredient getGasInput() {
        return gasInput;
    }

    public double getEnergyRequired() {
        return energyRequired;
    }

    public int getDuration() {
        return duration;
    }

    public boolean test(ItemStack solid, FluidStack liquid, GasStack gas) {
        return this.inputSolid.test(solid) && this.inputFluid.test(liquid) && this.gasInput.test(gas);
    }

    public @NonNull Pair<List<@NonNull ItemStack>, @NonNull GasStack> getOutputDefinition() {
        if (outputDefinition.isEmpty()) {
            return Pair.of(Collections.emptyList(), this.gasOutputDefinition);
        }
        return Pair.of(Collections.singletonList(this.outputDefinition), this.gasOutputDefinition);
    }

    public @NonNull Pair<@NonNull ItemStack, @NonNull GasStack> getOutput(ItemStack solid, FluidStack liquid, GasStack gas) {
        return Pair.of(this.outputDefinition.copy(), this.gasOutputDefinition.copy());
    }

    @Override
    public void write(PacketBuffer buffer) {
        inputSolid.write(buffer);
        inputFluid.write(buffer);
        gasInput.write(buffer);
        buffer.writeRegistryId(outputGas);
        buffer.writeInt(outputGasAmount);
        buffer.writeDouble(energyRequired);
        buffer.writeInt(duration);
        buffer.writeItemStack(outputDefinition);
    }
}
