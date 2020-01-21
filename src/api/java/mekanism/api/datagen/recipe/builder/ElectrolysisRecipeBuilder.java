package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import net.minecraft.advancements.Advancement;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectrolysisRecipeBuilder extends MekanismRecipeBuilder<ElectrolysisRecipeBuilder> {

    private final FluidStackIngredient input;
    private final GasStack leftGasOutput;
    private final GasStack rightGasOutput;
    private double energyUsage;

    protected ElectrolysisRecipeBuilder(FluidStackIngredient input, GasStack leftGasOutput, GasStack rightGasOutput) {
        super(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "separating"));
        this.input = input;
        this.leftGasOutput = leftGasOutput;
        this.rightGasOutput = rightGasOutput;
    }

    public static ElectrolysisRecipeBuilder separating(FluidStackIngredient input, GasStack leftGasOutput, GasStack rightGasOutput) {
        if (leftGasOutput.isEmpty() || rightGasOutput.isEmpty()) {
            throw new IllegalArgumentException("This separating recipe requires non empty gas outputs.");
        }
        return new ElectrolysisRecipeBuilder(input, leftGasOutput, rightGasOutput);
    }

    public ElectrolysisRecipeBuilder energyUsage(double energyUsage) {
        if (energyUsage < 0) {
            throw new IllegalArgumentException("Energy usage must be at least zero");
        }
        this.energyUsage = energyUsage;
        return this;
    }

    @Override
    protected ElectrolysisRecipeResult getResult(ResourceLocation id) {
        return new ElectrolysisRecipeResult(id, input, energyUsage, leftGasOutput, rightGasOutput, advancementBuilder,
              new ResourceLocation(id.getNamespace(), "recipes/" + id.getPath()), serializerName);
    }

    public static class ElectrolysisRecipeResult extends RecipeResult {

        private final FluidStackIngredient input;
        private final GasStack leftGasOutput;
        private final GasStack rightGasOutput;
        private final double energyUsage;

        public ElectrolysisRecipeResult(ResourceLocation id, FluidStackIngredient input, double energyUsage, GasStack leftGasOutput, GasStack rightGasOutput,
              Advancement.Builder advancementBuilder, ResourceLocation advancementId, ResourceLocation serializerName) {
            super(id, advancementBuilder, advancementId, serializerName);
            this.input = input;
            this.energyUsage = energyUsage;
            this.leftGasOutput = leftGasOutput;
            this.rightGasOutput = rightGasOutput;
        }

        @Override
        public void serialize(@Nonnull JsonObject json) {
            json.add("input", input.serialize());
            if (energyUsage > 0) {
                //Only add energy usage if it is not zero, as otherwise it will default to zero
                json.addProperty("energyUsage", energyUsage);
            }
            json.add("leftGasOutput", SerializerHelper.serializeGasStack(leftGasOutput));
            json.add("rightGasOutput", SerializerHelper.serializeGasStack(rightGasOutput));
        }
    }
}