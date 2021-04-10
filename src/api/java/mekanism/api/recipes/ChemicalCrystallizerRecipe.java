package mekanism.api.recipes;

import java.util.Collections;
import java.util.List;
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

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ChemicalCrystallizerRecipe extends MekanismRecipe implements Predicate<@NonNull BoxedChemicalStack> {

    private final ChemicalType chemicalType;
    private final IChemicalStackIngredient<?, ?> input;
    private final ItemStack output;

    public ChemicalCrystallizerRecipe(ResourceLocation id, IChemicalStackIngredient<?, ?> input, ItemStack output) {
        super(id);
        this.input = input;
        this.chemicalType = ChemicalType.getTypeFor(input);
        this.output = output.copy();
    }

    @Contract(value = "_ -> new", pure = true)
    public ItemStack getOutput(BoxedChemicalStack input) {
        return output.copy();
    }

    @Nonnull
    @Override
    public ItemStack getResultItem() {
        return output.copy();
    }

    public List<ItemStack> getOutputDefinition() {
        return output.isEmpty() ? Collections.emptyList() : Collections.singletonList(output);
    }

    @Override
    public boolean test(BoxedChemicalStack chemicalStack) {
        return chemicalType == chemicalStack.getChemicalType() && testInternal(chemicalStack.getChemicalStack());
    }

    public boolean test(ChemicalStack<?> stack) {
        return chemicalType == ChemicalType.getTypeFor(stack) && testInternal(stack);
    }

    public boolean testType(ChemicalStack<?> stack) {
        return chemicalType == ChemicalType.getTypeFor(stack) && testTypeInternal(stack);
    }

    public boolean testType(BoxedChemicalStack stack) {
        return chemicalType == stack.getChemicalType() && testTypeInternal(stack.getChemicalStack());
    }

    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> boolean testInternal(STACK stack) {
        return ((IChemicalStackIngredient<CHEMICAL, STACK>) input).test(stack);
    }

    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> boolean testTypeInternal(STACK stack) {
        return ((IChemicalStackIngredient<CHEMICAL, STACK>) input).testType(stack);
    }

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