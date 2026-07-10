package mekanism.common.registries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.SerializationConstants;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.display.CombiningRecipeDisplay;
import mekanism.api.recipes.display.MixingRecipeDisplay;
import mekanism.api.recipes.display.NucleosynthesizingRecipeDisplay;
import mekanism.api.recipes.display.PerTickCombiningRecipeDisplay;
import mekanism.api.recipes.display.ReactionRecipeDisplay;
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
    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<PerTickCombiningRecipeDisplay>> PER_TICK_COMBINING = RECIPE_DISPLAY_TYPE.register("per_tick_combining", () -> new RecipeDisplay.Type<>(
          RecordCodecBuilder.mapCodec(i -> i.group(
                SlotDisplay.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(PerTickCombiningRecipeDisplay::input),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.EXTRA_INPUT).forGetter(PerTickCombiningRecipeDisplay::secondaryInput),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.RESULT).forGetter(RecipeDisplay::result),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.CRAFTING_STATION).forGetter(RecipeDisplay::craftingStation)
          ).apply(i, PerTickCombiningRecipeDisplay::new)),
          StreamCodec.composite(
                SlotDisplay.STREAM_CODEC, PerTickCombiningRecipeDisplay::input,
                SlotDisplay.STREAM_CODEC, PerTickCombiningRecipeDisplay::secondaryInput,
                SlotDisplay.STREAM_CODEC, RecipeDisplay::result,
                SlotDisplay.STREAM_CODEC, RecipeDisplay::craftingStation,
                PerTickCombiningRecipeDisplay::new
          )
    ));

    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<ReactionRecipeDisplay>> REACTION = RECIPE_DISPLAY_TYPE.register("reaction", () -> new RecipeDisplay.Type<>(
          RecordCodecBuilder.mapCodec(i -> i.group(
                SlotDisplay.CODEC.fieldOf(SerializationConstants.ITEM_INPUT).forGetter(ReactionRecipeDisplay::inputSolid),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.FLUID_INPUT).forGetter(ReactionRecipeDisplay::inputFluid),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.CHEMICAL_INPUT).forGetter(ReactionRecipeDisplay::inputChemical),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(ReactionRecipeDisplay::result),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.CRAFTING_STATION).forGetter(RecipeDisplay::craftingStation)
          ).apply(i, ReactionRecipeDisplay::new)),
          StreamCodec.composite(
                SlotDisplay.STREAM_CODEC, ReactionRecipeDisplay::inputSolid,
                SlotDisplay.STREAM_CODEC, ReactionRecipeDisplay::inputFluid,
                SlotDisplay.STREAM_CODEC, ReactionRecipeDisplay::inputChemical,
                SlotDisplay.STREAM_CODEC, ReactionRecipeDisplay::result,
                SlotDisplay.STREAM_CODEC, RecipeDisplay::craftingStation,
                ReactionRecipeDisplay::new
          )
    ));

    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<NucleosynthesizingRecipeDisplay>> NUCLEOSYNTHESIZING = RECIPE_DISPLAY_TYPE.register("nucleosynthesizing", () -> new RecipeDisplay.Type<>(
          RecordCodecBuilder.mapCodec(i -> i.group(
                SlotDisplay.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(NucleosynthesizingRecipeDisplay::input),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.EXTRA_INPUT).forGetter(NucleosynthesizingRecipeDisplay::secondaryInput),
                Codec.BOOL.optionalFieldOf(SerializationConstants.PER_TICK_USAGE, false).forGetter(NucleosynthesizingRecipeDisplay::perTickUsage),
                ExtraCodecs.POSITIVE_INT.fieldOf(SerializationConstants.DURATION).forGetter(NucleosynthesizingRecipeDisplay::duration),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.RESULT).forGetter(RecipeDisplay::result),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.CRAFTING_STATION).forGetter(RecipeDisplay::craftingStation)
          ).apply(i, NucleosynthesizingRecipeDisplay::new)),
          StreamCodec.composite(
                SlotDisplay.STREAM_CODEC, NucleosynthesizingRecipeDisplay::input,
                SlotDisplay.STREAM_CODEC, NucleosynthesizingRecipeDisplay::secondaryInput,
                ByteBufCodecs.BOOL, NucleosynthesizingRecipeDisplay::perTickUsage,
                ByteBufCodecs.VAR_INT, NucleosynthesizingRecipeDisplay::duration,
                SlotDisplay.STREAM_CODEC, RecipeDisplay::result,
                SlotDisplay.STREAM_CODEC, RecipeDisplay::craftingStation,
                NucleosynthesizingRecipeDisplay::new
          )
    ));
}