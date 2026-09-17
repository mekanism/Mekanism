package mekanism.client.render.item.block;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.LightCoordsUtil;
import org.joml.Matrix4fc;

///Version of [BlockModelRenderState] that supports rendering with foil
public class FoilableBlockModelRenderState extends BlockModelRenderState {

    private boolean hasTranslucency;

    @Override
    public void clear() {
        this.hasTranslucency = false;
        super.clear();
    }

    @Override
    public List<BlockStateModelPart> setupModel(Matrix4fc transformation, boolean hasTranslucency) {
        this.hasTranslucency = hasTranslucency;
        return super.setupModel(transformation, hasTranslucency);
    }

    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int externalLightCoords, int overlayCoords, int outlineColor, boolean hasFoil) {
        submitModel(poseStack, submitNodeCollector, externalLightCoords, overlayCoords, outlineColor, hasFoil);
        submitSpecialRenderer(poseStack, submitNodeCollector, externalLightCoords, overlayCoords, outlineColor, hasFoil);
    }

    private void submitModel(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int externalLightCoords, int overlayCoords, int outlineColor, boolean hasFoil) {
        if (this.renderType != null && this.modelParts != null && !this.modelParts.isEmpty()) {
            List<BlockStateModelPart> modelPartsCopy = new ObjectArrayList<>(this.modelParts);
            int[] tints = this.tintLayers != null ? this.tintLayers.toArray(EMPTY_TINTS) : EMPTY_TINTS;
            int lightCoords = LightCoordsUtil.max(externalLightCoords, this.blockLightCoords);
            if (this.transformation != null) {
                poseStack.pushPose();
                poseStack.mulPose(this.transformation);
                submitNodeCollector.submitMultiLayerBlockModel(poseStack, modelPartsCopy, hasTranslucency, tints, externalLightCoords, overlayCoords, outlineColor);
                if (hasFoil) {
                    submitNodeCollector.order(1).submitBlockModel(poseStack, glintRenderType(), modelPartsCopy, tints, lightCoords, overlayCoords, outlineColor);
                }
                poseStack.popPose();
            } else {
                submitNodeCollector.submitMultiLayerBlockModel(poseStack, modelPartsCopy, hasTranslucency, tints, externalLightCoords, overlayCoords, outlineColor);
                if (hasFoil) {
                    submitNodeCollector.order(1).submitBlockModel(poseStack, glintRenderType(), modelPartsCopy, tints, lightCoords, overlayCoords, outlineColor);
                }
            }
        }
    }

    private RenderType glintRenderType() {
        //TODO - 26.3: Test and validate this
        return hasTranslucency ? Sheets.translucentBlockItemGlintSheet() : Sheets.cutoutBlockItemGlintSheet();
    }

    private void submitSpecialRenderer(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int externalLightCoords, int overlayCoords, int outlineColor, boolean hasFoil) {
        if (this.specialRenderer != null) {
            int lightCoords = LightCoordsUtil.max(externalLightCoords, this.blockLightCoords);
            if (this.specialRendererTransformation != null) {
                poseStack.pushPose();
                poseStack.mulPose(this.specialRendererTransformation);
                this.specialRenderer.submit(null, poseStack, submitNodeCollector, lightCoords, overlayCoords, hasFoil, outlineColor);
                poseStack.popPose();
            } else {
                this.specialRenderer.submit(null, poseStack, submitNodeCollector, lightCoords, overlayCoords, hasFoil, outlineColor);
            }
        }
    }
}