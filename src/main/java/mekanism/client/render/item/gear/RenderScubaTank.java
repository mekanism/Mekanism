package mekanism.client.render.item.gear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.model.ModelScubaTank;
import mekanism.client.render.item.MekanismISTER;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3fc;

@NothingNullByDefault
public class RenderScubaTank extends MekanismISTER {

    public static final RenderScubaTank RENDERER = new RenderScubaTank();
    private final ModelScubaTank scubaTank;

    public RenderScubaTank() {
        scubaTank = new ModelScubaTank(getEntityModels());
    }

    @Override
    public void submit(ItemDisplayContext type, PoseStack poseStack, SubmitNodeCollector nodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        //TODO - 1.21.11: Do we need to pass the texture as well?
        nodeCollector.submitModel(
              this.scubaTank,
              Unit.INSTANCE,
              poseStack,
              this.scubaTank.getRenderType(),
              lightCoords,
              overlayCoords,
              outlineColor,
              null
        );
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        PoseStack poseStack = new PoseStack();
        this.scubaTank.setupAnim(Unit.INSTANCE);
        this.scubaTank.root().getExtentsForGui(poseStack, output);
    }
}