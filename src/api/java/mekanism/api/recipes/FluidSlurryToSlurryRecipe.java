package mekanism.api.recipes;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.chemical.FluidChemicalToChemicalRecipe;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.ChemicalStackIngredient.SlurryStackIngredient;
import net.minecraft.resources.ResourceLocation;

/**
 * Input: FluidStack
 * <br>
 * Input: Slurry
 * <br>
 * Output: SlurryStack
 *
 * @apiNote Chemical Washers can process this recipe type.
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class FluidSlurryToSlurryRecipe extends FluidChemicalToChemicalRecipe<Slurry, SlurryStack, SlurryStackIngredient> {

    /**
     * @param id          Recipe name.
     * @param fluidInput  Fluid input.
     * @param slurryInput Slurry input.
     * @param output      Output.
     */
    public FluidSlurryToSlurryRecipe(ResourceLocation id, FluidStackIngredient fluidInput, SlurryStackIngredient slurryInput, SlurryStack output) {
        super(id, fluidInput, slurryInput, output);
    }
}