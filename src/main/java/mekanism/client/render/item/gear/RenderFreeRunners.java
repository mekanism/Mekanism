package mekanism.client.render.item.gear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.model.ModelArmoredFreeRunners;
import mekanism.client.model.ModelFreeRunners;
import mekanism.client.model.ModelFreeRunners.FreeRunnerRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

@NothingNullByDefault
public class RenderFreeRunners implements NoDataSpecialModelRenderer {

    private final ModelFreeRunners freeRunners;

    private RenderFreeRunners(ModelFreeRunners freeRunners) {
        this.freeRunners = freeRunners;
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

    public static class Unbaked implements NoDataSpecialModelRenderer.Unbaked {

        public static final MapCodec<Unbaked> MAP_CODEC = Codec.BOOL.fieldOf("armored").xmap(Unbaked::new, u -> u.armored);

        private final boolean armored;

        public Unbaked(boolean armored) {
            this.armored = armored;
        }

        @Override
        public @Nullable SpecialModelRenderer<Void> bake(BakingContext context) {
            ModelFreeRunners freeRunners;
            if (armored) {
                freeRunners = new ModelArmoredFreeRunners(context.entityModelSet());
            } else {
                freeRunners = new ModelFreeRunners(context.entityModelSet());
            }
            return new RenderFreeRunners(freeRunners);
        }

        @Override
        public MapCodec<? extends NoDataSpecialModelRenderer.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}