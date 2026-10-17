package mekanism.api.recipes.basic;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.SerializationConstants;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.book.ElectrolysisBookCategory.ElectrolysisBookInfo;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.core.TypedInstance;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public class BasicElectrolysisRecipe extends ElectrolysisRecipe {

    protected final FluidStackIngredient input;
    protected final ElectrolysisRecipeOutput output;
    protected final int energyMultiplier;//todo double?

    /// @param commonInfo       Common recipe info.
    /// @param bookInfo         Recipe book info.
    /// @param input            Input.
    /// @param energyMultiplier Multiplier to the energy cost in relation to the configured hydrogen separating energy cost. Must be at least one.
    /// @param output           Output.
    public BasicElectrolysisRecipe(CommonInfo commonInfo, ElectrolysisBookInfo bookInfo, FluidStackIngredient input, int energyMultiplier, ElectrolysisRecipeOutput output) {
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        this.energyMultiplier = energyMultiplier;
        if (energyMultiplier < 1) {
            throw new IllegalArgumentException("Energy multiplier must be at least one.");
        }
        this.output = Objects.requireNonNull(output, "Output cannot be null");
        super(commonInfo, bookInfo);
    }

    @Override
    public FluidStackIngredient getInput() {
        return input;
    }

    @Override
    public List<ElectrolysisRecipeOutput> getOutputDefinition(ContextMap contextMap) {
        return Collections.singletonList(output);
    }

    @Override
    public SlotDisplay.Composite getOutputDisplay() {
        return output.asDisplay();
    }

    @Override
    @Contract(value = "_ -> new", pure = true)
    public ElectrolysisRecipeOutput getOutput(TypedInstance<Fluid> input) {
        return output;
    }

    @Override
    public int getEnergyMultiplier() {
        return energyMultiplier;
    }

    public ElectrolysisRecipeOutput getOutputRaw() {
        return output;
    }

    @Override
    public RecipeSerializer<BasicElectrolysisRecipe> getSerializer() {
        return MekanismRecipeSerializers.SEPARATING.get();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicElectrolysisRecipe other = (BasicElectrolysisRecipe) o;
        return energyMultiplier == other.energyMultiplier && input.equals(other.input) && output.equals(other.output);
    }

    @Override
    public int hashCode() {
        int result = input.hashCode();
        result = 31 * result + output.hashCode();
        result = 31 * result + energyMultiplier;
        return result;
    }

    /// Helper to create a [MapCodec] for a simple [BasicElectrolysisRecipe].
    ///
    /// @param factory Recipe constructor
    ///
    /// @since 10.8.0
    public static <RECIPE extends BasicElectrolysisRecipe> MapCodec<RECIPE> simpleMapCodec(ElectrolysisRecipe.Factory<RECIPE> factory) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
              Recipe.CommonInfo.MAP_CODEC.forGetter(MekanismRecipe::commonInfo),
              ElectrolysisBookInfo.MAP_CODEC.forGetter(MekanismRecipe::bookInfo),
              FluidStackIngredient.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(ElectrolysisRecipe::getInput),
              ExtraCodecs.POSITIVE_INT.optionalFieldOf(SerializationConstants.ENERGY_MULTIPLIER, 1).forGetter(ElectrolysisRecipe::getEnergyMultiplier),
              ElectrolysisRecipeOutput.CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(recipe -> recipe.output)
        ).apply(instance, factory::create));
    }

    /// Helper to create a [StreamCodec] for a simple [BasicElectrolysisRecipe].
    ///
    /// @param factory Recipe constructor
    ///
    /// @since 10.8.0
    public static <RECIPE extends BasicElectrolysisRecipe> StreamCodec<RegistryFriendlyByteBuf, RECIPE> simpleStreamCodec(ElectrolysisRecipe.Factory<RECIPE> factory) {
        return StreamCodec.composite(
              CommonInfo.STREAM_CODEC, MekanismRecipe::commonInfo,
              ElectrolysisBookInfo.STREAM_CODEC, MekanismRecipe::bookInfo,
              FluidStackIngredient.STREAM_CODEC, ElectrolysisRecipe::getInput,
              ByteBufCodecs.VAR_INT, ElectrolysisRecipe::getEnergyMultiplier,
              ElectrolysisRecipeOutput.STREAM_CODEC, recipe -> recipe.output,
              factory::create
        );
    }
}