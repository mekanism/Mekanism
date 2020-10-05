package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RotaryRecipeBuilder extends MekanismRecipeBuilder<RotaryRecipeBuilder> {

    private static final GasStackIngredient EMPTY_GAS_INPUT = GasStackIngredient.from(GasStack.EMPTY);
    private static final FluidStackIngredient EMPTY_FLUID_INPUT = FluidStackIngredient.from(FluidStack.EMPTY);
    private final RecipeDirection direction;
    private final GasStackIngredient gasInput;
    private final FluidStackIngredient fluidInput;
    private final FluidStack fluidOutput;
    private final GasStack gasOutput;

    protected RotaryRecipeBuilder(FluidStackIngredient fluidInput, GasStackIngredient gasInput, GasStack gasOutput, FluidStack fluidOutput, RecipeDirection direction) {
        super(mekSerializer("rotary"));
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
    protected RotaryRecipeResult getResult(ResourceLocation id) {
        return new RotaryRecipeResult(id);
    }

    public class RotaryRecipeResult extends RecipeResult {

        protected RotaryRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serialize(@Nonnull JsonObject json) {
            if (direction.hasFluidToGas) {
                json.add(JsonConstants.FLUID_INPUT, fluidInput.serialize());
                json.add(JsonConstants.GAS_OUTPUT, SerializerHelper.serializeGasStack(gasOutput));
            }
            if (direction.hasGasToFluid) {
                json.add(JsonConstants.GAS_INPUT, gasInput.serialize());
                json.add(JsonConstants.FLUID_OUTPUT, SerializerHelper.serializeFluidStack(fluidOutput));
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