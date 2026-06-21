package mekanism.client.render.item.gear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import mekanism.client.model.ModelScubaTank;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.util.Unit;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public class RenderScubaTank implements NoDataSpecialModelRenderer {

    private final ModelScubaTank scubaTank;

    public RenderScubaTank(EntityModelSet entityModels) {
        scubaTank = new ModelScubaTank(entityModels);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        //TODO - 26.2: Figure out foil
        nodeCollector.submitModel(this.scubaTank, Unit.INSTANCE, poseStack, scubaTank.RENDER_TYPE, lightCoords, overlayCoords, 0, null);
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        PoseStack poseStack = new PoseStack();
        this.scubaTank.setupAnim(Unit.INSTANCE);
        this.scubaTank.root().getExtentsForGui(poseStack, output);
    }

    public static class Unbaked implements NoDataSpecialModelRenderer.Unbaked {

        public static final Unbaked INSTANCE = new Unbaked();
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public @Nullable SpecialModelRenderer<Void> bake(BakingContext context) {
            return new RenderScubaTank(context.entityModelSet());
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}