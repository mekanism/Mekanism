package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import java.util.List;
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
import net.minecraftforge.common.crafting.conditions.ICondition;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectrolysisRecipeBuilder extends MekanismRecipeBuilder<ElectrolysisRecipeBuilder> {

    private final FluidStackIngredient input;
    private final GasStack leftGasOutput;
    private final GasStack rightGasOutput;
    private double energyMultiplier = 1;

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

    public ElectrolysisRecipeBuilder energyMultiplier(double multiplier) {
        if (multiplier < 1) {
            throw new IllegalArgumentException("Energy multiplier must be greater than or equal to one");
        }
        this.energyMultiplier = multiplier;
        return this;
    }

    @Override
    protected ElectrolysisRecipeResult getResult(ResourceLocation id) {
        return new ElectrolysisRecipeResult(id, input, energyMultiplier, leftGasOutput, rightGasOutput, conditions, advancementBuilder,
              new ResourceLocation(id.getNamespace(), "recipes/" + id.getPath()), serializerName);
    }

    public static class ElectrolysisRecipeResult extends RecipeResult {

        private final FluidStackIngredient input;
        private final GasStack leftGasOutput;
        private final GasStack rightGasOutput;
        private final double energyMultiplier;

        public ElectrolysisRecipeResult(ResourceLocation id, FluidStackIngredient input, double energyMultiplier, GasStack leftGasOutput, GasStack rightGasOutput,
              List<ICondition> conditions, Advancement.Builder advancementBuilder, ResourceLocation advancementId, ResourceLocation serializerName) {
            super(id, conditions, advancementBuilder, advancementId, serializerName);
            this.input = input;
            this.energyMultiplier = energyMultiplier;
            this.leftGasOutput = leftGasOutput;
            this.rightGasOutput = rightGasOutput;
        }

        @Override
        public void serialize(@Nonnull JsonObject json) {
            json.add("input", input.serialize());
            if (energyMultiplier > 1) {
                //Only add energy usage if it is greater than one, as otherwise it will default to one
                json.addProperty("energyMultiplier", energyMultiplier);
            }
            json.add("leftGasOutput", SerializerHelper.serializeGasStack(leftGasOutput));
            json.add("rightGasOutput", SerializerHelper.serializeGasStack(rightGasOutput));
        }
    }
}