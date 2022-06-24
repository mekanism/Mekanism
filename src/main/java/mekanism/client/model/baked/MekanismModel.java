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
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nonnull;
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
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
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

        protected Loader() {
        }

        @Override
        public void onResourceManagerReload(@Nonnull ResourceManager resourceManager) {
        }

        @Nonnull
        @Override
        public MekanismModel read(@Nonnull JsonDeserializationContext ctx, @Nonnull JsonObject modelContents) {
            return new MekanismModel(readElements(ctx, modelContents));
        }

        protected static Multimap<String, BlockPartWrapper> readElements(@Nonnull JsonDeserializationContext ctx, @Nonnull JsonObject modelContents) {
            Multimap<String, BlockPartWrapper> multimap = HashMultimap.create();
            if (modelContents.has("elements")) {
                for (JsonElement element : GsonHelper.getAsJsonArray(modelContents, "elements")) {
                    JsonObject obj = element.getAsJsonObject();
                    BlockElement part = ctx.deserialize(element, BlockElement.class);
                    String name = obj.has("name") ? obj.get("name").getAsString() : "undefined";
                    BlockPartWrapper wrapper = new BlockPartWrapper(name, part);
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
                                int light = face.get("lightLevel").getAsInt();
                                wrapper.light(side, light);
                            }
                        });
                    }
                }
            }
            return multimap;
        }
    }

    public static class BlockPartWrapper implements IModelGeometryPart {

        private final String name;
        private final BlockElement blockPart;

        private final Object2IntMap<BlockElementFace> litFaceMap = new Object2IntOpenHashMap<>();

        public BlockPartWrapper(String name, BlockElement blockPart) {
            this.name = name;
            this.blockPart = blockPart;
        }

        public BlockElement getPart() {
            return blockPart;
        }

        @Override
        public String name() {
            return name;
        }

        public void light(Direction side, int light) {
            litFaceMap.put(blockPart.faces.get(side), light);
        }

        @Override
        public void addQuads(IModelConfiguration owner, IModelBuilder<?> modelBuilder, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform,
              ResourceLocation modelLocation) {
            for (Map.Entry<Direction, BlockElementFace> entry : blockPart.faces.entrySet()) {
                BlockElementFace face = entry.getValue();
                TextureAtlasSprite sprite = spriteGetter.apply(owner.resolveTexture(face.texture));
                BakedQuad quad = BlockModel.makeBakedQuad(blockPart, face, sprite, entry.getKey(), modelTransform, modelLocation);
                if (litFaceMap.containsKey(face)) {
                    quad = new Quad(quad).transform(QuadTransformation.light(litFaceMap.getInt(face) / 15F)).bake();
                }
                if (face.cullForDirection == null) {
                    modelBuilder.addGeneralQuad(quad);
                } else {
                    modelBuilder.addFaceQuad(modelTransform.getRotation().rotateTransform(face.cullForDirection), quad);
                }
            }
        }

        @Override
        public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
            Set<Material> textures = Sets.newHashSet();
            for (BlockElementFace face : blockPart.faces.values()) {
                Material texture = owner.resolveTexture(face.texture);
                if (Objects.equals(texture.texture(), MissingTextureAtlasSprite.getLocation())) {
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
