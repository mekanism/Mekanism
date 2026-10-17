package mekanism.common.recipe.serializer;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.Optional;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.basic.BasicRotaryRecipe;
import mekanism.api.recipes.book.RotaryBookCategory.RotaryBookInfo;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.Recipe.CommonInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import net.neoforged.neoforge.fluids.FluidStackTemplate;

public class RotaryRecipeSerializer {

    public static <RECIPE extends BasicRotaryRecipe> RecipeSerializer<RECIPE> create(RotaryRecipe.Factory<RECIPE> bothWaysFactory,
          RotaryRecipe.FluidToChemicalFactory<RECIPE> toChemicalFactory, RotaryRecipe.ChemicalToFluidFactory<RECIPE> toFluidFactory) {
        return new RecipeSerializer<>(
              RecipeData.CODEC.flatXmap(recipe -> {
                  if (recipe.fluidInput.isPresent() && recipe.chemicalOutput.isPresent()) {
                      if (recipe.chemicalInput.isPresent() && recipe.fluidOutput.isPresent()) {
                          return DataResult.success(bothWaysFactory.create(recipe.commonInfo(), recipe.bookInfo(), recipe.fluidInput.get(), recipe.chemicalInput.get(), recipe.chemicalOutput.get(), recipe.fluidOutput.get()));
                      }
                      return DataResult.success(toChemicalFactory.create(recipe.commonInfo(), recipe.bookInfo(), recipe.fluidInput.get(), recipe.chemicalOutput.get()));
                  } else if (recipe.chemicalInput.isPresent() && recipe.fluidOutput.isPresent()) {
                      return DataResult.success(toFluidFactory.create(recipe.commonInfo(), recipe.bookInfo(), recipe.chemicalInput.get(), recipe.fluidOutput.get()));
                  }
                  return DataResult.error(() -> "At least a fluid to chemical or a chemical to fluid recipe must be provided.");
              }, recipe -> DataResult.success(new RecipeData(recipe.commonInfo(), recipe.bookInfo(), recipe.getFluidInputRaw(), recipe.getChemicalOutputRaw(), recipe.getChemicalInputRaw(), recipe.getFluidOutputRaw()))),
              StreamCodec.composite(
                    CommonInfo.STREAM_CODEC, MekanismRecipe::commonInfo,
                    RotaryBookInfo.STREAM_CODEC, MekanismRecipe::bookInfo,
                    ByteBufCodecs.optional(FluidToChemical.STREAM_CODEC), recipe -> recipe.hasFluidToChemical() ? Optional.of(new FluidToChemical(recipe)) : Optional.empty(),
                    ByteBufCodecs.optional(ChemicalToFluid.STREAM_CODEC), recipe -> recipe.hasChemicalToFluid() ? Optional.of(new ChemicalToFluid(recipe)) : Optional.empty(),
                    (commonInfo, bookInfo, toGas, toFluid) -> {
                        if (toGas.isPresent()) {
                            FluidToChemical fluidToChemical = toGas.get();
                            if (toFluid.isPresent()) {
                                ChemicalToFluid chemicalToFluid = toFluid.get();
                                return bothWaysFactory.create(commonInfo, bookInfo, fluidToChemical.input(), chemicalToFluid.input(), fluidToChemical.output(), chemicalToFluid.output());
                            }
                            return toChemicalFactory.create(commonInfo, bookInfo, fluidToChemical.input(), fluidToChemical.output());
                        } else if (toFluid.isPresent()) {
                            ChemicalToFluid chemicalToFluid = toFluid.get();
                            return toFluidFactory.create(commonInfo, bookInfo, chemicalToFluid.input(), chemicalToFluid.output());
                        }
                        throw new DecoderException("A recipe got sent with no conversion in either direction.");
                    }
              )
        );
    }

