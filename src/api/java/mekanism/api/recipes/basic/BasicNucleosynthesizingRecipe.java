package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.ItemStackTemplateHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Contract;

@NothingNullByDefault
public class BasicNucleosynthesizingRecipe extends NucleosynthesizingRecipe implements IBasicItemStackOutput {

    protected final ItemStackIngredient itemInput;
    protected final ChemicalStackIngredient chemicalInput;
    protected final ItemStackTemplate output;
    private final int duration;
    private final boolean perTickUsage;

    /**
     * @param itemInput     Item input.
     * @param chemicalInput Chemical input.
     * @param output        Output.
     * @param duration      Duration in ticks that it takes the recipe to complete. Must be greater than zero.
     * @param perTickUsage  Should the recipe consume the chemical input each tick it is processing.
     */
    public BasicNucleosynthesizingRecipe(ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput, ItemStackTemplate output, int duration, boolean perTickUsage) {
        this.itemInput = Objects.requireNonNull(itemInput, "Item input cannot be null.");
        this.chemicalInput = Objects.requireNonNull(chemicalInput, "Chemical input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        this.output = output;
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be a number greater than zero.");
        }
        this.duration = duration;
        this.perTickUsage = perTickUsage;
    }

    @Override
    public int getDuration() {
        return duration;
    }

    @Override
    public boolean perTickUsage() {
        return perTickUsage;
    }

    @Override
    public ItemStackIngredient getItemInput() {
        return itemInput;
    }

    @Override
    public ChemicalStackIngredient getChemicalInput() {
        return chemicalInput;
    }

    @Override
    @Contract(value = "_, _ -> new", pure = true)
    public <ITEM_INPUT extends TypedInstance<Item> & DataComponentHolder> ItemStackTemplate getOutput(ITEM_INPUT inputItem, TypedInstance<Chemical> inputChemical) {
        return output;
    }

    @Override
    public boolean test(ItemStack itemStack, ChemicalStack chemicalStack) {
        return itemInput.test(itemStack) && chemicalInput.test(chemicalStack);
    }

    @Override
    public List<ItemStackTemplate> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    @Override
    public ItemStackTemplate getOutputRaw() {
        return output;
    }

    @Override
    public RecipeSerializer<BasicNucleosynthesizingRecipe> getSerializer() {
        return MekanismRecipeSerializers.NUCLEOSYNTHESIZING.get();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicNucleosynthesizingRecipe other = (BasicNucleosynthesizingRecipe) o;
        return duration == other.duration && perTickUsage == other.perTickUsage && itemInput.equals(other.itemInput) && chemicalInput.equals(other.chemicalInput) &&
               ItemStackTemplateHelper.matches(output, other.output);
    }

    @Override
    public int hashCode() {
        int result = itemInput.hashCode();
        result = 31 * result + chemicalInput.hashCode();
        result = 31 * result + duration;
        result = 31 * result + Boolean.hashCode(perTickUsage);
        result = 31 * result + ItemStackTemplateHelper.hashItemAndComponents(output);
        result = 31 * result + output.count();
        return result;
    }
}