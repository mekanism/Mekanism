package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.FluidSlurryToSlurryRecipe;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.ingredients.SlurryStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;

@NothingNullByDefault
public class BasicFluidSlurryToSlurryRecipe extends FluidSlurryToSlurryRecipe {

    protected final FluidStackIngredient fluidInput;
    protected final SlurryStackIngredient chemicalInput;
    protected final SlurryStack output;

    /**
     * @param fluidInput  Fluid input.
     * @param slurryInput Slurry input.
     * @param output      Output.
     */
    public BasicFluidSlurryToSlurryRecipe(FluidStackIngredient fluidInput, SlurryStackIngredient slurryInput, SlurryStack output) {
        this.fluidInput = Objects.requireNonNull(fluidInput, "Fluid input cannot be null.");
        this.chemicalInput = Objects.requireNonNull(slurryInput, "Chemical input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output cannot be empty.");
        }
        this.output = output.copy();
    }

    @Override
    public boolean test(FluidStack fluidStack, SlurryStack chemicalStack) {
        return fluidInput.test(fluidStack) && chemicalInput.test(chemicalStack);
    }

    @Override
    public FluidStackIngredient getFluidInput() {
        return fluidInput;
    }

    @Override
    public SlurryStackIngredient getChemicalInput() {
        return chemicalInput;
    }

    @Override
    public List<SlurryStack> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    @Override
    @Contract(value = "_, _ -> new", pure = true)
    public SlurryStack getOutput(FluidStack fluidStack, SlurryStack chemicalStack) {
        return output.copy();
    }

    public SlurryStack getOutputRaw() {
        return output;
    }

    @Override
    public RecipeSerializer<BasicFluidSlurryToSlurryRecipe> getSerializer() {
        return MekanismRecipeSerializers.WASHING.get();
    }
}