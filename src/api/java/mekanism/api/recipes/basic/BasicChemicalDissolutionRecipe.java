package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.chemical.display.ChemicalStackSlotDisplay;
import net.minecraft.core.TypedInstance;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jspecify.annotations.Nullable;

public class BasicChemicalDissolutionRecipe extends ChemicalDissolutionRecipe {

    protected final ItemStackIngredient itemInput;
    protected final ChemicalStackIngredient chemicalInput;
    protected final ChemicalStackTemplate output;
    private final boolean perTickUsage;

    /// @param itemInput     Item input.
    /// @param chemicalInput Chemical input.
    /// @param output        Output.
    /// @param perTickUsage  Should the recipe consume the chemical input each tick it is processing.
    public BasicChemicalDissolutionRecipe(ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput, ChemicalStackTemplate output, boolean perTickUsage) {
        this.itemInput = Objects.requireNonNull(itemInput, "Item input cannot be null.");
        this.chemicalInput = Objects.requireNonNull(chemicalInput, "Chemical input cannot be null.");
        this.output = Objects.requireNonNull(output, "Output cannot be null.");
        this.perTickUsage = perTickUsage;
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
    public ChemicalStackTemplate getOutput(TypedInstance<Item> inputItem, TypedInstance<Chemical> inputChemical) {
        return output;
    }

    @Override
    public List<ChemicalStackTemplate> getOutputDefinition(ContextMap contextMap) {
        return Collections.singletonList(output);
    }

    @Override
    public SlotDisplay getOutputDisplay() {
        return new ChemicalStackSlotDisplay(output);
    }

    /// For Serializer usage only. Do not modify the returned stack!
    ///
    /// @return the uncopied output definition
    public ChemicalStackTemplate getOutputRaw() {
        return output;
    }

    @Override
    public RecipeSerializer<BasicChemicalDissolutionRecipe> getSerializer() {
        return MekanismRecipeSerializers.DISSOLUTION.get();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicChemicalDissolutionRecipe other = (BasicChemicalDissolutionRecipe) o;
        return perTickUsage == other.perTickUsage && itemInput.equals(other.itemInput) && chemicalInput.equals(other.chemicalInput) && output.equals(other.output);
    }

    @Override
    public int hashCode() {
        int result = itemInput.hashCode();
        result = 31 * result + chemicalInput.hashCode();
        result = 31 * result + output.hashCode();
        result = 31 * result + Boolean.hashCode(perTickUsage);
        return result;
    }
}