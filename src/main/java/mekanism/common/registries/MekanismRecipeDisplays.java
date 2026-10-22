package mekanism.common.registries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.SerializationConstants;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.display.recipe.ActivatingRecipeDisplay;
import mekanism.api.recipes.display.recipe.CentrifugingRecipeDisplay;
import mekanism.api.recipes.display.recipe.ChemicalConversionRecipeDisplay;
import mekanism.api.recipes.display.recipe.ChemicalInfusingRecipeDisplay;
import mekanism.api.recipes.display.recipe.CombiningRecipeDisplay;
import mekanism.api.recipes.display.recipe.CompressingRecipeDisplay;
import mekanism.api.recipes.display.recipe.CondensentratingRecipeDisplay;
import mekanism.api.recipes.display.recipe.CrushingRecipeDisplay;
import mekanism.api.recipes.display.recipe.CrystallizerRecipeDisplay;
import mekanism.api.recipes.display.recipe.DecondensentratingRecipeDisplay;
import mekanism.api.recipes.display.recipe.DissolutionRecipeDisplay;
import mekanism.api.recipes.display.recipe.ElectrolysisRecipeDisplay;
import mekanism.api.recipes.display.recipe.EnergyConversionRecipeDisplay;
import mekanism.api.recipes.display.recipe.EnrichingRecipeDisplay;
import mekanism.api.recipes.display.recipe.EvaporatingRecipeDisplay;
import mekanism.api.recipes.display.recipe.InjectingRecipeDisplay;
import mekanism.api.recipes.display.recipe.LiquificationRecipeDisplay;
import mekanism.api.recipes.display.recipe.MetallurgicInfusingRecipeDisplay;
import mekanism.api.recipes.display.recipe.NucleosynthesizingRecipeDisplay;
import mekanism.api.recipes.display.recipe.OxidizingRecipeDisplay;
import mekanism.api.recipes.display.recipe.PaintingRecipeDisplay;
import mekanism.api.recipes.display.recipe.PigmentExtractingRecipeDisplay;
import mekanism.api.recipes.display.recipe.PigmentMixingRecipeDisplay;
import mekanism.api.recipes.display.recipe.PurifyingRecipeDisplay;
import mekanism.api.recipes.display.recipe.ReactionRecipeDisplay;
import mekanism.api.recipes.display.recipe.SawingRecipeDisplay;
import mekanism.api.recipes.display.recipe.SmeltingRecipeDisplay;
import mekanism.api.recipes.display.recipe.WashingRecipeDisplay;
import mekanism.common.Mekanism;
import mekanism.common.registration.impl.RecipeDisplayDeferredRegister;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

public class MekanismRecipeDisplays extends MekanismRecipeSerializers {

    private MekanismRecipeDisplays() {
    }

