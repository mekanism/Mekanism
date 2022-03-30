package mekanism.api.recipes.chemical;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;

/**
 * Base class for defining ItemStack to chemical recipes.
 * <br>
 * Input: ItemStack
 * <br>
 * Output: ChemicalStack
 *
 * @param <STACK> Output type
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ItemStackToChemicalRecipe<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> extends MekanismRecipe implements
      Predicate<@NonNull ItemStack> {

    protected final ItemStackIngredient input;
    protected final STACK output;

    /**
     * @param id     Recipe name.
     * @param input  Input.
     * @param output Output.
     */
    public ItemStackToChemicalRecipe(ResourceLocation id, ItemStackIngredient input, STACK output) {
        super(id);
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output cannot be empty.");
        }
        this.output = (STACK) output.copy();
    }

    @Override
    public boolean test(ItemStack itemStack) {
        return input.test(itemStack);
    }

    /**
     * Gets the input ingredient.
     */
    public ItemStackIngredient getInput() {
        return input;
    }

    /**
     * Gets a new output based on the given input.
     *
     * @param input Specific input.
     *
     * @return New output.
     *
     * @apiNote While Mekanism does not currently make use of the input, it is important to support it and pass the proper value in case any addons define input based
     * outputs where things like NBT may be different
     * @implNote The passed in input should <strong>NOT</strong> be modified.
     */
    @Contract(value = "_ -> new", pure = true)
    public STACK getOutput(ItemStack input) {
        return (STACK) output.copy();
    }

    /**
     * For JEI, gets the output representations to display.
     *
     * @return Representation of the output, <strong>MUST NOT</strong> be modified.
     */
    public List<STACK> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    @Override
    public boolean isIncomplete() {
        return input.hasNoMatchingInstances();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        input.write(buffer);
        output.writeToPacket(buffer);
    }
}