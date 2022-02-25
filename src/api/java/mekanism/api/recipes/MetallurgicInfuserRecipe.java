package mekanism.api.recipes;

import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.chemical.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.InfusionStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Input: ItemStack
 * <br>
 * Input: Infuse Type
 * <br>
 * Output: ItemStack
 *
 * @apiNote Metallurgic Infusers and Infusing Factories can process this recipe type.
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MetallurgicInfuserRecipe extends ItemStackChemicalToItemStackRecipe<InfuseType, InfusionStack, InfusionStackIngredient> {

    /**
     * @param id            Recipe name.
     * @param itemInput     Item input.
     * @param infusionInput Infusion input.
     * @param output        Output.
     */
    public MetallurgicInfuserRecipe(ResourceLocation id, ItemStackIngredient itemInput, InfusionStackIngredient infusionInput, ItemStack output) {
        super(id, itemInput, infusionInput, output);
    }
}