package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class FluidToFluidRecipeBuilder extends MekanismRecipeBuilder<FluidToFluidRecipeBuilder> {

    private final FluidStackIngredient input;
    private final FluidStack output;

    protected FluidToFluidRecipeBuilder(FluidStackIngredient input, FluidStack output) {
        super(mekSerializer("evaporating"));
        this.input = input;
        this.output = output;
    }

    /**
     * Creates an Evaporating recipe builder.
     *
     * @param input  Input.
     * @param output Output.
     */
    public static FluidToFluidRecipeBuilder evaporating(FluidStackIngredient input, FluidStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This evaporating recipe requires a non empty fluid output.");
        }
        return new FluidToFluidRecipeBuilder(input, output);
    }

    @Override
    protected MekanismRecipeBuilder<FluidToFluidRecipeBuilder>.RecipeResult getResult(ResourceLocation id, Provider registries) {
        return new FluidToFluidRecipeResult(id, registries);
    }

    public class FluidToFluidRecipeResult extends RecipeResult {

        protected FluidToFluidRecipeResult(ResourceLocation id, Provider registries) {
            super(id, registries);
        }

        @Override
        public void serializeRecipeData(@NotNull JsonObject json) {
            json.add(JsonConstants.INPUT, input.serialize());
            json.add(JsonConstants.OUTPUT, SerializerHelper.serializeFluidStack(output));
        }
    }
}