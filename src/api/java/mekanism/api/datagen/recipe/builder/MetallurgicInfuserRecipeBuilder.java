package mekanism.api.datagen.recipe.builder;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.JsonConstants;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.InfusionStackIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@Deprecated//TODO - 1.18: Remove
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MetallurgicInfuserRecipeBuilder extends ItemStackChemicalToItemStackRecipeBuilder<InfuseType, InfusionStack, InfusionStackIngredient> {

    protected MetallurgicInfuserRecipeBuilder(ItemStackIngredient itemInput, InfusionStackIngredient infusionInput, ItemStack output) {
        super(mekSerializer("metallurgic_infusing"), itemInput, infusionInput, output, JsonConstants.INFUSION_INPUT);
    }

    /**
     * @deprecated Use {@link ItemStackChemicalToItemStackRecipeBuilder#metallurgicInfusing(ItemStackIngredient, InfusionStackIngredient, ItemStack)} instead.
     */
    @Deprecated
    public static MetallurgicInfuserRecipeBuilder metallurgicInfusing(ItemStackIngredient itemInput, InfusionStackIngredient infusionInput, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This metallurgic infusing recipe requires a non empty output.");
        }
        return new MetallurgicInfuserRecipeBuilder(itemInput, infusionInput, output);
    }

    @Override
    protected MetallurgicInfuserRecipeResult getResult(ResourceLocation id) {
        return new MetallurgicInfuserRecipeResult(id);
    }

    public class MetallurgicInfuserRecipeResult extends ItemStackChemicalToItemStackRecipeResult {

        protected MetallurgicInfuserRecipeResult(ResourceLocation id) {
            super(id);
        }
    }
}