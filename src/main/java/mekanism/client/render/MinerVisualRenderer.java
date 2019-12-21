package mekanism.client.render;

import java.util.HashMap;
import java.util.Map;
import mekanism.api.Coord4D;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.tile.TileEntityDigitalMiner;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public final class MinerVisualRenderer {

    private static final double offset = 0.01;
    private static Minecraft minecraft = Minecraft.getInstance();
    private static Map<MinerRenderData, DisplayInteger> cachedVisuals = new HashMap<>();

    public static void render(TileEntityDigitalMiner miner) {
        //TODO: 1.15 - note light map would be disabled by digital miner previously
        /*RenderSystem.pushMatrix();
        RenderSystem.translatef((float) getX(miner.getPos().getX()), (float) getY(miner.getPos().getY()), (float) getZ(miner.getPos().getZ()));
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        RenderSystem.disableAlphaTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlowInfo glowInfo = MekanismRenderer.enableGlow();
        RenderSystem.enableCull();
        RenderSystem.color4f(1, 1, 1, 0.8F);
        minecraft.getTextureManager().bindTexture(PlayerContainer.field_226615_c_);
        getList(new MinerRenderData(miner)).render();
        MekanismRenderer.resetColor();
        RenderSystem.disableCull();
        MekanismRenderer.disableGlow(glowInfo);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.popMatrix();*/
    }

    //TODO: 1.15
    /*private static DisplayInteger getList(MinerRenderData data) {
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
                            models.add(createModel(new Coord4D(x, y, z, minecraft.world.getDimension().getType())));
                        }
                    }
                }
            }
        }

        for (Model3D model : models) {
            MekanismRenderer.renderObject(model);
        }

        GlStateManager.endList();

        return display;
    }*/

    private static Model3D createModel(Coord4D rel) {
        Model3D toReturn = new Model3D();

        toReturn.setBlockBounds(rel.x + 0.4, rel.y + 0.4, rel.z + 0.4, rel.x + 0.6, rel.y + 0.6, rel.z + 0.6);
        toReturn.baseBlock = Blocks.WATER;
        toReturn.setTexture(MekanismRenderer.whiteIcon);

        return toReturn;
    }

    private static double getX(int x) {
        //TODO: 1.15 - check this
        return x - TileEntityRendererDispatcher.instance.renderInfo.getProjectedView().getX();//TileEntityRendererDispatcher.staticPlayerX;
    }

    private static double getY(int y) {
        //TODO: 1.15 - check this
        return y - TileEntityRendererDispatcher.instance.renderInfo.getProjectedView().getY();//TileEntityRendererDispatcher.staticPlayerY;
    }

    private static double getZ(int z) {
        //TODO: 1.15 - check this
        return z - TileEntityRendererDispatcher.instance.renderInfo.getProjectedView().getZ();//TileEntityRendererDispatcher.staticPlayerZ;
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