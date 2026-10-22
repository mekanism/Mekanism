package mekanism.api.recipes.basic;

import com.google.common.base.Preconditions;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.book.ReactingBookCategory.ReactingBookInfo;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public class BasicPressurizedReactionRecipe extends PressurizedReactionRecipe {

    protected final ItemStackIngredient inputSolid;
    protected final FluidStackIngredient inputFluid;
    protected final ChemicalStackIngredient inputChemical;
    protected final int energyRequired;
    protected final int duration;
    protected final PressurizedReactionRecipeOutput output;

    /// @param commonInfo     Common recipe info.
    /// @param bookInfo       Recipe book info.
    /// @param inputSolid     Item input.
    /// @param inputFluid     Fluid input.
    /// @param inputChemical  Chemical input.
    /// @param energyRequired Amount of "extra" energy this recipe requires, compared to the base energy requirements of the machine performing the recipe.
    /// @param duration       Base duration in ticks that this recipe takes to complete. Must be greater than zero.
    /// @param output         Output.
    ///
    /// @apiNote At least one output must not be empty.
    public BasicPressurizedReactionRecipe(CommonInfo commonInfo, ReactingBookInfo bookInfo, ItemStackIngredient inputSolid, FluidStackIngredient inputFluid,
          ChemicalStackIngredient inputChemical, int energyRequired, int duration, PressurizedReactionRecipeOutput output) {
        this.inputSolid = Objects.requireNonNull(inputSolid, "Item input cannot be null.");
        this.inputFluid = Objects.requireNonNull(inputFluid, "Fluid input cannot be null.");
        this.inputChemical = Objects.requireNonNull(inputChemical, "Chemical input cannot be null.");
        Preconditions.checkArgument(energyRequired >= 0, "Energy required must not be negative");
        this.energyRequired = energyRequired;
        Preconditions.checkArgument(duration > 0, "Duration must be positive.");
        this.duration = duration;
        this.output = Objects.requireNonNull(output, "Output cannot be null.");
        super(commonInfo, bookInfo);
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
    public List<PressurizedReactionRecipeOutput> getOutputDefinition(ContextMap contextMap) {
        return Collections.singletonList(output);
    }

    @Override
    public SlotDisplay getOutputDisplay() {
        return output.asDisplay();
    }

    @Override
    @Contract(value = "_, _, _ -> new", pure = true)
    public PressurizedReactionRecipeOutput getOutput(ItemStack solid, FluidStack liquid, ChemicalStack chemical) {
        return output;
    }

    public PressurizedReactionRecipeOutput getOutputRaw() {
        return output;
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
               inputChemical.equals(other.inputChemical) && output.equals(other.output);
    }

    @Override
    public int hashCode() {
        int result = inputSolid.hashCode();
        result = 31 * result + inputFluid.hashCode();
        result = 31 * result + inputChemical.hashCode();
        result = 31 * result + energyRequired;
        result = 31 * result + duration;
        result = 31 * result + output.hashCode();
        return result;
    }

    /// Helper to create a [MapCodec] for a simple [BasicPressurizedReactionRecipe].
    ///
    /// @param factory Recipe constructor
    ///
    /// @since 10.8.0
    public static <RECIPE extends BasicPressurizedReactionRecipe> MapCodec<RECIPE> simpleMapCodec(PressurizedReactionRecipe.Factory<RECIPE> factory) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
              Recipe.CommonInfo.MAP_CODEC.forGetter(MekanismRecipe::commonInfo),
              ReactingBookInfo.MAP_CODEC.forGetter(MekanismRecipe::bookInfo),
              ItemStackIngredient.CODEC.fieldOf(SerializationConstants.ITEM_INPUT).forGetter(PressurizedReactionRecipe::getInputSolid),
              FluidStackIngredient.CODEC.fieldOf(SerializationConstants.FLUID_INPUT).forGetter(PressurizedReactionRecipe::getInputFluid),
              ChemicalStackIngredient.CODEC.fieldOf(SerializationConstants.CHEMICAL_INPUT).forGetter(PressurizedReactionRecipe::getInputChemical),
              ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf(SerializationConstants.ENERGY_REQUIRED, 0).forGetter(PressurizedReactionRecipe::getEnergyRequired),
              ExtraCodecs.POSITIVE_INT.fieldOf(SerializationConstants.DURATION).forGetter(PressurizedReactionRecipe::getDuration),
              PressurizedReactionRecipeOutput.CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(recipe -> recipe.output)
        ).apply(instance, factory::create));
    }

    /// Helper to create a [StreamCodec] for a simple [BasicPressurizedReactionRecipe].
    ///
    /// @param factory Recipe constructor
    ///
    /// @since 10.8.0
    public static <RECIPE extends BasicPressurizedReactionRecipe> StreamCodec<RegistryFriendlyByteBuf, RECIPE> simpleStreamCodec(PressurizedReactionRecipe.Factory<RECIPE> factory) {
        return StreamCodec.composite(
              CommonInfo.STREAM_CODEC, MekanismRecipe::commonInfo,
              ReactingBookInfo.STREAM_CODEC, MekanismRecipe::bookInfo,
              ItemStackIngredient.STREAM_CODEC, PressurizedReactionRecipe::getInputSolid,
              FluidStackIngredient.STREAM_CODEC, PressurizedReactionRecipe::getInputFluid,
              ChemicalStackIngredient.STREAM_CODEC, PressurizedReactionRecipe::getInputChemical,
              ByteBufCodecs.VAR_INT, PressurizedReactionRecipe::getEnergyRequired,
              ByteBufCodecs.VAR_INT, PressurizedReactionRecipe::getDuration,
              PressurizedReactionRecipeOutput.STREAM_CODEC, recipe -> recipe.output,
              factory::create
        );
    }
}