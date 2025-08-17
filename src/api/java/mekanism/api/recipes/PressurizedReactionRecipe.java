package mekanism.api.recipes;

import java.util.List;
import java.util.Objects;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.vanilla_input.ReactionRecipeInput;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.TriPredicate;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * Input: ItemStack
 * <br>
 * Input: FluidStack
 * <br>
 * Input: ChemicalStack
 * <br>
 * Item Output: ItemStack (can be empty if chemical output is not empty)
 * <br>
 * Chemical Output: ChemicalStack (can be empty if item output is not empty)
 *
 * @apiNote Pressurized Reaction Chambers can process this recipe type.
 */
@NothingNullByDefault
public abstract class PressurizedReactionRecipe extends MekanismRecipe<ReactionRecipeInput> implements TriPredicate<@NotNull ItemStack, @NotNull FluidStack, @NotNull ChemicalStack> {

    private static final Holder<Item> PRESSURIZED_REACTION_CHAMBER = DeferredHolder.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "pressurized_reaction_chamber"));

    /**
     * Gets the item input ingredient.
     */
    public abstract ItemStackIngredient getInputSolid();

    /**
     * Gets the fluid input ingredient.
     */
    public abstract FluidStackIngredient getInputFluid();

    /**
     * Gets the chemical input ingredient.
     */
    public abstract ChemicalStackIngredient getInputChemical();

    /**
     * Gets the amount of "extra" energy this recipe requires, compared to the base energy requirements of the machine performing the recipe.
     */
    @Range(from = 0, to = Long.MAX_VALUE)
    public abstract long getEnergyRequired();

    /**
     * Gets the base duration in ticks that this recipe takes to complete.
     */
    public abstract int getDuration();

    @Override
    public abstract boolean test(ItemStack solid, FluidStack liquid, ChemicalStack chemical);

    @Override
    public boolean matches(ReactionRecipeInput input, Level level) {
        //Don't match incomplete recipes or ones that don't match
        return !isIncomplete() && test(input.item(), input.fluid(), input.chemical());
    }

    /**
     * For JEI, gets the output representations to display.
     *
     * @return Representation of the output, <strong>MUST NOT</strong> be modified.
     */
    public abstract List<PressurizedReactionRecipeOutput> getOutputDefinition();

    /**
     * Gets a new output based on the given inputs.
     *
     * @param solid  Specific item input.
     * @param liquid Specific fluid input.
     * @param chemical    Specific chemical input.
     *
     * @return New output.
     *
     * @apiNote While Mekanism does not currently make use of the inputs, it is important to support it and pass the proper value in case any addons define input based
     * outputs where things like NBT may be different.
     * @implNote The passed in inputs should <strong>NOT</strong> be modified.
     */
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
    public final RecipeType<?> getType() {
        return MekanismRecipeTypes.TYPE_REACTION.value();
    }

    @Override
    public String getGroup() {
        return "pressurized_reaction_chamber";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(PRESSURIZED_REACTION_CHAMBER);
    }

    /**
     * @apiNote Both item and chemical may be present or one may be empty.
     */
    public record PressurizedReactionRecipeOutput(@NotNull ItemStack item, @NotNull ChemicalStack chemical) {

        public PressurizedReactionRecipeOutput {
            Objects.requireNonNull(item, "Item output cannot be null.");
            Objects.requireNonNull(chemical, "Chemical output cannot be null.");
            if (item.isEmpty() && chemical.isEmpty()) {
                throw new IllegalArgumentException("At least one output must be present.");
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (o == null || getClass() != o.getClass()) {
                return false;
            }
            PressurizedReactionRecipeOutput other = (PressurizedReactionRecipeOutput) o;
            return ItemStack.matches(item, other.item) && chemical.equals(other.chemical);
        }

        @Override
        public int hashCode() {
            int hash = chemical.hashCode();
            if (!item.isEmpty()) {
                hash = 31 * hash + ItemStack.hashItemAndComponents(item);
                hash = 31 * hash + item.getCount();
            }
            return hash;
        }
    }
}
