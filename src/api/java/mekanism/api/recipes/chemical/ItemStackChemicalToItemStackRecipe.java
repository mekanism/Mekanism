package mekanism.api.recipes.chemical;

import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
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

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ItemStackChemicalToItemStackRecipe<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends IChemicalStackIngredient<CHEMICAL, STACK>> extends MekanismRecipe implements BiPredicate<@NonNull ItemStack, @NonNull STACK> {

    private final ItemStackIngredient itemInput;
    private final INGREDIENT chemicalInput;
    private final ItemStack output;

    public ItemStackChemicalToItemStackRecipe(ResourceLocation id, ItemStackIngredient itemInput, INGREDIENT chemicalInput, ItemStack output) {
        super(id);
        this.itemInput = itemInput;
        this.chemicalInput = chemicalInput;
        this.output = output;
    }

    public ItemStackIngredient getItemInput() {
        return itemInput;
    }

    public INGREDIENT getChemicalInput() {
        return chemicalInput;
    }

    @Contract(value = "_, _ -> new", pure = true)
    public ItemStack getOutput(ItemStack inputItem, STACK inputChemical) {
        return output.copy();
    }

    @Override
    public boolean test(ItemStack itemStack, STACK gasStack) {
        return itemInput.test(itemStack) && chemicalInput.test(gasStack);
    }

    public @NonNull List<@NonNull ItemStack> getOutputDefinition() {
        return output.isEmpty() ? Collections.emptyList() : Collections.singletonList(output);
    }

    @Override
    public void write(PacketBuffer buffer) {
        itemInput.write(buffer);
        chemicalInput.write(buffer);
        buffer.writeItemStack(output);
    }
}