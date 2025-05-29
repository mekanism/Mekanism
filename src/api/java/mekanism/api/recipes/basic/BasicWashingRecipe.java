package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.FluidChemicalToChemicalRecipe;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Contract;

@NothingNullByDefault
public class BasicWashingRecipe extends FluidChemicalToChemicalRecipe {

    private static final Holder<Item> CHEMICAL_WASHER = DeferredHolder.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "chemical_washer"));

    protected final FluidStackIngredient fluidInput;
    protected final ChemicalStackIngredient chemicalInput;
    protected final ChemicalStack output;

    /**
     * @param fluidInput    Fluid input.
     * @param chemicalInput Chemical input.
     * @param output        Output.
     */
    public BasicWashingRecipe(FluidStackIngredient fluidInput, ChemicalStackIngredient chemicalInput, ChemicalStack output) {
        this.fluidInput = Objects.requireNonNull(fluidInput, "Fluid input cannot be null.");
        this.chemicalInput = Objects.requireNonNull(chemicalInput, "Chemical input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output cannot be empty.");
        }
        this.output = output.copy();
    }

    @Override
    public final RecipeType<FluidChemicalToChemicalRecipe> getType() {
        return MekanismRecipeTypes.TYPE_WASHING.value();
    }

    @Override
    public String getGroup() {
        return "chemical_washer";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(CHEMICAL_WASHER);
    }

    @Override
    public boolean test(FluidStack fluidStack, ChemicalStack chemicalStack) {
        return fluidInput.test(fluidStack) && chemicalInput.test(chemicalStack);
    }

    @Override
    public FluidStackIngredient getFluidInput() {
        return fluidInput;
    }

    @Override
    public ChemicalStackIngredient getChemicalInput() {
        return chemicalInput;
    }

    @Override
    public List<ChemicalStack> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    @Override
    @Contract(value = "_, _ -> new", pure = true)
    public ChemicalStack getOutput(FluidStack fluidStack, ChemicalStack chemicalStack) {
        return output.copy();
    }

    public ChemicalStack getOutputRaw() {
        return output;
    }

    @Override
    public RecipeSerializer<BasicWashingRecipe> getSerializer() {
        return MekanismRecipeSerializers.WASHING.get();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicWashingRecipe other = (BasicWashingRecipe) o;
        return fluidInput.equals(other.fluidInput) && chemicalInput.equals(other.chemicalInput) && output.equals(other.output);
    }

    @Override
    public int hashCode() {
        int result = fluidInput.hashCode();
        result = 31 * result + chemicalInput.hashCode();
        result = 31 * result + output.hashCode();
        return result;
    }
}