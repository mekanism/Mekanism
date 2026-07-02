package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

//TODO - 26.2: Test all our renderers, and figure out if/how to get profiling per type working again
public abstract class MekanismTileEntityRenderer<TILE extends BlockEntity, STATE extends BlockEntityRenderState> implements BlockEntityRenderer<TILE, STATE> {

    protected final BlockEntityRendererProvider.Context context;

    //TODO - 26.2: do we want to be passing context all the way up, or just grab what we need where we need it? I think probably the latter
    protected MekanismTileEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public int getViewDistance() {
        //Override and change the default range for TERs for mekanism tiles to the value defined in the config
        return MekanismConfig.client.berRange.get();
    }

    protected boolean isTickingNormally(TILE tile) {
        return !Minecraft.getInstance().isPaused() && MekanismUtils.isTickingNormally(tile.getLevel());
    }

    protected abstract String getProfilerSection();

    protected void submitBreakableBlockModel(SubmitNodeCollector nodeCollector, PoseStack poseStack, RenderType renderType, List<BlockStateModelPart> parts, int[] tintLayers,
          int lightCoords, int overlayCoords, int outlineColor, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        nodeCollector.submitBlockModel(poseStack, renderType, parts, tintLayers, lightCoords, overlayCoords, outlineColor);
        if (breakProgress != null) {
            nodeCollector.submitBreakingBlockModel(poseStack, parts, breakProgress.progress());
        }
    }
}