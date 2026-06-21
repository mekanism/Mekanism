package mekanism.client.render.item.gear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import mekanism.client.model.ModelFlamethrower;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.util.Unit;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public class RenderFlameThrower implements NoDataSpecialModelRenderer {

    private final ModelFlamethrower flamethrower;

    public RenderFlameThrower(EntityModelSet entityModels) {
        flamethrower = new ModelFlamethrower(entityModels);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        //TODO - 26.2: Figure out foil
        nodeCollector.submitModel(flamethrower, Unit.INSTANCE, poseStack, flamethrower.RENDER_TYPE, lightCoords, overlayCoords, 0, null);
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        PoseStack poseStack = new PoseStack();
        this.flamethrower.setupAnim(Unit.INSTANCE);
        this.flamethrower.root().getExtentsForGui(poseStack, output);
    }

    public static class Unbaked implements NoDataSpecialModelRenderer.Unbaked {

        public static final Unbaked INSTANCE = new Unbaked();
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public @Nullable SpecialModelRenderer<Void> bake(BakingContext context) {
            return new RenderFlameThrower(context.entityModelSet());
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}