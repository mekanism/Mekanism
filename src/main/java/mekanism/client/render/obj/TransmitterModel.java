package mekanism.client.render.obj;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import mekanism.api.text.EnumColor;
import mekanism.client.model.data.ModelProperties;
import mekanism.client.model.data.TransmitterModelData;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.transmitter.TileEntitySidedPipe.ConnectionType;
import mekanism.common.util.EnumUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.obj.OBJModel.Face;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.Pair;

public class TransmitterModel extends OBJBakedModelBase {

    //TODO: Move this into one place that is referenced by both this and ItemLayerWrapper
    // Copy from old CTM
    public static Map<TransformType, TRSRTransformation> transforms = ImmutableMap.<TransformType, TRSRTransformation>builder()
          .put(TransformType.GUI, get(0, 0, 0, 30, 225, 0, 0.625f))
          .put(TransformType.THIRD_PERSON_RIGHT_HAND, get(0, 2.5f, 0, 75, 45, 0, 0.375f))
          .put(TransformType.THIRD_PERSON_LEFT_HAND, get(0, 2.5f, 0, 75, 45, 0, 0.375f))
          .put(TransformType.FIRST_PERSON_RIGHT_HAND, get(0, 0, 0, 0, 45, 0, 0.4f))
          .put(TransformType.FIRST_PERSON_LEFT_HAND, get(0, 0, 0, 0, 225, 0, 0.4f))
          .put(TransformType.GROUND, get(0, 2, 0, 0, 0, 0, 0.25f))
          .put(TransformType.HEAD, get(0, 0, 0, 0, 0, 0, 1))
          .put(TransformType.FIXED, get(0, 0, 0, 0, 0, 0, 1))
          .put(TransformType.NONE, get(0, 0, 0, 0, 0, 0, 0))
          .build();
    private static Set<TransmitterModel> modelInstances = new HashSet<>();
    private static TextureAtlasSprite[] transporter_center = new TextureAtlasSprite[2];
    private static TextureAtlasSprite[] transporter_center_color = new TextureAtlasSprite[2];
    private static TextureAtlasSprite[] transporter_side = new TextureAtlasSprite[2];
    private static TextureAtlasSprite[] transporter_side_color = new TextureAtlasSprite[2];
    private Map<Integer, List<BakedQuad>> modelCache = new HashMap<>();
    private TransmitterModel itemCache;
    private IModelData tempModelData;
    @Nullable
    private EnumColor color;
    private TextureAtlasSprite particle;
    private TransmitterOverride override = new TransmitterOverride();

    public TransmitterModel(IBakedModel base, OBJModel model, IModelState state, VertexFormat format, ImmutableMap<String, TextureAtlasSprite> textures,
          Map<TransformType, Matrix4f> transform) {
        super(base, model, state, format, textures, transform);
        particle = textureMap.getOrDefault("None_Center", textureMap.getOrDefault("CentreMaterial", tempSprite));
        modelInstances.add(this);
    }

    private static TRSRTransformation get(float tx, float ty, float tz, float ax, float ay, float az, float s) {
        return new TRSRTransformation(new Vector3f(tx / 16, ty / 16, tz / 16), TRSRTransformation.quatFromXYZDegrees(new Vector3f(ax, ay, az)),
              new Vector3f(s, s, s), null);
    }

    public static void addIcons(TextureStitchEvent.Pre event) {
        event.addSprite(new ResourceLocation(Mekanism.MODID, "block/models/multipart/logistical_transporter_glass"));
        event.addSprite(new ResourceLocation(Mekanism.MODID, "block/models/multipart/logistical_transporter_glass_colored"));
        event.addSprite(new ResourceLocation(Mekanism.MODID, "block/models/multipart/logistical_transporter_vertical_glass"));
        event.addSprite(new ResourceLocation(Mekanism.MODID, "block/models/multipart/logistical_transporter_vertical_glass_colored"));

        event.addSprite(new ResourceLocation(Mekanism.MODID, "block/models/multipart/opaque/logistical_transporter_glass"));
        event.addSprite(new ResourceLocation(Mekanism.MODID, "block/models/multipart/opaque/logistical_transporter_glass_colored"));
        event.addSprite(new ResourceLocation(Mekanism.MODID, "block/models/multipart/opaque/logistical_transporter_vertical_glass"));
        event.addSprite(new ResourceLocation(Mekanism.MODID, "block/models/multipart/opaque/logistical_transporter_vertical_glass_colored"));
    }

