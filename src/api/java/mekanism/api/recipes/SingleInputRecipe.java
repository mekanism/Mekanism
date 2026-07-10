package mekanism.api.recipes;

import java.util.List;
import java.util.function.Predicate;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.vanilla_input.SingleChemicalRecipeInput;
import mekanism.api.recipes.vanilla_input.SingleFluidRecipeInput;
import net.minecraft.core.TypedInstance;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;

/// Base class to make implementing single input recipes easier
///
/// @since 10.8.0
public abstract class SingleInputRecipe<HOLDER_TYPE, STACK extends TypedInstance<HOLDER_TYPE>, INPUT extends InputIngredient<HOLDER_TYPE, STACK>,
      VANILLA_INPUT extends RecipeInput, OUTPUT> extends MekanismRecipe<VANILLA_INPUT> implements Predicate<STACK> {

    /// Gets the input ingredient.
    public abstract INPUT getInput();

    @Override
    public final boolean test(STACK stack) {
        return getInput().test(stack);
    }

    /// Helper to test this recipe against an instance but ignoring size.
    ///
    /// @param instance Input instance.
    ///
    /// @return `true` if the instance's type matches the input.
    public final boolean testType(TypedInstance<HOLDER_TYPE> instance) {
        return getInput().testType(instance);
    }

    /// Gets a new output based on the given input.
    ///
    /// @param input Specific input.
    ///
    /// @return New output.
    ///
    /// @apiNote While Mekanism does not currently make use of the input, it is important to support it and pass the proper value in case any addons define input based
    /// outputs where things like NBT may be different
    @Contract(pure = true)
    public abstract OUTPUT getOutput(TypedInstance<HOLDER_TYPE> input);

    /// For JEI, gets the output representations to display.
    ///
    /// @return Representation of the output, **MUST NOT** be modified.
    public abstract List<OUTPUT> getOutputDefinition(ContextMap contextMap);

    /// {@return a slot display for the output of the recipe}
    ///
    /// @since 10.8.0
    public abstract SlotDisplay getOutputDisplay();

    @Override
    public boolean isIncomplete() {
        return getInput().hasNoMatchingInstances();
    }

    @Override
    public void logMissingTags() {
        getInput().logMissingTags();
    }

    public abstract static class ChemicalInputRecipe<OUTPUT> extends SingleInputRecipe<Chemical, ChemicalStack, ChemicalStackIngredient, SingleChemicalRecipeInput, OUTPUT> {

        @Override
        public boolean matches(SingleChemicalRecipeInput input, Level level) {
            //Don't match incomplete recipes or ones that don't match
            return !isIncomplete() && test(input.chemical());
        }
    }

    public abstract static class FluidInputRecipe<OUTPUT> extends SingleInputRecipe<Fluid, FluidStack, FluidStackIngredient, SingleFluidRecipeInput, OUTPUT> {

        @Override
        public boolean matches(SingleFluidRecipeInput input, Level level) {
            //Don't match incomplete recipes or ones that don't match
            return !isIncomplete() && test(input.fluid());
        }
    }

    public abstract static class ItemInputRecipe<OUTPUT> extends SingleInputRecipe<Item, ItemStack, ItemStackIngredient, SingleRecipeInput, OUTPUT> {

        @Override
        public boolean matches(SingleRecipeInput input, Level level) {
            //Don't match incomplete recipes or ones that don't match
            return !isIncomplete() && test(input.item());
        }
    }
}