    public static final RecipeDisplayDeferredRegister RECIPE_DISPLAY_TYPE = new RecipeDisplayDeferredRegister(Mekanism.MODID);

    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<CrushingRecipeDisplay>> CRUSHING = RECIPE_DISPLAY_TYPE.registerOneInput(MekanismRecipeTypes.NAME_CRUSHING, CrushingRecipeDisplay::new);
    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<EnrichingRecipeDisplay>> ENRICHING = RECIPE_DISPLAY_TYPE.registerOneInput(MekanismRecipeTypes.NAME_ENRICHING, EnrichingRecipeDisplay::new);
    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<SmeltingRecipeDisplay>> SMELTING = RECIPE_DISPLAY_TYPE.registerOneInput(MekanismRecipeTypes.NAME_SMELTING, SmeltingRecipeDisplay::new);
    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<ChemicalInfusingRecipeDisplay>> CHEMICAL_INFUSING = RECIPE_DISPLAY_TYPE.registerTwoInput(MekanismRecipeTypes.NAME_CHEMICAL_INFUSING, ChemicalInfusingRecipeDisplay::new);
    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<CombiningRecipeDisplay>> COMBINING = RECIPE_DISPLAY_TYPE.registerTwoInput(MekanismRecipeTypes.NAME_COMBINING, CombiningRecipeDisplay::new);
    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<ElectrolysisRecipeDisplay>> SEPARATING = RECIPE_DISPLAY_TYPE.register(MekanismRecipeTypes.NAME_SEPARATING,
          () -> RecordCodecBuilder.mapCodec(i -> i.group(
                SlotDisplay.CODEC.fieldOf(SerializationConstants.INGREDIENT).forGetter(ElectrolysisRecipeDisplay::ingredient),
                SlotDisplay.CODEC.listOf(2, 2).xmap(SlotDisplay.Composite::new, SlotDisplay.Composite::contents).fieldOf(SerializationConstants.RESULT).forGetter(ElectrolysisRecipeDisplay::result),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.CRAFTING_STATION).forGetter(RecipeDisplay::craftingStation),
                ExtraCodecs.POSITIVE_INT.optionalFieldOf(SerializationConstants.ENERGY_MULTIPLIER, 1).forGetter(ElectrolysisRecipeDisplay::energyMultiplier)
          ).apply(i, ElectrolysisRecipeDisplay::new)), () -> StreamCodec.composite(
                SlotDisplay.STREAM_CODEC, ElectrolysisRecipeDisplay::ingredient,
                SlotDisplay.STREAM_CODEC, ElectrolysisRecipeDisplay::leftResult,
                SlotDisplay.STREAM_CODEC, ElectrolysisRecipeDisplay::rightResult,
                SlotDisplay.STREAM_CODEC, RecipeDisplay::craftingStation,
                ByteBufCodecs.VAR_INT, ElectrolysisRecipeDisplay::energyMultiplier,
                ElectrolysisRecipeDisplay::new
          )
    );
    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<WashingRecipeDisplay>> WASHING = RECIPE_DISPLAY_TYPE.registerTwoInput(MekanismRecipeTypes.NAME_WASHING, WashingRecipeDisplay::new);
    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<EvaporatingRecipeDisplay>> EVAPORATING = RECIPE_DISPLAY_TYPE.registerOneInput(MekanismRecipeTypes.NAME_EVAPORATING, EvaporatingRecipeDisplay::new);
    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<ActivatingRecipeDisplay>> ACTIVATING = RECIPE_DISPLAY_TYPE.registerOneInput(MekanismRecipeTypes.NAME_ACTIVATING, ActivatingRecipeDisplay::new);
    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<CentrifugingRecipeDisplay>> CENTRIFUGING = RECIPE_DISPLAY_TYPE.registerOneInput(MekanismRecipeTypes.NAME_CENTRIFUGING, CentrifugingRecipeDisplay::new);
    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<CrystallizerRecipeDisplay>> CRYSTALLIZING = RECIPE_DISPLAY_TYPE.register(MekanismRecipeTypes.NAME_CRYSTALLIZING,
          () -> RecordCodecBuilder.mapCodec(i -> i.group(
                SlotDisplay.CODEC.fieldOf(SerializationConstants.INGREDIENT).forGetter(CrystallizerRecipeDisplay::ingredient),
                SlotDisplay.CODEC.optionalFieldOf(SerializationConstants.TYPE_DISPLAY, SlotDisplay.Empty.INSTANCE).forGetter(CrystallizerRecipeDisplay::typeDisplay),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.RESULT).forGetter(RecipeDisplay::result),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.CRAFTING_STATION).forGetter(RecipeDisplay::craftingStation)
          ).apply(i, CrystallizerRecipeDisplay::new)), () -> StreamCodec.composite(
                SlotDisplay.STREAM_CODEC, CrystallizerRecipeDisplay::ingredient,
                SlotDisplay.STREAM_CODEC, CrystallizerRecipeDisplay::typeDisplay,
                SlotDisplay.STREAM_CODEC, RecipeDisplay::result,
                SlotDisplay.STREAM_CODEC, RecipeDisplay::craftingStation,
                CrystallizerRecipeDisplay::new
          )
    );
    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<DissolutionRecipeDisplay>> DISSOLUTION = RECIPE_DISPLAY_TYPE.registerTwoInput(MekanismRecipeTypes.NAME_DISSOLUTION, DissolutionRecipeDisplay::new);
    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<CompressingRecipeDisplay>> COMPRESSING = RECIPE_DISPLAY_TYPE.registerTwoPerTickInput(MekanismRecipeTypes.NAME_COMPRESSING, true, CompressingRecipeDisplay::new);
    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<PurifyingRecipeDisplay>> PURIFYING = RECIPE_DISPLAY_TYPE.registerTwoPerTickInput(MekanismRecipeTypes.NAME_PURIFYING, true, PurifyingRecipeDisplay::new);
    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<InjectingRecipeDisplay>> INJECTING = RECIPE_DISPLAY_TYPE.registerTwoPerTickInput(MekanismRecipeTypes.NAME_INJECTING, true, InjectingRecipeDisplay::new);
    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<NucleosynthesizingRecipeDisplay>> NUCLEOSYNTHESIZING = RECIPE_DISPLAY_TYPE.register(MekanismRecipeTypes.NAME_NUCLEOSYNTHESIZING,
          () -> RecordCodecBuilder.mapCodec(i -> i.group(
                SlotDisplay.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(NucleosynthesizingRecipeDisplay::ingredient),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.EXTRA_INPUT).forGetter(NucleosynthesizingRecipeDisplay::secondIngredient),
                Codec.BOOL.optionalFieldOf(SerializationConstants.PER_TICK_USAGE, false).forGetter(NucleosynthesizingRecipeDisplay::perTickSecondary),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.RESULT).forGetter(RecipeDisplay::result),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.CRAFTING_STATION).forGetter(RecipeDisplay::craftingStation),
                ExtraCodecs.POSITIVE_INT.fieldOf(SerializationConstants.DURATION).forGetter(NucleosynthesizingRecipeDisplay::duration)
          ).apply(i, NucleosynthesizingRecipeDisplay::new)), () -> StreamCodec.composite(
                SlotDisplay.STREAM_CODEC, NucleosynthesizingRecipeDisplay::ingredient,
                SlotDisplay.STREAM_CODEC, NucleosynthesizingRecipeDisplay::secondIngredient,
                ByteBufCodecs.BOOL, NucleosynthesizingRecipeDisplay::perTickSecondary,
                SlotDisplay.STREAM_CODEC, RecipeDisplay::result,
                SlotDisplay.STREAM_CODEC, RecipeDisplay::craftingStation,
                ByteBufCodecs.VAR_INT, NucleosynthesizingRecipeDisplay::duration,
                NucleosynthesizingRecipeDisplay::new
          )
    );
    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<EnergyConversionRecipeDisplay>> ENERGY_CONVERSION = RECIPE_DISPLAY_TYPE.registerOneInput(MekanismRecipeTypes.NAME_ENERGY_CONVERSION, EnergyConversionRecipeDisplay::new);
    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<ChemicalConversionRecipeDisplay>> CHEMICAL_CONVERSION = RECIPE_DISPLAY_TYPE.registerOneInput(MekanismRecipeTypes.NAME_CHEMICAL_CONVERSION, ChemicalConversionRecipeDisplay::new);
    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<OxidizingRecipeDisplay>> OXIDIZING = RECIPE_DISPLAY_TYPE.registerOneInput(MekanismRecipeTypes.NAME_OXIDIZING, OxidizingRecipeDisplay::new);
    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<PigmentExtractingRecipeDisplay>> PIGMENT_EXTRACTING = RECIPE_DISPLAY_TYPE.registerOneInput(MekanismRecipeTypes.NAME_PIGMENT_EXTRACTING, PigmentExtractingRecipeDisplay::new);
    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<PigmentMixingRecipeDisplay>> PIGMENT_MIXING = RECIPE_DISPLAY_TYPE.registerTwoInput(MekanismRecipeTypes.NAME_PIGMENT_MIXING, PigmentMixingRecipeDisplay::new);
    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<PaintingRecipeDisplay>> PAINTING = RECIPE_DISPLAY_TYPE.registerTwoPerTickInput(MekanismRecipeTypes.NAME_PAINTING, false, PaintingRecipeDisplay::new);
    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<MetallurgicInfusingRecipeDisplay>> METALLURGIC_INFUSING = RECIPE_DISPLAY_TYPE.registerTwoPerTickInput(MekanismRecipeTypes.NAME_METALLURGIC_INFUSING, false, MetallurgicInfusingRecipeDisplay::new);
    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<ReactionRecipeDisplay>> REACTION = RECIPE_DISPLAY_TYPE.register(MekanismRecipeTypes.NAME_REACTION,
          () -> RecordCodecBuilder.mapCodec(i -> i.group(
                SlotDisplay.CODEC.fieldOf(SerializationConstants.ITEM_INPUT).forGetter(ReactionRecipeDisplay::itemIngredient),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.FLUID_INPUT).forGetter(ReactionRecipeDisplay::fluidIngredient),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.CHEMICAL_INPUT).forGetter(ReactionRecipeDisplay::chemicalIngredient),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(ReactionRecipeDisplay::result),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.CRAFTING_STATION).forGetter(RecipeDisplay::craftingStation),
                ExtraCodecs.POSITIVE_INT.fieldOf(SerializationConstants.DURATION).forGetter(ReactionRecipeDisplay::duration),
                ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf(SerializationConstants.ENERGY_REQUIRED, 0).forGetter(ReactionRecipeDisplay::energyRequired)
          ).apply(i, ReactionRecipeDisplay::new)), () -> StreamCodec.composite(
                SlotDisplay.STREAM_CODEC, ReactionRecipeDisplay::itemIngredient,
                SlotDisplay.STREAM_CODEC, ReactionRecipeDisplay::fluidIngredient,
                SlotDisplay.STREAM_CODEC, ReactionRecipeDisplay::chemicalIngredient,
                SlotDisplay.STREAM_CODEC, ReactionRecipeDisplay::result,
                SlotDisplay.STREAM_CODEC, RecipeDisplay::craftingStation,
                ByteBufCodecs.VAR_INT, ReactionRecipeDisplay::duration,
                ByteBufCodecs.VAR_INT, ReactionRecipeDisplay::energyRequired,
                ReactionRecipeDisplay::new
          )
    );
    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<CondensentratingRecipeDisplay>> CONDENSENTRATING = RECIPE_DISPLAY_TYPE.registerOneInput(MekanismRecipeTypes.NAME_ROTARY.withSuffix("_condensentrating"), CondensentratingRecipeDisplay::new);
    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<DecondensentratingRecipeDisplay>> DECONDENSENTRATING = RECIPE_DISPLAY_TYPE.registerOneInput(MekanismRecipeTypes.NAME_ROTARY.withSuffix("_decondensentrating"), DecondensentratingRecipeDisplay::new);
    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<SawingRecipeDisplay>> SAWING = RECIPE_DISPLAY_TYPE.registerOneInput(MekanismRecipeTypes.NAME_SAWING, SawingRecipeDisplay::new);
    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<LiquificationRecipeDisplay>> LIQUIFICATION = RECIPE_DISPLAY_TYPE.registerOneInput(MekanismRecipeTypes.NAME_LIQUIFICATION, LiquificationRecipeDisplay::new);
}