    public static void getIcons(AtlasTexture map) {
        transporter_center[0] = map.getSprite(new ResourceLocation(Mekanism.MODID, "block/models/multipart/logistical_transporter_glass"));
        transporter_center_color[0] = map.getSprite(new ResourceLocation(Mekanism.MODID, "block/models/multipart/logistical_transporter_glass_colored"));
        transporter_side[0] = map.getSprite(new ResourceLocation(Mekanism.MODID, "block/models/multipart/logistical_transporter_vertical_glass"));
        transporter_side_color[0] = map.getSprite(new ResourceLocation(Mekanism.MODID, "block/models/multipart/logistical_transporter_vertical_glass_colored"));

        transporter_center[1] = map.getSprite(new ResourceLocation(Mekanism.MODID, "block/models/multipart/opaque/logistical_transporter_glass"));
        transporter_center_color[1] = map.getSprite(new ResourceLocation(Mekanism.MODID, "block/models/multipart/opaque/logistical_transporter_glass_colored"));
        transporter_side[1] = map.getSprite(new ResourceLocation(Mekanism.MODID, "block/models/multipart/opaque/logistical_transporter_vertical_glass"));
        transporter_side_color[1] = map.getSprite(new ResourceLocation(Mekanism.MODID, "block/models/multipart/opaque/logistical_transporter_vertical_glass_colored"));
    }

    public static void clearCache() {
        for (TransmitterModel model : modelInstances) {
            model.modelCache.clear();
            model.itemCache = null;
        }
    }

    @Nonnull
    @Override
    public ItemOverrideList getOverrides() {
        return override;
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        if (side != null) {
            return ImmutableList.of();
        }

        //TODO: Do we still need this state != null?
        if (state != null && tempModelData == null) {
            if (extraData.hasProperty(ModelProperties.DOWN_CONNECTION) && extraData.hasProperty(ModelProperties.UP_CONNECTION) &&
                extraData.hasProperty(ModelProperties.NORTH_CONNECTION) && extraData.hasProperty(ModelProperties.SOUTH_CONNECTION) &&
                extraData.hasProperty(ModelProperties.WEST_CONNECTION) && extraData.hasProperty(ModelProperties.EAST_CONNECTION)) {
                ConnectionType down = extraData.getData(ModelProperties.DOWN_CONNECTION);
                ConnectionType up = extraData.getData(ModelProperties.UP_CONNECTION);
                ConnectionType north = extraData.getData(ModelProperties.NORTH_CONNECTION);
                ConnectionType south = extraData.getData(ModelProperties.SOUTH_CONNECTION);
                ConnectionType west = extraData.getData(ModelProperties.WEST_CONNECTION);
                ConnectionType east = extraData.getData(ModelProperties.EAST_CONNECTION);

                BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
                EnumColor color = null;
                if (extraData.hasProperty(ModelProperties.COLOR) && layer == BlockRenderLayer.TRANSLUCENT) {
                    //Only try getting the color property for ones that will have a color
                    color = extraData.getData(ModelProperties.COLOR);
                }

                int hash = 1;
                hash = hash * 31 + layer.ordinal();
                hash = hash * 31 + down.ordinal();
                hash = hash * 31 + up.ordinal();
                hash = hash * 31 + north.ordinal();
                hash = hash * 31 + south.ordinal();
                hash = hash * 31 + west.ordinal();
                hash = hash * 31 + east.ordinal();
                if (color != null) {
                    hash = hash * 31 + color.ordinal();
                }

                if (!modelCache.containsKey(hash)) {
                    TransmitterModel model = new TransmitterModel(baseModel, getModel(), new OBJState(getVisibleGroups(down, up, north, south, west, east), true),
                          vertexFormat, textureMap, transformationMap);
                    model.tempModelData = extraData;
                    model.color = color;
                    modelCache.put(hash, model.getQuads(state, side, rand, extraData));
                }

                return modelCache.get(hash);
            }
            //TODO: print error about missing data?
        }
        return super.getQuads(state, side, rand, extraData);
    }

    public List<String> getVisibleGroups(ConnectionType down, ConnectionType up, ConnectionType north, ConnectionType south, ConnectionType west, ConnectionType east) {
        List<String> visible = new ArrayList<>();
        visible.add(Direction.DOWN.getName() + down.getName().toUpperCase());
        visible.add(Direction.UP.getName() + up.getName().toUpperCase());
        visible.add(Direction.NORTH.getName() + north.getName().toUpperCase());
        visible.add(Direction.SOUTH.getName() + south.getName().toUpperCase());
        visible.add(Direction.WEST.getName() + west.getName().toUpperCase());
        visible.add(Direction.EAST.getName() + east.getName().toUpperCase());
        return visible;
    }

    @Override
    public float[] getOverrideColor(Face f, String groupName) {
        if (tempModelData != null && color != null && MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.TRANSLUCENT) {
            return new float[]{color.getColor(0), color.getColor(1), color.getColor(2), 1};
        }
        return null;
    }

