package mekanism.api.recipes;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import net.minecraft.util.ResourceLocation;

/**
 * Input: ItemStack
 * <br>
 * Output: PigmentStack
 *
 * @apiNote Pigment Extractors can process this recipe type.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ItemStackToPigmentRecipe extends ItemStackToChemicalRecipe<Pigment, PigmentStack> {

    /**
     * @param id     Recipe name.
     * @param input  Input.
     * @param output Output.
     */
    public ItemStackToPigmentRecipe(ResourceLocation id, ItemStackIngredient input, PigmentStack output) {
        super(id, input, output);
    }
}