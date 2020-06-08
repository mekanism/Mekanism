package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.chemical.SlurryStackIngredient;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidSlurryToSlurryRecipeBuilder extends MekanismRecipeBuilder<FluidSlurryToSlurryRecipeBuilder> {

    private final SlurryStackIngredient slurryInput;
    private final FluidStackIngredient fluidInput;
    private final SlurryStack output;

    protected FluidSlurryToSlurryRecipeBuilder(FluidStackIngredient fluidInput, SlurryStackIngredient slurryInput, SlurryStack output) {
        super(mekSerializer("washing"));
        this.fluidInput = fluidInput;
        this.slurryInput = slurryInput;
        this.output = output;
    }

    public static FluidSlurryToSlurryRecipeBuilder washing(FluidStackIngredient fluidInput, SlurryStackIngredient slurryInput, SlurryStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This washing recipe requires a non empty slurry output.");
        }
        return new FluidSlurryToSlurryRecipeBuilder(fluidInput, slurryInput, output);
    }

    @Override
    protected FluidSlurryToSlurryRecipeResult getResult(ResourceLocation id) {
        return new FluidSlurryToSlurryRecipeResult(id);
    }

    public class FluidSlurryToSlurryRecipeResult extends RecipeResult {

        protected FluidSlurryToSlurryRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serialize(@Nonnull JsonObject json) {
            json.add(JsonConstants.FLUID_INPUT, fluidInput.serialize());
            json.add(JsonConstants.SLURRY_INPUT, slurryInput.serialize());
            json.add(JsonConstants.OUTPUT, SerializerHelper.serializeSlurryStack(output));
        }
    }
}