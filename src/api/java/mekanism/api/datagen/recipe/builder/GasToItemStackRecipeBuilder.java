package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.inputs.GasStackIngredient;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GasToItemStackRecipeBuilder extends MekanismRecipeBuilder<GasToItemStackRecipeBuilder> {

    private final GasStackIngredient input;
    private final ItemStack output;

    protected GasToItemStackRecipeBuilder(GasStackIngredient input, ItemStack output) {
        super(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "crystallizing"));
        this.input = input;
        this.output = output;
    }

    public static GasToItemStackRecipeBuilder crystallizing(GasStackIngredient input, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This crystallizing recipe requires a non empty item output.");
        }
        return new GasToItemStackRecipeBuilder(input, output);
    }

    @Override
    protected GasToItemStackRecipeResult getResult(ResourceLocation id) {
        return new GasToItemStackRecipeResult(id);
    }

    public void build(Consumer<IFinishedRecipe> consumer) {
        build(consumer, output.getItem().getRegistryName());
    }

    public class GasToItemStackRecipeResult extends RecipeResult {

        protected GasToItemStackRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serialize(@Nonnull JsonObject json) {
            json.add("input", input.serialize());
            json.add("output", SerializerHelper.serializeItemStack(output));
        }
    }
}