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
    protected final ChemicalStackIngredient inputGas;
    protected final long energyRequired;
    protected final int duration;
    protected final ItemStack outputItem;
    protected final ChemicalStack outputGas;

    /**
     * @param inputSolid     Item input.
     * @param inputFluid     Fluid input.
     * @param inputGas       Gas input.
     * @param energyRequired Amount of "extra" energy this recipe requires, compared to the base energy requirements of the machine performing the recipe.
     * @param duration       Base duration in ticks that this recipe takes to complete. Must be greater than zero.
     * @param outputItem     Item output.
     * @param outputGas      Gas output.
     *
     * @apiNote At least one output must not be empty.
     */
    public BasicPressurizedReactionRecipe(ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, ChemicalStackIngredient inputGas,
          long energyRequired, int duration, ItemStack outputItem, ChemicalStack outputGas) {
        this.inputSolid = Objects.requireNonNull(inputSolid, "Item input cannot be null.");
        this.inputFluid = Objects.requireNonNull(inputFluid, "Fluid input cannot be null.");
        this.inputGas = Objects.requireNonNull(inputGas, "Gas input cannot be null.");
        Preconditions.checkArgument(energyRequired >= 0, "Energy required must not be negative");
        this.energyRequired = energyRequired;

        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be positive.");
        }
        this.duration = duration;
        Objects.requireNonNull(outputItem, "Item output cannot be null.");
        Objects.requireNonNull(outputGas, "Gas output cannot be null.");
        if (outputItem.isEmpty() && outputGas.isEmpty()) {
            throw new IllegalArgumentException("At least one output must not be empty.");
        }
        this.outputItem = outputItem.copy();
        this.outputGas = outputGas.copy();
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
    public ChemicalStackIngredient getInputGas() {
        return inputGas;
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
    public boolean test(ItemStack solid, FluidStack liquid, ChemicalStack gas) {
        return this.inputSolid.test(solid) && this.inputFluid.test(liquid) && this.inputGas.test(gas);
    }

    @Override
    public List<PressurizedReactionRecipeOutput> getOutputDefinition() {
        return Collections.singletonList(new PressurizedReactionRecipeOutput(outputItem, outputGas));
    }

    @Override
    @Contract(value = "_, _, _ -> new", pure = true)
    public PressurizedReactionRecipeOutput getOutput(ItemStack solid, FluidStack liquid, ChemicalStack gas) {
        return new PressurizedReactionRecipeOutput(this.outputItem.copy(), this.outputGas.copy());
    }

    public ItemStack getOutputItem() {
        return outputItem;
    }

    public ChemicalStack getOutputGas() {
        return outputGas;
    }

    @Override
    public RecipeSerializer<BasicPressurizedReactionRecipe> getSerializer() {
        return MekanismRecipeSerializers.REACTION.get();
    }
}