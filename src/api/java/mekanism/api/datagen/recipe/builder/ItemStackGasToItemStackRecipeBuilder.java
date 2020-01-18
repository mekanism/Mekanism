package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import net.minecraft.advancements.Advancement;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemStackGasToItemStackRecipeBuilder extends MekanismRecipeBuilder<ItemStackGasToItemStackRecipeBuilder> {

    private final ItemStackIngredient itemInput;
    private final GasStackIngredient gasInput;
    private final ItemStack output;

    protected ItemStackGasToItemStackRecipeBuilder(ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output, ResourceLocation serializerName) {
        super(serializerName);
        this.itemInput = itemInput;
        this.gasInput = gasInput;
        this.output = output;
    }

    public static ItemStackGasToItemStackRecipeBuilder compressing(ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This compressing recipe requires a non empty item output.");
        }
        return new ItemStackGasToItemStackRecipeBuilder(itemInput, gasInput, output, new ResourceLocation(MekanismAPI.MEKANISM_MODID, "compressing"));
    }

    public static ItemStackGasToItemStackRecipeBuilder purifying(ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This purifying recipe requires a non empty item output.");
        }
        return new ItemStackGasToItemStackRecipeBuilder(itemInput, gasInput, output, new ResourceLocation(MekanismAPI.MEKANISM_MODID, "purifying"));
    }

    public static ItemStackGasToItemStackRecipeBuilder injecting(ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This injecting recipe requires a non empty item output.");
        }
        return new ItemStackGasToItemStackRecipeBuilder(itemInput, gasInput, output, new ResourceLocation(MekanismAPI.MEKANISM_MODID, "injecting"));
    }

    @Override
    public ItemStackGasToItemStackRecipeResult getResult(ResourceLocation id) {
        return new ItemStackGasToItemStackRecipeResult(id, itemInput, gasInput, output, advancementBuilder,
              new ResourceLocation(id.getNamespace(), "recipes/" + output.getItem().getGroup().getPath() + "/" + id.getPath()), serializerName);
    }

    public static class ItemStackGasToItemStackRecipeResult extends RecipeResult {

        private final ItemStackIngredient itemInput;
        private final GasStackIngredient gasInput;
        private final ItemStack output;

        public ItemStackGasToItemStackRecipeResult(ResourceLocation id, ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output,
              Advancement.Builder advancementBuilder, ResourceLocation advancementId, ResourceLocation serializerName) {
            super(id, advancementBuilder, advancementId, serializerName);
            this.itemInput = itemInput;
            this.gasInput = gasInput;
            this.output = output;
        }

        @Override
        public void serialize(@Nonnull JsonObject json) {
            json.add("itemInput", itemInput.serialize());
            json.add("gasInput", gasInput.serialize());
            json.add("output", SerializerHelper.serializeItemStack(output));
        }
    }
}