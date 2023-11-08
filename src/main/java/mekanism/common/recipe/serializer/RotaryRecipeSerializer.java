package mekanism.common.recipe.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ingredient.creator.FluidStackIngredientCreator;
import mekanism.common.recipe.ingredient.creator.GasStackIngredientCreator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class RotaryRecipeSerializer<RECIPE extends RotaryRecipe> implements RecipeSerializer<RECIPE> {

    private final  RecordCodecBuilder<RECIPE, FluidStackIngredient> FLUID_INPUT_FIELD = RecordCodecBuilder.of(RotaryRecipe::getFluidInput, JsonConstants.FLUID_INPUT, FluidStackIngredientCreator.INSTANCE.codec());
    private final  RecordCodecBuilder<RECIPE, FluidStack> FLUID_OUTPUT_FIELD = RecordCodecBuilder.of(r->r.getFluidOutput(GasStack.EMPTY), JsonConstants.FLUID_OUTPUT, SerializerHelper.FLUIDSTACK_CODEC);
    private final RecordCodecBuilder<RECIPE, GasStackIngredient> GAS_INPUT_FIELD = RecordCodecBuilder.of(RotaryRecipe::getGasInput, JsonConstants.GAS_INPUT, GasStackIngredientCreator.INSTANCE.codec());
    private final RecordCodecBuilder<RECIPE, GasStack> GAS_OUTPUT_FIELD = RecordCodecBuilder.of(rotaryRecipe -> rotaryRecipe.getGasOutput(FluidStack.EMPTY), JsonConstants.GAS_INPUT, ChemicalUtils.GAS_STACK_CODEC);

    private Codec<RECIPE> bothWaysCodec() {
        return RecordCodecBuilder.create(i -> i.group(
              FLUID_INPUT_FIELD,
              GAS_INPUT_FIELD,
              GAS_OUTPUT_FIELD,
              FLUID_OUTPUT_FIELD
        ).apply(i, this.factory::create));
    }

    private Codec<RECIPE> fluidToGasCodec() {
        return RecordCodecBuilder.create(i -> i.group(
              FLUID_INPUT_FIELD,
              GAS_OUTPUT_FIELD
        ).apply(i, this.factory::create));
    }

    private Codec<RECIPE> gasToFluidCodec() {
        return RecordCodecBuilder.create(i -> i.group(
              GAS_INPUT_FIELD,
              FLUID_OUTPUT_FIELD
        ).apply(i, this.factory::create));
    }

    private final IFactory<RECIPE> factory;
    private final Lazy<Codec<RECIPE>> codec;

    public RotaryRecipeSerializer(IFactory<RECIPE> factory) {
        this.factory = factory;
        this.codec = Lazy.of(this::makeCodec);
        }

    private Codec<RECIPE> makeCodec() {
        return NeoForgeExtraCodecs.withAlternative(bothWaysCodec(), NeoForgeExtraCodecs.withAlternative(fluidToGasCodec(), gasToFluidCodec()));
    }

    @Override
    @NotNull
    public Codec<RECIPE> codec() {
        return this.codec.get();
    }

    @Override
    public RECIPE fromNetwork(@NotNull FriendlyByteBuf buffer) {
        try {
            FluidStackIngredient fluidInputIngredient = null;
            GasStackIngredient gasInputIngredient = null;
            GasStack gasOutput = null;
            FluidStack fluidOutput = null;
            boolean hasFluidToGas = buffer.readBoolean();
            if (hasFluidToGas) {
                fluidInputIngredient = IngredientCreatorAccess.fluid().read(buffer);
                gasOutput = GasStack.readFromPacket(buffer);
            }
            boolean hasGasToFluid = buffer.readBoolean();
            if (hasGasToFluid) {
                gasInputIngredient = IngredientCreatorAccess.gas().read(buffer);
                fluidOutput = FluidStack.readFromPacket(buffer);
            }
            if (hasFluidToGas && hasGasToFluid) {
                return this.factory.create(fluidInputIngredient, gasInputIngredient, gasOutput, fluidOutput);
            } else if (hasFluidToGas) {
                return this.factory.create(fluidInputIngredient, gasOutput);
            } else if (hasGasToFluid) {
                return this.factory.create(gasInputIngredient, fluidOutput);
            }
            //Should never happen, but if we somehow get here log it
            Mekanism.logger.error("Error reading rotary recipe from packet. A recipe got sent with no conversion in either direction.");
            return null;
        } catch (Exception e) {
            Mekanism.logger.error("Error reading rotary recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RECIPE recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing rotary recipe to packet.", e);
            throw e;
        }
    }

    public interface IFactory<RECIPE extends RotaryRecipe> {

        RECIPE create(FluidStackIngredient fluidInput, GasStack gasOutput);

        RECIPE create(GasStackIngredient gasInput, FluidStack fluidOutput);

        RECIPE create(FluidStackIngredient fluidInput, GasStackIngredient gasInput, GasStack gasOutput, FluidStack fluidOutput);
    }
}