package mekanism.api.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.ElectrolysisRecipe.ElectrolysisRecipeOutput;
import mekanism.api.recipes.SingleInputRecipe.FluidInputRecipe;
import mekanism.api.recipes.book.ElectrolysisBookCategory;
import mekanism.api.recipes.book.ElectrolysisBookCategory.ElectrolysisBookInfo;
import mekanism.api.recipes.display.recipe.ElectrolysisRecipeDisplay;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.chemical.display.ChemicalStackSlotDisplay;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
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
public abstract class ElectrolysisRecipe extends FluidInputRecipe<ElectrolysisRecipeOutput, ElectrolysisBookCategory, ElectrolysisBookInfo> {

    private static final Holder<Item> ELECTROLYTIC_SEPARATOR = DeferredHolder.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "electrolytic_separator"));

    /// @param commonInfo Common recipe info.
    /// @param bookInfo   Recipe book info.
    ///
    /// @since 10.8.0
    protected ElectrolysisRecipe(Recipe.CommonInfo commonInfo, ElectrolysisBookInfo bookInfo) {
        super(commonInfo, bookInfo);
    }

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
        return List.of(new ElectrolysisRecipeDisplay(
              getInput().display(),
              getOutputDisplay(),
              new SlotDisplay.ItemSlotDisplay(ELECTROLYTIC_SEPARATOR),
              getEnergyMultiplier()
        ));
    }

    public record ElectrolysisRecipeOutput(ChemicalStackTemplate left, ChemicalStackTemplate right) {

        public static final Codec<ElectrolysisRecipeOutput> CODEC = RecordCodecBuilder.create(instance -> instance.group(
              ChemicalStackTemplate.MAP_CODEC.fieldOf(SerializationConstants.LEFT).forGetter(ElectrolysisRecipeOutput::left),
              ChemicalStackTemplate.MAP_CODEC.fieldOf(SerializationConstants.RIGHT).forGetter(ElectrolysisRecipeOutput::right)
        ).apply(instance, ElectrolysisRecipeOutput::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, ElectrolysisRecipeOutput> STREAM_CODEC = StreamCodec.composite(
              ChemicalStackTemplate.STREAM_CODEC, ElectrolysisRecipeOutput::left,
              ChemicalStackTemplate.STREAM_CODEC, ElectrolysisRecipeOutput::right,
              ElectrolysisRecipeOutput::new
        );

        public ElectrolysisRecipeOutput {
            Objects.requireNonNull(left, "Left output cannot be null.");
            Objects.requireNonNull(right, "Right output cannot be null.");
        }

        /// The first element with be for the left output, the second will be for the right output.
        ///
        /// @since 10.8.0
        public SlotDisplay.Composite asDisplay() {
            return new SlotDisplay.Composite(List.of(new ChemicalStackSlotDisplay(left), new ChemicalStackSlotDisplay(right)));
        }

        /// {@return if either output is of the given chemical type}
        ///
        /// @since 10.8.0
        public boolean has(ResourceKey<Chemical> type) {
            return left.is(type) || right.is(type);
        }
    }

    /// Helper interface for constructing [ElectrolysisRecipes][ElectrolysisRecipe]
    ///
    /// @since 10.8.0
    @FunctionalInterface
    public interface Factory<RECIPE extends ElectrolysisRecipe> {

        /// @param commonInfo       Common recipe info.
        /// @param bookInfo         Recipe book info.
        /// @param input            Input ingredient.
        /// @param energyMultiplier Multiplier to the energy cost in relation to the configured hydrogen separating energy cost. Must be at least one.
        /// @param output           Output chemicals.
        RECIPE create(CommonInfo commonInfo, ElectrolysisBookInfo bookInfo, FluidStackIngredient input, int energyMultiplier, ElectrolysisRecipeOutput output);
    }
}
