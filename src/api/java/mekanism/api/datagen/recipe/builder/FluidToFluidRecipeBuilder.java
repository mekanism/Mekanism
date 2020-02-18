package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidToFluidRecipeBuilder extends MekanismRecipeBuilder<FluidToFluidRecipeBuilder> {

    private final FluidStackIngredient input;
    private final FluidStack output;

    protected FluidToFluidRecipeBuilder(FluidStackIngredient input, FluidStack output) {
        super(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "evaporating"));
        this.input = input;
        this.output = output;
    }

    public static FluidToFluidRecipeBuilder evaporating(FluidStackIngredient input, FluidStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This evaporating recipe requires a non empty fluid output.");
        }
        return new FluidToFluidRecipeBuilder(input, output);
    }

    @Override
    protected FluidToFluidRecipeResult getResult(ResourceLocation id) {
        return new FluidToFluidRecipeResult(id);
    }

    public class FluidToFluidRecipeResult extends RecipeResult {

        protected FluidToFluidRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serialize(@Nonnull JsonObject json) {
            json.add("input", input.serialize());
            json.add("output", SerializerHelper.serializeFluidStack(output));
        }
    }
}