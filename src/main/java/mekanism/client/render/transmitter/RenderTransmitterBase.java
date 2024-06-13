package mekanism.client.render.transmitter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.lib.Quad;
import mekanism.client.render.lib.QuadUtils;
import mekanism.client.render.obj.VisibleModelConfiguration;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.StandaloneGeometryBakingContext;
import org.joml.Vector3f;

@NothingNullByDefault
public abstract class RenderTransmitterBase<TRANSMITTER extends TileEntityTransmitter> extends MekanismTileEntityRenderer<TRANSMITTER> {

    public static final ResourceLocation MODEL_LOCATION = MekanismUtils.getResource(ResourceType.MODEL, "transmitter_contents.obj");
    private static final ModelResourceLocation MODEL_VARIANT = ModelResourceLocation.standalone(MODEL_LOCATION);
    private static final IGeometryBakingContext contentsConfiguration = StandaloneGeometryBakingContext.builder()
          .withGui3d(false)
          .withUseBlockLight(false)
          .withUseAmbientOcclusion(false)
          .build(Mekanism.rl("transmitter_contents"));
    private static final Map<ContentsModelData, List<BakedQuad>> contentModelCache = new Object2ObjectOpenHashMap<>();
    private static final Vector3f NORMAL = new Vector3f(1, 1, 1).normalize();

    public static void onStitch() {
        contentModelCache.clear();
    }

    private static List<BakedQuad> getBakedQuads(List<String> visible, TextureAtlasSprite icon, Level world) {
        ContentsModelData modelData = new ContentsModelData(visible, icon);
        List<BakedQuad> modelQuads = contentModelCache.get(modelData);
        if (modelQuads == null) {
            ModelBaker baker = Minecraft.getInstance().getModelManager().getModelBakery().new ModelBakerImpl(
                  (modelLoc, material) -> material.sprite(),
                  MODEL_VARIANT
            );
            //Note: We get model and then bake as we use different parameters and are caching after modifying
            List<BakedQuad> bakedQuads = MekanismModelCache.INSTANCE.TRANSMITTER_CONTENTS.getModel()
                  .bake(new VisibleModelConfiguration(contentsConfiguration, modelData.visible), baker, material -> modelData.icon,
                        BlockModelRotation.X0_Y0, ItemOverrides.EMPTY)
                  .getQuads(null, null, world.getRandom(), ModelData.EMPTY, null);
            List<Quad> unpackedQuads = QuadUtils.unpack(bakedQuads);
            for (Quad unpackedQuad : unpackedQuads) {
                //Set the normals to ones that ignore the diffuse light in the same way we do it in Render Resizable Cuboid
                unpackedQuad.vertexTransform(vertex -> vertex.normal(NORMAL));
            }
            modelQuads = QuadUtils.bake(unpackedQuads);
            contentModelCache.put(modelData, modelQuads);
        }
        return modelQuads;
    }

    protected RenderTransmitterBase(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    protected void renderModel(TRANSMITTER transmitter, PoseStack matrix, VertexConsumer builder, int rgb, float alpha, int light, int overlayLight,
          TextureAtlasSprite icon) {
        List<String> list = new ArrayList<>(EnumUtils.DIRECTIONS.length);
        for (Direction side : EnumUtils.DIRECTIONS) {
            list.add(side.getSerializedName() + transmitter.getTransmitter().getConnectionType(side).name());
        }
        renderModel(transmitter, matrix, builder, MekanismRenderer.getRed(rgb), MekanismRenderer.getGreen(rgb), MekanismRenderer.getBlue(rgb), alpha, light,
              overlayLight, icon, list);
    }

    protected void renderModel(TRANSMITTER transmitter, PoseStack matrix, VertexConsumer builder, float red, float green, float blue, float alpha, int light,
          int overlayLight, TextureAtlasSprite icon, List<String> visible) {
        if (!visible.isEmpty()) {
            Pose entry = matrix.last();
            //Get all the sides
            for (BakedQuad quad : getBakedQuads(visible, icon, transmitter.getLevel())) {
                builder.putBulkData(entry, quad, red, green, blue, alpha, light, overlayLight, false);
            }
        }
    }

    @Override
    public boolean shouldRender(TRANSMITTER tile, Vec3 camera) {
        return shouldRenderTransmitter(tile, camera) && super.shouldRender(tile, camera);
    }

    protected boolean shouldRenderTransmitter(TRANSMITTER tile, Vec3 camera) {
        return !MekanismConfig.client.opaqueTransmitters.get();
    }

    private record ContentsModelData(List<String> visible, TextureAtlasSprite icon) {
    }
}