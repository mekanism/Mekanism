package mekanism.client.render.item.gear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.model.ModelArmoredFreeRunners;
import mekanism.client.model.ModelFreeRunners;
import mekanism.client.model.ModelFreeRunners.FreeRunnerRenderState;
import mekanism.client.render.item.MekanismISTER;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import org.joml.Vector3fc;

@NothingNullByDefault
public class RenderFreeRunners implements NoDataSpecialModelRenderer {

    public static final RenderFreeRunners RENDERER = new RenderFreeRunners(false);
    public static final RenderFreeRunners ARMORED_RENDERER = new RenderFreeRunners(true);

    private final ModelFreeRunners freeRunners;

    private RenderFreeRunners(boolean armored) {
        if (armored) {
            freeRunners = new ModelArmoredFreeRunners(MekanismISTER.getEntityModels());
        } else {
            freeRunners = new ModelFreeRunners(MekanismISTER.getEntityModels());
        }
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        poseStack.translate(0, -1, 0);
        this.freeRunners.collect(FreeRunnerRenderState.BOTH, poseStack, nodeCollector, lightCoords, overlayCoords, hasFoil);
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        PoseStack poseStack = new PoseStack();
        this.freeRunners.setupAnim(FreeRunnerRenderState.BOTH);
        this.freeRunners.root().getExtentsForGui(poseStack, output);
    }
}