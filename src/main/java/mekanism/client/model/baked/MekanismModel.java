package mekanism.client.model.baked;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import mekanism.client.render.lib.Quad;
import mekanism.client.render.lib.QuadTransformation;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.client.renderer.model.BlockPart;
import net.minecraft.client.renderer.model.BlockPartFace;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelBuilder;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.geometry.ISimpleModelGeometry;

public class MekanismModel implements ISimpleModelGeometry<MekanismModel> {

    private final List<BlockPart> elements;
    private final Object2IntMap<BlockPartFace> litFaceMap;

    public MekanismModel(List<BlockPart> list, Object2IntMap<BlockPartFace> litFaceMap) {
        this.elements = list;
        this.litFaceMap = litFaceMap;
    }

    @Override
    public void addQuads(IModelConfiguration owner, IModelBuilder<?> modelBuilder, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform,
          ResourceLocation modelLocation) {
        for (BlockPart blockpart : elements) {
            for (Direction direction : blockpart.mapFaces.keySet()) {
                BlockPartFace face = blockpart.mapFaces.get(direction);
                TextureAtlasSprite sprite = spriteGetter.apply(owner.resolveTexture(face.texture));
                BakedQuad quad = BlockModel.makeBakedQuad(blockpart, face, sprite, direction, modelTransform, modelLocation);
                if (litFaceMap.containsKey(face)) {
                    quad = new Quad(quad).transform(QuadTransformation.light(litFaceMap.getInt(face) / 15F)).bake();
                }
                if (face.cullFace == null) {
                    modelBuilder.addGeneralQuad(quad);
                } else {
                    modelBuilder.addFaceQuad(modelTransform.getRotation().rotateTransform(face.cullFace), quad);
                }
            }
        }
    }

    @Override
    public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        Set<RenderMaterial> textures = Sets.newHashSet();

        for (BlockPart part : elements) {
            for (BlockPartFace face : part.mapFaces.values()) {
                RenderMaterial texture = owner.resolveTexture(face.texture);
                if (Objects.equals(texture.getTextureLocation(), MissingTextureSprite.getLocation())) {
                    missingTextureErrors.add(Pair.of(face.texture, owner.getModelName()));
                }

                textures.add(texture);
            }
        }

        return textures;
    }

    public static class Loader implements IModelLoader<MekanismModel> {

        public static final Loader INSTANCE = new Loader();

        private Loader() {}

        @Override
        public void onResourceManagerReload(IResourceManager resourceManager) {}

        @Override
        public MekanismModel read(JsonDeserializationContext ctx, JsonObject modelContents) {
            List<BlockPart> list = Lists.newArrayList();
            Object2IntMap<BlockPartFace> litFaceMap = new Object2IntOpenHashMap<>();
            if (modelContents.has("elements")) {
                for (JsonElement element : JSONUtils.getJsonArray(modelContents, "elements")) {
                    BlockPart part = ctx.deserialize(element, BlockPart.class);
                    list.add(part);

                    JsonObject obj = element.getAsJsonObject();
                    if (obj.has("faces")) {
                        JsonObject faces = obj.get("faces").getAsJsonObject();
                        faces.entrySet().forEach(e -> {
                            Direction side = Direction.byName(e.getKey());
                            if (side == null)
                                return;
                            JsonObject face = e.getValue().getAsJsonObject();
                            if (face.has("lightLevel")) {
                                int light = face.get("lightLevel").getAsInt();
                                litFaceMap.put(part.mapFaces.get(side), light);
                            }
                        });
                    }
                }
            }
            return new MekanismModel(list, litFaceMap);
        }
    }
}