    @Nonnull
    @Override
    public TextureAtlasSprite getParticleTexture() {
        return particle;
    }

    @Override
    public TextureAtlasSprite getOverrideTexture(Face f, String groupName) {
        if (tempModelData != null) {
            boolean sideIconOverride = getIconStatus(f, tempModelData) > 0;

            if (MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.TRANSLUCENT) {
                int opaqueVal = MekanismConfig.client.opaqueTransmitters.get() ? 1 : 0;
                if (color != null) {
                    return (!sideIconOverride && f.getMaterialName().contains("Center")) ? transporter_center_color[opaqueVal] : transporter_side_color[opaqueVal];
                }
                return (!sideIconOverride && f.getMaterialName().contains("Center")) ? transporter_center[opaqueVal] : transporter_side[opaqueVal];
            } else if (groupName.endsWith("NONE") && sideIconOverride) {
                for (String s : getModel().getMatLib().getMaterialNames()) {
                    if (s.contains("Texture.Name")) {
                        continue;
                    }
                    if (!s.contains("Center") && !s.contains("Centre") && (MekanismConfig.client.opaqueTransmitters.get() == s.contains("Opaque"))) {
                        return textureMap.get(s);
                    }
                }
            } else if (MekanismConfig.client.opaqueTransmitters.get()) {
                return textureMap.get(f.getMaterialName() + "_Opaque");
            }
        }

        return null;
    }

    @Override
    public boolean shouldRotate(Face f, String groupName) {
        if (tempModelData != null) {
            return groupName.endsWith("NONE") && getIconStatus(f, tempModelData) == 2;
        }
        return false;
    }

    public byte getIconStatus(Face f, @Nonnull IModelData modelData) {
        return getIconStatus(Direction.getFacingFromVector(f.getNormal().x, f.getNormal().y, f.getNormal().z), modelData);
    }

    public byte getIconStatus(Direction side, @Nonnull IModelData modelData) {
        if (modelData instanceof TransmitterModelData.Diversion) {
            return (byte) 0;
        }
        boolean hasDown = modelData.getData(ModelProperties.DOWN_CONNECTION) != ConnectionType.NONE;
        boolean hasUp = modelData.getData(ModelProperties.UP_CONNECTION) != ConnectionType.NONE;
        boolean hasNorth = modelData.getData(ModelProperties.NORTH_CONNECTION) != ConnectionType.NONE;
        boolean hasSouth = modelData.getData(ModelProperties.SOUTH_CONNECTION) != ConnectionType.NONE;
        boolean hasWest = modelData.getData(ModelProperties.WEST_CONNECTION) != ConnectionType.NONE;
        boolean hasEast = modelData.getData(ModelProperties.EAST_CONNECTION) != ConnectionType.NONE;
        boolean hasConnection = false;
        if (side == Direction.DOWN) {
            hasConnection = hasDown;
        } else if (side == Direction.UP) {
            hasConnection = hasUp;
        } else if (side == Direction.NORTH) {
            hasConnection = hasNorth;
        } else if (side == Direction.SOUTH) {
            hasConnection = hasSouth;
        } else if (side == Direction.WEST) {
            hasConnection = hasWest;
        } else if (side == Direction.EAST) {
            hasConnection = hasEast;
        }
        if (!hasConnection) {
            if (hasDown && hasUp && side != Direction.DOWN && side != Direction.UP) {
                return (byte) 1;
            } else if (hasNorth && hasSouth && (side == Direction.DOWN || side == Direction.UP)) {
                return (byte) 1;
            } else if (hasNorth && hasSouth && (side == Direction.EAST || side == Direction.WEST)) {
                return (byte) 2;
            } else if (hasWest && hasEast && side != Direction.EAST && side != Direction.WEST) {
                return (byte) 2;
            }
        }
        return (byte) 0;
    }

    @Nonnull
    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        return Pair.of(this, transforms.get(cameraTransformType).getMatrixVec());
    }

    private class TransmitterOverride extends ItemOverrideList {

        @Nonnull
        @Override
        public IBakedModel getModelWithOverrides(@Nonnull IBakedModel originalModel, @Nonnull ItemStack stack, @Nullable World world, @Nullable LivingEntity entity) {
            if (itemCache == null) {
                List<String> visible = new ArrayList<>();
                for (Direction side : EnumUtils.DIRECTIONS) {
                    visible.add(side.getName() + (side.getAxis() == Axis.Y ? "NORMAL" : "NONE"));
                }
                itemCache = new TransmitterModel(baseModel, getModel(), new OBJState(visible, true), vertexFormat, textureMap, transformationMap);
            }
            return itemCache;
        }
    }
}