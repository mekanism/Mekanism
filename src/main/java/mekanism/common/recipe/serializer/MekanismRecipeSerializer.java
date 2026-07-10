package mekanism.common.recipe.serializer;

import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Function5;
import com.mojang.datafixers.util.Function7;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.BiFunction;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.ChemicalToChemicalRecipe;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.FluidChemicalToChemicalRecipe;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.ItemStackToChemicalRecipe;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.api.recipes.ItemStackToFluidOptionalItemRecipe.FluidOptionalItemOutput;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.basic.BasicChemicalCrystallizerRecipe;
import mekanism.api.recipes.basic.BasicChemicalDissolutionRecipe;
import mekanism.api.recipes.basic.BasicChemicalToChemicalRecipe;
import mekanism.api.recipes.basic.BasicCombinerRecipe;
import mekanism.api.recipes.basic.BasicElectrolysisRecipe;
import mekanism.api.recipes.basic.BasicFluidToFluidRecipe;
import mekanism.api.recipes.basic.BasicItemStackToEnergyRecipe;
import mekanism.api.recipes.basic.BasicItemStackToFluidOptionalItemRecipe;
import mekanism.api.recipes.basic.BasicItemStackToItemStackRecipe;
import mekanism.api.recipes.basic.BasicNucleosynthesizingRecipe;
import mekanism.api.recipes.basic.BasicPressurizedReactionRecipe;
import mekanism.api.recipes.basic.BasicWashingRecipe;
import mekanism.api.recipes.basic.IBasicChemicalOutput;
import mekanism.api.recipes.basic.IBasicItemStackOutput;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStackTemplate;

public class MekanismRecipeSerializer {

    public static <RECIPE extends Recipe<?>> RecipeSerializer<RECIPE> singleton(RECIPE instance) {
        return new RecipeSerializer<>(MapCodec.unit(instance), StreamCodec.unit(instance));
    }

