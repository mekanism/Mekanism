package mekanism.api.recipes.basic;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public class BasicPressurizedReactionRecipe extends PressurizedReactionRecipe {

    protected final ItemStackIngredient inputSolid;
    protected final FluidStackIngredient inputFluid;
    protected final ChemicalStackIngredient inputChemical;
    protected final int energyRequired;
    protected final int duration;
    @Nullable
    protected final ItemStackTemplate outputItem;
    @Nullable
    protected final ChemicalStackTemplate outputChemical;

    /// @param inputSolid     Item input.
    /// @param inputFluid     Fluid input.
    /// @param inputChemical  Chemical input.
    /// @param energyRequired Amount of "extra" energy this recipe requires, compared to the base energy requirements of the machine performing the recipe.
    /// @param duration       Base duration in ticks that this recipe takes to complete. Must be greater than zero.
    /// @param outputItem     Item output.
    /// @param outputChemical Chemical output.
    ///
    /// @apiNote At least one output must not be empty.
    public BasicPressurizedReactionRecipe(ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, ChemicalStackIngredient inputChemical,
          int energyRequired, int duration, @Nullable ItemStackTemplate outputItem, @Nullable ChemicalStackTemplate outputChemical) {
        this.inputSolid = Objects.requireNonNull(inputSolid, "Item input cannot be null.");
        this.inputFluid = Objects.requireNonNull(inputFluid, "Fluid input cannot be null.");
        this.inputChemical = Objects.requireNonNull(inputChemical, "Chemical input cannot be null.");
        Preconditions.checkArgument(energyRequired >= 0, "Energy required must not be negative");
        this.energyRequired = energyRequired;

        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be positive.");
        }
        this.duration = duration;
        if (outputItem == null && outputChemical == null) {
            throw new IllegalArgumentException("At least one output must not be empty.");
        }
        this.outputItem = outputItem;
        this.outputChemical = outputChemical;
    }

    public BasicPressurizedReactionRecipe(ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, ChemicalStackIngredient inputChemical,
          int energyRequired, int duration, Optional<ItemStackTemplate> outputItem, Optional<ChemicalStackTemplate> outputChemical) {
        this(inputSolid, inputFluid, inputChemical, energyRequired, duration, outputItem.orElse(null), outputChemical.orElse(null));
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
    public int getEnergyRequired() {
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
        return new PressurizedReactionRecipeOutput(this.outputItem, this.outputChemical);
    }

    @Nullable
    public ItemStackTemplate getOutputItem() {
        return outputItem;
    }

    public Optional<ItemStackTemplate> getOutputItemOptional() {
        return Optional.ofNullable(outputItem);
    }

    public Optional<ChemicalStackTemplate> getOutputChemicalOptional() {
        return Optional.ofNullable(outputChemical);
    }

    @Nullable
    public ChemicalStackTemplate getOutputChemical() {
        return outputChemical;
    }

    @Override
    public RecipeSerializer<BasicPressurizedReactionRecipe> getSerializer() {
        return MekanismRecipeSerializers.REACTION.get();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicPressurizedReactionRecipe other = (BasicPressurizedReactionRecipe) o;
        return energyRequired == other.energyRequired && duration == other.duration && inputSolid.equals(other.inputSolid) && inputFluid.equals(other.inputFluid) &&
               inputChemical.equals(other.inputChemical) && Objects.equals(outputItem, other.outputItem) && Objects.equals(outputChemical, other.outputChemical);
    }

    @Override
    public int hashCode() {
        int result = inputSolid.hashCode();
        result = 31 * result + inputFluid.hashCode();
        result = 31 * result + inputChemical.hashCode();
        result = 31 * result + energyRequired;
        result = 31 * result + duration;
        if (outputChemical != null) {
            result = 31 * result + outputChemical.hashCode();
        }
        if (outputItem != null) {
            result = 31 * result + outputItem.hashCode();
        }
        return result;
    }
}