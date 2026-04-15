package mekanism.client.render.item.gear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.model.ModelAtomicDisassembler;
import mekanism.client.render.item.MekanismISTER;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import org.joml.Vector3fc;

@NothingNullByDefault
public class RenderAtomicDisassembler implements NoDataSpecialModelRenderer {

    public static final RenderAtomicDisassembler RENDERER = new RenderAtomicDisassembler();
    private final ModelAtomicDisassembler atomicDisassembler;

    public RenderAtomicDisassembler() {
        atomicDisassembler = new ModelAtomicDisassembler(MekanismISTER.getEntityModels());
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        atomicDisassembler.collect(null, poseStack, submitNodeCollector, lightCoords, overlayCoords, hasFoil);
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        PoseStack poseStack = new PoseStack();
        this.atomicDisassembler.setupAnim();
        this.atomicDisassembler.root().getExtentsForGui(poseStack, output);
    }
}