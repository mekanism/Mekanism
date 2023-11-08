package mekanism.common.recipe.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.JsonConstants;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class ElectrolysisRecipeSerializer<RECIPE extends ElectrolysisRecipe> implements RecipeSerializer<RECIPE> {

    private static final Codec<FloatingLong> FLOAT_LONG_AT_LEAST_ONE = ExtraCodecs.validate(FloatingLong.CODEC, fl -> fl.smallerThan(FloatingLong.ONE) ? DataResult.error(() -> "Expected energyMultiplier to be at least one.") : DataResult.success(fl));
    private final IFactory<RECIPE> factory;
    private Codec<RECIPE> codec;

    public ElectrolysisRecipeSerializer(IFactory<RECIPE> factory) {
        this.factory = factory;
    }

    @Override
    @NotNull
    public Codec<RECIPE> codec() {
        if (codec == null) {
            codec = RecordCodecBuilder.create(instance->instance.group(
                  IngredientCreatorAccess.fluid().codec().fieldOf(JsonConstants.INPUT).forGetter(ElectrolysisRecipe::getInput),
                  FLOAT_LONG_AT_LEAST_ONE.optionalFieldOf(JsonConstants.ENERGY_MULTIPLIER, FloatingLong.ONE).forGetter(ElectrolysisRecipe::getEnergyMultiplier),
                  ChemicalUtils.GAS_STACK_CODEC.fieldOf(JsonConstants.LEFT_GAS_OUTPUT).forGetter(ElectrolysisRecipe::getLeftGasOutput),
                  ChemicalUtils.GAS_STACK_CODEC.fieldOf(JsonConstants.RIGHT_GAS_OUTPUT).forGetter(ElectrolysisRecipe::getRightGasOutput)
            ).apply(instance, factory::create));
        }
        return codec;
    }

    @Override
    public RECIPE fromNetwork(@NotNull FriendlyByteBuf buffer) {
        try {
            FluidStackIngredient input = IngredientCreatorAccess.fluid().read(buffer);
            FloatingLong energyMultiplier = FloatingLong.readFromBuffer(buffer);
            GasStack leftGasOutput = GasStack.readFromPacket(buffer);
            GasStack rightGasOutput = GasStack.readFromPacket(buffer);
            return this.factory.create(input, energyMultiplier, leftGasOutput, rightGasOutput);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading electrolysis recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RECIPE recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing electrolysis recipe to packet.", e);
            throw e;
        }
    }

    @FunctionalInterface
    public interface IFactory<RECIPE extends ElectrolysisRecipe> {

        RECIPE create(FluidStackIngredient input, FloatingLong energyMultiplier, GasStack leftGasOutput, GasStack rightGasOutput);
    }
}