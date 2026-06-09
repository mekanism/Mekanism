package mekanism.common.recipe.serializer;

import com.mojang.datafixers.util.Function4;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.handler.codec.DecoderException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.basic.BasicRotaryRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.recipe.ingredients.creator.ChemicalStackIngredientCreator;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import net.neoforged.neoforge.fluids.FluidStackTemplate;

public class RotaryRecipeSerializer {

    private static final RecordCodecBuilder<BasicRotaryRecipe, FluidStackIngredient> FLUID_INPUT_FIELD = FluidStackIngredient.CODEC.validate(
          ingredient -> ingredient == null ? DataResult.error(() -> "Fluid input may not be empty") : DataResult.success(ingredient)
    ).fieldOf(SerializationConstants.FLUID_INPUT).forGetter(BasicRotaryRecipe::getFluidInputRaw);
    private static final RecordCodecBuilder<BasicRotaryRecipe, Optional<FluidStackTemplate>> FLUID_OUTPUT_FIELD = FluidStackTemplate.CODEC.optionalFieldOf(SerializationConstants.FLUID_OUTPUT)
          .forGetter(recipe -> Optional.ofNullable(recipe.getFluidOutputRaw()));
    private static final RecordCodecBuilder<BasicRotaryRecipe, ChemicalStackIngredient> CHEMICAL_INPUT_FIELD = ChemicalStackIngredientCreator.INSTANCE.codec().validate(
          ingredient -> ingredient == null ? DataResult.error(() -> "Chemical input may not be empty") : DataResult.success(ingredient)
    ).fieldOf(SerializationConstants.CHEMICAL_INPUT).forGetter(BasicRotaryRecipe::getChemicalInputRaw);
    private static final RecordCodecBuilder<BasicRotaryRecipe, Optional<ChemicalStackTemplate>> CHEMICAL_OUTPUT_FIELD = ChemicalStackTemplate.CODEC.optionalFieldOf(SerializationConstants.CHEMICAL_OUTPUT)
          .forGetter(recipe -> Optional.ofNullable(recipe.getChemicalOutputRaw()));

    public static RecipeSerializer<BasicRotaryRecipe> create(Function4<FluidStackIngredient, ChemicalStackIngredient, ChemicalStackTemplate, FluidStackTemplate, BasicRotaryRecipe> bothWaysFactory,
          BiFunction<FluidStackIngredient, ChemicalStackTemplate, BasicRotaryRecipe> toChemicalFactory,
          BiFunction<ChemicalStackIngredient, FluidStackTemplate, BasicRotaryRecipe> toFluidFactory) {
        return new RecipeSerializer<>(
              NeoForgeExtraCodecs.withAlternative(
                    RecordCodecBuilder.mapCodec(i -> i.group(
                          FLUID_INPUT_FIELD,
                          CHEMICAL_INPUT_FIELD,
                          CHEMICAL_OUTPUT_FIELD,
                          FLUID_OUTPUT_FIELD
                    ).apply(i, (fluidIn, chemicalIn, chemicalOut, fluidOut) ->
                          bothWaysFactory.apply(fluidIn, chemicalIn, chemicalOut.orElse(null), fluidOut.orElse(null)))),
                    NeoForgeExtraCodecs.withAlternative(
                          RecordCodecBuilder.mapCodec(i -> i.group(
                                FLUID_INPUT_FIELD,
                                CHEMICAL_OUTPUT_FIELD
                          ).apply(i, (fluid, chemical) -> toChemicalFactory.apply(fluid, chemical.orElse(null)))),
                          RecordCodecBuilder.mapCodec(i -> i.group(
                                CHEMICAL_INPUT_FIELD,
                                FLUID_OUTPUT_FIELD
                          ).apply(i, (chemical, fluid) -> toFluidFactory.apply(chemical, fluid.orElse(null))))
                    )
              ),
              StreamCodec.composite(
                    ByteBufCodecs.optional(FluidToChemical.STREAM_CODEC), recipe -> recipe.hasFluidToChemical() ? Optional.of(new FluidToChemical(recipe)) : Optional.empty(),
                    ByteBufCodecs.optional(ChemicalToFluid.STREAM_CODEC), recipe -> recipe.hasChemicalToFluid() ? Optional.of(new ChemicalToFluid(recipe)) : Optional.empty(),
                    (toGas, toFluid) -> {
                        if (toGas.isPresent()) {
                            FluidToChemical fluidToChemical = toGas.get();
                            if (toFluid.isPresent()) {
                                ChemicalToFluid chemicalToFluid = toFluid.get();
                                return bothWaysFactory.apply(fluidToChemical.input(), chemicalToFluid.input(), fluidToChemical.output(), chemicalToFluid.output());
                            }
                            return toChemicalFactory.apply(fluidToChemical.input(), fluidToChemical.output());
                        } else if (toFluid.isPresent()) {
                            ChemicalToFluid chemicalToFluid = toFluid.get();
                            return toFluidFactory.apply(chemicalToFluid.input(), chemicalToFluid.output());
                        }
                        throw new DecoderException("A recipe got sent with no conversion in either direction.");
                    }
              )
        );
    }

    private record FluidToChemical(FluidStackIngredient input, ChemicalStackTemplate output) {

        //Note: This doesn't need to be optional gas, as we only use this if we have a fluid to gas recipe
        public static final StreamCodec<RegistryFriendlyByteBuf, FluidToChemical> STREAM_CODEC = StreamCodec.composite(
              FluidStackIngredient.STREAM_CODEC, FluidToChemical::input,
              ChemicalStackTemplate.STREAM_CODEC, FluidToChemical::output,
              FluidToChemical::new
        );

        private FluidToChemical(BasicRotaryRecipe recipe) {
            this(recipe.getFluidInput(), Objects.requireNonNull(recipe.getChemicalOutputRaw()));
        }
    }

    private record ChemicalToFluid(ChemicalStackIngredient input, FluidStackTemplate output) {

        //Note: This doesn't need to be optional fluid, as we only use this if we have a gas to fluid recipe
        public static final StreamCodec<RegistryFriendlyByteBuf, ChemicalToFluid> STREAM_CODEC = StreamCodec.composite(
              IngredientCreatorAccess.chemicalStack().streamCodec(), ChemicalToFluid::input,
              FluidStackTemplate.STREAM_CODEC, ChemicalToFluid::output,
              ChemicalToFluid::new
        );

        private ChemicalToFluid(BasicRotaryRecipe recipe) {
            this(recipe.getChemicalInput(), Objects.requireNonNull(recipe.getFluidOutputRaw()));
        }
    }
}