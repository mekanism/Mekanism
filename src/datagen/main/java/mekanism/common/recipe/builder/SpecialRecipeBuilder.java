package mekanism.common.recipe.builder;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.common.registration.impl.RecipeSerializerRegistryObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SpecialRecipeBuilder implements FinishedRecipe {

    private final RecipeSerializer<?> serializer;

    private SpecialRecipeBuilder(RecipeSerializer<?> serializer) {
        this.serializer = serializer;
    }

    public static void build(Consumer<FinishedRecipe> consumer, RecipeSerializerRegistryObject<?> serializer) {
        build(consumer, serializer.get());
    }

    public static void build(Consumer<FinishedRecipe> consumer, RecipeSerializer<?> serializer) {
        consumer.accept(new SpecialRecipeBuilder(serializer));
    }

    @Nonnull
    @Override
    public RecipeSerializer<?> getType() {
        return serializer;
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
        //NO-OP
    }

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return serializer.getRegistryName();
    }

    @Nullable
    @Override
    public JsonObject serializeAdvancement() {
        return null;
    }

    @Nullable
    @Override
    public ResourceLocation getAdvancementId() {
        return null;
    }
}