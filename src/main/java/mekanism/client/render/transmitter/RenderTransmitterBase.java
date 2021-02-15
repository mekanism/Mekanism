package mekanism.client.render.transmitter;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.matrix.MatrixStack.Entry;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.ParametersAreNonnullByDefault;
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
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.data.EmptyModelData;

@ParametersAreNonnullByDefault
public abstract class RenderTransmitterBase<TRANSMITTER extends TileEntityTransmitter> extends MekanismTileEntityRenderer<TRANSMITTER> {

    public static final ResourceLocation MODEL_LOCATION = MekanismUtils.getResource(ResourceType.MODEL, "transmitter_contents.obj");
    private static final IModelConfiguration contentsConfiguration = new ContentsModelConfiguration();
    private static final Map<ContentsModelData, List<BakedQuad>> contentModelCache = new Object2ObjectOpenHashMap<>();
    private static final Vector3d NORMAL = new Vector3d(1, 1, 1).normalize();

    public static void onStitch() {
        contentModelCache.clear();
    }

    private static List<BakedQuad> getBakedQuads(List<String> visible, TextureAtlasSprite icon, World world) {
        return contentModelCache.computeIfAbsent(new ContentsModelData(visible, icon), modelData -> {
            List<BakedQuad> bakedQuads = MekanismRenderer.contentsModel.bake(new VisibleModelConfiguration(contentsConfiguration, modelData.visible),
                  ModelLoader.instance(), material -> modelData.icon, ModelRotation.X0_Y0, ItemOverrideList.EMPTY, MODEL_LOCATION
            ).getQuads(null, null, world.getRandom(), EmptyModelData.INSTANCE);
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

    protected RenderTransmitterBase(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    protected void renderModel(TRANSMITTER transmitter, MatrixStack matrix, IVertexBuilder builder, int rgb, float alpha, int light, int overlayLight,
          TextureAtlasSprite icon) {
        renderModel(transmitter, matrix, builder, MekanismRenderer.getRed(rgb), MekanismRenderer.getGreen(rgb), MekanismRenderer.getBlue(rgb), alpha, light,
              overlayLight, icon, Arrays.stream(EnumUtils.DIRECTIONS)
                    .map(side -> side.getString() + transmitter.getTransmitter().getConnectionType(side).getString().toUpperCase(Locale.ROOT))
                    .collect(Collectors.toList()));
    }

    protected void renderModel(TRANSMITTER transmitter, MatrixStack matrix, IVertexBuilder builder, float red, float green, float blue, float alpha, int light,
          int overlayLight, TextureAtlasSprite icon, List<String> visible) {
        if (!visible.isEmpty()) {
            Entry entry = matrix.getLast();
            //Get all the sides
            for (BakedQuad quad : getBakedQuads(visible, icon, transmitter.getWorld())) {
                builder.addVertexData(entry, quad, red, green, blue, alpha, light, overlayLight);
            }
        }
    }

    @Override
    public void render(TRANSMITTER transmitter, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight) {
        if (!MekanismConfig.client.opaqueTransmitters.get()) {
            super.render(transmitter, partialTick, matrix, renderer, light, overlayLight);
        }
    }

    private static class ContentsModelData {

        private final List<String> visible;
        private final TextureAtlasSprite icon;

        private ContentsModelData(List<String> visible, TextureAtlasSprite icon) {
            this.visible = visible;
            this.icon = icon;
        }

        @Override
        public int hashCode() {
            return Objects.hash(visible, icon);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof ContentsModelData) {
                ContentsModelData other = (ContentsModelData) o;
                return visible.equals(other.visible) && icon.equals(other.icon);
            }
            return false;
        }
    }
}