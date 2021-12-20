package mekanism.api.recipes;

import java.util.Objects;
import java.util.function.BiPredicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Contract;

/**
 * Input: ItemStack
 * <br>
 * Input: Gas (Base value, will be multiplied by a per tick amount)
 * <br>
 * Output: ChemicalStack
 *
 * @apiNote Chemical Dissolution Chambers can process this recipe type.
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ChemicalDissolutionRecipe extends MekanismRecipe implements BiPredicate<@NonNull ItemStack, @NonNull GasStack> {

    private final ItemStackIngredient itemInput;
    private final GasStackIngredient gasInput;
    private final BoxedChemicalStack output;

    /**
     * @param id        Recipe name.
     * @param itemInput Item input.
     * @param gasInput  Gas input.
     * @param output    Output.
     */
    public ChemicalDissolutionRecipe(ResourceLocation id, ItemStackIngredient itemInput, GasStackIngredient gasInput, ChemicalStack<?> output) {
        super(id);
        this.itemInput = Objects.requireNonNull(itemInput, "Item input cannot be null.");
        this.gasInput = Objects.requireNonNull(gasInput, "Gas input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output cannot be empty.");
        }
        this.output = BoxedChemicalStack.box(output.copy());
    }

    /**
     * Gets the input item ingredient.
     */
    public ItemStackIngredient getItemInput() {
        return itemInput;
    }

    /**
     * Gets the input gas ingredient.
     */
    public GasStackIngredient getGasInput() {
        return gasInput;
    }

    /**
     * Gets a new output based on the given inputs.
     *
     * @param inputItem Specific item input.
     * @param inputGas  Specific gas input.
     *
     * @return New output.
     *
     * @apiNote While Mekanism does not currently make use of the inputs, it is important to support it and pass the proper value in case any addons define input based
     * outputs where things like NBT may be different.
     * @implNote The passed in inputs should <strong>NOT</strong> be modified.
     */
    @Contract(value = "_, _ -> new", pure = true)
    public BoxedChemicalStack getOutput(ItemStack inputItem, GasStack inputGas) {
        return output.copy();
    }

    @Override
    public boolean test(ItemStack itemStack, GasStack gasStack) {
        return itemInput.test(itemStack) && gasInput.test(gasStack);
    }

    /**
     * For JEI, gets the output representations to display.
     *
     * @return Representation of the output, <strong>MUST NOT</strong> be modified.
     */
    public BoxedChemicalStack getOutputDefinition() {
        //TODO - 1.18: Re-evaluate this method not being a list
        return output;
    }

    @Override
    public void write(PacketBuffer buffer) {
        itemInput.write(buffer);
        gasInput.write(buffer);
        buffer.writeEnum(output.getChemicalType());
        output.getChemicalStack().writeToPacket(buffer);
    }
}