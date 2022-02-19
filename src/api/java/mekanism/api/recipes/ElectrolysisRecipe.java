package mekanism.api.recipes;

import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Contract;

/**
 * Input: FluidStack
 * <br>
 * Left Output: GasStack
 * <br>
 * Right Output: GasStack
 *
 * @apiNote Electrolytic Separators can process this recipe type.
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ElectrolysisRecipe extends MekanismRecipe implements Predicate<@NonNull FluidStack> {

    private final FluidStackIngredient input;
    private final GasStack leftGasOutput;
    private final GasStack rightGasOutput;
    private final FloatingLong energyMultiplier;

    /**
     * @param id               Recipe name.
     * @param input            Input.
     * @param energyMultiplier Multiplier to the energy cost in relation to the configured hydrogen separating energy cost. Must be at least one.
     * @param leftGasOutput    Left output.
     * @param rightGasOutput   Right output.
     */
    public ElectrolysisRecipe(ResourceLocation id, FluidStackIngredient input, FloatingLong energyMultiplier, GasStack leftGasOutput, GasStack rightGasOutput) {
        super(id);
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        this.energyMultiplier = Objects.requireNonNull(energyMultiplier, "Energy multiplier cannot be null.").copyAsConst();
        if (energyMultiplier.smallerThan(FloatingLong.ONE)) {
            throw new IllegalArgumentException("Energy multiplier must be at least one.");
        }
        Objects.requireNonNull(leftGasOutput, "Left output cannot be null");
        Objects.requireNonNull(rightGasOutput, "Right output cannot be null");
        if (leftGasOutput.isEmpty()) {
            throw new IllegalArgumentException("Left output cannot be empty.");
        } else if (rightGasOutput.isEmpty()) {
            throw new IllegalArgumentException("Right output cannot be empty.");
        }
        this.leftGasOutput = leftGasOutput.copy();
        this.rightGasOutput = rightGasOutput.copy();
    }

    /**
     * Gets the input ingredient.
     */
    public FluidStackIngredient getInput() {
        return input;
    }

    /**
     * For JEI, gets the left output representations to display.
     *
     * @return Representation of the left output, <strong>MUST NOT</strong> be modified.
     */
    public GasStack getLeftGasOutputRepresentation() {
        //TODO - 1.18: Re-evaluate this method and the other output representation method not being lists.
        // Strictly speaking it should be a list of pairs of gas stacks
        return leftGasOutput;
    }

    /**
     * For JEI, gets the right output representations to display.
     *
     * @return Representation of the right output, <strong>MUST NOT</strong> be modified.
     */
    public GasStack getRightGasOutputRepresentation() {
        return rightGasOutput;
    }

    @Override
    public boolean test(@Nonnull FluidStack fluidStack) {
        return this.input.test(fluidStack);
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
    @Contract(value = "_ -> new", pure = true)
    public Pair<@NonNull GasStack, @NonNull GasStack> getOutput(FluidStack input) {
        return Pair.of(leftGasOutput.copy(), rightGasOutput.copy());
    }

    /**
     * Gets the multiplier to the energy cost in relation to the configured hydrogen separating energy cost.
     */
    public FloatingLong getEnergyMultiplier() {
        return energyMultiplier;
    }

    @Override
    public void write(PacketBuffer buffer) {
        input.write(buffer);
        energyMultiplier.writeToBuffer(buffer);
        leftGasOutput.writeToPacket(buffer);
        rightGasOutput.writeToPacket(buffer);
    }
}