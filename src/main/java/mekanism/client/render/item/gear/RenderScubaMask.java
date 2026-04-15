package mekanism.client.render.item.gear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.model.ModelScubaMask;
import mekanism.client.render.item.MekanismISTER;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import org.joml.Vector3fc;

@NothingNullByDefault
public class RenderScubaMask implements NoDataSpecialModelRenderer {

    public static final RenderScubaMask RENDERER = new RenderScubaMask();
    private final ModelScubaMask scubaMask;

    public RenderScubaMask() {
        scubaMask = new ModelScubaMask(MekanismISTER.getEntityModels());
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        scubaMask.collect(poseStack, nodeCollector, lightCoords, overlayCoords, hasFoil);
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        PoseStack poseStack = new PoseStack();
        this.scubaMask.setupAnim();
        this.scubaMask.root().getExtentsForGui(poseStack, output);
    }
}