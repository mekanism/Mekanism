package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.FluidChemicalToChemicalRecipe;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.display.CombiningRecipeDisplay;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.chemical.display.ChemicalStackSlotDisplay;
import net.minecraft.core.Holder;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public class BasicWashingRecipe extends FluidChemicalToChemicalRecipe {

    private static final Holder<Item> CHEMICAL_WASHER = DeferredHolder.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "chemical_washer"));

    protected final FluidStackIngredient fluidInput;
    protected final ChemicalStackIngredient chemicalInput;
    protected final ChemicalStackTemplate output;

    /// @param fluidInput    Fluid input.
    /// @param chemicalInput Chemical input.
    /// @param output        Output.
    public BasicWashingRecipe(FluidStackIngredient fluidInput, ChemicalStackIngredient chemicalInput, ChemicalStackTemplate output) {
        this.fluidInput = Objects.requireNonNull(fluidInput, "Fluid input cannot be null.");
        this.chemicalInput = Objects.requireNonNull(chemicalInput, "Chemical input cannot be null.");
        this.output = Objects.requireNonNull(output, "Output cannot be null.");
    }

    @Override
    public final RecipeType<FluidChemicalToChemicalRecipe> getType() {
        return MekanismRecipeTypes.TYPE_WASHING.value();
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new CombiningRecipeDisplay(
              getChemicalInput().display(),
              getFluidInput().display(),
              getOutputDisplay(),
              new SlotDisplay.ItemSlotDisplay(CHEMICAL_WASHER)
        ));
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
    public List<ChemicalStackTemplate> getOutputDefinition(ContextMap contextMap) {
        return Collections.singletonList(output);
    }

    @Override
    public SlotDisplay getOutputDisplay() {
        return new ChemicalStackSlotDisplay(output);
    }

    @Override
    @Contract(value = "_, _ -> new", pure = true)
    public ChemicalStackTemplate getOutput(TypedInstance<Fluid> fluidStack, TypedInstance<Chemical> chemicalStack) {
        return output;
    }

    public ChemicalStackTemplate getOutputRaw() {
        return output;
    }

    @Override
    public RecipeSerializer<BasicWashingRecipe> getSerializer() {
        return MekanismRecipeSerializers.WASHING.get();
    }

    @Override
    public boolean equals(@Nullable Object o) {
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