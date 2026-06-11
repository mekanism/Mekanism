package mekanism.client.render.item.gear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import mekanism.client.model.ModelScubaMask;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import org.jspecify.annotations.Nullable;
import org.joml.Vector3fc;

public class RenderScubaMask implements NoDataSpecialModelRenderer {

    private final ModelScubaMask scubaMask;

    public RenderScubaMask(EntityModelSet entityModels) {
        scubaMask = new ModelScubaMask(entityModels);
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

    public static class Unbaked implements NoDataSpecialModelRenderer.Unbaked {

        public static final Unbaked INSTANCE = new Unbaked();
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public @Nullable SpecialModelRenderer<Void> bake(BakingContext context) {
            return new RenderScubaMask(context.entityModelSet());
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}