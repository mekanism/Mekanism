package mekanism.api.recipes;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.recipes.inputs.chemical.IChemicalStackIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Contract;

/**
 * Input: Chemical
 * <br>
 * Output: ItemStack
 *
 * @apiNote Chemical Crystallizers can process this recipe type.
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ChemicalCrystallizerRecipe extends MekanismRecipe implements Predicate<@NonNull BoxedChemicalStack> {

    private final ChemicalType chemicalType;
    private final IChemicalStackIngredient<?, ?> input;
    private final ItemStack output;

    /**
     * @param id     Recipe name.
     * @param input  Input.
     * @param output Output.
     */
    public ChemicalCrystallizerRecipe(ResourceLocation id, IChemicalStackIngredient<?, ?> input, ItemStack output) {
        super(id);
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        this.chemicalType = ChemicalType.getTypeFor(input);
        Objects.requireNonNull(output, "Output cannot be null.");
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output cannot be empty.");
        }
        this.output = output.copy();
    }

    /**
     * Gets the output based on the given input.
     *
     * @param input Specific input.
     *
     * @return Output as a constant.
     *
     * @apiNote While Mekanism does not currently make use of the input, it is important to support it and pass the proper value in case any addons define input based
     * outputs where things like NBT may be different.
     * @implNote The passed in input should <strong>NOT</strong> be modified.
     */
    @Contract(value = "_ -> new", pure = true)
    public ItemStack getOutput(BoxedChemicalStack input) {
        return output.copy();
    }

    @Nonnull
    @Override
    public ItemStack getResultItem() {
        return output.copy();
    }

    /**
     * For JEI, gets the output representations to display.
     *
     * @return Representation of the output, <strong>MUST NOT</strong> be modified.
     */
    public List<ItemStack> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    @Override
    public boolean test(BoxedChemicalStack chemicalStack) {
        return chemicalType == chemicalStack.getChemicalType() && testInternal(chemicalStack.getChemicalStack());
    }

    /**
     * Helper to test this recipe against a chemical stack without having to first box it up.
     *
     * @param stack Input stack.
     *
     * @return {@code true} if the stack matches the input.
     *
     * @apiNote See {@link #test(BoxedChemicalStack)}.
     */
    public boolean test(ChemicalStack<?> stack) {
        return chemicalType == ChemicalType.getTypeFor(stack) && testInternal(stack);
    }

    /**
     * Helper to test this recipe against a chemical stack's type without having to first box it up.
     *
     * @param stack Input stack.
     *
     * @return {@code true} if the stack's type matches the input.
     *
     * @apiNote See {@link #testType(BoxedChemicalStack)}.
     */
    public boolean testType(ChemicalStack<?> stack) {
        return chemicalType == ChemicalType.getTypeFor(stack) && testTypeInternal(stack);
    }

    /**
     * Helper to test this recipe against a chemical stack's type without having to first box it up.
     *
     * @param stack Input stack.
     *
     * @return {@code true} if the stack's type matches the input.
     */
    public boolean testType(BoxedChemicalStack stack) {
        return chemicalType == stack.getChemicalType() && testTypeInternal(stack.getChemicalStack());
    }

    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> boolean testInternal(STACK stack) {
        return ((IChemicalStackIngredient<CHEMICAL, STACK>) input).test(stack);
    }

    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> boolean testTypeInternal(STACK stack) {
        return ((IChemicalStackIngredient<CHEMICAL, STACK>) input).testType(stack);
    }

    /**
     * Gets the input ingredient.
     */
    public IChemicalStackIngredient<?, ?> getInput() {
        return input;
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeEnum(chemicalType);
        input.write(buffer);
        buffer.writeItem(output);
    }
}