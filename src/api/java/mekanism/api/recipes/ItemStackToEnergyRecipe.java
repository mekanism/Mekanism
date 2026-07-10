package mekanism.api.recipes;

import java.util.function.Predicate;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Range;

/// Input: ItemStack
///
/// Output: int
///
/// @apiNote Energy conversion recipes can be used in any slots in Mekanism machines that are able to convert items into energy.
public abstract class ItemStackToEnergyRecipe extends MekanismRecipe<SingleRecipeInput> implements Predicate<ItemStack> {

    @Override
    public abstract boolean test(ItemStack itemStack);

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        //Don't match incomplete recipes or ones that don't match
        return !isIncomplete() && test(input.item());
    }

    /// Gets the input ingredient.
    public abstract ItemStackIngredient getInput();

    /// Gets the output based on the given input.
    ///
    /// @param input Specific input.
    ///
    /// @return Output as a constant.
    ///
    /// @apiNote While Mekanism does not currently make use of the input, it is important to support it and pass the proper value in case any addons define input based
    /// outputs where things like NBT may be different.
    /// @implNote The passed in input should **NOT** be modified.
    @Range(from = 1, to = Integer.MAX_VALUE)
    public abstract int getOutput(ItemStack input);

    /// For JEI, gets the output representations to display.
    ///
    /// @return Representation of the output, **MUST NOT** be modified.
    public abstract int[] getOutputDefinition(ContextMap contextMap);

    @Override
    public boolean isIncomplete() {
        return getInput().hasNoMatchingInstances();
    }

    @Override
    public void logMissingTags() {
        getInput().logMissingTags();
    }

    @Override
    public final RecipeType<ItemStackToEnergyRecipe> getType() {
        return MekanismRecipeTypes.TYPE_ENERGY_CONVERSION.value();
    }
}
