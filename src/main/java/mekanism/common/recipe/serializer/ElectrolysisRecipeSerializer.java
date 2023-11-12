package mekanism.common.recipe.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.JsonConstants;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.basic.BasicElectrolysisRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.impl.ElectrolysisIRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class ElectrolysisRecipeSerializer implements RecipeSerializer<BasicElectrolysisRecipe> {

    private static final Codec<FloatingLong> FLOAT_LONG_AT_LEAST_ONE = ExtraCodecs.validate(FloatingLong.CODEC, fl -> fl.smallerThan(FloatingLong.ONE) ? DataResult.error(() -> "Expected energyMultiplier to be at least one.") : DataResult.success(fl));
    private final IFactory<BasicElectrolysisRecipe> factory;
    private Codec<BasicElectrolysisRecipe> codec;

    public ElectrolysisRecipeSerializer(IFactory<BasicElectrolysisRecipe> factory) {
        this.factory = factory;
    }

    @NotNull
    @Override
    public Codec<BasicElectrolysisRecipe> codec() {
        if (codec == null) {
            codec = RecordCodecBuilder.create(instance->instance.group(
                  IngredientCreatorAccess.fluid().codec().fieldOf(JsonConstants.INPUT).forGetter(ElectrolysisRecipe::getInput),
                  FLOAT_LONG_AT_LEAST_ONE.optionalFieldOf(JsonConstants.ENERGY_MULTIPLIER, FloatingLong.ONE).forGetter(ElectrolysisRecipe::getEnergyMultiplier),
                  ChemicalUtils.GAS_STACK_CODEC.fieldOf(JsonConstants.LEFT_GAS_OUTPUT).forGetter(BasicElectrolysisRecipe::getLeftGasOutput),
                  ChemicalUtils.GAS_STACK_CODEC.fieldOf(JsonConstants.RIGHT_GAS_OUTPUT).forGetter(BasicElectrolysisRecipe::getRightGasOutput)
            ).apply(instance, factory::create));
        }
        return codec;
    }

    @Override
    public BasicElectrolysisRecipe fromNetwork(@NotNull FriendlyByteBuf buffer) {
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
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull BasicElectrolysisRecipe recipe) {
        try {
            recipe.getInput().write(buffer);
            recipe.getEnergyMultiplier().writeToBuffer(buffer);
            recipe.getLeftGasOutput().writeToPacket(buffer);
            recipe.getRightGasOutput().writeToPacket(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing electrolysis recipe to packet.", e);
            throw e;
        }
    }

    @FunctionalInterface
    public interface IFactory<RECIPE extends BasicElectrolysisRecipe> {

        RECIPE create(FluidStackIngredient input, FloatingLong energyMultiplier, GasStack leftGasOutput, GasStack rightGasOutput);
    }
}