package mekanism.api.recipes;

import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.chemical.FluidChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.SlurryStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;

/**
 * Input: FluidStack
 * <br>
 * Input: Slurry
 * <br>
 * Output: SlurryStack
 *
 * @apiNote Chemical Washers can process this recipe type.
 */
@NothingNullByDefault
public abstract class FluidSlurryToSlurryRecipe extends FluidChemicalToChemicalRecipe<Slurry, SlurryStack, SlurryStackIngredient> {

    @Override
    public abstract boolean test(FluidStack fluidStack, SlurryStack chemicalStack);

    @Override
    public abstract FluidStackIngredient getFluidInput();

    @Override
    public abstract SlurryStackIngredient getChemicalInput();

    @Override
    public abstract List<SlurryStack> getOutputDefinition();

    @Override
    @Contract(value = "_, _ -> new", pure = true)
    public abstract SlurryStack getOutput(FluidStack fluidStack, SlurryStack chemicalStack);

    @Override
    public final RecipeType<FluidSlurryToSlurryRecipe> getType() {
        return MekanismRecipeTypes.TYPE_WASHING.get();
    }

    @Override
    public String getGroup() {
        return "chemical_washer";
    }
}
