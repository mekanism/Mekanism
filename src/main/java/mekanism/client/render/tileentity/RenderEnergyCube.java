package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.EnumColor;
import mekanism.client.model.ModelEnergyCube;
import mekanism.client.model.ModelEnergyCube.ModelEnergyCore;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.RenderTickHandler;
import mekanism.client.render.RenderTickHandler.LazyRender;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;

@NothingNullByDefault
public class RenderEnergyCube extends ModelTileEntityRenderer<TileEntityEnergyCube, ModelEnergyCube> {

    public static final Vector3f coreVec = new Vector3f(0.0F, MekanismUtils.ONE_OVER_ROOT_TWO, MekanismUtils.ONE_OVER_ROOT_TWO);
    private final ModelEnergyCore core;

    public RenderEnergyCube(BlockEntityRendererProvider.Context context) {
        super(context, ModelEnergyCube::new);
        core = new ModelEnergyCore(context.getModelSet());
    }

    @Override
    protected void render(TileEntityEnergyCube tile, float partialTicks, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler) {
        profiler.push(ProfilerConstants.FRAME);
        matrix.pushPose();
        matrix.translate(0.5, 1.5, 0.5);
        switch (tile.getDirection()) {
            case DOWN -> {
                matrix.mulPose(Vector3f.XN.rotationDegrees(90));
                matrix.translate(0, 1, -1);
            }
            case UP -> {
                matrix.mulPose(Vector3f.XP.rotationDegrees(90));
                matrix.translate(0, 1, 1);
            }
            //Otherwise, use the helper method for handling different face options because it is one of them
            default -> MekanismRenderer.rotate(matrix, tile.getDirection(), 0, 180, 90, 270);
        }
        matrix.mulPose(Vector3f.ZP.rotationDegrees(180));
        profiler.push(ProfilerConstants.CORNERS);
        model.render(matrix, renderer, light, overlayLight, tile.getTier(), false, false);
        profiler.popPush(ProfilerConstants.SIDES);
        model.renderSidesBatched(tile, matrix, renderer, light, overlayLight);
        profiler.pop();//End sides
        matrix.popPose();
        endIfNeeded(renderer, null);

        profiler.popPush(ProfilerConstants.CORE);//End frame start core
        float energyScale = tile.getEnergyScale();
        if (energyScale > 0) {
            Vec3 renderPos = Vec3.atCenterOf(tile.getBlockPos());
            EnumColor color = tile.getTier().getBaseTier().getColor();
            RenderTickHandler.addTransparentRenderer(ModelEnergyCore.BATCHED_RENDER_TYPE, new LazyRender() {
                @Override
                public void render(Camera camera, VertexConsumer buffer, PoseStack poseStack, int renderTick, float partialTick, ProfilerFiller profiler) {
                    float ticks = renderTick + partialTick;
                    float scaledTicks = 4 * ticks;
                    poseStack.pushPose();
                    poseStack.translate(renderPos.x, renderPos.y, renderPos.z);
                    poseStack.scale(0.4F, 0.4F, 0.4F);
                    poseStack.translate(0, Math.sin(Math.toRadians(3 * ticks)) / 7, 0);
                    poseStack.mulPose(Vector3f.YP.rotationDegrees(scaledTicks));
                    poseStack.mulPose(coreVec.rotationDegrees(36F + scaledTicks));
                    core.render(poseStack, buffer, LightTexture.FULL_BRIGHT, overlayLight, color, energyScale);
                    poseStack.popPose();
                }

                @Override
                public Vec3 getCenterPos(float partialTick) {
                    return renderPos;
                }

                @Override
                public String getProfilerSection() {
                    return ProfilerConstants.ENERGY_CUBE_CORE;
                }
            });
        }
        profiler.pop();
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.ENERGY_CUBE;
    }
}