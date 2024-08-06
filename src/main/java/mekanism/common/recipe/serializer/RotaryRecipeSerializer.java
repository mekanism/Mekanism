package mekanism.common.recipe.serializer;

import com.mojang.datafixers.util.Function4;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.handler.codec.DecoderException;
import java.util.Optional;
import java.util.function.BiFunction;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
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
import net.neoforged.neoforge.fluids.FluidStack;

@NothingNullByDefault
public class RotaryRecipeSerializer implements RecipeSerializer<BasicRotaryRecipe> {

    private final RecordCodecBuilder<BasicRotaryRecipe, FluidStackIngredient> FLUID_INPUT_FIELD = FluidStackIngredient.CODEC.validate(
          ingredient -> ingredient == null ? DataResult.error(() -> "Fluid input may not be empty") : DataResult.success(ingredient)
    ).fieldOf(SerializationConstants.FLUID_INPUT).forGetter(BasicRotaryRecipe::getFluidInputRaw);
    private final RecordCodecBuilder<BasicRotaryRecipe, FluidStack> FLUID_OUTPUT_FIELD = FluidStack.CODEC.fieldOf(SerializationConstants.FLUID_OUTPUT).forGetter(BasicRotaryRecipe::getFluidOutputRaw);
    private final RecordCodecBuilder<BasicRotaryRecipe, ChemicalStackIngredient> GAS_INPUT_FIELD = ChemicalStackIngredientCreator.INSTANCE.codec().validate(
          ingredient -> ingredient == null ? DataResult.error(() -> "Gas input may not be empty") : DataResult.success(ingredient)
    ).fieldOf(SerializationConstants.GAS_INPUT).forGetter(BasicRotaryRecipe::getGasInputRaw);
    private final RecordCodecBuilder<BasicRotaryRecipe, ChemicalStack> GAS_OUTPUT_FIELD = ChemicalStack.CODEC.fieldOf(SerializationConstants.GAS_OUTPUT).forGetter(BasicRotaryRecipe::getGasOutputRaw);

    private final StreamCodec<RegistryFriendlyByteBuf, BasicRotaryRecipe> streamCodec;
    private final MapCodec<BasicRotaryRecipe> codec;

    public RotaryRecipeSerializer(Function4<FluidStackIngredient, ChemicalStackIngredient, ChemicalStack, FluidStack, BasicRotaryRecipe> bothWaysFactory,
          BiFunction<FluidStackIngredient, ChemicalStack, BasicRotaryRecipe> toGasFactory,
          BiFunction<ChemicalStackIngredient, FluidStack, BasicRotaryRecipe> toFluidFactory) {
        this.codec = NeoForgeExtraCodecs.withAlternative(
              RecordCodecBuilder.mapCodec(i -> i.group(
                    FLUID_INPUT_FIELD,
                    GAS_INPUT_FIELD,
                    GAS_OUTPUT_FIELD,
                    FLUID_OUTPUT_FIELD
              ).apply(i, bothWaysFactory)),
              NeoForgeExtraCodecs.withAlternative(
                    RecordCodecBuilder.mapCodec(i -> i.group(
                          FLUID_INPUT_FIELD,
                          GAS_OUTPUT_FIELD
                    ).apply(i, toGasFactory)),
                    RecordCodecBuilder.mapCodec(i -> i.group(
                          GAS_INPUT_FIELD,
                          FLUID_OUTPUT_FIELD
                    ).apply(i, toFluidFactory))
              )
        );
        this.streamCodec = StreamCodec.composite(
              ByteBufCodecs.optional(FluidToGas.STREAM_CODEC), recipe -> recipe.hasFluidToGas() ? Optional.of(new FluidToGas(recipe)) : Optional.empty(),
              ByteBufCodecs.optional(GasToFluid.STREAM_CODEC), recipe -> recipe.hasGasToFluid() ? Optional.of(new GasToFluid(recipe)) : Optional.empty(),
              (toGas, toFluid) -> {
                  if (toGas.isPresent()) {
                      FluidToGas fluidToGas = toGas.get();
                      if (toFluid.isPresent()) {
                          GasToFluid gasToFluid = toFluid.get();
                          return bothWaysFactory.apply(fluidToGas.input(), gasToFluid.input(), fluidToGas.output(), gasToFluid.output());
                      }
                      return toGasFactory.apply(fluidToGas.input(), fluidToGas.output());
                  } else if (toFluid.isPresent()) {
                      GasToFluid gasToFluid = toFluid.get();
                      return toFluidFactory.apply(gasToFluid.input(), gasToFluid.output());
                  }
                  throw new DecoderException("A recipe got sent with no conversion in either direction.");
              }
        );
    }

    @Override
    public MapCodec<BasicRotaryRecipe> codec() {
        return codec;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, BasicRotaryRecipe> streamCodec() {
        return streamCodec;
    }

    private record FluidToGas(FluidStackIngredient input, ChemicalStack output) {

        //Note: This doesn't need to be optional gas, as we only use this if we have a fluid to gas recipe
        public static final StreamCodec<RegistryFriendlyByteBuf, FluidToGas> STREAM_CODEC = StreamCodec.composite(
              FluidStackIngredient.STREAM_CODEC, FluidToGas::input,
              ChemicalStack.STREAM_CODEC, FluidToGas::output,
              FluidToGas::new
        );

        private FluidToGas(BasicRotaryRecipe recipe) {
            this(recipe.getFluidInput(), recipe.getGasOutputRaw());
        }
    }

    private record GasToFluid(ChemicalStackIngredient input, FluidStack output) {

        //Note: This doesn't need to be optional fluid, as we only use this if we have a gas to fluid recipe
        public static final StreamCodec<RegistryFriendlyByteBuf, GasToFluid> STREAM_CODEC = StreamCodec.composite(
              IngredientCreatorAccess.chemicalStack().streamCodec(), GasToFluid::input,
              FluidStack.STREAM_CODEC, GasToFluid::output,
              GasToFluid::new
        );

        private GasToFluid(BasicRotaryRecipe recipe) {
            this(recipe.getGasInput(), recipe.getFluidOutputRaw());
        }
    }
}