package mekanism.client.render.tileentity;

import java.util.Random;
import javax.annotation.ParametersAreNonnullByDefault;
import com.google.common.base.Objects;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.bolt.BoltEffect;
import mekanism.client.render.bolt.BoltRenderer;
import mekanism.client.render.bolt.BoltRenderer.BoltData;
import mekanism.client.render.bolt.BoltRenderer.SpawnFunction;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.content.sps.SPSMultiblockData;
import mekanism.common.content.sps.SPSMultiblockData.CoilData;
import mekanism.common.lib.math.Plane;
import mekanism.common.lib.math.VoxelCuboid;
import mekanism.common.lib.math.VoxelCuboid.CuboidSide;
import mekanism.common.tile.multiblock.TileEntitySPSCasing;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@ParametersAreNonnullByDefault
public class RenderSPS extends MekanismTileEntityRenderer<TileEntitySPSCasing> {

    private static final ResourceLocation GLOW = MekanismUtils.getResource(ResourceType.RENDER, "energy_effect.png");
    private static final Random rand = new Random();
    private static final int GRID_SIZE = 4;
    private static final int ALPHA = 240;
    private static float MIN_SCALE = 0.5F, MAX_SCALE = 4F;

    private Minecraft minecraft = Minecraft.getInstance();
    private BoltRenderer bolts = BoltRenderer.create(BoltEffect.ELECTRICITY, 12, SpawnFunction.delay(6));
    private BoltRenderer edgeBolts = BoltRenderer.create(BoltEffect.ELECTRICITY, 8, SpawnFunction.NO_DELAY);

    public RenderSPS(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    protected void render(TileEntitySPSCasing tile, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight, IProfiler profiler) {
        if (tile.isMaster && tile.getMultiblock().isFormed() && tile.getMultiblock().renderLocation != null) {
            Vec3d center = new Vec3d(tile.getMultiblock().minLocation).add(new Vec3d(tile.getMultiblock().maxLocation)).add(new Vec3d(1, 1, 1)).scale(0.5);
            center = center.subtract(tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ());
            if (!minecraft.isGamePaused()) {
                for (CoilData data : tile.getMultiblock().coilData.coilMap.values()) {
                    if (data.prevLevel > 0) {
                        bolts.update(data.coilPos.hashCode(), getBoltFromData(data, tile.getPos(), tile.getMultiblock(), center), partialTick);
                    }
                }
            }

            if (!minecraft.isGamePaused() && !tile.getMultiblock().lastReceivedEnergy.isZero()) {
                double rate = Math.log10(tile.getMultiblock().lastReceivedEnergy.doubleValue());
                if (rand.nextDouble() < (rate / 16F)) {
                    CuboidSide side = CuboidSide.SIDES[rand.nextInt(6)];
                    Plane plane = Plane.getInnerCuboidPlane(new VoxelCuboid(tile.getMultiblock().minLocation, tile.getMultiblock().maxLocation), side);
                    Vec3d endPos = plane.getRandomPoint(rand).subtract(tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ());
                    BoltData data = new BoltData(center, endPos, 1, 15, 0.01F * (float) rate);
                    edgeBolts.update(Objects.hashCode(side.hashCode(), endPos.hashCode()), data, partialTick);
                }
            }

            bolts.render(partialTick, matrix, renderer);
            edgeBolts.render(partialTick, matrix, renderer);

            if (tile.getMultiblock().lastProcessed > 0) {
                double scaledEnergy = (Math.log10(tile.getMultiblock().lastProcessed) + 4) / 5D;
                float scale = MIN_SCALE + (float)Math.min(1, Math.max(0, scaledEnergy)) * (MAX_SCALE - MIN_SCALE);
                IVertexBuilder buffer = renderer.getBuffer(MekanismRenderType.renderSPS(GLOW));
                renderCore(matrix, buffer, center, tile.getWorld().getGameTime(), scale);
            }
        }
    }

    private void renderCore(MatrixStack matrixStack, IVertexBuilder buffer, Vec3d pos, long time, float scale) {
        RenderSystem.pushMatrix();
        Matrix4f matrix = matrixStack.getLast().getMatrix();
        ActiveRenderInfo renderInfo = minecraft.gameRenderer.getActiveRenderInfo();
        int tick = (int) time % (GRID_SIZE * GRID_SIZE);
        int xIndex = tick % GRID_SIZE, yIndex = tick / GRID_SIZE;
        float spriteSize = 1F / GRID_SIZE;
        Quaternion quaternion = renderInfo.getRotation();
        new Vector3f(-1.0F, -1.0F, 0.0F).transform(quaternion);
        Vector3f[] vertexPos = new Vector3f[] {new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F),
                                               new Vector3f(1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, -1.0F, 0.0F)};
        for(int i = 0; i < 4; i++) {
           Vector3f vector3f = vertexPos[i];
           vector3f.transform(quaternion);
           vector3f.mul(scale);
           vector3f.add((float) pos.getX(), (float) pos.getY(), (float) pos.getZ());
        }

        float minU = xIndex * spriteSize, maxU = minU + spriteSize;
        float minV = yIndex * spriteSize, maxV = minV + spriteSize;

        buffer.pos(matrix, vertexPos[0].getX(), vertexPos[0].getY(), vertexPos[0].getZ()).color(255, 255, 255, ALPHA).tex(minU, maxV).endVertex();
        buffer.pos(matrix, vertexPos[1].getX(), vertexPos[1].getY(), vertexPos[1].getZ()).color(255, 255, 255, ALPHA).tex(maxU, maxV).endVertex();
        buffer.pos(matrix, vertexPos[2].getX(), vertexPos[2].getY(), vertexPos[2].getZ()).color(255, 255, 255, ALPHA).tex(maxU, minV).endVertex();
        buffer.pos(matrix, vertexPos[3].getX(), vertexPos[3].getY(), vertexPos[3].getZ()).color(255, 255, 255, ALPHA).tex(minU, minV).endVertex();
        RenderSystem.popMatrix();
    }

    private static BoltData getBoltFromData(CoilData data, BlockPos pos, SPSMultiblockData multiblock, Vec3d center) {
        Vec3d start = new Vec3d(data.coilPos.offset(data.side)).add(0.5, 0.5, 0.5);
        start = start.add(new Vec3d(data.side.getDirectionVec()).scale(0.5));
        int count = 1 + (data.prevLevel - 1) / 2;
        float size = 0.01F * data.prevLevel;
        return new BoltData(start.subtract(pos.getX(), pos.getY(), pos.getZ()), center, count, 10, size);
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.SPS;
    }

    @Override
    public boolean isGlobalRenderer(TileEntitySPSCasing tile) {
        return tile.isMaster && tile.getMultiblock().isFormed() && tile.getMultiblock().renderLocation != null;
    }
}
