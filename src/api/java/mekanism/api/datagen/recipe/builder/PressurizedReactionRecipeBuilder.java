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
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PressurizedReactionRecipeBuilder extends MekanismRecipeBuilder<PressurizedReactionRecipeBuilder> {

    private final ItemStackIngredient inputSolid;
    private final FluidStackIngredient inputFluid;
    private final GasStackIngredient inputGas;
    private double energyRequired;
    private final int duration;
    private final ItemStack outputItem;
    private final GasStack outputGas;

    protected PressurizedReactionRecipeBuilder(ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, GasStackIngredient inputGas, int duration,
          ItemStack outputItem, GasStack outputGas) {
        super(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "reaction"));
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
        return new PressurizedReactionRecipeBuilder(inputSolid, inputFluid, inputGas, duration, outputItem, GasStack.EMPTY);
    }

    public static PressurizedReactionRecipeBuilder reaction(ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, GasStackIngredient inputGas, int duration,
          GasStack outputGas) {
        if (outputGas.isEmpty()) {
            throw new IllegalArgumentException("This reaction recipe requires a non empty output gas.");
        }
        return new PressurizedReactionRecipeBuilder(inputSolid, inputFluid, inputGas, duration, ItemStack.EMPTY, outputGas);
    }

    public static PressurizedReactionRecipeBuilder reaction(ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, GasStackIngredient inputGas, int duration,
          ItemStack outputItem, GasStack outputGas) {
        if (outputItem.isEmpty() || outputGas.isEmpty()) {
            throw new IllegalArgumentException("This reaction recipe requires non empty item and gas outputs.");
        }
        return new PressurizedReactionRecipeBuilder(inputSolid, inputFluid, inputGas, duration, outputItem, outputGas);
    }

    public PressurizedReactionRecipeBuilder energyRequired(double energyRequired) {
        if (energyRequired < 0) {
            throw new IllegalArgumentException("Required energy must be at least zero");
        }
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
            json.add("itemInput", inputSolid.serialize());
            json.add("fluidInput", inputFluid.serialize());
            json.add("gasInput", inputGas.serialize());
            if (energyRequired > 0) {
                //Only add energy required if it is not zero, as otherwise it will default to zero
                json.addProperty("energyRequired", energyRequired);
            }
            json.addProperty("duration", duration);
            if (!outputItem.isEmpty()) {
                json.add("itemOutput", SerializerHelper.serializeItemStack(outputItem));
            }
            if (!outputGas.isEmpty()) {
                json.add("gasOutput", SerializerHelper.serializeGasStack(outputGas));
            }
        }
    }
}