package mekanism.api.recipes.basic;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;

@NothingNullByDefault
public class BasicPressurizedReactionRecipe extends PressurizedReactionRecipe {

    protected final ItemStackIngredient inputSolid;
    protected final FluidStackIngredient inputFluid;
    protected final ChemicalStackIngredient inputChemical;
    protected final long energyRequired;
    protected final int duration;
    protected final ItemStack outputItem;
    protected final ChemicalStack outputChemical;

    /**
     * @param inputSolid     Item input.
     * @param inputFluid     Fluid input.
     * @param inputChemical       Chemical input.
     * @param energyRequired Amount of "extra" energy this recipe requires, compared to the base energy requirements of the machine performing the recipe.
     * @param duration       Base duration in ticks that this recipe takes to complete. Must be greater than zero.
     * @param outputItem     Item output.
     * @param outputChemical      Chemical output.
     *
     * @apiNote At least one output must not be empty.
     */
    public BasicPressurizedReactionRecipe(ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, ChemicalStackIngredient inputChemical,
          long energyRequired, int duration, ItemStack outputItem, ChemicalStack outputChemical) {
        this.inputSolid = Objects.requireNonNull(inputSolid, "Item input cannot be null.");
        this.inputFluid = Objects.requireNonNull(inputFluid, "Fluid input cannot be null.");
        this.inputChemical = Objects.requireNonNull(inputChemical, "Chemical input cannot be null.");
        Preconditions.checkArgument(energyRequired >= 0, "Energy required must not be negative");
        this.energyRequired = energyRequired;

        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be positive.");
        }
        this.duration = duration;
        Objects.requireNonNull(outputItem, "Item output cannot be null.");
        Objects.requireNonNull(outputChemical, "Chemical output cannot be null.");
        if (outputItem.isEmpty() && outputChemical.isEmpty()) {
            throw new IllegalArgumentException("At least one output must not be empty.");
        }
        this.outputItem = outputItem.copy();
        this.outputChemical = outputChemical.copy();
    }

    @Override
    public ItemStackIngredient getInputSolid() {
        return inputSolid;
    }

    @Override
    public FluidStackIngredient getInputFluid() {
        return inputFluid;
    }

    @Override
    public ChemicalStackIngredient getInputChemical() {
        return inputChemical;
    }

    @Override
    public long getEnergyRequired() {
        return energyRequired;
    }

    @Override
    public int getDuration() {
        return duration;
    }

    @Override
    public boolean test(ItemStack solid, FluidStack liquid, ChemicalStack chemical) {
        return this.inputSolid.test(solid) && this.inputFluid.test(liquid) && this.inputChemical.test(chemical);
    }

    @Override
    public List<PressurizedReactionRecipeOutput> getOutputDefinition() {
        return Collections.singletonList(new PressurizedReactionRecipeOutput(outputItem, outputChemical));
    }

    @Override
    @Contract(value = "_, _, _ -> new", pure = true)
    public PressurizedReactionRecipeOutput getOutput(ItemStack solid, FluidStack liquid, ChemicalStack chemical) {
        return new PressurizedReactionRecipeOutput(this.outputItem.copy(), this.outputChemical.copy());
    }

    public ItemStack getOutputItem() {
        return outputItem;
    }

    public ChemicalStack getOutputChemical() {
        return outputChemical;
    }

    @Override
    public RecipeSerializer<BasicPressurizedReactionRecipe> getSerializer() {
        return MekanismRecipeSerializers.REACTION.get();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicPressurizedReactionRecipe other = (BasicPressurizedReactionRecipe) o;
        return energyRequired == other.energyRequired && duration == other.duration && inputSolid.equals(other.inputSolid) && inputFluid.equals(other.inputFluid) &&
               inputChemical.equals(other.inputChemical) && ItemStack.matches(outputItem, other.outputItem) && outputChemical.equals(other.outputChemical);
    }

    @Override
    public int hashCode() {
        int result = inputSolid.hashCode();
        result = 31 * result + inputFluid.hashCode();
        result = 31 * result + inputChemical.hashCode();
        result = 31 * result + Long.hashCode(energyRequired);
        result = 31 * result + duration;
        result = 31 * result + outputChemical.hashCode();
        if (!outputItem.isEmpty()) {
            result = 31 * result + ItemStack.hashItemAndComponents(outputItem);
            result = 31 * result + outputItem.getCount();
        }
        return result;
    }
}