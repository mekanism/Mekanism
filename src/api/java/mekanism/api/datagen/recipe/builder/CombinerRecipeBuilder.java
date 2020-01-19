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
import mekanism.api.recipes.inputs.ItemStackIngredient;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CombinerRecipeBuilder extends MekanismRecipeBuilder<CombinerRecipeBuilder> {

    private final ItemStackIngredient mainInput;
    private final ItemStackIngredient extraInput;
    private final ItemStack output;

    protected CombinerRecipeBuilder(ItemStackIngredient mainInput, ItemStackIngredient extraInput, ItemStack output) {
        super(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "combining"));
        this.mainInput = mainInput;
        this.extraInput = extraInput;
        this.output = output;
    }

    public static CombinerRecipeBuilder combining(ItemStackIngredient mainInput, ItemStackIngredient extraInput, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This combining recipe requires a non empty item output.");
        }
        return new CombinerRecipeBuilder(mainInput, extraInput, output);
    }

    @Override
    public CombinerRecipeResult getResult(ResourceLocation id) {
        return new CombinerRecipeResult(id, mainInput, extraInput, output, advancementBuilder,
              new ResourceLocation(id.getNamespace(), "recipes/" + output.getItem().getGroup().getPath() + "/" + id.getPath()), serializerName);
    }

    public void build(Consumer<IFinishedRecipe> consumer) {
        build(consumer, output.getItem().getRegistryName());
    }

    public static class CombinerRecipeResult extends RecipeResult {

        private final ItemStackIngredient mainInput;
        private final ItemStackIngredient extraInput;
        private final ItemStack output;

        public CombinerRecipeResult(ResourceLocation id, ItemStackIngredient mainInput, ItemStackIngredient extraInput, ItemStack output,
              Advancement.Builder advancementBuilder, ResourceLocation advancementId, ResourceLocation serializerName) {
            super(id, advancementBuilder, advancementId, serializerName);
            this.mainInput = mainInput;
            this.extraInput = extraInput;
            this.output = output;
        }

        @Override
        public void serialize(@Nonnull JsonObject json) {
            json.add("mainInput", mainInput.serialize());
            json.add("extraInput", extraInput.serialize());
            json.add("output", SerializerHelper.serializeItemStack(output));
        }
    }
}