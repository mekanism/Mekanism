package mekanism.client.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mekanism.api.Coord4D;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.GlowInfo;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.tile.TileEntityDigitalMiner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.init.Blocks;
import org.lwjgl.opengl.GL11;

public final class MinerVisualRenderer {

    private static final double offset = 0.01;
    private static Minecraft mc = Minecraft.getMinecraft();
    private static Map<MinerRenderData, DisplayInteger> cachedVisuals = new HashMap<>();

    public static void render(TileEntityDigitalMiner miner) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) getX(miner.getPos().getX()), (float) getY(miner.getPos().getY()), (float) getZ(miner.getPos().getZ()));
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlowInfo glowInfo = MekanismRenderer.enableGlow();
        GlStateManager.enableCull();
        GlStateManager.color(1, 1, 1, 0.8F);
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        getList(new MinerRenderData(miner)).render();
        MekanismRenderer.resetColor();
        GlStateManager.disableCull();
        MekanismRenderer.disableGlow(glowInfo);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.popMatrix();
    }

    private static DisplayInteger getList(MinerRenderData data) {
        if (cachedVisuals.containsKey(data)) {
            return cachedVisuals.get(data);
        }

        DisplayInteger display = DisplayInteger.createAndStart();
        cachedVisuals.put(data, display);

        List<Model3D> models = new ArrayList<>();

        if (data.radius <= 64) {
            for (int x = -data.radius; x <= data.radius; x++) {
                for (int y = data.minY - data.yCoord; y <= data.maxY - data.yCoord; y++) {
                    for (int z = -data.radius; z <= data.radius; z++) {
                        if (x == -data.radius || x == data.radius || y == data.minY - data.yCoord || y == data.maxY - data.yCoord || z == -data.radius || z == data.radius) {
                            models.add(createModel(new Coord4D(x, y, z, mc.world.provider.getDimension())));
                        }
                    }
                }
            }
        }

        for (Model3D model : models) {
            MekanismRenderer.renderObject(model);
        }

        DisplayInteger.endList();

        return display;
    }

    private static Model3D createModel(Coord4D rel) {
        Model3D toReturn = new Model3D();

        toReturn.setBlockBounds(rel.x + 0.4, rel.y + 0.4, rel.z + 0.4, rel.x + 0.6, rel.y + 0.6, rel.z + 0.6);
        toReturn.baseBlock = Blocks.WATER;
        toReturn.setTexture(MekanismRenderer.whiteIcon);

        return toReturn;
    }

    private static double getX(int x) {
        return x - TileEntityRendererDispatcher.staticPlayerX;
    }

    private static double getY(int y) {
        return y - TileEntityRendererDispatcher.staticPlayerY;
    }

    private static double getZ(int z) {
        return z - TileEntityRendererDispatcher.staticPlayerZ;
    }

    public static class MinerRenderData {

        public int minY;
        public int maxY;
        public int radius;
        public int yCoord;

        public MinerRenderData(int min, int max, int rad, int y) {
            minY = min;
            maxY = max;
            radius = rad;
            yCoord = y;
        }

        public MinerRenderData(TileEntityDigitalMiner miner) {
            this(miner.minY, miner.maxY, miner.getRadius(), miner.getPos().getY());
        }

        @Override
        public boolean equals(Object data) {
            return data instanceof MinerRenderData && ((MinerRenderData) data).minY == minY && ((MinerRenderData) data).maxY == maxY &&
                   ((MinerRenderData) data).radius == radius && ((MinerRenderData) data).yCoord == yCoord;
        }

        @Override
        public int hashCode() {
            int code = 1;
            code = 31 * code + minY;
            code = 31 * code + maxY;
            code = 31 * code + radius;
            code = 31 * code + yCoord;
            return code;
        }
    }
}