    public static <RECIPE extends BasicItemStackToItemStackRecipe> RecipeSerializer<RECIPE> itemToItem(BiFunction<ItemStackIngredient, ItemStackTemplate, RECIPE> factory) {
        return new RecipeSerializer<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
              ItemStackIngredient.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(BasicItemStackToItemStackRecipe::getInput),
              ItemStackTemplate.CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(BasicItemStackToItemStackRecipe::getOutputRaw)
        ).apply(instance, factory)), StreamCodec.composite(
              ItemStackIngredient.STREAM_CODEC, BasicItemStackToItemStackRecipe::getInput,
              ItemStackTemplate.STREAM_CODEC, BasicItemStackToItemStackRecipe::getOutputRaw,
              factory
        ));
    }

    public static RecipeSerializer<BasicChemicalCrystallizerRecipe> crystallizing(BiFunction<ChemicalStackIngredient, ItemStackTemplate, BasicChemicalCrystallizerRecipe> factory) {
        return new RecipeSerializer<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
              IngredientCreatorAccess.chemicalStack().codec().fieldOf(SerializationConstants.INPUT).forGetter(BasicChemicalCrystallizerRecipe::getInput),
              ItemStackTemplate.CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(BasicChemicalCrystallizerRecipe::getOutputRaw)
        ).apply(instance, factory)), StreamCodec.composite(
              IngredientCreatorAccess.chemicalStack().streamCodec(), BasicChemicalCrystallizerRecipe::getInput,
              ItemStackTemplate.STREAM_CODEC, BasicChemicalCrystallizerRecipe::getOutputRaw,
              factory
        ));
    }

    public static RecipeSerializer<BasicCombinerRecipe> combining(Function3<ItemStackIngredient, ItemStackIngredient, ItemStackTemplate, BasicCombinerRecipe> factory) {
        return new RecipeSerializer<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
              ItemStackIngredient.CODEC.fieldOf(SerializationConstants.MAIN_INPUT).forGetter(CombinerRecipe::getMainInput),
              ItemStackIngredient.CODEC.fieldOf(SerializationConstants.EXTRA_INPUT).forGetter(CombinerRecipe::getExtraInput),
              ItemStackTemplate.CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(BasicCombinerRecipe::getOutputRaw)
        ).apply(instance, factory)), StreamCodec.composite(
              ItemStackIngredient.STREAM_CODEC, BasicCombinerRecipe::getMainInput,
              ItemStackIngredient.STREAM_CODEC, BasicCombinerRecipe::getExtraInput,
              ItemStackTemplate.STREAM_CODEC, BasicCombinerRecipe::getOutputRaw,
              factory
        ));
    }

    public static <RECIPE extends BasicItemStackToEnergyRecipe> RecipeSerializer<RECIPE> itemToEnergy(BiFunction<ItemStackIngredient, Integer, RECIPE> factory) {
        return new RecipeSerializer<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
              ItemStackIngredient.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(ItemStackToEnergyRecipe::getInput),
              ExtraCodecs.POSITIVE_INT.fieldOf(SerializationConstants.OUTPUT).forGetter(BasicItemStackToEnergyRecipe::getOutputRaw)
        ).apply(instance, factory)), StreamCodec.composite(
              ItemStackIngredient.STREAM_CODEC, ItemStackToEnergyRecipe::getInput,
              ByteBufCodecs.VAR_INT, BasicItemStackToEnergyRecipe::getOutputRaw,
              factory
        ));
    }

    public static <RECIPE extends BasicFluidToFluidRecipe> RecipeSerializer<RECIPE> fluidToFluid(BiFunction<FluidStackIngredient, FluidStackTemplate, RECIPE> factory) {
        return new RecipeSerializer<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
              FluidStackIngredient.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(FluidToFluidRecipe::getInput),
              FluidStackTemplate.CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(BasicFluidToFluidRecipe::getOutputRaw)
        ).apply(instance, factory)), StreamCodec.composite(
              FluidStackIngredient.STREAM_CODEC, FluidToFluidRecipe::getInput,
              FluidStackTemplate.STREAM_CODEC, BasicFluidToFluidRecipe::getOutputRaw,
              factory
        ));
    }

    public static <RECIPE extends BasicChemicalToChemicalRecipe> RecipeSerializer<RECIPE> chemicalToChemical(BiFunction<ChemicalStackIngredient, ChemicalStackTemplate, RECIPE> factory) {
        return new RecipeSerializer<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
              IngredientCreatorAccess.chemicalStack().codec().fieldOf(SerializationConstants.INPUT).forGetter(ChemicalToChemicalRecipe::getInput),
              ChemicalStackTemplate.MAP_CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(BasicChemicalToChemicalRecipe::getOutputRaw)
        ).apply(instance, factory)), StreamCodec.composite(
              IngredientCreatorAccess.chemicalStack().streamCodec(), ChemicalToChemicalRecipe::getInput,
              ChemicalStackTemplate.STREAM_CODEC, BasicChemicalToChemicalRecipe::getOutputRaw,
              factory
        ));
    }

    public static RecipeSerializer<BasicWashingRecipe> fluidChemicalToChemical(Function3<FluidStackIngredient, ChemicalStackIngredient, ChemicalStackTemplate, BasicWashingRecipe> factory) {
        return new RecipeSerializer<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
              FluidStackIngredient.CODEC.fieldOf(SerializationConstants.FLUID_INPUT).forGetter(FluidChemicalToChemicalRecipe::getFluidInput),
              IngredientCreatorAccess.chemicalStack().codec().fieldOf(SerializationConstants.CHEMICAL_INPUT).forGetter(FluidChemicalToChemicalRecipe::getChemicalInput),
              ChemicalStackTemplate.CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(BasicWashingRecipe::getOutputRaw)
        ).apply(instance, factory)), StreamCodec.composite(
              FluidStackIngredient.STREAM_CODEC, FluidChemicalToChemicalRecipe::getFluidInput,
              IngredientCreatorAccess.chemicalStack().streamCodec(), FluidChemicalToChemicalRecipe::getChemicalInput,
              ChemicalStackTemplate.STREAM_CODEC, BasicWashingRecipe::getOutputRaw,
              factory
        ));
    }

    public static RecipeSerializer<BasicNucleosynthesizingRecipe> nucleosynthesizing(
          Function5<ItemStackIngredient, ChemicalStackIngredient, ItemStackTemplate, Integer, Boolean, BasicNucleosynthesizingRecipe> factory) {
        return new RecipeSerializer<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
              ItemStackIngredient.CODEC.fieldOf(SerializationConstants.ITEM_INPUT).forGetter(NucleosynthesizingRecipe::getItemInput),
              IngredientCreatorAccess.chemicalStack().codec().fieldOf(SerializationConstants.CHEMICAL_INPUT).forGetter(NucleosynthesizingRecipe::getChemicalInput),
              ItemStackTemplate.CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(BasicNucleosynthesizingRecipe::getOutputRaw),
              ExtraCodecs.POSITIVE_INT.fieldOf(SerializationConstants.DURATION).forGetter(NucleosynthesizingRecipe::getDuration),
              Codec.BOOL.fieldOf(SerializationConstants.PER_TICK_USAGE).forGetter(NucleosynthesizingRecipe::perTickUsage)
        ).apply(instance, factory)), StreamCodec.composite(
              ItemStackIngredient.STREAM_CODEC, NucleosynthesizingRecipe::getItemInput,
              IngredientCreatorAccess.chemicalStack().streamCodec(), NucleosynthesizingRecipe::getChemicalInput,
              ItemStackTemplate.STREAM_CODEC, BasicNucleosynthesizingRecipe::getOutputRaw,
              ByteBufCodecs.VAR_INT, NucleosynthesizingRecipe::getDuration,
              ByteBufCodecs.BOOL, NucleosynthesizingRecipe::perTickUsage,
              factory
        ));
    }

    public static RecipeSerializer<BasicItemStackToFluidOptionalItemRecipe> itemToFluidOptionalItem(BiFunction<ItemStackIngredient, FluidOptionalItemOutput, BasicItemStackToFluidOptionalItemRecipe> factory) {
        return new RecipeSerializer<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
              ItemStackIngredient.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(BasicItemStackToFluidOptionalItemRecipe::getInput),
              FluidOptionalItemOutput.CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(BasicItemStackToFluidOptionalItemRecipe::getOutputRaw)
        ).apply(instance, factory)), StreamCodec.composite(
              ItemStackIngredient.STREAM_CODEC, BasicItemStackToFluidOptionalItemRecipe::getInput,
              FluidOptionalItemOutput.STREAM_CODEC, BasicItemStackToFluidOptionalItemRecipe::getOutputRaw,
              factory
        ));
    }

    public static RecipeSerializer<BasicElectrolysisRecipe> separating(Function4<FluidStackIngredient, Integer, ChemicalStackTemplate, ChemicalStackTemplate, BasicElectrolysisRecipe> factory) {
        return new RecipeSerializer<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
              FluidStackIngredient.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(ElectrolysisRecipe::getInput),
              ExtraCodecs.POSITIVE_INT.optionalFieldOf(SerializationConstants.ENERGY_MULTIPLIER, 1).forGetter(ElectrolysisRecipe::getEnergyMultiplier),
              ChemicalStackTemplate.MAP_CODEC.fieldOf(SerializationConstants.LEFT_CHEMICAL_OUTPUT).forGetter(BasicElectrolysisRecipe::getLeftChemicalOutput),
              ChemicalStackTemplate.MAP_CODEC.fieldOf(SerializationConstants.RIGHT_CHEMICAL_OUTPUT).forGetter(BasicElectrolysisRecipe::getRightChemicalOutput)
        ).apply(instance, factory)), StreamCodec.composite(
              FluidStackIngredient.STREAM_CODEC, ElectrolysisRecipe::getInput,
              ByteBufCodecs.VAR_INT, ElectrolysisRecipe::getEnergyMultiplier,
              ChemicalStackTemplate.STREAM_CODEC, BasicElectrolysisRecipe::getLeftChemicalOutput,
              ChemicalStackTemplate.STREAM_CODEC, BasicElectrolysisRecipe::getRightChemicalOutput,
              factory
        ));
    }

    public static RecipeSerializer<BasicChemicalDissolutionRecipe> dissolution(
          Function4<ItemStackIngredient, ChemicalStackIngredient, ChemicalStackTemplate, Boolean, BasicChemicalDissolutionRecipe> factory) {
        return new RecipeSerializer<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
              ItemStackIngredient.CODEC.fieldOf(SerializationConstants.ITEM_INPUT).forGetter(ChemicalDissolutionRecipe::getItemInput),
              IngredientCreatorAccess.chemicalStack().codec().fieldOf(SerializationConstants.CHEMICAL_INPUT).forGetter(ChemicalDissolutionRecipe::getChemicalInput),
              ChemicalStackTemplate.CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(BasicChemicalDissolutionRecipe::getOutputRaw),
              Codec.BOOL.fieldOf(SerializationConstants.PER_TICK_USAGE).forGetter(BasicChemicalDissolutionRecipe::perTickUsage)
        ).apply(instance, factory)), StreamCodec.composite(
              ItemStackIngredient.STREAM_CODEC, BasicChemicalDissolutionRecipe::getItemInput,
              IngredientCreatorAccess.chemicalStack().streamCodec(), BasicChemicalDissolutionRecipe::getChemicalInput,
              ChemicalStackTemplate.STREAM_CODEC, BasicChemicalDissolutionRecipe::getOutputRaw,
              ByteBufCodecs.BOOL, BasicChemicalDissolutionRecipe::perTickUsage,
              factory
        ));
    }

    public static RecipeSerializer<BasicPressurizedReactionRecipe> reaction(
          Function7<ItemStackIngredient, FluidStackIngredient, ChemicalStackIngredient, Integer, Integer, Optional<ItemStackTemplate>, Optional<ChemicalStackTemplate>, BasicPressurizedReactionRecipe> factory) {
        return new RecipeSerializer<>(RecordCodecBuilder.<BasicPressurizedReactionRecipe>mapCodec(instance -> instance.group(
              ItemStackIngredient.CODEC.fieldOf(SerializationConstants.ITEM_INPUT).forGetter(PressurizedReactionRecipe::getInputSolid),
              FluidStackIngredient.CODEC.fieldOf(SerializationConstants.FLUID_INPUT).forGetter(PressurizedReactionRecipe::getInputFluid),
              IngredientCreatorAccess.chemicalStack().codec().fieldOf(SerializationConstants.CHEMICAL_INPUT).forGetter(PressurizedReactionRecipe::getInputChemical),
              ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf(SerializationConstants.ENERGY_REQUIRED, 0).forGetter(PressurizedReactionRecipe::getEnergyRequired),
              ExtraCodecs.POSITIVE_INT.fieldOf(SerializationConstants.DURATION).forGetter(PressurizedReactionRecipe::getDuration),
              ItemStackTemplate.CODEC.optionalFieldOf(SerializationConstants.ITEM_OUTPUT).forGetter(BasicPressurizedReactionRecipe::getOutputItemOptional),
              ChemicalStackTemplate.CODEC.optionalFieldOf(SerializationConstants.CHEMICAL_OUTPUT).forGetter(BasicPressurizedReactionRecipe::getOutputChemicalOptional)
        ).apply(instance, factory)).validate(result -> {
            if (result.getOutputItem() == null && result.getOutputChemical() == null) {
                return DataResult.error(() -> "No output specified, must have at least an Item or Chemical output");
            }
            return DataResult.success(result);
        }), StreamCodec.composite(
              ItemStackIngredient.STREAM_CODEC, PressurizedReactionRecipe::getInputSolid,
              FluidStackIngredient.STREAM_CODEC, PressurizedReactionRecipe::getInputFluid,
              IngredientCreatorAccess.chemicalStack().streamCodec(), PressurizedReactionRecipe::getInputChemical,
              ByteBufCodecs.VAR_INT, PressurizedReactionRecipe::getEnergyRequired,
              ByteBufCodecs.VAR_INT, PressurizedReactionRecipe::getDuration,
              ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC), BasicPressurizedReactionRecipe::getOutputItemOptional,
              ByteBufCodecs.optional(ChemicalStackTemplate.STREAM_CODEC), BasicPressurizedReactionRecipe::getOutputChemicalOptional,
              factory
        ));
    }

    public static <RECIPE extends ItemStackToChemicalRecipe & IBasicChemicalOutput> RecipeSerializer<RECIPE> itemToChemical(
          BiFunction<ItemStackIngredient, ChemicalStackTemplate, RECIPE> factory) {
        return new RecipeSerializer<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
              ItemStackIngredient.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(ItemStackToChemicalRecipe::getInput),
              ChemicalStackTemplate.MAP_CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(IBasicChemicalOutput::getOutputRaw)
        ).apply(instance, factory)), StreamCodec.composite(
              ItemStackIngredient.STREAM_CODEC, ItemStackToChemicalRecipe::getInput,
              ChemicalStackTemplate.STREAM_CODEC, IBasicChemicalOutput::getOutputRaw,
              factory
        ));
    }

    public static <RECIPE extends ItemStackChemicalToItemStackRecipe & IBasicItemStackOutput> RecipeSerializer<RECIPE> itemChemicalToItem(
          Function4<ItemStackIngredient, ChemicalStackIngredient, ItemStackTemplate, Boolean, RECIPE> factory) {
        return new RecipeSerializer<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
              ItemStackIngredient.CODEC.fieldOf(SerializationConstants.ITEM_INPUT).forGetter(ItemStackChemicalToItemStackRecipe::getItemInput),
              IngredientCreatorAccess.chemicalStack().codec().fieldOf(SerializationConstants.CHEMICAL_INPUT).forGetter(ItemStackChemicalToItemStackRecipe::getChemicalInput),
              ItemStackTemplate.CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(IBasicItemStackOutput::getOutputRaw),
              Codec.BOOL.fieldOf(SerializationConstants.PER_TICK_USAGE).forGetter(ItemStackChemicalToItemStackRecipe::perTickUsage)
        ).apply(instance, factory)), StreamCodec.composite(
              ItemStackIngredient.STREAM_CODEC, ItemStackChemicalToItemStackRecipe::getItemInput,
              IngredientCreatorAccess.chemicalStack().streamCodec(), ItemStackChemicalToItemStackRecipe::getChemicalInput,
              ItemStackTemplate.STREAM_CODEC, IBasicItemStackOutput::getOutputRaw,
              ByteBufCodecs.BOOL, ItemStackChemicalToItemStackRecipe::perTickUsage,
              factory
        ));
    }

    public static <RECIPE extends ChemicalChemicalToChemicalRecipe & IBasicChemicalOutput> RecipeSerializer<RECIPE> chemicalChemicalToChemical(
          Function3<ChemicalStackIngredient, ChemicalStackIngredient, ChemicalStackTemplate, RECIPE> factory, IIngredientCreator<Chemical, ChemicalStack, ChemicalStackIngredient> ingredientCreator) {
        return new RecipeSerializer<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
              ingredientCreator.codec().fieldOf(SerializationConstants.LEFT_INPUT).forGetter(ChemicalChemicalToChemicalRecipe::getLeftInput),
              ingredientCreator.codec().fieldOf(SerializationConstants.RIGHT_INPUT).forGetter(ChemicalChemicalToChemicalRecipe::getRightInput),
              ChemicalStackTemplate.MAP_CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(IBasicChemicalOutput::getOutputRaw)
        ).apply(instance, factory)), StreamCodec.composite(
              ingredientCreator.streamCodec(), ChemicalChemicalToChemicalRecipe::getLeftInput,
              ingredientCreator.streamCodec(), ChemicalChemicalToChemicalRecipe::getRightInput,
              ChemicalStackTemplate.STREAM_CODEC, IBasicChemicalOutput::getOutputRaw,
              factory
        ));
    }
}