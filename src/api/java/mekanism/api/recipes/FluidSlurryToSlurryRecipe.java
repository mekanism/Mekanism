package mekanism.api.recipes;

import java.util.function.BiPredicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.chemical.SlurryStackIngredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class FluidSlurryToSlurryRecipe extends MekanismRecipe implements BiPredicate<@NonNull FluidStack, @NonNull SlurryStack> {

    private final SlurryStackIngredient slurryInput;
    private final FluidStackIngredient fluidInput;
    private final SlurryStack output;

    public FluidSlurryToSlurryRecipe(ResourceLocation id, FluidStackIngredient fluidInput, SlurryStackIngredient slurryInput, SlurryStack output) {
        super(id);
        this.fluidInput = fluidInput;
        this.slurryInput = slurryInput;
        this.output = output;
    }

    @Override
    public boolean test(FluidStack fluidStack, SlurryStack slurryStack) {
        return fluidInput.test(fluidStack) && slurryInput.test(slurryStack);
    }

    public FluidStackIngredient getFluidInput() {
        return fluidInput;
    }

    public SlurryStackIngredient getSlurryInput() {
        return slurryInput;
    }

    public SlurryStack getOutputRepresentation() {
        return output;
    }

    @Contract(value = "_, _ -> new", pure = true)
    public SlurryStack getOutput(FluidStack fluidStack, SlurryStack slurryStack) {
        return output.copy();
    }

    @Override
    public void write(PacketBuffer buffer) {
        fluidInput.write(buffer);
        slurryInput.write(buffer);
        output.writeToPacket(buffer);
    }
}