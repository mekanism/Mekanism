package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import java.util.List;
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
import net.minecraftforge.common.crafting.conditions.ICondition;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemStackToItemStackRecipeBuilder extends MekanismRecipeBuilder<ItemStackToItemStackRecipeBuilder> {

    private final ItemStackIngredient input;
    private final ItemStack output;

    protected ItemStackToItemStackRecipeBuilder(ItemStackIngredient input, ItemStack output, ResourceLocation serializerName) {
        super(serializerName);
        this.input = input;
        this.output = output;
    }

    public static ItemStackToItemStackRecipeBuilder crushing(ItemStackIngredient input, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This crushing recipe requires a non empty item output.");
        }
        return new ItemStackToItemStackRecipeBuilder(input, output, new ResourceLocation(MekanismAPI.MEKANISM_MODID, "crushing"));
    }

    public static ItemStackToItemStackRecipeBuilder enriching(ItemStackIngredient input, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This enriching recipe requires a non empty item output.");
        }
        return new ItemStackToItemStackRecipeBuilder(input, output, new ResourceLocation(MekanismAPI.MEKANISM_MODID, "enriching"));
    }

    public static ItemStackToItemStackRecipeBuilder smelting(ItemStackIngredient input, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This smelting recipe requires a non empty item output.");
        }
        return new ItemStackToItemStackRecipeBuilder(input, output, new ResourceLocation(MekanismAPI.MEKANISM_MODID, "smelting"));
    }

    @Override
    protected ItemStackToItemStackRecipeResult getResult(ResourceLocation id) {
        return new ItemStackToItemStackRecipeResult(id, input, output, conditions, advancementBuilder,
              new ResourceLocation(id.getNamespace(), "recipes/" + id.getPath()), serializerName);
    }

    public void build(Consumer<IFinishedRecipe> consumer) {
        build(consumer, output.getItem().getRegistryName());
    }

    public static class ItemStackToItemStackRecipeResult extends RecipeResult {

        private final ItemStackIngredient input;
        private final ItemStack output;

        public ItemStackToItemStackRecipeResult(ResourceLocation id, ItemStackIngredient input, ItemStack output, List<ICondition> conditions,
              Advancement.Builder advancementBuilder, ResourceLocation advancementId, ResourceLocation serializerName) {
            super(id, conditions, advancementBuilder, advancementId, serializerName);
            this.input = input;
            this.output = output;
        }

        @Override
        public void serialize(@Nonnull JsonObject json) {
            json.add("input", input.serialize());
            json.add("output", SerializerHelper.serializeItemStack(output));
        }
    }
}