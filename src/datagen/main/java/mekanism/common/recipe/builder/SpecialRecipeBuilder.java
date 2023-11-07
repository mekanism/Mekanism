package mekanism.common.recipe.builder;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.registration.impl.RecipeSerializerRegistryObject;
import mekanism.common.util.RegistryUtils;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class SpecialRecipeBuilder implements FinishedRecipe {

    private final RecipeSerializer<?> serializer;

    private SpecialRecipeBuilder(RecipeSerializer<?> serializer) {
        this.serializer = serializer;
    }

    public static void build(RecipeOutput consumer, RecipeSerializerRegistryObject<?> serializer) {
        build(consumer, serializer.get());
    }

    public static void build(RecipeOutput consumer, RecipeSerializer<?> serializer) {
        consumer.accept(new SpecialRecipeBuilder(serializer));
    }

    @Override
    public RecipeSerializer<?> type() {
        return serializer;
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
        //NO-OP
    }

    @Override
    public ResourceLocation id() {
        return RegistryUtils.getName(type());
    }

    @Nullable
    @Override
    public AdvancementHolder advancement() {
        return null;
    }
}