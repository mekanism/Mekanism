package mekanism.client.model.baked;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nonnull;
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
import net.minecraftforge.client.model.geometry.IModelGeometryPart;
import net.minecraftforge.client.model.geometry.IMultipartModelGeometry;

public class MekanismModel implements IMultipartModelGeometry<MekanismModel> {

    private final Multimap<String, BlockPartWrapper> elements;

    public MekanismModel(Multimap<String, BlockPartWrapper> list) {
        this.elements = list;
    }

    public static class Loader implements IModelLoader<MekanismModel> {

        public static final Loader INSTANCE = new Loader();

        private Loader() {
        }

        @Override
        public void onResourceManagerReload(@Nonnull IResourceManager resourceManager) {
        }

        @Nonnull
        @Override
        public MekanismModel read(@Nonnull JsonDeserializationContext ctx, JsonObject modelContents) {
            Multimap<String, BlockPartWrapper> multimap = HashMultimap.create();
            if (modelContents.has("elements")) {
                for (JsonElement element : JSONUtils.getJsonArray(modelContents, "elements")) {
                    JsonObject obj = element.getAsJsonObject();
                    BlockPart part = ctx.deserialize(element, BlockPart.class);
                    String name = obj.has("name") ? obj.get("name").getAsString() : "undefined";
                    BlockPartWrapper wrapper = new BlockPartWrapper(name, part);
                    multimap.put(name, wrapper);

                    if (obj.has("faces")) {
                        JsonObject faces = obj.get("faces").getAsJsonObject();
                        faces.entrySet().forEach(e -> {
                            Direction side = Direction.byName(e.getKey());
                            if (side == null) {
                                return;
                            }
                            JsonObject face = e.getValue().getAsJsonObject();
                            if (face.has("lightLevel")) {
                                int light = face.get("lightLevel").getAsInt();
                                wrapper.light(side, light);
                            }
                        });
                    }
                }
            }
            return new MekanismModel(multimap);
        }
    }

    public static class BlockPartWrapper implements IModelGeometryPart {

        private final String name;
        private final BlockPart blockPart;

        private final Object2IntMap<BlockPartFace> litFaceMap = new Object2IntOpenHashMap<>();

        public BlockPartWrapper(String name, BlockPart blockPart) {
            this.name = name;
            this.blockPart = blockPart;
        }

        public BlockPart getPart() {
            return blockPart;
        }

        @Override
        public String name() {
            return name;
        }

        public void light(Direction side, int light) {
            litFaceMap.put(blockPart.mapFaces.get(side), light);
        }

        @Override
        public void addQuads(IModelConfiguration owner, IModelBuilder<?> modelBuilder, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform,
              ResourceLocation modelLocation) {
            for (Direction direction : blockPart.mapFaces.keySet()) {
                BlockPartFace face = blockPart.mapFaces.get(direction);
                TextureAtlasSprite sprite = spriteGetter.apply(owner.resolveTexture(face.texture));
                BakedQuad quad = BlockModel.makeBakedQuad(blockPart, face, sprite, direction, modelTransform, modelLocation);
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

        @Override
        public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
            Set<RenderMaterial> textures = Sets.newHashSet();
            for (BlockPartFace face : blockPart.mapFaces.values()) {
                RenderMaterial texture = owner.resolveTexture(face.texture);
                if (Objects.equals(texture.getTextureLocation(), MissingTextureSprite.getLocation())) {
                    missingTextureErrors.add(Pair.of(face.texture, owner.getModelName()));
                }
                textures.add(texture);
            }
            return textures;
        }
    }

    @Override
    public Collection<BlockPartWrapper> getParts() {
        return elements.values();
    }

    @Override
    public Optional<BlockPartWrapper> getPart(String name) {
        return elements.get(name).stream().findFirst();
    }
}
