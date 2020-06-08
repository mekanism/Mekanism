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
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PressurizedReactionRecipeBuilder extends MekanismRecipeBuilder<PressurizedReactionRecipeBuilder> {

    private final ItemStackIngredient inputSolid;
    private final FluidStackIngredient inputFluid;
    private final GasStackIngredient inputGas;
    private FloatingLong energyRequired = FloatingLong.ZERO;
    private final int duration;
    private final ItemStack outputItem;
    private final GasStack outputGas;

    protected PressurizedReactionRecipeBuilder(ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, GasStackIngredient inputGas, int duration,
          ItemStack outputItem, GasStack outputGas) {
        super(mekSerializer("reaction"));
        this.inputSolid = inputSolid;
        this.inputFluid = inputFluid;
        this.inputGas = inputGas;
        this.duration = duration;
        this.outputItem = outputItem;
        this.outputGas = outputGas;
    }

    public static PressurizedReactionRecipeBuilder reaction(ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, GasStackIngredient inputGas,
          int duration, ItemStack outputItem) {
        if (outputItem.isEmpty()) {
            throw new IllegalArgumentException("This reaction recipe requires a non empty output item.");
        }
        validateDuration(duration);
        return new PressurizedReactionRecipeBuilder(inputSolid, inputFluid, inputGas, duration, outputItem, GasStack.EMPTY);
    }

    public static PressurizedReactionRecipeBuilder reaction(ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, GasStackIngredient inputGas, int duration,
          GasStack outputGas) {
        if (outputGas.isEmpty()) {
            throw new IllegalArgumentException("This reaction recipe requires a non empty output gas.");
        }
        validateDuration(duration);
        return new PressurizedReactionRecipeBuilder(inputSolid, inputFluid, inputGas, duration, ItemStack.EMPTY, outputGas);
    }

    public static PressurizedReactionRecipeBuilder reaction(ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, GasStackIngredient inputGas, int duration,
          ItemStack outputItem, GasStack outputGas) {
        if (outputItem.isEmpty() || outputGas.isEmpty()) {
            throw new IllegalArgumentException("This reaction recipe requires non empty item and gas outputs.");
        }
        validateDuration(duration);
        return new PressurizedReactionRecipeBuilder(inputSolid, inputFluid, inputGas, duration, outputItem, outputGas);
    }

    private static void validateDuration(int duration) {
        if (duration <= 0) {
            throw new IllegalArgumentException("This reaction recipe must have a positive duration.");
        }
    }

    public PressurizedReactionRecipeBuilder energyRequired(FloatingLong energyRequired) {
        this.energyRequired = energyRequired;
        return this;
    }

    @Override
    protected PressurizedReactionRecipeResult getResult(ResourceLocation id) {
        return new PressurizedReactionRecipeResult(id);
    }

    public class PressurizedReactionRecipeResult extends RecipeResult {

        protected PressurizedReactionRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serialize(@Nonnull JsonObject json) {
            json.add(JsonConstants.ITEM_INPUT, inputSolid.serialize());
            json.add(JsonConstants.FLUID_INPUT, inputFluid.serialize());
            json.add(JsonConstants.GAS_INPUT, inputGas.serialize());
            if (!energyRequired.isZero()) {
                //Only add energy required if it is not zero, as otherwise it will default to zero
                json.addProperty(JsonConstants.ENERGY_REQUIRED, energyRequired);
            }
            json.addProperty(JsonConstants.DURATION, duration);
            if (!outputItem.isEmpty()) {
                json.add(JsonConstants.ITEM_OUTPUT, SerializerHelper.serializeItemStack(outputItem));
            }
            if (!outputGas.isEmpty()) {
                json.add(JsonConstants.GAS_OUTPUT, SerializerHelper.serializeGasStack(outputGas));
            }
        }
    }
}