package mekanism.api.recipes.chemical;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.IChemicalStackIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Contract;

/**
 * Base class for defining item chemical to item recipes.
 * <br>
 * Input: ItemStack
 * <br>
 * Input: Chemical
 * <br>
 * Output: ItemStack
 *
 * @param <INGREDIENT> Input Ingredient type
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ItemStackChemicalToItemStackRecipe<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends IChemicalStackIngredient<CHEMICAL, STACK>> extends MekanismRecipe implements BiPredicate<@NonNull ItemStack, @NonNull STACK> {

    private final ItemStackIngredient itemInput;
    private final INGREDIENT chemicalInput;
    private final ItemStack output;

    /**
     * @param id            Recipe name.
     * @param itemInput     Item input.
     * @param chemicalInput Chemical input.
     * @param output        Output.
     */
    public ItemStackChemicalToItemStackRecipe(ResourceLocation id, ItemStackIngredient itemInput, INGREDIENT chemicalInput, ItemStack output) {
        super(id);
        this.itemInput = Objects.requireNonNull(itemInput, "Item input cannot be null.");
        this.chemicalInput = Objects.requireNonNull(chemicalInput, "Chemical input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output cannot be empty.");
        }
        this.output = output.copy();
    }

    /**
     * Gets the input item ingredient.
     */
    public ItemStackIngredient getItemInput() {
        return itemInput;
    }

    /**
     * Gets the input chemical ingredient.
     */
    public INGREDIENT getChemicalInput() {
        return chemicalInput;
    }

    /**
     * Gets a new output based on the given inputs.
     *
     * @param inputItem     Specific item input.
     * @param inputChemical Specific chemical input.
     *
     * @return New output.
     *
     * @apiNote While Mekanism does not currently make use of the inputs, it is important to support it and pass the proper value in case any addons define input based
     * outputs where things like NBT may be different.
     * @implNote The passed in inputs should <strong>NOT</strong> be modified.
     */
    @Contract(value = "_, _ -> new", pure = true)
    public ItemStack getOutput(ItemStack inputItem, STACK inputChemical) {
        return output.copy();
    }

    @Nonnull
    @Override
    public ItemStack getResultItem() {
        return output.copy();
    }

    @Override
    public boolean test(ItemStack itemStack, STACK gasStack) {
        return itemInput.test(itemStack) && chemicalInput.test(gasStack);
    }

    /**
     * For JEI, gets the output representations to display.
     *
     * @return Representation of the output, <strong>MUST NOT</strong> be modified.
     */
    public List<@NonNull ItemStack> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    @Override
    public void write(PacketBuffer buffer) {
        itemInput.write(buffer);
        chemicalInput.write(buffer);
        buffer.writeItem(output);
    }
}