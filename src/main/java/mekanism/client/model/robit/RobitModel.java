package mekanism.client.model.robit;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.model.ElementsModel;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import org.jetbrains.annotations.NotNull;

public class RobitModel extends ElementsModel {

    private RobitModel(List<BlockElement> elements) {
        super(elements);
    }

    @Override
    public BakedModel bake(IGeometryBakingContext owner, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform,
          ItemOverrides overrides) {
        return new RobitBakedModel(super.bake(owner, baker, spriteGetter, modelTransform, overrides));
    }

    /**
     * Mekanism model loader that gets automatically wrapped into a robit baked model
     */
    public static class Loader implements IGeometryLoader<ElementsModel> {

        public static final Loader INSTANCE = new Loader();

        private Loader() {
        }

        @NotNull
        @Override
        public RobitModel read(@NotNull JsonObject modelContents, @NotNull JsonDeserializationContext ctx) {
            if (!modelContents.has("elements")) {
                throw new JsonParseException("An element model must have an \"elements\" member.");
            }
            List<BlockElement> elements = new ArrayList<>();
            for (JsonElement element : GsonHelper.getAsJsonArray(modelContents, "elements")) {
                elements.add(ctx.deserialize(element, BlockElement.class));
            }
            return new RobitModel(elements);
        }
    }
}
