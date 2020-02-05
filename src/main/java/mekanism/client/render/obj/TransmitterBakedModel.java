package mekanism.client.render.obj;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.ints.AbstractInt2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.client.model.data.ModelProperties;
import mekanism.common.tile.transmitter.TileEntitySidedPipe.ConnectionType;
import mekanism.common.util.EnumUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.IModelBuilder;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.geometry.IModelGeometryPart;
import net.minecraftforge.client.model.obj.OBJModel;

public class TransmitterBakedModel implements IBakedModel {

    private final OBJModel internal;
    @Nullable
    private final OBJModel glass;
    private final IModelConfiguration owner;
    private final ModelBakery bakery;
    private final Function<Material, TextureAtlasSprite> spriteGetter;
    private final IModelTransform modelTransform;
    private final ItemOverrideList overrides;
    private final ResourceLocation modelLocation;
    private final IBakedModel bakedVariant;

    private AbstractInt2ObjectMap<List<BakedQuad>> modelCache = new Int2ObjectOpenHashMap<>();

    public TransmitterBakedModel(OBJModel internal, @Nullable OBJModel glass, IModelConfiguration owner, ModelBakery bakery,
          Function<Material, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
        this.internal = internal;
        this.glass = glass;
        this.owner = owner;
        this.bakery = bakery;
        this.spriteGetter = spriteGetter;
        this.modelTransform = modelTransform;
        this.overrides = overrides;
        this.modelLocation = modelLocation;
        //We define our baked variant to be how the item is. As we should always have model data when we have a state
        List<String> visible = Arrays.stream(EnumUtils.DIRECTIONS).map(side -> side.getName() + (side.getAxis() == Axis.Y ? "NORMAL" : "NONE")).collect(Collectors.toList());
        bakedVariant = internal.bake(new VisibleModelConfiguration(owner, visible), bakery, spriteGetter, modelTransform, overrides, modelLocation);
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand) {
        return getQuads(state, side, rand, EmptyModelData.INSTANCE);
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        if (side != null) {
            return ImmutableList.of();
        }
        if (state != null) {
            if (extraData.hasProperty(ModelProperties.DOWN_CONNECTION) && extraData.hasProperty(ModelProperties.UP_CONNECTION) &&
                extraData.hasProperty(ModelProperties.NORTH_CONNECTION) && extraData.hasProperty(ModelProperties.SOUTH_CONNECTION) &&
                extraData.hasProperty(ModelProperties.WEST_CONNECTION) && extraData.hasProperty(ModelProperties.EAST_CONNECTION)) {
                ConnectionType down = extraData.getData(ModelProperties.DOWN_CONNECTION);
                ConnectionType up = extraData.getData(ModelProperties.UP_CONNECTION);
                ConnectionType north = extraData.getData(ModelProperties.NORTH_CONNECTION);
                ConnectionType south = extraData.getData(ModelProperties.SOUTH_CONNECTION);
                ConnectionType west = extraData.getData(ModelProperties.WEST_CONNECTION);
                ConnectionType east = extraData.getData(ModelProperties.EAST_CONNECTION);

                RenderType layer = MinecraftForgeClient.getRenderLayer();
                boolean hasColor = false;
                if (extraData.hasProperty(ModelProperties.COLOR) && layer == RenderType.translucent()) {
                    //Only try getting the color property for ones that will have a color
                    Boolean color = extraData.getData(ModelProperties.COLOR);
                    hasColor = color != null && color;
                }

                int hash = 1;
                hash = hash * 31 + down.ordinal();
                hash = hash * 31 + up.ordinal();
                hash = hash * 31 + north.ordinal();
                hash = hash * 31 + south.ordinal();
                hash = hash * 31 + west.ordinal();
                hash = hash * 31 + east.ordinal();
                if (hasColor) {
                    hash = hash * 31 + 1;
                }
                if (!modelCache.containsKey(hash)) {
                    List<String> visible = new ArrayList<>();
                    visible.add(Direction.DOWN.getName() + down.getName().toUpperCase());
                    visible.add(Direction.UP.getName() + up.getName().toUpperCase());
                    visible.add(Direction.NORTH.getName() + north.getName().toUpperCase());
                    visible.add(Direction.SOUTH.getName() + south.getName().toUpperCase());
                    visible.add(Direction.WEST.getName() + west.getName().toUpperCase());
                    visible.add(Direction.EAST.getName() + east.getName().toUpperCase());
                    List<BakedQuad> result = bake(new TransmitterModelConfiguration(owner, visible, extraData), hasColor).getQuads(state, side, rand, extraData);
                    modelCache.put(hash, result);
                    return result;
                }
                return modelCache.get(hash);
            }
            //TODO: print error about missing data?
        }
        return bakedVariant.getQuads(state, side, rand, extraData);
    }

