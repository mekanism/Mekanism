package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.PaintingRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class BasicPaintingRecipe extends PaintingRecipe implements IBasicItemStackOutput {

    protected final ItemStackIngredient itemInput;
    protected final ChemicalStackIngredient pigmentInput;
    protected final ItemStack output;

    /**
     * @param itemInput    Item input.
     * @param pigmentInput Pigment input.
     * @param output       Output.
     */
    public BasicPaintingRecipe(ItemStackIngredient itemInput, ChemicalStackIngredient pigmentInput, ItemStack output) {
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
    public ChemicalStackIngredient getChemicalInput() {
        return pigmentInput;
    }

    @Override
    @Contract(value = "_, _ -> new", pure = true)
    public ItemStack getOutput(ItemStack inputItem, ChemicalStack inputChemical) {
        return output.copy();
    }

    @NotNull
    @Override
    public ItemStack getResultItem(@NotNull HolderLookup.Provider provider) {
        return output.copy();
    }

    @Override
    public boolean test(ItemStack itemStack, ChemicalStack gasStack) {
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

    @Override
    public RecipeSerializer<BasicPaintingRecipe> getSerializer() {
        return MekanismRecipeSerializers.PAINTING.get();
    }
}