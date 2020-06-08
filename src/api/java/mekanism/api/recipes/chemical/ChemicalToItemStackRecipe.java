package mekanism.api.recipes.chemical;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.inputs.chemical.IChemicalStackIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Contract;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ChemicalToItemStackRecipe<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends IChemicalStackIngredient<CHEMICAL, STACK>> extends MekanismRecipe implements Predicate<@NonNull STACK> {

    private final INGREDIENT input;
    private final ItemStack output;

    public ChemicalToItemStackRecipe(ResourceLocation id, INGREDIENT input, ItemStack output) {
        super(id);
        this.input = input;
        this.output = output.copy();
    }

    @Contract(value = "_ -> new", pure = true)
    public ItemStack getOutput(STACK input) {
        return output.copy();
    }

    public List<ItemStack> getOutputDefinition() {
        return output.isEmpty() ? Collections.emptyList() : Collections.singletonList(output);
    }

    @Override
    public boolean test(STACK chemicalStack) {
        return input.test(chemicalStack);
    }

    public INGREDIENT getInput() {
        return input;
    }

    @Override
    public void write(PacketBuffer buffer) {
        input.write(buffer);
        buffer.writeItemStack(output);
    }
}