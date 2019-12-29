package mekanism.client.render.transmitter;

import java.util.HashMap;
import java.util.Map;
import mekanism.common.ColourRGBA;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.obj.OBJModel;

//TODO: 1.15
//@Mod.EventBusSubscriber(modid = Mekanism.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public abstract class RenderTransmitterBase<T extends TileEntityTransmitter<?, ?, ?>> extends TileEntityRenderer<T> {

    /* Credit to Eternal Energy */
    //TODO: 1.15
    //public static Function<ResourceLocation, TextureAtlasSprite> textureGetterFlipV = location -> DummyAtlasTextureFlipV.instance;
    private static OBJModel contentsModel;
    private static Map<String, IBakedModel> contentsMap = new HashMap<>();
    protected Minecraft minecraft = Minecraft.getInstance();

    public RenderTransmitterBase(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    //TODO: 1.15
    /*@SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        if (contentsModel == null) {
            try {
                //TODO: Is the obj model loading/creating contentsMap correctly or is it totally broken
                contentsModel = OBJLoader.INSTANCE.loadModel(MekanismUtils.getResource(ResourceType.MODEL, "transmitter_contents.obj"));
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
                    OBJState objState = new OBJState(Collections.singletonList(key), false);
                    //TODO: The texture flipper doesn't even seem to be used
                    modelParts.put(key, objModel.bake(bakery, textureGetterFlipV, new BasicState(objState, false), Attributes.DEFAULT_BAKED_FORMAT));
                }
            }
        }
        return modelParts;
    }*/

    public void renderTransparency(BufferBuilder renderer, TextureAtlasSprite icon, IBakedModel cc, ColourRGBA color, BlockState state, IModelData modelData) {
        //TODO: 1.15
        /*if (!renderer.isDrawing) {
            renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.field_227849_i_);
        }

        int argb = color.argb();
        //TODO: Is there a reason to be going over each side? Given we are getting a model for a specific side anyways
        for (Direction side : EnumUtils.DIRECTIONS) {
            for (BakedQuad quad : cc.getQuads(state, side, minecraft.world.getRandom(), modelData)) {
                renderQuad(renderer, icon, quad, argb);
            }
        }

        for (BakedQuad quad : cc.getQuads(state, null, minecraft.world.getRandom(), modelData)) {
            renderQuad(renderer, icon, quad, argb);
        }*/
    }

    //TODO: 1.15
    /*private void renderQuad(BufferBuilder renderer, TextureAtlasSprite icon, BakedQuad quad, int argb) {
        //TODO: Check if retextureQuad or MekanismRenderer#iconTransform is more efficient
        // If it is iconTransform, we have to figure out why it is broken
        // NOTE: It does not seem that the retexture quad method supports "luminosity" for example with lava
        quad = retextureQuad(quad, icon);
        //quad = MekanismRenderer.iconTransform(quad, icon);
        LightUtil.renderQuadColor(renderer, quad, argb);
    }

    private BakedQuad retextureQuad(BakedQuad quad, TextureAtlasSprite sprite) {
        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(quad.getFormat());

        builder.setQuadTint(quad.getTintIndex());
        builder.setQuadOrientation(quad.getFace());
        builder.setApplyDiffuseLighting(quad.shouldApplyDiffuseLighting());
        builder.setTexture(sprite);
        int[] vertexData = quad.getVertexData();
        putVertex(builder, quad.getFormat(), quad.getFace(), vertexData[0], vertexData[1], vertexData[2], sprite.getMinU(), sprite.getMaxV());
        putVertex(builder, quad.getFormat(), quad.getFace(), vertexData[7], vertexData[8], vertexData[9], sprite.getMinU(), sprite.getMinV());
        putVertex(builder, quad.getFormat(), quad.getFace(), vertexData[14], vertexData[15], vertexData[16], sprite.getMaxU(), sprite.getMinV());
        putVertex(builder, quad.getFormat(), quad.getFace(), vertexData[21], vertexData[22], vertexData[23], sprite.getMaxU(), sprite.getMaxV());
        return builder.build();
    }

    private void putVertex(IVertexConsumer consumer, VertexFormat format, Direction side, int xVertexData, int yVertexData, int zVertexData, float u, float v) {
        float x = Float.intBitsToFloat(xVertexData);
        float y = Float.intBitsToFloat(yVertexData);
        float z = Float.intBitsToFloat(zVertexData);
        // From ItemLayerModel#putVertex
        for (int e = 0; e < format.getElementCount(); e++) {
            switch (format.getElement(e).getUsage()) {
                case POSITION:
                    consumer.put(e, x, y, z, 1f);
                    break;
                case COLOR:
                    consumer.put(e, 1f, 1f, 1f, 1f);
                    break;
                case NORMAL:
                    float offX = (float) side.getXOffset();
                    float offY = (float) side.getYOffset();
                    float offZ = (float) side.getZOffset();
                    consumer.put(e, offX, offY, offZ, 0f);
                    break;
                case UV:
                    if (format.getElement(e).getIndex() == 0) {
                        consumer.put(e, u, v, 0f, 1f);
                        break;
                    }
                    // else fallthrough to default
                default:
                    consumer.put(e);
                    break;
            }
        }
    }

    public IBakedModel getModelForSide(TileEntityTransmitter<?, ?, ?> part, Direction side) {
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
    }*/
}