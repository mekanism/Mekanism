package mekanism.common.recipe.serializer;

import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Function5;
import com.mojang.datafixers.util.Function7;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiFunction;
import java.util.function.Function;
import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
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
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.basic.BasicChemicalCrystallizerRecipe;
import mekanism.api.recipes.basic.BasicChemicalDissolutionRecipe;
import mekanism.api.recipes.basic.BasicChemicalToChemicalRecipe;
import mekanism.api.recipes.basic.BasicCombinerRecipe;
import mekanism.api.recipes.basic.BasicElectrolysisRecipe;
import mekanism.api.recipes.basic.BasicFluidToFluidRecipe;
import mekanism.api.recipes.basic.BasicItemStackToEnergyRecipe;
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
import mekanism.common.recipe.WrappedShapedRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public record MekanismRecipeSerializer<RECIPE extends Recipe<?>>(MapCodec<RECIPE> codec, StreamCodec<RegistryFriendlyByteBuf, RECIPE> streamCodec)
      implements RecipeSerializer<RECIPE> {

    public static <RECIPE extends WrappedShapedRecipe> MekanismRecipeSerializer<RECIPE> wrapped(Function<ShapedRecipe, RECIPE> wrapper) {
        return new MekanismRecipeSerializer<>(
              SHAPED_RECIPE.codec().xmap(wrapper, WrappedShapedRecipe::getInternal),
              SHAPED_RECIPE.streamCodec().map(wrapper, WrappedShapedRecipe::getInternal)
        );
    }

    public static <RECIPE extends BasicItemStackToItemStackRecipe> MekanismRecipeSerializer<RECIPE> itemToItem(BiFunction<ItemStackIngredient, ItemStack, RECIPE> factory) {
        return new MekanismRecipeSerializer<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
              ItemStackIngredient.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(BasicItemStackToItemStackRecipe::getInput),
              ItemStack.CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(BasicItemStackToItemStackRecipe::getOutputRaw)
        ).apply(instance, factory)), StreamCodec.composite(
              ItemStackIngredient.STREAM_CODEC, BasicItemStackToItemStackRecipe::getInput,
              ItemStack.STREAM_CODEC, BasicItemStackToItemStackRecipe::getOutputRaw,
              factory
        ));
    }

    public static MekanismRecipeSerializer<BasicChemicalCrystallizerRecipe> crystallizing(BiFunction<ChemicalStackIngredient, ItemStack, BasicChemicalCrystallizerRecipe> factory) {
        return new MekanismRecipeSerializer<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
              IngredientCreatorAccess.chemicalStack().codec().fieldOf(SerializationConstants.INPUT).forGetter(BasicChemicalCrystallizerRecipe::getInput),
              ItemStack.CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(BasicChemicalCrystallizerRecipe::getOutputRaw)
        ).apply(instance, factory)), StreamCodec.composite(
              IngredientCreatorAccess.chemicalStack().streamCodec(), BasicChemicalCrystallizerRecipe::getInput,
              ItemStack.STREAM_CODEC, BasicChemicalCrystallizerRecipe::getOutputRaw,
              factory
        ));
    }

    public static MekanismRecipeSerializer<BasicCombinerRecipe> combining(Function3<ItemStackIngredient, ItemStackIngredient, ItemStack, BasicCombinerRecipe> factory) {
        return new MekanismRecipeSerializer<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
              ItemStackIngredient.CODEC.fieldOf(SerializationConstants.MAIN_INPUT).forGetter(CombinerRecipe::getMainInput),
              ItemStackIngredient.CODEC.fieldOf(SerializationConstants.EXTRA_INPUT).forGetter(CombinerRecipe::getExtraInput),
              ItemStack.CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(BasicCombinerRecipe::getOutputRaw)
        ).apply(instance, factory)), StreamCodec.composite(
              ItemStackIngredient.STREAM_CODEC, BasicCombinerRecipe::getMainInput,
              ItemStackIngredient.STREAM_CODEC, BasicCombinerRecipe::getExtraInput,
              ItemStack.STREAM_CODEC, BasicCombinerRecipe::getOutputRaw,
              factory
        ));
    }

    public static <RECIPE extends BasicItemStackToEnergyRecipe> MekanismRecipeSerializer<RECIPE> itemToEnergy(BiFunction<ItemStackIngredient, Long, RECIPE> factory) {
        return new MekanismRecipeSerializer<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
              ItemStackIngredient.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(ItemStackToEnergyRecipe::getInput),
              SerializerHelper.POSITIVE_NONZERO_LONG_CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(BasicItemStackToEnergyRecipe::getOutputRaw)
        ).apply(instance, factory)), StreamCodec.composite(
              ItemStackIngredient.STREAM_CODEC, ItemStackToEnergyRecipe::getInput,
              ByteBufCodecs.VAR_LONG, BasicItemStackToEnergyRecipe::getOutputRaw,
              factory
        ));
    }

    public static <RECIPE extends BasicFluidToFluidRecipe> MekanismRecipeSerializer<RECIPE> fluidToFluid(BiFunction<FluidStackIngredient, FluidStack, RECIPE> factory) {
        return new MekanismRecipeSerializer<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
              FluidStackIngredient.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(FluidToFluidRecipe::getInput),
              FluidStack.CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(BasicFluidToFluidRecipe::getOutputRaw)
        ).apply(instance, factory)), StreamCodec.composite(
              FluidStackIngredient.STREAM_CODEC, FluidToFluidRecipe::getInput,
              FluidStack.STREAM_CODEC, BasicFluidToFluidRecipe::getOutputRaw,
              factory
        ));
    }

    public static <RECIPE extends BasicChemicalToChemicalRecipe> MekanismRecipeSerializer<RECIPE> chemicalToChemical(BiFunction<ChemicalStackIngredient, ChemicalStack, RECIPE> factory) {
        return new MekanismRecipeSerializer<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
              IngredientCreatorAccess.chemicalStack().codec().fieldOf(SerializationConstants.INPUT).forGetter(ChemicalToChemicalRecipe::getInput),
              ChemicalStack.MAP_CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(BasicChemicalToChemicalRecipe::getOutputRaw)
        ).apply(instance, factory)), StreamCodec.composite(
              IngredientCreatorAccess.chemicalStack().streamCodec(), ChemicalToChemicalRecipe::getInput,
              ChemicalStack.STREAM_CODEC, BasicChemicalToChemicalRecipe::getOutputRaw,
              factory
        ));
    }

    public static MekanismRecipeSerializer<BasicWashingRecipe> fluidChemicalToChemical(Function3<FluidStackIngredient, ChemicalStackIngredient, ChemicalStack, BasicWashingRecipe> factory) {
        return new MekanismRecipeSerializer<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
              FluidStackIngredient.CODEC.fieldOf(SerializationConstants.FLUID_INPUT).forGetter(FluidChemicalToChemicalRecipe::getFluidInput),
              IngredientCreatorAccess.chemicalStack().codec().fieldOf(SerializationConstants.CHEMICAL_INPUT).forGetter(FluidChemicalToChemicalRecipe::getChemicalInput),
              ChemicalStack.CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(BasicWashingRecipe::getOutputRaw)
        ).apply(instance, factory)), StreamCodec.composite(
              FluidStackIngredient.STREAM_CODEC, FluidChemicalToChemicalRecipe::getFluidInput,
              IngredientCreatorAccess.chemicalStack().streamCodec(), FluidChemicalToChemicalRecipe::getChemicalInput,
              ChemicalStack.STREAM_CODEC, BasicWashingRecipe::getOutputRaw,
              factory
        ));
    }

    public static MekanismRecipeSerializer<BasicNucleosynthesizingRecipe> nucleosynthesizing(
          Function5<ItemStackIngredient, ChemicalStackIngredient, ItemStack, Integer, Boolean, BasicNucleosynthesizingRecipe> factory) {
        return new MekanismRecipeSerializer<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
              ItemStackIngredient.CODEC.fieldOf(SerializationConstants.ITEM_INPUT).forGetter(NucleosynthesizingRecipe::getItemInput),
              IngredientCreatorAccess.chemicalStack().codec().fieldOf(SerializationConstants.CHEMICAL_INPUT).forGetter(NucleosynthesizingRecipe::getChemicalInput),
              ItemStack.CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(BasicNucleosynthesizingRecipe::getOutputRaw),
              ExtraCodecs.POSITIVE_INT.fieldOf(SerializationConstants.DURATION).forGetter(NucleosynthesizingRecipe::getDuration),
              Codec.BOOL.fieldOf(SerializationConstants.PER_TICK_USAGE).forGetter(NucleosynthesizingRecipe::perTickUsage)
        ).apply(instance, factory)), StreamCodec.composite(
              ItemStackIngredient.STREAM_CODEC, NucleosynthesizingRecipe::getItemInput,
              IngredientCreatorAccess.chemicalStack().streamCodec(), NucleosynthesizingRecipe::getChemicalInput,
              ItemStack.STREAM_CODEC, BasicNucleosynthesizingRecipe::getOutputRaw,
              ByteBufCodecs.VAR_INT, NucleosynthesizingRecipe::getDuration,
              ByteBufCodecs.BOOL, NucleosynthesizingRecipe::perTickUsage,
              factory
        ));
    }

    public static MekanismRecipeSerializer<BasicElectrolysisRecipe> separating(Function4<FluidStackIngredient, Long, ChemicalStack, ChemicalStack, BasicElectrolysisRecipe> factory) {
        return new MekanismRecipeSerializer<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
              FluidStackIngredient.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(ElectrolysisRecipe::getInput),
              SerializerHelper.POSITIVE_NONZERO_LONG_CODEC.optionalFieldOf(SerializationConstants.ENERGY_MULTIPLIER, 1L).forGetter(ElectrolysisRecipe::getEnergyMultiplier),
              ChemicalStack.MAP_CODEC.fieldOf(SerializationConstants.LEFT_CHEMICAL_OUTPUT).forGetter(BasicElectrolysisRecipe::getLeftChemicalOutput),
              ChemicalStack.MAP_CODEC.fieldOf(SerializationConstants.RIGHT_CHEMICAL_OUTPUT).forGetter(BasicElectrolysisRecipe::getRightChemicalOutput)
        ).apply(instance, factory)), StreamCodec.composite(
              FluidStackIngredient.STREAM_CODEC, ElectrolysisRecipe::getInput,
              ByteBufCodecs.VAR_LONG, ElectrolysisRecipe::getEnergyMultiplier,
              ChemicalStack.STREAM_CODEC, BasicElectrolysisRecipe::getLeftChemicalOutput,
              ChemicalStack.STREAM_CODEC, BasicElectrolysisRecipe::getRightChemicalOutput,
              factory
        ));
    }

    public static MekanismRecipeSerializer<BasicChemicalDissolutionRecipe> dissolution(
          Function4<ItemStackIngredient, ChemicalStackIngredient, ChemicalStack, Boolean, BasicChemicalDissolutionRecipe> factory) {
        return new MekanismRecipeSerializer<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
              ItemStackIngredient.CODEC.fieldOf(SerializationConstants.ITEM_INPUT).forGetter(ChemicalDissolutionRecipe::getItemInput),
              IngredientCreatorAccess.chemicalStack().codec().fieldOf(SerializationConstants.CHEMICAL_INPUT).forGetter(ChemicalDissolutionRecipe::getChemicalInput),
              ChemicalStack.CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(BasicChemicalDissolutionRecipe::getOutputRaw),
              Codec.BOOL.fieldOf(SerializationConstants.PER_TICK_USAGE).forGetter(BasicChemicalDissolutionRecipe::perTickUsage)
        ).apply(instance, factory)), StreamCodec.composite(
              ItemStackIngredient.STREAM_CODEC, BasicChemicalDissolutionRecipe::getItemInput,
              IngredientCreatorAccess.chemicalStack().streamCodec(), BasicChemicalDissolutionRecipe::getChemicalInput,
              ChemicalStack.STREAM_CODEC, BasicChemicalDissolutionRecipe::getOutputRaw,
              ByteBufCodecs.BOOL, BasicChemicalDissolutionRecipe::perTickUsage,
              factory
        ));
    }

    public static MekanismRecipeSerializer<BasicPressurizedReactionRecipe> reaction(
          Function7<ItemStackIngredient, FluidStackIngredient, ChemicalStackIngredient, Long, Integer, ItemStack, ChemicalStack, BasicPressurizedReactionRecipe> factory) {
        return new MekanismRecipeSerializer<>(RecordCodecBuilder.<BasicPressurizedReactionRecipe>mapCodec(instance -> instance.group(
              ItemStackIngredient.CODEC.fieldOf(SerializationConstants.ITEM_INPUT).forGetter(PressurizedReactionRecipe::getInputSolid),
              FluidStackIngredient.CODEC.fieldOf(SerializationConstants.FLUID_INPUT).forGetter(PressurizedReactionRecipe::getInputFluid),
              IngredientCreatorAccess.chemicalStack().codec().fieldOf(SerializationConstants.CHEMICAL_INPUT).forGetter(PressurizedReactionRecipe::getInputChemical),
              SerializerHelper.POSITIVE_LONG_CODEC.optionalFieldOf(SerializationConstants.ENERGY_REQUIRED, 0L).forGetter(PressurizedReactionRecipe::getEnergyRequired),
              ExtraCodecs.POSITIVE_INT.fieldOf(SerializationConstants.DURATION).forGetter(PressurizedReactionRecipe::getDuration),
              ItemStack.CODEC.optionalFieldOf(SerializationConstants.ITEM_OUTPUT, ItemStack.EMPTY).forGetter(BasicPressurizedReactionRecipe::getOutputItem),
              ChemicalStack.CODEC.optionalFieldOf(SerializationConstants.CHEMICAL_OUTPUT, ChemicalStack.EMPTY).forGetter(BasicPressurizedReactionRecipe::getOutputChemical)
        ).apply(instance, factory)).validate(result -> {
            if (result.getOutputItem().isEmpty() && result.getOutputChemical().isEmpty()) {
                return DataResult.error(() -> "No output specified, must have at least an Item or Chemical output");
            }
            return DataResult.success(result);
        }), NeoForgeStreamCodecs.composite(
              ItemStackIngredient.STREAM_CODEC, PressurizedReactionRecipe::getInputSolid,
              FluidStackIngredient.STREAM_CODEC, PressurizedReactionRecipe::getInputFluid,
              IngredientCreatorAccess.chemicalStack().streamCodec(), PressurizedReactionRecipe::getInputChemical,
              ByteBufCodecs.VAR_LONG, PressurizedReactionRecipe::getEnergyRequired,
              ByteBufCodecs.VAR_INT, PressurizedReactionRecipe::getDuration,
              ItemStack.OPTIONAL_STREAM_CODEC, BasicPressurizedReactionRecipe::getOutputItem,
              ChemicalStack.OPTIONAL_STREAM_CODEC, BasicPressurizedReactionRecipe::getOutputChemical,
              factory
        ));
    }

    public static <RECIPE extends ItemStackToChemicalRecipe & IBasicChemicalOutput>
    MekanismRecipeSerializer<RECIPE> itemToChemical(BiFunction<ItemStackIngredient, ChemicalStack, RECIPE> factory, MapCodec<ChemicalStack> stackCodec, StreamCodec<? super RegistryFriendlyByteBuf, ChemicalStack> stackStreamCodec) {
        return new MekanismRecipeSerializer<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
              ItemStackIngredient.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(ItemStackToChemicalRecipe::getInput),
              stackCodec.fieldOf(SerializationConstants.OUTPUT).forGetter(IBasicChemicalOutput::getOutputRaw)
        ).apply(instance, factory)), StreamCodec.composite(
              ItemStackIngredient.STREAM_CODEC, ItemStackToChemicalRecipe::getInput,
              stackStreamCodec, IBasicChemicalOutput::getOutputRaw,
              factory
        ));
    }

    public static <RECIPE extends ItemStackChemicalToItemStackRecipe & IBasicItemStackOutput> MekanismRecipeSerializer<RECIPE> itemChemicalToItem(
          Function4<ItemStackIngredient, ChemicalStackIngredient, ItemStack, Boolean, RECIPE> factory) {
        return new MekanismRecipeSerializer<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
              ItemStackIngredient.CODEC.fieldOf(SerializationConstants.ITEM_INPUT).forGetter(ItemStackChemicalToItemStackRecipe::getItemInput),
              IngredientCreatorAccess.chemicalStack().codec().fieldOf(SerializationConstants.CHEMICAL_INPUT).forGetter(ItemStackChemicalToItemStackRecipe::getChemicalInput),
              ItemStack.CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(IBasicItemStackOutput::getOutputRaw),
              Codec.BOOL.fieldOf(SerializationConstants.PER_TICK_USAGE).forGetter(ItemStackChemicalToItemStackRecipe::perTickUsage)
        ).apply(instance, factory)), StreamCodec.composite(
              ItemStackIngredient.STREAM_CODEC, ItemStackChemicalToItemStackRecipe::getItemInput,
              IngredientCreatorAccess.chemicalStack().streamCodec(), ItemStackChemicalToItemStackRecipe::getChemicalInput,
              ItemStack.STREAM_CODEC, IBasicItemStackOutput::getOutputRaw,
              ByteBufCodecs.BOOL, ItemStackChemicalToItemStackRecipe::perTickUsage,
              factory
        ));
    }

    public static <RECIPE extends ChemicalChemicalToChemicalRecipe & IBasicChemicalOutput> MekanismRecipeSerializer<RECIPE>
    chemicalChemicalToChemical(Function3<ChemicalStackIngredient, ChemicalStackIngredient, ChemicalStack, RECIPE> factory, IIngredientCreator<Chemical, ChemicalStack, ChemicalStackIngredient> ingredientCreator,
          MapCodec<ChemicalStack> stackCodec, StreamCodec<? super RegistryFriendlyByteBuf, ChemicalStack> stackStreamCodec) {
        return new MekanismRecipeSerializer<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
              ingredientCreator.codec().fieldOf(SerializationConstants.LEFT_INPUT).forGetter(ChemicalChemicalToChemicalRecipe::getLeftInput),
              ingredientCreator.codec().fieldOf(SerializationConstants.RIGHT_INPUT).forGetter(ChemicalChemicalToChemicalRecipe::getRightInput),
              stackCodec.fieldOf(SerializationConstants.OUTPUT).forGetter(IBasicChemicalOutput::getOutputRaw)
        ).apply(instance, factory)), StreamCodec.composite(
              ingredientCreator.streamCodec(), ChemicalChemicalToChemicalRecipe::getLeftInput,
              ingredientCreator.streamCodec(), ChemicalChemicalToChemicalRecipe::getRightInput,
              stackStreamCodec, IBasicChemicalOutput::getOutputRaw,
              factory
        ));
    }
}