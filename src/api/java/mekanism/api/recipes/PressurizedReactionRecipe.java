package mekanism.api.recipes;

import java.util.Collections;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.TriPredicate;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Contract;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class PressurizedReactionRecipe extends MekanismRecipe implements TriPredicate<@NonNull ItemStack, @NonNull FluidStack, @NonNull GasStack> {

    private final ItemStackIngredient inputSolid;
    private final FluidStackIngredient inputFluid;
    private final GasStackIngredient inputGas;
    private final FloatingLong energyRequired;
    private final int duration;
    private final ItemStack outputItem;
    private final GasStack outputGas;

    public PressurizedReactionRecipe(ResourceLocation id, ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, GasStackIngredient inputGas,
          FloatingLong energyRequired, int duration, ItemStack outputItem, GasStack outputGas) {
        super(id);
        this.inputSolid = inputSolid;
        this.inputFluid = inputFluid;
        this.inputGas = inputGas;
        this.energyRequired = energyRequired;
        this.duration = duration;
        this.outputItem = outputItem;
        this.outputGas = outputGas;
    }

    public ItemStackIngredient getInputSolid() {
        return inputSolid;
    }

    public FluidStackIngredient getInputFluid() {
        return inputFluid;
    }

    public GasStackIngredient getInputGas() {
        return inputGas;
    }

    public FloatingLong getEnergyRequired() {
        return energyRequired;
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public boolean test(ItemStack solid, FluidStack liquid, GasStack gas) {
        return this.inputSolid.test(solid) && this.inputFluid.test(liquid) && this.inputGas.test(gas);
    }

    public Pair<List<@NonNull ItemStack>, @NonNull GasStack> getOutputDefinition() {
        if (outputItem.isEmpty()) {
            return Pair.of(Collections.emptyList(), this.outputGas);
        }
        return Pair.of(Collections.singletonList(this.outputItem), this.outputGas);
    }

    @Contract(value = "_, _, _ -> new", pure = true)
    public Pair<@NonNull ItemStack, @NonNull GasStack> getOutput(ItemStack solid, FluidStack liquid, GasStack gas) {
        return Pair.of(this.outputItem.copy(), this.outputGas.copy());
    }

    @Override
    public void write(PacketBuffer buffer) {
        inputSolid.write(buffer);
        inputFluid.write(buffer);
        inputGas.write(buffer);
        energyRequired.writeToBuffer(buffer);
        buffer.writeVarInt(duration);
        buffer.writeItemStack(outputItem);
        outputGas.writeToPacket(buffer);
    }
}