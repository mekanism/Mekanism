package mekanism.api.recipes;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.TriPredicate;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Input: ItemStack
 * <br>
 * Input: FluidStack
 * <br>
 * Input: GasStack
 * <br>
 * Item Output: ItemStack (can be empty if gas output is not empty)
 * <br>
 * Gas Output: GasStack (can be empty if item output is not empty)
 *
 * @apiNote Pressurized Reaction Chambers can process this recipe type.
 */
@NothingNullByDefault
public abstract class PressurizedReactionRecipe extends MekanismRecipe implements TriPredicate<@NotNull ItemStack, @NotNull FluidStack, @NotNull GasStack> {

    private final ItemStackIngredient inputSolid;
    private final FluidStackIngredient inputFluid;
    private final GasStackIngredient inputGas;
    private final FloatingLong energyRequired;
    private final int duration;
    private final ItemStack outputItem;
    private final GasStack outputGas;

    /**
     * @param id             Recipe name.
     * @param inputSolid     Item input.
     * @param inputFluid     Fluid input.
     * @param inputGas       Gas input.
     * @param energyRequired Amount of "extra" energy this recipe requires, compared to the base energy requirements of the machine performing the recipe.
     * @param duration       Base duration in ticks that this recipe takes to complete. Must be greater than zero.
     * @param outputItem     Item output.
     * @param outputGas      Gas output.
     *
     * @apiNote At least one output must not be empty.
     */
    public PressurizedReactionRecipe(ResourceLocation id, ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, GasStackIngredient inputGas,
          FloatingLong energyRequired, int duration, ItemStack outputItem, GasStack outputGas) {
        super(id);
        this.inputSolid = Objects.requireNonNull(inputSolid, "Item input cannot be null.");
        this.inputFluid = Objects.requireNonNull(inputFluid, "Fluid input cannot be null.");
        this.inputGas = Objects.requireNonNull(inputGas, "Gas input cannot be null.");
        this.energyRequired = Objects.requireNonNull(energyRequired, "Required energy cannot be null.").copyAsConst();
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be positive.");
        }
        this.duration = duration;
        Objects.requireNonNull(outputItem, "Item output cannot be null.");
        Objects.requireNonNull(outputGas, "Gas output cannot be null.");
        if (outputItem.isEmpty() && outputGas.isEmpty()) {
            throw new IllegalArgumentException("At least one output must not be empty.");
        }
        Objects.requireNonNull(outputItem, "Item output cannot be null.");
        Objects.requireNonNull(outputGas, "Gas output cannot be null.");
        if (outputItem.isEmpty() && outputGas.isEmpty()) {
            throw new IllegalArgumentException("At least one output must not be empty.");
        }
        this.outputItem = outputItem.copy();
        this.outputGas = outputGas.copy();
    }

    /**
     * Gets the item input ingredient.
     */
    public ItemStackIngredient getInputSolid() {
        return inputSolid;
    }

    /**
     * Gets the fluid input ingredient.
     */
    public FluidStackIngredient getInputFluid() {
        return inputFluid;
    }

    /**
     * Gets the gas input ingredient.
     */
    public GasStackIngredient getInputGas() {
        return inputGas;
    }

    /**
     * Gets the amount of "extra" energy this recipe requires, compared to the base energy requirements of the machine performing the recipe.
     */
    public FloatingLong getEnergyRequired() {
        return energyRequired;
    }

    /**
     * Gets the base duration in ticks that this recipe takes to complete.
     */
    public int getDuration() {
        return duration;
    }

    @Override
    public boolean test(ItemStack solid, FluidStack liquid, GasStack gas) {
        return this.inputSolid.test(solid) && this.inputFluid.test(liquid) && this.inputGas.test(gas);
    }

    /**
     * For JEI, gets the output representations to display.
     *
     * @return Representation of the output, <strong>MUST NOT</strong> be modified.
     */
    public List<PressurizedReactionRecipeOutput> getOutputDefinition() {
        return Collections.singletonList(new PressurizedReactionRecipeOutput(outputItem, outputGas));
    }

    /**
     * Gets a new output based on the given inputs.
     *
     * @param solid  Specific item input.
     * @param liquid Specific fluid input.
     * @param gas    Specific gas input.
     *
     * @return New output.
     *
     * @apiNote While Mekanism does not currently make use of the inputs, it is important to support it and pass the proper value in case any addons define input based
     * outputs where things like NBT may be different.
     * @implNote The passed in inputs should <strong>NOT</strong> be modified.
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    public PressurizedReactionRecipeOutput getOutput(ItemStack solid, FluidStack liquid, GasStack gas) {
        return new PressurizedReactionRecipeOutput(this.outputItem.copy(), this.outputGas.copy());
    }

    @Override
    public boolean isIncomplete() {
        return inputSolid.hasNoMatchingInstances() || inputFluid.hasNoMatchingInstances() || inputGas.hasNoMatchingInstances();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        inputSolid.write(buffer);
        inputFluid.write(buffer);
        inputGas.write(buffer);
        energyRequired.writeToBuffer(buffer);
        buffer.writeVarInt(duration);
        buffer.writeItem(outputItem);
        outputGas.writeToPacket(buffer);
    }

    /**
     * @apiNote Both item and gas may be present or one may be empty.
     */
    public record PressurizedReactionRecipeOutput(@NotNull ItemStack item, @NotNull GasStack gas) {

        public PressurizedReactionRecipeOutput {
            Objects.requireNonNull(item, "Item output cannot be null.");
            Objects.requireNonNull(gas, "Gas output cannot be null.");
            if (item.isEmpty() && gas.isEmpty()) {
                throw new IllegalArgumentException("At least one output must be present.");
            }
        }
    }
}