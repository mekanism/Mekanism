package mekanism.api.recipes;

import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;

/**
 * Input: ItemStack
 * <br>
 * Output: InfusionStack
 *
 * @apiNote Infusion conversion recipes can be used in any slots in Mekanism machines that are able to convert items to infuse types, for example in Metallurgic Infusers
 * and Infusing Factories.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ItemStackToInfuseTypeRecipe extends ItemStackToChemicalRecipe<InfuseType, InfusionStack> {

    /**
     * @param id     Recipe name.
     * @param input  Input.
     * @param output Output.
     */
    public ItemStackToInfuseTypeRecipe(ResourceLocation id, ItemStackIngredient input, InfusionStack output) {
        super(id, input, output);
    }
}