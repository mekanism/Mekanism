package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;


@NothingNullByDefault
public class BasicChemicalCrystallizerRecipe extends ChemicalCrystallizerRecipe {

    protected final ChemicalType chemicalType;
    protected final ChemicalStackIngredient<?, ?> input;
    protected final ItemStack output;

    /**
     * @param input  Input.
     * @param output Output.
     */
    public BasicChemicalCrystallizerRecipe(ChemicalStackIngredient<?, ?> input, ItemStack output) {
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        this.chemicalType = ChemicalType.getTypeFor(input);
        Objects.requireNonNull(output, "Output cannot be null.");
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output cannot be empty.");
        }
        this.output = output.copy();
    }

    @Contract(value = "_ -> new", pure = true)
    @Override
    public ItemStack getOutput(BoxedChemicalStack input) {
        return output.copy();
    }

    @NotNull
    @Override
    public ItemStack getResultItem(@NotNull HolderLookup.Provider provider) {
        return output.copy();
    }

    @Override
    public List<ItemStack> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    @Override
    public boolean test(BoxedChemicalStack chemicalStack) {
        return chemicalType == chemicalStack.getChemicalType() && testInternal(chemicalStack.getChemicalStack());
    }

    @Override
    public boolean test(ChemicalStack<?> stack) {
        return chemicalType == ChemicalType.getTypeFor(stack) && testInternal(stack);
    }

    @Override
    public boolean testType(ChemicalStack<?> stack) {
        return chemicalType == ChemicalType.getTypeFor(stack) && testTypeInternal(stack);
    }

    @Override
    public boolean testType(BoxedChemicalStack stack) {
        return chemicalType == stack.getChemicalType() && testTypeInternal(stack.getChemicalStack());
    }

    @SuppressWarnings("unchecked")
    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> boolean testInternal(STACK stack) {
        return ((ChemicalStackIngredient<CHEMICAL, STACK>) input).test(stack);
    }

    @SuppressWarnings("unchecked")
    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> boolean testTypeInternal(STACK stack) {
        return ((ChemicalStackIngredient<CHEMICAL, STACK>) input).testType(stack);
    }

    @Override
    public ChemicalStackIngredient<?, ?> getInput() {
        return input;
    }

    /**
     * For Serializer usage only. Do not modify the returned stack!
     *
     * @return the uncopied output definition
     */
    public ItemStack getOutputRaw() {
        return this.output;
    }

    @Override
    public RecipeSerializer<BasicChemicalCrystallizerRecipe> getSerializer() {
        return MekanismRecipeSerializers.CRYSTALLIZING.get();
    }
}