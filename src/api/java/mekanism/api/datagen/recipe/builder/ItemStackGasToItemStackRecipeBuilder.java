package mekanism.api.datagen.recipe.builder;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.JsonConstants;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@Deprecated//TODO - 1.18: Remove
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemStackGasToItemStackRecipeBuilder extends ItemStackChemicalToItemStackRecipeBuilder<Gas, GasStack, GasStackIngredient> {

    protected ItemStackGasToItemStackRecipeBuilder(ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output, ResourceLocation serializerName) {
        super(serializerName, itemInput, gasInput, output, JsonConstants.GAS_INPUT);
    }

    /**
     * @deprecated Use {@link ItemStackChemicalToItemStackRecipeBuilder#compressing(ItemStackIngredient, GasStackIngredient, ItemStack)} instead.
     */
    @Deprecated
    public static ItemStackGasToItemStackRecipeBuilder compressing(ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This compressing recipe requires a non empty item output.");
        }
        return new ItemStackGasToItemStackRecipeBuilder(itemInput, gasInput, output, mekSerializer("compressing"));
    }

    /**
     * @deprecated Use {@link ItemStackChemicalToItemStackRecipeBuilder#purifying(ItemStackIngredient, GasStackIngredient, ItemStack)} instead.
     */
    @Deprecated
    public static ItemStackGasToItemStackRecipeBuilder purifying(ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This purifying recipe requires a non empty item output.");
        }
        return new ItemStackGasToItemStackRecipeBuilder(itemInput, gasInput, output, mekSerializer("purifying"));
    }

    /**
     * @deprecated Use {@link ItemStackChemicalToItemStackRecipeBuilder#injecting(ItemStackIngredient, GasStackIngredient, ItemStack)} instead.
     */
    @Deprecated
    public static ItemStackGasToItemStackRecipeBuilder injecting(ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This injecting recipe requires a non empty item output.");
        }
        return new ItemStackGasToItemStackRecipeBuilder(itemInput, gasInput, output, mekSerializer("injecting"));
    }

    @Override
    protected ItemStackGasToItemStackRecipeResult getResult(ResourceLocation id) {
        return new ItemStackGasToItemStackRecipeResult(id);
    }

    public class ItemStackGasToItemStackRecipeResult extends ItemStackChemicalToItemStackRecipeResult {

        protected ItemStackGasToItemStackRecipeResult(ResourceLocation id) {
            super(id);
        }
    }
}