package mekanism.api.recipes;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.vanilla_input.SingleFluidChemicalRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Base class for defining fluid chemical to chemical recipes.
 * <br>
 * Input: FluidStack
 * <br>
 * Input: Chemical
 * <br>
 * Output: ChemicalStack
 *
 * @apiNote Chemical Washers can process this recipe type.
 */
@NothingNullByDefault
public abstract class FluidChemicalToChemicalRecipe extends TwoInputMekRecipe<Fluid, FluidStack, FluidStackIngredient, Chemical, ChemicalStack, ChemicalStackIngredient, SingleFluidChemicalRecipeInput, ChemicalStackTemplate> {

    @Override
    public boolean matches(SingleFluidChemicalRecipeInput input, Level level) {
        //Don't match incomplete recipes or ones that don't match
        return !isIncomplete() && test(input.fluid(), input.chemical());
    }

    /**
     * Gets the input fluid ingredient.
     */
    public abstract FluidStackIngredient getFluidInput();

    @Override
    public final FluidStackIngredient getInputA() {
        return getFluidInput();
    }

    /**
     * Gets the input chemical ingredient.
     */
    public abstract ChemicalStackIngredient getChemicalInput();

    @Override
    public final ChemicalStackIngredient getInputB() {
        return getChemicalInput();
    }
}