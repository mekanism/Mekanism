package mekanism.common.recipe.builder;

import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.common.recipe.serializer.SerializerHelper;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.advancements.Advancement;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RotaryRecipeBuilder extends MekanismRecipeBuilder<RotaryRecipe, RotaryRecipeBuilder> {

    private final static GasStackIngredient EMPTY_GAS_INPUT = GasStackIngredient.from(GasStack.EMPTY);
    private final static FluidStackIngredient EMPTY_FLUID_INPUT = FluidStackIngredient.from(FluidStack.EMPTY);
    private final RecipeDirection direction;
    private final GasStackIngredient gasInput;
    private final FluidStackIngredient fluidInput;
    private final FluidStack fluidOutput;
    private final GasStack gasOutput;

    protected RotaryRecipeBuilder(FluidStackIngredient fluidInput, GasStackIngredient gasInput, GasStack gasOutput, FluidStack fluidOutput, RecipeDirection direction) {
        super(MekanismRecipeSerializers.ROTARY.getRecipeSerializer());
        this.direction = direction;
        this.gasInput = gasInput;
        this.fluidInput = fluidInput;
        this.gasOutput = gasOutput;
        this.fluidOutput = fluidOutput;
    }

    public static RotaryRecipeBuilder rotary(FluidStackIngredient fluidInput, GasStack gasOutput) {
        if (gasOutput.isEmpty()) {
            throw new IllegalArgumentException("This rotary condensentrator recipe requires a non empty gas output.");
        }
        return new RotaryRecipeBuilder(fluidInput, EMPTY_GAS_INPUT, gasOutput, FluidStack.EMPTY, RecipeDirection.FLUID_TO_GAS);
    }

    public static RotaryRecipeBuilder rotary(GasStackIngredient gasInput, FluidStack fluidOutput) {
        if (fluidOutput.isEmpty()) {
            throw new IllegalArgumentException("This rotary condensentrator recipe requires a non empty fluid output.");
        }
        return new RotaryRecipeBuilder(EMPTY_FLUID_INPUT, gasInput, GasStack.EMPTY, fluidOutput, RecipeDirection.GAS_TO_FLUID);
    }

    public static RotaryRecipeBuilder rotary(FluidStackIngredient fluidInput, GasStackIngredient gasInput, GasStack gasOutput, FluidStack fluidOutput) {
        if (gasOutput.isEmpty() || fluidOutput.isEmpty()) {
            throw new IllegalArgumentException("This rotary condensentrator recipe requires non empty gas and fluid outputs.");
        }
        return new RotaryRecipeBuilder(fluidInput, gasInput, gasOutput, fluidOutput, RecipeDirection.BOTH);
    }

    @Override
    public RotaryRecipeResult getResult(ResourceLocation id) {
        return new RotaryRecipeResult(id, fluidInput, gasInput, gasOutput, fluidOutput, direction, advancementBuilder,
              new ResourceLocation(id.getNamespace(), "recipes/" + id.getPath()), recipeSerializer);
    }

    public static class RotaryRecipeResult extends RecipeResult<RotaryRecipe> {

        private final RecipeDirection direction;
        private final GasStackIngredient gasInput;
        private final FluidStackIngredient fluidInput;
        private final FluidStack fluidOutput;
        private final GasStack gasOutput;

        public RotaryRecipeResult(ResourceLocation id, FluidStackIngredient fluidInput, GasStackIngredient gasInput, GasStack gasOutput, FluidStack fluidOutput,
              RecipeDirection direction, Advancement.Builder advancementBuilder, ResourceLocation advancementId, IRecipeSerializer<RotaryRecipe> recipeSerializer) {
            super(id, advancementBuilder, advancementId, recipeSerializer);
            this.direction = direction;
            this.gasInput = gasInput;
            this.fluidInput = fluidInput;
            this.gasOutput = gasOutput;
            this.fluidOutput = fluidOutput;
        }

        @Override
        public void serialize(@Nonnull JsonObject json) {
            if (direction.hasFluidToGas) {
                json.add("fluidInput", fluidInput.serialize());
                json.add("gasOutput", SerializerHelper.serializeGasStack(gasOutput));
            }
            if (direction.hasGasToFluid) {
                json.add("gasInput", gasInput.serialize());
                json.add("fluidOutput", SerializerHelper.serializeFluidStack(fluidOutput));
            }
        }
    }

    private enum RecipeDirection {
        FLUID_TO_GAS(true, false),
        GAS_TO_FLUID(false, true),
        BOTH(true, true);

        private final boolean hasFluidToGas;
        private final boolean hasGasToFluid;

        RecipeDirection(boolean hasFluidToGas, boolean hasGasToFluid) {
            this.hasFluidToGas = hasFluidToGas;
            this.hasGasToFluid = hasGasToFluid;
        }
    }
}