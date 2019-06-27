package mekanism.client.render.obj;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import mekanism.api.EnumColor;
import mekanism.client.render.GLSMHelper;
import mekanism.common.block.property.PropertyColor;
import mekanism.common.tile.TileEntityGlowPanel;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.obj.OBJModel.Face;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.apache.commons.lang3.tuple.Pair;

public class GlowPanelModel extends OBJBakedModelBase {

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
    private static Map<Integer, List<BakedQuad>> glowPanelCache = new HashMap<>();
    private static Map<Integer, GlowPanelModel> glowPanelItemCache = new HashMap<>();
    private IBlockState tempState;
    private ItemStack tempStack;
    private GlowPanelOverride override = new GlowPanelOverride();

    public GlowPanelModel(IBakedModel base, OBJModel model, IModelState state, VertexFormat format, ImmutableMap<String, TextureAtlasSprite> textures,
          Map<TransformType, Matrix4f> transform) {
        super(base, model, state, format, textures, transform);
    }

    public static void forceRebake() {
        glowPanelCache.clear();
        glowPanelItemCache.clear();
    }

    private static TRSRTransformation get(float tx, float ty, float tz, float ax, float ay, float az, float s) {
        return new TRSRTransformation(new Vector3f(tx / 16, ty / 16, tz / 16), TRSRTransformation.quatFromXYZDegrees(new Vector3f(ax, ay, az)),
              new Vector3f(s, s, s), null);
    }

    public EnumColor getColor() {
        if (tempStack != null && !tempStack.isEmpty()) {
            return EnumColor.DYES[tempStack.getItemDamage()];
        }

        if (tempState != null && ((IExtendedBlockState) tempState).getValue(PropertyColor.INSTANCE) != null) {
            return ((IExtendedBlockState) tempState).getValue(PropertyColor.INSTANCE).color;
        }

        return EnumColor.WHITE;
    }

    @Nonnull
    @Override
    public ItemOverrideList getOverrides() {
        return override;
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if (side != null) {
            return ImmutableList.of();
        }

        if (state != null && tempState == null) {
            int hash = TileEntityGlowPanel.hash((IExtendedBlockState) state);
            if (!glowPanelCache.containsKey(hash)) {
                GlowPanelModel model = new GlowPanelModel(baseModel, getModel(), getState(), vertexFormat, textureMap, transformationMap);
                model.tempState = state;
                glowPanelCache.put(hash, model.getQuads(state, side, rand));
            }
            return glowPanelCache.get(hash);
        }
        return super.getQuads(state, side, rand);
    }

    @Override
    public float[] getOverrideColor(Face f, String groupName) {
        if (groupName.equals("light")) {
            EnumColor c = getColor();
            return new float[]{c.getColor(0), c.getColor(1), c.getColor(2), 1};
        }
        return null;
    }

    @Nonnull
    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType transformType) {
        if (transformType == TransformType.GUI) {
            GLSMHelper.INSTANCE.rotateX(180, 1);
            ForgeHooksClient.multiplyCurrentGlMatrix(transforms.get(transformType).getMatrix());
            GLSMHelper.INSTANCE.translateXY(0.65F, 0.45F).rotateX(90, 1).scale(1.6F);
            return Pair.of(this, null);
        } else if (transformType == TransformType.FIRST_PERSON_RIGHT_HAND || transformType == TransformType.FIRST_PERSON_LEFT_HAND) {
            GLSMHelper.INSTANCE.translateY(0.2F);
        } else if (transformType == TransformType.THIRD_PERSON_RIGHT_HAND || transformType == TransformType.THIRD_PERSON_LEFT_HAND) {
            ForgeHooksClient.multiplyCurrentGlMatrix(transforms.get(transformType).getMatrix());
            GLSMHelper.INSTANCE.translateYZ(0.3F, 0.2F);
            return Pair.of(this, null);
        }
        return Pair.of(this, transforms.get(transformType).getMatrix());
    }

    private class GlowPanelOverride extends ItemOverrideList {

        public GlowPanelOverride() {
            super(new ArrayList<>());
        }

        @Nonnull
        @Override
        public IBakedModel handleItemState(@Nonnull IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
            if (glowPanelItemCache.containsKey(stack.getItemDamage())) {
                return glowPanelItemCache.get(stack.getItemDamage());
            }
            ImmutableMap.Builder<String, TextureAtlasSprite> builder = ImmutableMap.builder();
            builder.put(ModelLoader.White.LOCATION.toString(), ModelLoader.White.INSTANCE);
            TextureAtlasSprite missing = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(new ResourceLocation("missingno").toString());

            for (String s : getModel().getMatLib().getMaterialNames()) {
                builder.put(s, Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(getModel().getMatLib().getMaterial(s).getTexture().getTextureLocation().toString()));
            }

            builder.put("missingno", missing);
            GlowPanelModel bakedModel = new GlowPanelModel(baseModel, getModel(), getState(), vertexFormat, builder.build(), transformationMap);
            bakedModel.tempStack = stack;
            glowPanelItemCache.put(stack.getItemDamage(), bakedModel);
            return bakedModel;
        }
    }
}