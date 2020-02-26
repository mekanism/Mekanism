package mekanism.common.recipe.builder;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.common.registration.impl.IRecipeSerializerRegistryObject;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SpecialRecipeBuilder implements IFinishedRecipe {

    private final IRecipeSerializer<?> serializer;

    private SpecialRecipeBuilder(IRecipeSerializer<?> serializer) {
        this.serializer = serializer;
    }

    public static void build(Consumer<IFinishedRecipe> consumer, IRecipeSerializerRegistryObject<?> serializer) {
        build(consumer, serializer.getRecipeSerializer());
    }

    public static void build(Consumer<IFinishedRecipe> consumer, IRecipeSerializer<?> serializer) {
        consumer.accept(new SpecialRecipeBuilder(serializer));
    }

    @Nonnull
    @Override
    public IRecipeSerializer<?> getSerializer() {
        return serializer;
    }

    @Override
    public void serialize(JsonObject json) {
        //NO-OP
    }

    @Nonnull
    @Override
    public ResourceLocation getID() {
        return serializer.getRegistryName();
    }

    @Nullable
    @Override
    public JsonObject getAdvancementJson() {
        return null;
    }

    @Nullable
    @Override
    public ResourceLocation getAdvancementID() {
        return null;
    }
}