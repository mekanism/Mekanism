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
    private final RecordCodecBuilder<BasicRotaryRecipe, ChemicalStackIngredient> CHEMICAL_INPUT_FIELD = ChemicalStackIngredientCreator.INSTANCE.codec().validate(
          ingredient -> ingredient == null ? DataResult.error(() -> "Chemical input may not be empty") : DataResult.success(ingredient)
    ).fieldOf(SerializationConstants.CHEMICAL_INPUT).forGetter(BasicRotaryRecipe::getChemicalInputRaw);
    private final RecordCodecBuilder<BasicRotaryRecipe, ChemicalStack> CHEMICAL_OUTPUT_FIELD = ChemicalStack.CODEC.fieldOf(SerializationConstants.CHEMICAL_OUTPUT).forGetter(BasicRotaryRecipe::getChemicalOutputRaw);

    private final StreamCodec<RegistryFriendlyByteBuf, BasicRotaryRecipe> streamCodec;
    private final MapCodec<BasicRotaryRecipe> codec;

    public RotaryRecipeSerializer(Function4<FluidStackIngredient, ChemicalStackIngredient, ChemicalStack, FluidStack, BasicRotaryRecipe> bothWaysFactory,
          BiFunction<FluidStackIngredient, ChemicalStack, BasicRotaryRecipe> toChemicalFactory,
          BiFunction<ChemicalStackIngredient, FluidStack, BasicRotaryRecipe> toFluidFactory) {
        this.codec = NeoForgeExtraCodecs.withAlternative(
              RecordCodecBuilder.mapCodec(i -> i.group(
                    FLUID_INPUT_FIELD,
                    CHEMICAL_INPUT_FIELD,
                    CHEMICAL_OUTPUT_FIELD,
                    FLUID_OUTPUT_FIELD
              ).apply(i, bothWaysFactory)),
              NeoForgeExtraCodecs.withAlternative(
                    RecordCodecBuilder.mapCodec(i -> i.group(
                          FLUID_INPUT_FIELD,
                          CHEMICAL_OUTPUT_FIELD
                    ).apply(i, toChemicalFactory)),
                    RecordCodecBuilder.mapCodec(i -> i.group(
                          CHEMICAL_INPUT_FIELD,
                          FLUID_OUTPUT_FIELD
                    ).apply(i, toFluidFactory))
              )
        );
        this.streamCodec = StreamCodec.composite(
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

    private record FluidToChemical(FluidStackIngredient input, ChemicalStack output) {

        //Note: This doesn't need to be optional gas, as we only use this if we have a fluid to gas recipe
        public static final StreamCodec<RegistryFriendlyByteBuf, FluidToChemical> STREAM_CODEC = StreamCodec.composite(
              FluidStackIngredient.STREAM_CODEC, FluidToChemical::input,
              ChemicalStack.STREAM_CODEC, FluidToChemical::output,
              FluidToChemical::new
        );

        private FluidToChemical(BasicRotaryRecipe recipe) {
            this(recipe.getFluidInput(), recipe.getChemicalOutputRaw());
        }
    }

    private record ChemicalToFluid(ChemicalStackIngredient input, FluidStack output) {

        //Note: This doesn't need to be optional fluid, as we only use this if we have a gas to fluid recipe
        public static final StreamCodec<RegistryFriendlyByteBuf, ChemicalToFluid> STREAM_CODEC = StreamCodec.composite(
              IngredientCreatorAccess.chemicalStack().streamCodec(), ChemicalToFluid::input,
              FluidStack.STREAM_CODEC, ChemicalToFluid::output,
              ChemicalToFluid::new
        );

        private ChemicalToFluid(BasicRotaryRecipe recipe) {
            this(recipe.getChemicalInput(), recipe.getFluidOutputRaw());
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ChemicalToFluid other = (ChemicalToFluid) o;
            return FluidStack.matches(output, other.output) && input.equals(other.input);
        }

        @Override
        public int hashCode() {
            int hash = input.hashCode();
            hash = 31 * hash + FluidStack.hashFluidAndComponents(output);
            hash = 31 * hash + output.getAmount();
            return hash;
        }
    }
}