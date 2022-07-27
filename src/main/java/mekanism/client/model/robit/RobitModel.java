package mekanism.client.model.robit;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.client.model.ElementsModel;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import org.jetbrains.annotations.NotNull;

public class RobitModel extends ElementsModel {

    private RobitModel(List<BlockElement> elements) {
        super(elements);
    }

    @Override
    public BakedModel bake(IGeometryBakingContext owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform,
          ItemOverrides overrides, ResourceLocation modelLocation) {
        return new RobitBakedModel(super.bake(owner, bakery, spriteGetter, modelTransform, overrides, modelLocation));
    }

    @NotNull
    @Override
    public Collection<Material> getMaterials(@NotNull IGeometryBakingContext owner, @NotNull Function<ResourceLocation, UnbakedModel> modelGetter,
          @NotNull Set<Pair<String, String>> missingTextureErrors) {
        Collection<Material> textures = super.getMaterials(owner, modelGetter, missingTextureErrors);
        //Remove any missing errors where the texture in the file was #robit
        missingTextureErrors.removeIf(p -> p.getFirst().equals("#robit"));
        return textures;
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
