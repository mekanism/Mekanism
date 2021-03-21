package mekanism.api.recipes;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.chemical.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.InfusionStackIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Contract;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MetallurgicInfuserRecipe extends ItemStackChemicalToItemStackRecipe<InfuseType, InfusionStack, InfusionStackIngredient> {

    public MetallurgicInfuserRecipe(ResourceLocation id, ItemStackIngredient itemInput, InfusionStackIngredient infusionInput, ItemStack output) {
        super(id, itemInput, infusionInput, output);
    }

    /**
     * @deprecated Switch to using {@link ItemStackChemicalToItemStackRecipe#test(ItemStack, ChemicalStack)}
     */
    @Deprecated//TODO - 1.17: Remove this method
    public boolean test(InfusionStack infusionContainer, ItemStack itemStack) {
        return test(itemStack, infusionContainer);
    }

    /**
     * @deprecated Switch to using {@link ItemStackChemicalToItemStackRecipe#getOutput(ItemStack, ChemicalStack)}
     */
    @Deprecated//TODO - 1.17: Remove this method
    @Contract(value = "_, _ -> new", pure = true)
    public ItemStack getOutput(InfusionStack inputInfuse, ItemStack inputItem) {
        return getOutput(inputItem, inputInfuse);
    }

    /**
     * @deprecated Switch to using {@link ItemStackChemicalToItemStackRecipe#getChemicalInput()}
     */
    @Deprecated//TODO - 1.17: Remove this method
    public InfusionStackIngredient getInfusionInput() {
        return getChemicalInput();
    }
}