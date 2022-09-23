package mekanism.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import mekanism.client.model.baked.ExtensionBakedModel.TransformedBakedModel;
import mekanism.client.render.lib.QuadTransformation;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.NotNull;

//TODO: Replace with a transform like the following once forge's transform system doesn't break the shape of the models:
// "transform": {
//    "origin": "center",
//    "rotation": [45, 0, 45]
// },
public class CustomRotationModel implements IUnbakedGeometry<CustomRotationModel> {

    private final QuadTransformation transformation;
    private final BlockModel model;

    private CustomRotationModel(BlockModel model, QuadTransformation transformation) {
        this.model = model;
        this.transformation = transformation;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform,
          ItemOverrides overrides, ResourceLocation modelLocation) {
        return new TransformedBakedModel<>(model.bake(bakery, model, spriteGetter, modelTransform, modelLocation, owner.isGui3d()), transformation);
    }

    @NotNull
    @Override
    public Collection<Material> getMaterials(@NotNull IGeometryBakingContext owner, @NotNull Function<ResourceLocation, UnbakedModel> modelGetter,
          @NotNull Set<Pair<String, String>> missingTextureErrors) {
        return model.getMaterials(modelGetter, missingTextureErrors);
    }

    public static class Loader implements IGeometryLoader<CustomRotationModel> {

        public static final Loader INSTANCE = new Loader();

        private Loader() {
        }

        @NotNull
        @Override
        public CustomRotationModel read(@NotNull JsonObject modelContents, @NotNull JsonDeserializationContext ctx) {
            if (!modelContents.has("model")) {
                throw new JsonParseException("A custom rotation model must have a \"model\" member.");
            }
            JsonArray rotation = GsonHelper.getAsJsonArray(modelContents, "rotation");
            if (rotation.size() != 3) {
                throw new JsonParseException("Rotation must have an x, y, and z rotation");
            }
            return new CustomRotationModel(ctx.deserialize(modelContents.get("model"), BlockModel.class),
                  QuadTransformation.rotate(rotation.get(0).getAsDouble(), rotation.get(1).getAsDouble(), rotation.get(2).getAsDouble()));
        }
    }
}
