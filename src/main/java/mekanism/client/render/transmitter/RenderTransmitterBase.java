package mekanism.client.render.transmitter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.lib.Quad;
import mekanism.client.render.lib.QuadUtils;
import mekanism.client.render.lib.Vertex;
import mekanism.client.render.obj.ContentsModelConfiguration;
import mekanism.client.render.obj.VisibleModelConfiguration;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.data.EmptyModelData;

@ParametersAreNonnullByDefault
public abstract class RenderTransmitterBase<TRANSMITTER extends TileEntityTransmitter> extends MekanismTileEntityRenderer<TRANSMITTER> {

    public static final ResourceLocation MODEL_LOCATION = MekanismUtils.getResource(ResourceType.MODEL, "transmitter_contents.obj");
    private static final IModelConfiguration contentsConfiguration = new ContentsModelConfiguration();
    private static final Map<ContentsModelData, List<BakedQuad>> contentModelCache = new Object2ObjectOpenHashMap<>();
    private static final Vec3 NORMAL = new Vec3(1, 1, 1).normalize();

    public static void onStitch() {
        contentModelCache.clear();
    }

    private static List<BakedQuad> getBakedQuads(List<String> visible, TextureAtlasSprite icon, Level world) {
        return contentModelCache.computeIfAbsent(new ContentsModelData(visible, icon), modelData -> {
            //Note: We get model and then bake as we use different parameters and are caching after modifying
            List<BakedQuad> bakedQuads = MekanismModelCache.INSTANCE.TRANSMITTER_CONTENTS.getModel()
                  .bake(new VisibleModelConfiguration(contentsConfiguration, modelData.visible), ForgeModelBakery.instance(), material -> modelData.icon,
                        BlockModelRotation.X0_Y0, ItemOverrides.EMPTY, MODEL_LOCATION)
                  .getQuads(null, null, world.getRandom(), EmptyModelData.INSTANCE);
            List<Quad> unpackedQuads = QuadUtils.unpack(bakedQuads);
            for (Quad unpackedQuad : unpackedQuads) {
                for (Vertex vertex : unpackedQuad.getVertices()) {
                    //Set the normals to ones that ignore the diffuse light in the same way we do it in Render Resizable Cuboid
                    vertex.normal(NORMAL);
                }
            }
            return QuadUtils.bake(unpackedQuads);
        });
    }

    protected RenderTransmitterBase(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    protected void renderModel(TRANSMITTER transmitter, PoseStack matrix, VertexConsumer builder, int rgb, float alpha, int light, int overlayLight,
          TextureAtlasSprite icon) {
        renderModel(transmitter, matrix, builder, MekanismRenderer.getRed(rgb), MekanismRenderer.getGreen(rgb), MekanismRenderer.getBlue(rgb), alpha, light,
              overlayLight, icon, Arrays.stream(EnumUtils.DIRECTIONS)
                    .map(side -> side.getSerializedName() + transmitter.getTransmitter().getConnectionType(side).getSerializedName().toUpperCase(Locale.ROOT))
                    .toList());
    }

    protected void renderModel(TRANSMITTER transmitter, PoseStack matrix, VertexConsumer builder, float red, float green, float blue, float alpha, int light,
          int overlayLight, TextureAtlasSprite icon, List<String> visible) {
        if (!visible.isEmpty()) {
            Pose entry = matrix.last();
            //Get all the sides
            for (BakedQuad quad : getBakedQuads(visible, icon, transmitter.getLevel())) {
                builder.putBulkData(entry, quad, red, green, blue, alpha, light, overlayLight);
            }
        }
    }

    @Override
    public void render(TRANSMITTER transmitter, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight) {
        if (!MekanismConfig.client.opaqueTransmitters.get()) {
            super.render(transmitter, partialTick, matrix, renderer, light, overlayLight);
        }
    }

    private record ContentsModelData(List<String> visible, TextureAtlasSprite icon) {
    }
}