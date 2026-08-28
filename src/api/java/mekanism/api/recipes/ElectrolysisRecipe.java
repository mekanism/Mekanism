package mekanism.api.recipes;

import java.util.List;
import java.util.Objects;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.ElectrolysisRecipe.ElectrolysisRecipeOutput;
import mekanism.api.recipes.SingleInputRecipe.FluidInputRecipe;
import mekanism.api.recipes.display.SimpleMachineRecipeDisplay;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

/// Input: FluidStack
///
/// Left Output: ChemicalStack
///
/// Right Output: ChemicalStack
///
/// @apiNote Electrolytic Separators can process this recipe type.
public abstract class ElectrolysisRecipe extends FluidInputRecipe<ElectrolysisRecipeOutput> {

    private static final Holder<Item> ELECTROLYTIC_SEPARATOR = DeferredHolder.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "electrolytic_separator"));

    /// Gets the multiplier to the energy cost in relation to the configured hydrogen separating energy cost.
    public abstract int getEnergyMultiplier();

    @Override
    public final RecipeType<ElectrolysisRecipe> getType() {
        return MekanismRecipeTypes.TYPE_SEPARATING.value();
    }

    /// {@inheritDoc} The first element with be for the left output, the second will be for the right output.
    @Override
    public abstract SlotDisplay.Composite getOutputDisplay();

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new SimpleMachineRecipeDisplay(
              getInput().display(),
              getOutputDisplay(),
              new SlotDisplay.ItemSlotDisplay(ELECTROLYTIC_SEPARATOR)
        ));
    }

    public record ElectrolysisRecipeOutput(ChemicalStackTemplate left, ChemicalStackTemplate right) {

        public ElectrolysisRecipeOutput {
            Objects.requireNonNull(left, "Left output cannot be null.");
            Objects.requireNonNull(right, "Right output cannot be null.");
        }
    }
}