    private record RecipeData(Recipe.CommonInfo commonInfo, RotaryBookInfo bookInfo,
                              Optional<FluidStackIngredient> fluidInput, Optional<ChemicalStackTemplate> chemicalOutput,
                              Optional<ChemicalStackIngredient> chemicalInput, Optional<FluidStackTemplate> fluidOutput) {

        private static final RecordCodecBuilder<RecipeData, Recipe.CommonInfo> COMMON_INFO_FIELD = Recipe.CommonInfo.MAP_CODEC.forGetter(RecipeData::commonInfo);
        private static final RecordCodecBuilder<RecipeData, RotaryBookInfo> BOOK_INFO_FIELD = RotaryBookInfo.MAP_CODEC.forGetter(RecipeData::bookInfo);

        private static final RecordCodecBuilder<RecipeData, Optional<FluidStackIngredient>> FLUID_INPUT_FIELD = FluidStackIngredient.CODEC
              .optionalFieldOf(SerializationConstants.FLUID_INPUT)
              .forGetter(RecipeData::fluidInput);
        private static final RecordCodecBuilder<RecipeData, Optional<ChemicalStackTemplate>> CHEMICAL_OUTPUT_FIELD = ChemicalStackTemplate.MAP_CODEC.codec()
              .optionalFieldOf(SerializationConstants.CHEMICAL_OUTPUT)
              .forGetter(RecipeData::chemicalOutput);

        private static final RecordCodecBuilder<RecipeData, Optional<ChemicalStackIngredient>> CHEMICAL_INPUT_FIELD = ChemicalStackIngredient.CODEC
              .optionalFieldOf(SerializationConstants.CHEMICAL_INPUT)
              .forGetter(RecipeData::chemicalInput);
        private static final RecordCodecBuilder<RecipeData, Optional<FluidStackTemplate>> FLUID_OUTPUT_FIELD = FluidStackTemplate.MAP_CODEC.codec()
              .optionalFieldOf(SerializationConstants.FLUID_OUTPUT)
              .forGetter(RecipeData::fluidOutput);

        private static final MapCodec<RecipeData> BOTH_WAYS_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
              COMMON_INFO_FIELD,
              BOOK_INFO_FIELD,
              FLUID_INPUT_FIELD,
              CHEMICAL_OUTPUT_FIELD,
              CHEMICAL_INPUT_FIELD,
              FLUID_OUTPUT_FIELD
        ).apply(i, RecipeData::new));
        private static final MapCodec<RecipeData> FLUID_TO_CHEMICAL_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
              COMMON_INFO_FIELD,
              BOOK_INFO_FIELD,
              FLUID_INPUT_FIELD,
              CHEMICAL_OUTPUT_FIELD
        ).apply(i, (commonInfo, bookInfo, fluid, chemical) -> new RecipeData(commonInfo, bookInfo, fluid, chemical, Optional.empty(), Optional.empty())));
        private static final MapCodec<RecipeData> CHEMICAL_TO_FLUID_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
              COMMON_INFO_FIELD,
              BOOK_INFO_FIELD,
              CHEMICAL_INPUT_FIELD,
              FLUID_OUTPUT_FIELD
        ).apply(i, (commonInfo, bookInfo, chemical, fluid) -> new RecipeData(commonInfo, bookInfo, Optional.empty(), Optional.empty(), chemical, fluid)));
        private static final MapCodec<RecipeData> CODEC = NeoForgeExtraCodecs.withAlternative(BOTH_WAYS_CODEC, NeoForgeExtraCodecs.withAlternative(FLUID_TO_CHEMICAL_CODEC, CHEMICAL_TO_FLUID_CODEC));
    }

    private record FluidToChemical(FluidStackIngredient input, ChemicalStackTemplate output) {

        //Note: This doesn't need to be optional gas, as we only use this if we have a fluid to gas recipe
        private static final StreamCodec<RegistryFriendlyByteBuf, FluidToChemical> STREAM_CODEC = StreamCodec.composite(
              FluidStackIngredient.STREAM_CODEC, FluidToChemical::input,
              ChemicalStackTemplate.STREAM_CODEC, FluidToChemical::output,
              FluidToChemical::new
        );

        private FluidToChemical(BasicRotaryRecipe recipe) {
            this(recipe.getFluidInputRaw().orElseThrow(EncoderException::new), recipe.getChemicalOutputRaw().orElseThrow(EncoderException::new));
        }
    }

    private record ChemicalToFluid(ChemicalStackIngredient input, FluidStackTemplate output) {

        //Note: This doesn't need to be optional fluid, as we only use this if we have a gas to fluid recipe
        private static final StreamCodec<RegistryFriendlyByteBuf, ChemicalToFluid> STREAM_CODEC = StreamCodec.composite(
              ChemicalStackIngredient.STREAM_CODEC, ChemicalToFluid::input,
              FluidStackTemplate.STREAM_CODEC, ChemicalToFluid::output,
              ChemicalToFluid::new
        );

        private ChemicalToFluid(BasicRotaryRecipe recipe) {
            this(recipe.getChemicalInputRaw().orElseThrow(EncoderException::new), recipe.getFluidOutputRaw().orElseThrow(EncoderException::new));
        }
    }
}