package mekanism.common.registries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.SerializationConstants;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.display.CombiningRecipeDisplay;
import mekanism.api.recipes.display.MultiOutputRecipeDisplay;
import mekanism.api.recipes.display.MixingRecipeDisplay;
import mekanism.api.recipes.display.NucleosynthesizingRecipeDisplay;
import mekanism.api.recipes.display.RateBasedCombiningRecipeDisplay;
import mekanism.api.recipes.display.ReactionRecipeDisplay;
import mekanism.api.recipes.display.SawingRecipeDisplay;
import mekanism.api.recipes.display.SimpleMachineRecipeDisplay;
import mekanism.common.Mekanism;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MekanismRecipeDisplays extends MekanismRecipeSerializers {

    private MekanismRecipeDisplays() {
    }

    public static final DeferredRegister<RecipeDisplay.Type<?>> RECIPE_DISPLAY_TYPE = DeferredRegister.create(Registries.RECIPE_DISPLAY, Mekanism.MODID);

    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<SimpleMachineRecipeDisplay>> SIMPLE_MACHINE = RECIPE_DISPLAY_TYPE.register("simple_machine", () -> new RecipeDisplay.Type<>(
          RecordCodecBuilder.mapCodec(i -> i.group(
                SlotDisplay.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(SimpleMachineRecipeDisplay::input),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.RESULT).forGetter(RecipeDisplay::result),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.CRAFTING_STATION).forGetter(RecipeDisplay::craftingStation)
          ).apply(i, SimpleMachineRecipeDisplay::new)),
          StreamCodec.composite(
                SlotDisplay.STREAM_CODEC, SimpleMachineRecipeDisplay::input,
                SlotDisplay.STREAM_CODEC, RecipeDisplay::result,
                SlotDisplay.STREAM_CODEC, RecipeDisplay::craftingStation,
                SimpleMachineRecipeDisplay::new
          )
    ));
    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<MixingRecipeDisplay>> MIXING = RECIPE_DISPLAY_TYPE.register("mixing", () -> new RecipeDisplay.Type<>(
          RecordCodecBuilder.mapCodec(i -> i.group(
                SlotDisplay.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(MixingRecipeDisplay::leftInput),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.EXTRA_INPUT).forGetter(MixingRecipeDisplay::rightInput),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.RESULT).forGetter(RecipeDisplay::result),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.CRAFTING_STATION).forGetter(RecipeDisplay::craftingStation)
          ).apply(i, MixingRecipeDisplay::new)),
          StreamCodec.composite(
                SlotDisplay.STREAM_CODEC, MixingRecipeDisplay::leftInput,
                SlotDisplay.STREAM_CODEC, MixingRecipeDisplay::rightInput,
                SlotDisplay.STREAM_CODEC, RecipeDisplay::result,
                SlotDisplay.STREAM_CODEC, RecipeDisplay::craftingStation,
                MixingRecipeDisplay::new
          )
    ));
    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<CombiningRecipeDisplay>> COMBINING = RECIPE_DISPLAY_TYPE.register("combining", () -> new RecipeDisplay.Type<>(
          RecordCodecBuilder.mapCodec(i -> i.group(
                SlotDisplay.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(CombiningRecipeDisplay::input),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.EXTRA_INPUT).forGetter(CombiningRecipeDisplay::secondaryInput),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.RESULT).forGetter(RecipeDisplay::result),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.CRAFTING_STATION).forGetter(RecipeDisplay::craftingStation)
          ).apply(i, CombiningRecipeDisplay::new)),
          StreamCodec.composite(
                SlotDisplay.STREAM_CODEC, CombiningRecipeDisplay::input,
                SlotDisplay.STREAM_CODEC, CombiningRecipeDisplay::secondaryInput,
                SlotDisplay.STREAM_CODEC, RecipeDisplay::result,
                SlotDisplay.STREAM_CODEC, RecipeDisplay::craftingStation,
                CombiningRecipeDisplay::new
          )
    ));
    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<RateBasedCombiningRecipeDisplay>> RATE_BASED_COMBINING = RECIPE_DISPLAY_TYPE.register("rate_based_combining", () -> new RecipeDisplay.Type<>(
          RecordCodecBuilder.mapCodec(i -> i.group(
                SlotDisplay.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(RateBasedCombiningRecipeDisplay::input),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.EXTRA_INPUT).forGetter(RateBasedCombiningRecipeDisplay::secondaryInput),
                Codec.BOOL.optionalFieldOf(SerializationConstants.PER_TICK_USAGE, false).forGetter(RateBasedCombiningRecipeDisplay::perTickUsage),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.RESULT).forGetter(RecipeDisplay::result),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.CRAFTING_STATION).forGetter(RecipeDisplay::craftingStation)
          ).apply(i, RateBasedCombiningRecipeDisplay::new)),
          StreamCodec.composite(
                SlotDisplay.STREAM_CODEC, RateBasedCombiningRecipeDisplay::input,
                SlotDisplay.STREAM_CODEC, RateBasedCombiningRecipeDisplay::secondaryInput,
                ByteBufCodecs.BOOL, RateBasedCombiningRecipeDisplay::perTickUsage,
                SlotDisplay.STREAM_CODEC, RecipeDisplay::result,
                SlotDisplay.STREAM_CODEC, RecipeDisplay::craftingStation,
                RateBasedCombiningRecipeDisplay::new
          )
    ));

    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<MultiOutputRecipeDisplay>> MULTI_OUTPUT = RECIPE_DISPLAY_TYPE.register("multi_output", () -> new RecipeDisplay.Type<>(
          RecordCodecBuilder.mapCodec(i -> i.group(
                SlotDisplay.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(MultiOutputRecipeDisplay::input),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(MultiOutputRecipeDisplay::output),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.SECONDARY_OUTPUT).forGetter(MultiOutputRecipeDisplay::secondaryOutput),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.CRAFTING_STATION).forGetter(RecipeDisplay::craftingStation)
          ).apply(i, MultiOutputRecipeDisplay::new)),
          StreamCodec.composite(
                SlotDisplay.STREAM_CODEC, MultiOutputRecipeDisplay::input,
                SlotDisplay.STREAM_CODEC, MultiOutputRecipeDisplay::output,
                SlotDisplay.STREAM_CODEC, MultiOutputRecipeDisplay::secondaryOutput,
                SlotDisplay.STREAM_CODEC, RecipeDisplay::craftingStation,
                MultiOutputRecipeDisplay::new
          )
    ));

    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<ReactionRecipeDisplay>> REACTION = RECIPE_DISPLAY_TYPE.register("reaction", () -> new RecipeDisplay.Type<>(
          RecordCodecBuilder.mapCodec(i -> i.group(
                SlotDisplay.CODEC.fieldOf(SerializationConstants.ITEM_INPUT).forGetter(ReactionRecipeDisplay::inputSolid),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.FLUID_INPUT).forGetter(ReactionRecipeDisplay::inputFluid),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.CHEMICAL_INPUT).forGetter(ReactionRecipeDisplay::inputChemical),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.ITEM_OUTPUT).forGetter(ReactionRecipeDisplay::itemOutput),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.CHEMICAL_OUTPUT).forGetter(ReactionRecipeDisplay::chemicalOutput),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.CRAFTING_STATION).forGetter(RecipeDisplay::craftingStation)
          ).apply(i, ReactionRecipeDisplay::new)),
          StreamCodec.composite(
                SlotDisplay.STREAM_CODEC, ReactionRecipeDisplay::inputSolid,
                SlotDisplay.STREAM_CODEC, ReactionRecipeDisplay::inputFluid,
                SlotDisplay.STREAM_CODEC, ReactionRecipeDisplay::inputChemical,
                SlotDisplay.STREAM_CODEC, ReactionRecipeDisplay::itemOutput,
                SlotDisplay.STREAM_CODEC, ReactionRecipeDisplay::chemicalOutput,
                SlotDisplay.STREAM_CODEC, RecipeDisplay::craftingStation,
                ReactionRecipeDisplay::new
          )
    ));

    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<SawingRecipeDisplay>> SAWING = RECIPE_DISPLAY_TYPE.register("sawing", () -> new RecipeDisplay.Type<>(
          //TODO - 26.2: Do we want to make this like SawmillRecipeSerializer? At the very least we probably want to validate the double's value
          RecordCodecBuilder.mapCodec(i -> i.group(
                SlotDisplay.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(SawingRecipeDisplay::input),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.MAIN_OUTPUT).forGetter(SawingRecipeDisplay::mainOutput),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.SECONDARY_OUTPUT).forGetter(SawingRecipeDisplay::secondaryOutput),
                Codec.DOUBLE.fieldOf(SerializationConstants.CHANCE).forGetter(SawingRecipeDisplay::secondaryChance),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.CRAFTING_STATION).forGetter(RecipeDisplay::craftingStation)
          ).apply(i, SawingRecipeDisplay::new)),
          StreamCodec.composite(
                SlotDisplay.STREAM_CODEC, SawingRecipeDisplay::input,
                SlotDisplay.STREAM_CODEC, SawingRecipeDisplay::mainOutput,
                SlotDisplay.STREAM_CODEC, SawingRecipeDisplay::secondaryOutput,
                ByteBufCodecs.DOUBLE, SawingRecipeDisplay::secondaryChance,
                SlotDisplay.STREAM_CODEC, RecipeDisplay::craftingStation,
                SawingRecipeDisplay::new
          )
    ));

    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<NucleosynthesizingRecipeDisplay>> NUCLEOSYNTHESIZING = RECIPE_DISPLAY_TYPE.register("nucleosynthesizing", () -> new RecipeDisplay.Type<>(
          RecordCodecBuilder.mapCodec(i -> i.group(
                SlotDisplay.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(NucleosynthesizingRecipeDisplay::input),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.EXTRA_INPUT).forGetter(NucleosynthesizingRecipeDisplay::secondaryInput),
                ExtraCodecs.POSITIVE_INT.fieldOf(SerializationConstants.DURATION).forGetter(NucleosynthesizingRecipeDisplay::duration),
                Codec.BOOL.optionalFieldOf(SerializationConstants.PER_TICK_USAGE, false).forGetter(NucleosynthesizingRecipeDisplay::perTickUsage),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.RESULT).forGetter(RecipeDisplay::result),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.CRAFTING_STATION).forGetter(RecipeDisplay::craftingStation)
          ).apply(i, NucleosynthesizingRecipeDisplay::new)),
          StreamCodec.composite(
                SlotDisplay.STREAM_CODEC, NucleosynthesizingRecipeDisplay::input,
                SlotDisplay.STREAM_CODEC, NucleosynthesizingRecipeDisplay::secondaryInput,
                ByteBufCodecs.VAR_INT, NucleosynthesizingRecipeDisplay::duration,
                ByteBufCodecs.BOOL, NucleosynthesizingRecipeDisplay::perTickUsage,
                SlotDisplay.STREAM_CODEC, RecipeDisplay::result,
                SlotDisplay.STREAM_CODEC, RecipeDisplay::craftingStation,
                NucleosynthesizingRecipeDisplay::new
          )
    ));
}