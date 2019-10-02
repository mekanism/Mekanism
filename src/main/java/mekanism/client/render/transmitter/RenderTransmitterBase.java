package mekanism.client.render.transmitter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.ColourRGBA;
import mekanism.common.Mekanism;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.ISprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.Attributes;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

@Mod.EventBusSubscriber(modid = Mekanism.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public abstract class RenderTransmitterBase<T extends TileEntityTransmitter> extends TileEntityRenderer<T> {

    /* Credit to Eternal Energy */
    public static Function<ResourceLocation, TextureAtlasSprite> textureGetterFlipV = location -> DummyAtlasTextureFlipV.instance;
    private static OBJModel contentsModel;
    private static Map<String, IBakedModel> contentsMap = new HashMap<>();
    protected Minecraft minecraft = Minecraft.getInstance();

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        if (contentsModel == null) {
            try {
                contentsModel = (OBJModel) OBJLoader.INSTANCE.loadModel(MekanismUtils.getResource(ResourceType.MODEL, "transmitter_contents.obj"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            contentsMap = buildModelMap(event.getModelLoader(), contentsModel);
        }
    }

    public static Map<String, IBakedModel> buildModelMap(ModelBakery bakery, OBJModel objModel) {
        Map<String, IBakedModel> modelParts = new HashMap<>();
        Set<String> keys = objModel.getMatLib().getGroups().keySet();
        if (!keys.isEmpty()) {
            for (String key : keys) {
                if (!modelParts.containsKey(key)) {
                    //TODO: Fix this if needed
                    OBJState objState = new OBJState(Collections.singletonList(key), false);
                    //TODO: The texture flipper doesn't even seem to be used
                    modelParts.put(key, objModel.bake(bakery, textureGetterFlipV, new ISprite() {
                        @Nonnull
                        @Override
                        public IModelState getState() {
                            return objState;
                        }
                    }, Attributes.DEFAULT_BAKED_FORMAT));
                }
            }
        }
        return modelParts;
    }

    public void renderTransparency(BufferBuilder renderer, TextureAtlasSprite icon, IBakedModel cc, ColourRGBA color, BlockState state, IModelData modelData) {
        if (!renderer.isDrawing) {
            renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
        }

        //TODO: Fix the rendering of contents
        int argb = color.argb();
        //TODO: Is there a reason to be going over each side? Given we are getting a model for a specific side anyways
        for (Direction side : Direction.values()) {
            for (BakedQuad quad : cc.getQuads(state, side, minecraft.world.getRandom(), modelData)) {
                quad = MekanismRenderer.iconTransform(quad, icon);
                LightUtil.renderQuadColor(renderer, quad, argb);
            }
        }

        for (BakedQuad quad : cc.getQuads(state, null, minecraft.world.getRandom(), modelData)) {
            quad = MekanismRenderer.iconTransform(quad, icon);
            LightUtil.renderQuadColor(renderer, quad, argb);
        }
    }

    public IBakedModel getModelForSide(TileEntityTransmitter part, Direction side) {
        return contentsMap.get(side.getName() + part.getConnectionType(side).getName().toUpperCase());
    }

    private static class DummyAtlasTextureFlipV extends TextureAtlasSprite {

        public static DummyAtlasTextureFlipV instance = new DummyAtlasTextureFlipV();

        protected DummyAtlasTextureFlipV() {
            super(new ResourceLocation(Mekanism.MODID, "dummy_flip_v"), 0, 0);
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