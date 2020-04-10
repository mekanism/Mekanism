package mekanism.common.recipe.builder;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MekDataShapedRecipeBuilder extends ExtendedShapedRecipeBuilder {

    private MekDataShapedRecipeBuilder(IItemProvider result, int count) {
        super(result, count);
    }

    public static MekDataShapedRecipeBuilder shapedRecipe(IItemProvider result) {
        return shapedRecipe(result, 1);
    }

    public static MekDataShapedRecipeBuilder shapedRecipe(IItemProvider result, int count) {
        return new MekDataShapedRecipeBuilder(result, count);
    }

    @Override
    public void build(Consumer<IFinishedRecipe> consumerIn, ResourceLocation id) {
        //Wrap the recipe into a MekDataRecipeResult so that we can give it the correct serializer to use
        super.build(recipe -> consumerIn.accept(new MekDataRecipeResult(recipe)), id);
    }

    private static class MekDataRecipeResult implements IFinishedRecipe {

        private final IFinishedRecipe internal;

        public MekDataRecipeResult(IFinishedRecipe internal) {
            this.internal = internal;
        }

        @Override
        public void serialize(JsonObject json) {
            internal.serialize(json);
        }

        @Override
        public ResourceLocation getID() {
            return internal.getID();
        }

        @Override
        public IRecipeSerializer<?> getSerializer() {
            return MekanismRecipeSerializers.MEK_DATA.getRecipeSerializer();
        }

        @Nullable
        @Override
        public JsonObject getAdvancementJson() {
            return internal.getAdvancementJson();
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementID() {
            return internal.getAdvancementID();
        }
    }
}