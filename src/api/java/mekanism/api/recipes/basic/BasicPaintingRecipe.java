package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.PaintingRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.PigmentStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public abstract class BasicPaintingRecipe extends PaintingRecipe implements IBasicItemStackOutput {

    protected final ItemStackIngredient itemInput;
    protected final PigmentStackIngredient pigmentInput;
    protected final ItemStack output;

    /**
     * @param itemInput    Item input.
     * @param pigmentInput Pigment input.
     * @param output       Output.
     */
    public BasicPaintingRecipe(ItemStackIngredient itemInput, PigmentStackIngredient pigmentInput, ItemStack output) {
        super();
        this.itemInput = Objects.requireNonNull(itemInput, "Item input cannot be null.");
        this.pigmentInput = Objects.requireNonNull(pigmentInput, "Chemical input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output cannot be empty.");
        }
        this.output = output.copy();
    }

    @Override
    public ItemStackIngredient getItemInput() {
        return itemInput;
    }

    @Override
    public PigmentStackIngredient getChemicalInput() {
        return pigmentInput;
    }

    @Override
    @Contract(value = "_, _ -> new", pure = true)
    public ItemStack getOutput(ItemStack inputItem, PigmentStack inputChemical) {
        return output.copy();
    }

    @NotNull
    @Override
    public ItemStack getResultItem(@NotNull RegistryAccess registryAccess) {
        return output.copy();
    }

    @Override
    public boolean test(ItemStack itemStack, PigmentStack gasStack) {
        return itemInput.test(itemStack) && pigmentInput.test(gasStack);
    }

    @Override
    public List<@NotNull ItemStack> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    @Override
    public ItemStack getOutputRaw() {
        return output;
    }
}