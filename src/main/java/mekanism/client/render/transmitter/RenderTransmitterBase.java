package mekanism.client.render.transmitter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.ColourRGBA;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.Attributes;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;
import net.minecraftforge.client.model.pipeline.LightUtil;
import org.lwjgl.opengl.GL11;

public abstract class RenderTransmitterBase<T extends TileEntityTransmitter> extends TileEntitySpecialRenderer<T> {

    /* Credit to Eternal Energy */
    public static Function<ResourceLocation, TextureAtlasSprite> textureGetterFlipV = location -> DummyAtlasTextureFlipV.instance;
    private static OBJModel contentsModel;
    private static Map<String, IBakedModel> contentsMap = new HashMap<>();
    protected Minecraft mc = Minecraft.getMinecraft();

    public RenderTransmitterBase() {
        if (contentsModel == null) {
            try {
                contentsModel = (OBJModel) OBJLoader.INSTANCE.loadModel(MekanismUtils.getResource(ResourceType.MODEL, "transmitter_contents.obj"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            contentsMap = buildModelMap(contentsModel);
        }
    }

    public static Map<String, IBakedModel> buildModelMap(OBJModel objModel) {
        Map<String, IBakedModel> modelParts = new HashMap<>();
        Set<String> keys = objModel.getMatLib().getGroups().keySet();
        if (!keys.isEmpty()) {
            for (String key : keys) {
                if (!modelParts.containsKey(key)) {
                    modelParts.put(key, objModel.bake(new OBJState(Collections.singletonList(key), false), Attributes.DEFAULT_BAKED_FORMAT, textureGetterFlipV));
                }
            }
        }
        return modelParts;
    }

    protected void push() {
        GlStateManager.pushMatrix();
        GlStateManager.enableCull();
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
    }

    protected void pop() {
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.disableCull();
        GlStateManager.popMatrix();
    }

    public void renderTransparency(BufferBuilder renderer, TextureAtlasSprite icon, IBakedModel cc, ColourRGBA color) {
        if (!renderer.isDrawing) {
            renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
        }

        for (EnumFacing side : EnumFacing.values()) {
            for (BakedQuad quad : cc.getQuads(null, side, 0)) {
                quad = MekanismRenderer.iconTransform(quad, icon);
                LightUtil.renderQuadColor(renderer, quad, color.argb());
            }
        }

        for (BakedQuad quad : cc.getQuads(null, null, 0)) {
            quad = MekanismRenderer.iconTransform(quad, icon);
            LightUtil.renderQuadColor(renderer, quad, color.argb());
        }
    }

    public IBakedModel getModelForSide(TileEntityTransmitter part, EnumFacing side) {
        String sideName = side.name().toLowerCase(Locale.ROOT);
        String typeName = part.getConnectionType(side).name().toUpperCase();
        String name = sideName + typeName;
        return contentsMap.get(name);
    }

    private static class DummyAtlasTextureFlipV extends TextureAtlasSprite {

        public static DummyAtlasTextureFlipV instance = new DummyAtlasTextureFlipV();

        protected DummyAtlasTextureFlipV() {
            super("dummyFlipV");
        }

        @Override
        public float getInterpolatedU(double u) {
            return (float) u / 16;
        }

        @Override
        public float getInterpolatedV(double v) {
            return (float) v / -16;
        }
    }
}