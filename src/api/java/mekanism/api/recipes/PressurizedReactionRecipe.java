package mekanism.api.recipes;

import java.util.List;
import java.util.Objects;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.vanilla_input.ReactionRecipeInput;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.TriPredicate;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.Nullable;

/// Input: ItemStack
///
/// Input: FluidStack
///
/// Input: ChemicalStack
///
/// Item Output: ItemStack (can be empty if chemical output is not empty)
///
/// Chemical Output: ChemicalStack (can be empty if item output is not empty)
///
/// @apiNote Pressurized Reaction Chambers can process this recipe type.
public abstract class PressurizedReactionRecipe extends MekanismRecipe<ReactionRecipeInput> implements TriPredicate<ItemStack, FluidStack, ChemicalStack> {

    private static final Holder<Item> PRESSURIZED_REACTION_CHAMBER = DeferredHolder.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "pressurized_reaction_chamber"));

    /// Gets the item input ingredient.
    public abstract ItemStackIngredient getInputSolid();

    /// Gets the fluid input ingredient.
    public abstract FluidStackIngredient getInputFluid();

    /// Gets the chemical input ingredient.
    public abstract ChemicalStackIngredient getInputChemical();

    /// Gets the amount of "extra" energy this recipe requires, compared to the base energy requirements of the machine performing the recipe.
    @Range(from = 0, to = Integer.MAX_VALUE)
    public abstract int getEnergyRequired();

    /// Gets the base duration in ticks that this recipe takes to complete.
    public abstract int getDuration();

    @Override
    public abstract boolean test(ItemStack solid, FluidStack liquid, ChemicalStack chemical);

    @Override
    public boolean matches(ReactionRecipeInput input, Level level) {
        //Don't match incomplete recipes or ones that don't match
        return !isIncomplete() && test(input.item(), input.fluid(), input.chemical());
    }

    /// For JEI, gets the output representations to display.
    ///
    /// @return Representation of the output, **MUST NOT** be modified.
    public abstract List<PressurizedReactionRecipeOutput> getOutputDefinition();

    /// Gets a new output based on the given inputs.
    ///
    /// @param solid    Specific item input.
    /// @param liquid   Specific fluid input.
    /// @param chemical Specific chemical input.
    ///
    /// @return New output.
    ///
    /// @apiNote While Mekanism does not currently make use of the inputs, it is important to support it and pass the proper value in case any addons define input based
    /// outputs where things like NBT may be different.
    /// @implNote The passed in inputs should **NOT** be modified.
    @Contract(value = "_, _, _ -> new", pure = true)
    public abstract PressurizedReactionRecipeOutput getOutput(ItemStack solid, FluidStack liquid, ChemicalStack chemical);

    @Override
    public boolean isIncomplete() {
        return getInputSolid().hasNoMatchingInstances() || getInputFluid().hasNoMatchingInstances() || getInputChemical().hasNoMatchingInstances();
    }

    @Override
    public void logMissingTags() {
        getInputSolid().logMissingTags();
        getInputFluid().logMissingTags();
        getInputChemical().logMissingTags();
    }

    @Override
    public final RecipeType<? extends PressurizedReactionRecipe> getType() {
        return MekanismRecipeTypes.TYPE_REACTION.value();
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(PRESSURIZED_REACTION_CHAMBER);
    }

    /// @apiNote Both item and chemical may be present or one may be empty.
    public record PressurizedReactionRecipeOutput(@Nullable ItemStackTemplate item, @Nullable ChemicalStackTemplate chemical) {

        public PressurizedReactionRecipeOutput {
            if (item == null && chemical == null) {
                throw new IllegalArgumentException("At least one output must be present.");
            }
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (o == this) {
                return true;
            } else if (o == null || getClass() != o.getClass()) {
                return false;
            }
            PressurizedReactionRecipeOutput other = (PressurizedReactionRecipeOutput) o;
            return Objects.equals(item, other.item) && Objects.equals(chemical, other.chemical);
        }

        @Override
        public int hashCode() {
            int hash = 1;
            if (chemical != null) {
                hash = 31 * hash + chemical.hashCode();
            }
            if (item != null) {
                hash = 31 * hash + item.hashCode();
            }
            return hash;
        }
    }
}
