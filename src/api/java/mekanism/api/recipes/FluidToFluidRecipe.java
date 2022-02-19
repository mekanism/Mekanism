package mekanism.api.recipes;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;

/**
 * Input: FluidStack
 * <br>
 * Output: FluidStack
 *
 * @apiNote Thermal Evaporation Towers can process this recipe type.
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class FluidToFluidRecipe extends MekanismRecipe implements Predicate<@NonNull FluidStack> {

    private final FluidStackIngredient input;
    private final FluidStack output;

    /**
     * @param id     Recipe name.
     * @param input  Input.
     * @param output Output.
     */
    public FluidToFluidRecipe(ResourceLocation id, FluidStackIngredient input, FluidStack output) {
        super(id);
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output cannot be empty.");
        }
        this.output = output.copy();
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        return this.input.test(fluidStack);
    }

    /**
     * Gets the input ingredient.
     */
    public FluidStackIngredient getInput() {
        return input;
    }

    /**
     * @deprecated Use {@link #getOutputDefinition()}.
     */
    @Deprecated//TODO - 1.18: Remove this
    public FluidStack getOutputRepresentation() {
        return output;
    }

    /**
     * For JEI, gets the output representations to display.
     *
     * @return Representation of the output, <strong>MUST NOT</strong> be modified.
     */
    public List<FluidStack> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    /**
     * Gets a new output based on the given input.
     *
     * @param input Specific input.
     *
     * @return New output.
     *
     * @apiNote While Mekanism does not currently make use of the input, it is important to support it and pass the proper value in case any addons define input based
     * outputs where things like NBT may be different.
     * @implNote The passed in input should <strong>NOT</strong> be modified.
     */
    @Contract(value = "_ ->new", pure = true)
    public FluidStack getOutput(FluidStack input) {
        return output.copy();
    }

    @Override
    public void write(PacketBuffer buffer) {
        input.write(buffer);
        output.writeToPacket(buffer);
    }
}