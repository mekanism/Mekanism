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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import mekanism.client.render.lib.Quad;
import mekanism.client.render.lib.QuadTransformation;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.client.model.IModelBuilder;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.SimpleUnbakedGeometry;
import org.jetbrains.annotations.NotNull;

public class MekanismModel extends SimpleUnbakedGeometry<MekanismModel> {

    private final Multimap<String, MekanismModelPart> elements;

    public MekanismModel(Multimap<String, MekanismModelPart> list) {
        this.elements = list;
    }

    @Override
    public void addQuads(IGeometryBakingContext owner, IModelBuilder<?> modelBuilder, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter,
          ModelState modelTransform, ResourceLocation modelLocation) {
        elements.values().stream().filter(part -> owner.isComponentVisible(part.name(), true))
              .forEach(part -> part.addQuads(owner, modelBuilder, bakery, spriteGetter, modelTransform, modelLocation));
    }

    @Override
    public Collection<Material> getMaterials(IGeometryBakingContext owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        Set<Material> combined = Sets.newHashSet();
        for (MekanismModelPart part : elements.values()) {
            combined.addAll(part.getMaterials(owner, modelGetter, missingTextureErrors));
        }
        return combined;
    }

    public static class Loader implements IGeometryLoader<MekanismModel> {

        public static final Loader INSTANCE = new Loader();

        protected Loader() {
        }

        @NotNull
        @Override
        public MekanismModel read(@NotNull JsonObject modelContents, @NotNull JsonDeserializationContext ctx) {
            return new MekanismModel(readElements(ctx, modelContents));
        }

        protected static Multimap<String, MekanismModelPart> readElements(@NotNull JsonDeserializationContext ctx, @NotNull JsonObject modelContents) {
            Multimap<String, MekanismModelPart> multimap = HashMultimap.create();
            if (modelContents.has("elements")) {
                for (JsonElement element : GsonHelper.getAsJsonArray(modelContents, "elements")) {
                    JsonObject obj = element.getAsJsonObject();
                    BlockElement part = ctx.deserialize(element, BlockElement.class);
                    String name = obj.has("name") ? obj.get("name").getAsString() : "undefined";
                    MekanismModelPart wrapper = new MekanismModelPart(name, part);
                    multimap.put(name, wrapper);

                    if (obj.has("faces")) {
                        JsonObject faces = obj.getAsJsonObject("faces");
                        faces.entrySet().forEach(e -> {
                            Direction side = Direction.byName(e.getKey());
                            if (side == null) {
                                return;
                            }
                            JsonObject face = e.getValue().getAsJsonObject();
                            if (face.has("lightLevel")) {
                                wrapper.light(side, face.get("lightLevel").getAsInt());
                            }
                        });
                    }
                }
            }
            return multimap;
        }
    }

    public static class MekanismModelPart {

        private final String name;
        private final BlockElement element;

        private final Object2IntMap<BlockElementFace> litFaceMap = new Object2IntOpenHashMap<>();

        public MekanismModelPart(String name, BlockElement element) {
            this.name = name;
            this.element = element;
        }

        public String name() {
            return name;
        }

        public void light(Direction side, int light) {
            litFaceMap.put(element.faces.get(side), light);
        }

        public void addQuads(IGeometryBakingContext owner, IModelBuilder<?> modelBuilder, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter,
              ModelState modelState, ResourceLocation modelLocation) {
            for (Map.Entry<Direction, BlockElementFace> entry : element.faces.entrySet()) {
                BlockElementFace face = entry.getValue();
                TextureAtlasSprite sprite = spriteGetter.apply(owner.getMaterial(face.texture));
                BakedQuad quad = BlockModel.bakeFace(element, face, sprite, entry.getKey(), modelState, modelLocation);
                if (litFaceMap.containsKey(face)) {
                    quad = new Quad(quad).transform(QuadTransformation.light(litFaceMap.getInt(face))).bake();
                }
                if (face.cullForDirection == null) {
                    modelBuilder.addUnculledFace(quad);
                } else {
                    modelBuilder.addCulledFace(modelState.getRotation().rotateTransform(face.cullForDirection), quad);
                }
            }
        }

        public Collection<Material> getMaterials(IGeometryBakingContext owner, Function<ResourceLocation, UnbakedModel> modelGetter,
              Set<Pair<String, String>> missingTextureErrors) {
            Set<Material> textures = Sets.newHashSet();
            for (BlockElementFace face : element.faces.values()) {
                Material texture = owner.getMaterial(face.texture);
                if (Objects.equals(texture.texture(), MissingTextureAtlasSprite.getLocation())) {
                    missingTextureErrors.add(Pair.of(face.texture, owner.getModelName()));
                }
                textures.add(texture);
            }
            return textures;
        }
    }
}