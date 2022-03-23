package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonArray;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import com.google.gson.JsonObject;

public class DistillingRecipeBuilder extends MekanismRecipeBuilder<DistillingRecipeBuilder> {
    private final FluidStackIngredient input;
    private final List<FluidStack> outputs;

    protected DistillingRecipeBuilder(FluidStackIngredient input, List<FluidStack> outputs) {
        super(mekSerializer("distilling"));
        this.input = input;
        this.outputs = outputs;
    }

    /**
     * Creates a distilling recipe builder
     */
    public static DistillingRecipeBuilder distilling(FluidStackIngredient input, List<FluidStack> outputs) {
        Objects.requireNonNull(outputs, "Output fluid list cannot be null.");
        if (outputs.size() < 2) {
            throw new IllegalArgumentException("Output fluid list must be at least two long.");
        }
        outputs.forEach(outputFluid -> {
            Objects.requireNonNull(outputFluid, "Output fluid cannot be null.");
            if (outputFluid.isEmpty()) {
                throw new IllegalArgumentException("Output fluid cannot be empty.");
            }
        });
        return new DistillingRecipeBuilder(input, outputs);
    }

    @Override
    protected DistillingRecipeResult getResult(ResourceLocation id) {
        return new DistillingRecipeResult(id);
    }

    public class DistillingRecipeResult extends RecipeResult {
        protected DistillingRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serializeRecipeData(@Nonnull JsonObject json) {
            json.add(JsonConstants.INPUT, input.serialize());
            JsonArray outputJson = new JsonArray();
            outputs.forEach(output -> outputJson.add(SerializerHelper.serializeFluidStack(output)));
            json.add(JsonConstants.OUTPUT, outputJson);
        }
    }
}
