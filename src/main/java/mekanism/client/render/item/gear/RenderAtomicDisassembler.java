package mekanism.client.render.item.gear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import mekanism.client.model.ModelAtomicDisassembler;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public class RenderAtomicDisassembler implements NoDataSpecialModelRenderer {

    private final ModelAtomicDisassembler atomicDisassembler;

    public RenderAtomicDisassembler(ModelAtomicDisassembler atomicDisassembler) {
        this.atomicDisassembler = atomicDisassembler;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        //TODO - 26.2: Figure out foil
        atomicDisassembler.collect(null, poseStack, submitNodeCollector, lightCoords, overlayCoords);
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        PoseStack poseStack = new PoseStack();
        this.atomicDisassembler.setupAnim();
        this.atomicDisassembler.root().getExtentsForGui(poseStack, output);
    }

    public static class Unbaked implements NoDataSpecialModelRenderer.Unbaked {

        public static final Unbaked INSTANCE = new Unbaked();
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public @Nullable SpecialModelRenderer<Void> bake(BakingContext context) {
            return new RenderAtomicDisassembler(new ModelAtomicDisassembler(context.entityModelSet()));
        }

        @Override
        public MapCodec<? extends NoDataSpecialModelRenderer.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}