package mekanism.client.model.energycube;

import com.mojang.datafixers.util.Pair;
import com.mojang.math.Transformation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import mekanism.api.RelativeSide;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.client.render.lib.QuadUtils;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.RenderTypeGroup;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.Nullable;

public class EnergyCubeGeometry implements IUnbakedGeometry<EnergyCubeGeometry> {

    private final List<BlockElement> frame;
    private final Map<RelativeSide, List<BlockElement>> leds;
    private final Map<RelativeSide, List<BlockElement>> ports;

    EnergyCubeGeometry(List<BlockElement> frame, Map<RelativeSide, List<BlockElement>> leds, Map<RelativeSide, List<BlockElement>> ports) {
        this.frame = frame;
        this.leds = leds;
        this.ports = ports;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
          ItemOverrides overrides, ResourceLocation modelLocation) {
        TextureAtlasSprite particle = spriteGetter.apply(context.getMaterial("particle"));

        ResourceLocation renderTypeHint = context.getRenderTypeHint();
        RenderTypeGroup renderTypes = renderTypeHint == null ? RenderTypeGroup.EMPTY : context.getRenderType(renderTypeHint);

        Transformation rootTransform = context.getRootTransform();
        if (!rootTransform.isIdentity()) {
            modelState = new SimpleModelState(modelState.getRotation().compose(rootTransform), modelState.isUvLocked());
        }
        Function<String, TextureAtlasSprite> rawSpriteGetter = spriteGetter.compose(context::getMaterial);
        FaceData frame = bakeElement(rawSpriteGetter, modelState, modelLocation, this.frame);
        Map<RelativeSide, FaceData> leds = bakeElements(rawSpriteGetter, modelState, modelLocation, this.leds);
        Map<RelativeSide, FaceData> ports = bakeElements(rawSpriteGetter, modelState, modelLocation, this.ports);
        return new EnergyCubeBakedModel(context.useAmbientOcclusion(), context.useBlockLight(), context.isGui3d(), context.getTransforms(), overrides, particle, frame, leds, ports,
              renderTypes);
    }

    private Map<RelativeSide, FaceData> bakeElements(Function<String, TextureAtlasSprite> spriteGetter, ModelState modelState,
          ResourceLocation modelLocation, Map<RelativeSide, List<BlockElement>> sideBasedElements) {
        Map<RelativeSide, FaceData> sideBasedFaceData = new EnumMap<>(RelativeSide.class);
        for (Map.Entry<RelativeSide, List<BlockElement>> entry : sideBasedElements.entrySet()) {
            FaceData faceData = bakeElement(spriteGetter, modelState, modelLocation, entry.getValue());
            sideBasedFaceData.put(entry.getKey(), faceData);
        }
        return sideBasedFaceData;
    }

    private FaceData bakeElement(Function<String, TextureAtlasSprite> spriteGetter, ModelState modelState, ResourceLocation modelLocation, List<BlockElement> elements) {
        FaceData data = new FaceData();
        for (BlockElement element : elements) {
            for (Entry<Direction, BlockElementFace> faceEntry : element.faces.entrySet()) {
                BlockElementFace face = faceEntry.getValue();
                TextureAtlasSprite sprite = spriteGetter.apply(face.texture);
                //noinspection ConstantConditions (can be null)
                Direction direction = face.cullForDirection == null ? null : modelState.getRotation().rotateTransform(face.cullForDirection);
                data.addFace(direction, BlockModel.bakeFace(element, face, sprite, faceEntry.getKey(), modelState, modelLocation));
            }
        }
        return data;
    }

    @Override
    public Collection<Material> getMaterials(IGeometryBakingContext context, Function<ResourceLocation, UnbakedModel> modelGetter,
          Set<Pair<String, String>> missingTextureErrors) {
        Set<Material> textures = new HashSet<>();
        if (context.hasMaterial("particle")) {
            textures.add(context.getMaterial("particle"));
        }
        addMaterials(context, missingTextureErrors, frame, textures);
        for (List<BlockElement> elements : leds.values()) {
            addMaterials(context, missingTextureErrors, elements, textures);
        }
        for (List<BlockElement> elements : ports.values()) {
            addMaterials(context, missingTextureErrors, elements, textures);
        }
        return textures;
    }

    private void addMaterials(IGeometryBakingContext context, Set<Pair<String, String>> missingTextureErrors, List<BlockElement> elements, Set<Material> textures) {
        for (BlockElement part : elements) {
            for (BlockElementFace face : part.faces.values()) {
                Material texture = context.getMaterial(face.texture);
                if (texture.texture().equals(MissingTextureAtlasSprite.getLocation())) {
                    missingTextureErrors.add(Pair.of(face.texture, context.getModelName()));
                }
                textures.add(texture);
            }
        }
    }

    static class FaceData {

        private List<BakedQuad> unculledFaces;
        private Map<Direction, List<BakedQuad>> culledFaces;

        public List<BakedQuad> getFaces(@Nullable Direction side) {
            if (side == null) {
                return unculledFaces == null ? Collections.emptyList() : unculledFaces;
            }
            return culledFaces == null ? Collections.emptyList() : culledFaces.getOrDefault(side, Collections.emptyList());
        }

        public void addFace(@Nullable Direction direction, BakedQuad quad) {
            List<BakedQuad> quads;
            if (direction == null) {
                if (unculledFaces == null) {
                    unculledFaces = new ArrayList<>();
                }
                quads = unculledFaces;
            } else {
                if (culledFaces == null) {
                    culledFaces = new EnumMap<>(Direction.class);
                }
                quads = culledFaces.computeIfAbsent(direction, dir -> new ArrayList<>());
            }
            quads.add(quad);
        }

        public FaceData transform(QuadTransformation transformation) {
            if (unculledFaces == null && culledFaces == null) {
                return this;
            }
            FaceData transformed = new FaceData();
            if (unculledFaces != null) {
                transformed.unculledFaces = QuadUtils.transformBakedQuads(unculledFaces, transformation);
            }
            if (culledFaces != null) {
                transformed.culledFaces = new EnumMap<>(Direction.class);
                for (Map.Entry<Direction, List<BakedQuad>> entry : culledFaces.entrySet()) {
                    transformed.culledFaces.put(entry.getKey(), QuadUtils.transformBakedQuads(entry.getValue(), transformation));
                }
            }
            return transformed;
        }
    }
}