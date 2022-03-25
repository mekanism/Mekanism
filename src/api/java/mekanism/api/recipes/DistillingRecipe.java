package mekanism.api.recipes;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;

/**
 * Input: Fluid
 * <br>
 * Output: FluidStack
 * <br>
 * Output: FluidStack
 * <br>
 * Output: FluidStack (can be empty if subsequent outputs are also empty)
 * <br>
 * Output: FluidStack (can be empty if subsequent outputs are also empty)
 * <br>
 * Output: FluidStack (can be empty if subsequent outputs are also empty)
 * <br>
 * Output: FluidStack (can be empty)
 *
 * @apiNote Fractionating Distillers process this recipe type
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class DistillingRecipe extends MekanismRecipe implements Predicate<@NonNull FluidStack> {

    private final FluidStackIngredient inputFluid;
    private final List<FluidStack> outputFluids;

    /**
     * @param id           Recipe name.
     * @param inputFluid   Fluid input.
     * @param outputFluids Output fluids, from the highest boiling point fluid to the lowest boiling point fluid.
     */
    public DistillingRecipe(ResourceLocation id, FluidStackIngredient inputFluid, List<FluidStack> outputFluids) {
        super(id);
        this.inputFluid = Objects.requireNonNull(inputFluid, "Fluid input cannot be null.");
        Objects.requireNonNull(outputFluids, "Output fluid list cannot be null.");
        if (outputFluids.size() < 2) {
            throw new IllegalArgumentException("Output fluid list must be at least two long.");
        }
        outputFluids.forEach(outputFluid -> {
            Objects.requireNonNull(outputFluid, "Output fluid cannot be null.");
            if (outputFluid.isEmpty()) {
                throw new IllegalArgumentException("Output fluid cannot be empty.");
            }
        });
        this.outputFluids = outputFluids.stream().map(FluidStack::copy).collect(Collectors.toList());
    }

    public FluidStackIngredient getInputFluid() {
        return inputFluid;
    }

    public List<FluidStack> getOutputFluids() {
        return outputFluids;
    }

    @Override
    public boolean test(@NonNull FluidStack fluidStack) {
        return this.inputFluid.test(fluidStack);
    }

    /**
     * Gets the input ingredient.
     */
    public FluidStackIngredient getInput() {
        return inputFluid;
    }

    /**
     * For JEI, gets the output representations to display.
     *
     * @return Representation of the output, <strong>MUST NOT</strong> be modified.
     */
    public List<DistillingRecipeOutput> getOutputDefinition() {
        return Collections.singletonList(new DistillingRecipeOutput(outputFluids));
    }

    /**
     * Gets a new output based on the given inputs.
     *
     * @param inputFluid Specific fluid input.
     *
     * @return New output.
     *
     * @apiNote While Mekanism does not currently make use of the inputs, it is important to support it and pass the proper value in case any addons define input based
     * outputs where things like NBT may be different.
     * @implNote The passed in inputs should <strong>NOT</strong> be modified.
     */
    @Contract(value = "_ -> new", pure = true)
    public DistillingRecipeOutput getOutput(FluidStack inputFluid) {
        return new DistillingRecipeOutput(this.outputFluids.stream().map(FluidStack::copy).collect(Collectors.toList()));
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        inputFluid.write(buffer);
        outputFluids.forEach(outputFluid -> outputFluid.writeToPacket(buffer));
    }

    /**
     * @apiNote list should be sorted by boiling point and must not contain any null or empty fluids
     */
    public record DistillingRecipeOutput(@Nonnull List<FluidStack> outputFluids) {
        public DistillingRecipeOutput {
            Objects.requireNonNull(outputFluids, "Output fluid list cannot be null.");
            if (outputFluids.size() < 2) {
                throw new IllegalArgumentException("Output fluid list must be at least two long.");
            }
            outputFluids.forEach(outputFluid -> {
                Objects.requireNonNull(outputFluid, "Output fluid cannot be null.");
                if (outputFluid.isEmpty()) {
                    throw new IllegalArgumentException("Output fluid cannot be empty.");
                }
            });
        }
    }
}