    /**
     * Rotates the pieces that need rotating.
     */
    private IBakedModel bake(TransmitterModelConfiguration configuration, boolean hasColor) {
        TextureAtlasSprite particle = spriteGetter.apply(configuration.resolveTexture("particle"));
        IModelBuilder<?> builder = IModelBuilder.of(configuration, overrides, particle);
        addPartQuads(configuration, builder, internal);
        if (glass != null && hasColor && MinecraftForgeClient.getRenderLayer() == RenderType.translucent()) {
            addPartQuads(configuration, builder, glass);
        }
        return builder.build();
    }

    private void addPartQuads(TransmitterModelConfiguration configuration, IModelBuilder<?> builder, OBJModel glass) {
        for (IModelGeometryPart part : glass.getParts()) {
            if (configuration.getPartVisibility(part)) {
                String name = part.name();
                IModelTransform transform = modelTransform;
                if (name.endsWith("NONE")) {
                    Direction dir = directionForPiece(name);
                    //We should not have been able to get here if dir was null but check just in case
                    if (dir != null && configuration.shouldRotate(dir)) {
                        //If the part should be rotated, then we need to use a custom IModelTransform
                        transform = new TransmitterModelTransform(transform, dir);
                    }
                }
                part.addQuads(configuration, builder, bakery, spriteGetter, transform, modelLocation);
            }
        }
    }

    @Nullable
    private static Direction directionForPiece(@Nonnull String piece) {
        if (piece.startsWith("down")) {
            return Direction.DOWN;
        } else if (piece.startsWith("up")) {
            return Direction.UP;
        } else if (piece.startsWith("north")) {
            return Direction.NORTH;
        } else if (piece.startsWith("south")) {
            return Direction.SOUTH;
        } else if (piece.startsWith("east")) {
            return Direction.EAST;
        } else if (piece.startsWith("west")) {
            return Direction.WEST;
        }
        return null;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return bakedVariant.isAmbientOcclusion();
    }

    @Override
    public boolean isAmbientOcclusion(BlockState state) {
        return bakedVariant.isAmbientOcclusion(state);
    }

    @Override
    public boolean isGui3d() {
        return bakedVariant.isGui3d();
    }

    @Override
    public boolean func_230044_c_() {
        return bakedVariant.func_230044_c_();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return bakedVariant.isBuiltInRenderer();
    }

    @Nonnull
    @Override
    @Deprecated
    public TextureAtlasSprite getParticleTexture() {
        return bakedVariant.getParticleTexture();
    }

    @Override
    public TextureAtlasSprite getParticleTexture(@Nonnull IModelData data) {
        return bakedVariant.getParticleTexture(data);
    }

    @Override
    public boolean doesHandlePerspectives() {
        return bakedVariant.doesHandlePerspectives();
    }

    @Override
    public IBakedModel handlePerspective(TransformType cameraTransformType, MatrixStack mat) {
        return bakedVariant.handlePerspective(cameraTransformType, mat);
    }

    @Nonnull
    @Override
    public ItemOverrideList getOverrides() {
        return bakedVariant.getOverrides();
    }

    @Nonnull
    @Override
    public IModelData getModelData(@Nonnull ILightReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData) {
        return bakedVariant.getModelData(world, pos, state, tileData);
    }

    @Nonnull
    @Override
    @Deprecated
    public ItemCameraTransforms getItemCameraTransforms() {
        return bakedVariant.getItemCameraTransforms();
    }
}