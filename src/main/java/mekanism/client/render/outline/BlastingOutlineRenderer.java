package mekanism.client.render.outline;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.CustomBlockOutlineRenderer;

public class BlastingOutlineRenderer implements CustomBlockOutlineRenderer {

    private final BlockOutlineRenderState state;

    public BlastingOutlineRenderer(BlockOutlineRenderState state) {
        this.state = state;
    }

    /// Copied and modified from [net.minecraft.client.renderer.LevelRenderer#submitBlockOutline]
    @Override
    public final boolean render(BlockOutlineRenderState ignored, SubmitNodeCollector nodeCollector, PoseStack poseStack, LevelRenderState levelRenderState) {
        boolean cancel = false;
        for (CustomBlockOutlineRenderer customRenderer : state.customRenderers()) {
            cancel |= customRenderer.render(state, nodeCollector, poseStack, levelRenderState);
        }
        if (!cancel) {
            Vec3 cameraPos = levelRenderState.cameraRenderState.pos;
            BlockPos pos = state.pos();
            poseStack.pushPose();
            poseStack.translate(pos.getX() - cameraPos.x, pos.getY() - cameraPos.y, pos.getZ() - cameraPos.z);
            if (state.highContrast()) {
                nodeCollector.submitShapeOutline(poseStack, state.shape(), RenderTypes.secondaryBlockOutline(), CommonColors.BLACK, 7.0F, state.isTranslucent());
            }

            GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
            int outlineColor = state.highContrast() ? CommonColors.HIGH_CONTRAST_DIAMOND : ARGB.black(0x66);
            RenderType blockOutlineRenderType;
            if (state.highContrast()) {
                blockOutlineRenderType = RenderTypes.linesDepthBias();
            } else if (gameRenderer.useImprovedTransparency()) {
                blockOutlineRenderType = RenderTypes.linesTranslucentNoDepthWrite();
            } else {
                blockOutlineRenderType = RenderTypes.linesTranslucent();
            }
            nodeCollector.submitShapeOutline(poseStack, state.shape(), blockOutlineRenderType, outlineColor, gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth, state.isTranslucent());
            poseStack.popPose();
        }
        return false;
    }
}