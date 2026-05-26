package mekanism.api.recipes;

import java.util.Objects;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ElectrolysisRecipe.ElectrolysisRecipeOutput;
import mekanism.api.recipes.SingleInputRecipe.FluidInputRecipe;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Input: FluidStack
 * <br>
 * Left Output: ChemicalStack
 * <br>
 * Right Output: ChemicalStack
 *
 * @apiNote Electrolytic Separators can process this recipe type.
 */
@NothingNullByDefault
public abstract class ElectrolysisRecipe extends FluidInputRecipe<ElectrolysisRecipeOutput> {

    private static final Holder<Item> ELECTROLYTIC_SEPARATOR = DeferredHolder.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "electrolytic_separator"));

    /**
     * Gets the multiplier to the energy cost in relation to the configured hydrogen separating energy cost.
     */
    public abstract long getEnergyMultiplier();

    @Override
    public final RecipeType<ElectrolysisRecipe> getType() {
        return MekanismRecipeTypes.TYPE_SEPARATING.value();
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ELECTROLYTIC_SEPARATOR);
    }

    public record ElectrolysisRecipeOutput(ChemicalStack left, ChemicalStack right) {

        public ElectrolysisRecipeOutput {
            Objects.requireNonNull(left, "Left output cannot be null.");
            Objects.requireNonNull(right, "Right output cannot be null.");
            if (left.isEmpty()) {
                throw new IllegalArgumentException("Left output cannot be empty.");
            } else if (right.isEmpty()) {
                throw new IllegalArgumentException("Right output cannot be empty.");
            }
        }
    }
}
