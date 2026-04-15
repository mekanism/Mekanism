package mekanism.client.render.item.gear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.model.ModelScubaTank;
import mekanism.client.render.item.MekanismISTER;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import org.joml.Vector3fc;

@NothingNullByDefault
public class RenderScubaTank implements NoDataSpecialModelRenderer {

    public static final RenderScubaTank RENDERER = new RenderScubaTank();
    private final ModelScubaTank scubaTank;

    public RenderScubaTank() {
        scubaTank = new ModelScubaTank(MekanismISTER.getEntityModels());
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        this.scubaTank.collect(poseStack, nodeCollector, lightCoords, overlayCoords, hasFoil);
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        PoseStack poseStack = new PoseStack();
        this.scubaTank.setupAnim();
        this.scubaTank.root().getExtentsForGui(poseStack, output);
    }
}