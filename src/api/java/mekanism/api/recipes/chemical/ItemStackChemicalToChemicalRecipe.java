package mekanism.api.recipes.chemical;

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
public abstract class ItemStackChemicalToChemicalRecipe<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends IChemicalStackIngredient<CHEMICAL, STACK>> extends MekanismRecipe implements BiPredicate<@NonNull ItemStack, @NonNull STACK> {

    private final ItemStackIngredient itemInput;
    private final INGREDIENT chemicalInput;
    protected final STACK output;

    public ItemStackChemicalToChemicalRecipe(ResourceLocation id, ItemStackIngredient itemInput, INGREDIENT chemicalInput, STACK output) {
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
    public abstract STACK getOutput(ItemStack inputItem, STACK inputChemical);

    @Override
    public boolean test(ItemStack itemStack, STACK chemicalStack) {
        return itemInput.test(itemStack) && chemicalInput.test(chemicalStack);
    }

    public STACK getOutputDefinition() {
        return output;
    }

    @Override
    public void write(PacketBuffer buffer) {
        itemInput.write(buffer);
        chemicalInput.write(buffer);
        output.writeToPacket(buffer);
    }
}