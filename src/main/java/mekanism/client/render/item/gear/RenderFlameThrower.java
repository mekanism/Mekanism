package mekanism.client.render.item.gear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.model.ModelFlamethrower;
import mekanism.client.render.item.MekanismISTER;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3fc;

@NothingNullByDefault
public class RenderFlameThrower extends MekanismISTER {

    public static final RenderFlameThrower RENDERER = new RenderFlameThrower();
    private final ModelFlamethrower flamethrower;

    public RenderFlameThrower() {
        flamethrower = new ModelFlamethrower(getEntityModels());
    }

    @Override
    public void submit(ItemDisplayContext type, PoseStack poseStack, SubmitNodeCollector nodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        flamethrower.render(poseStack, renderer, lightCoords, overlayCoords, hasFoil);
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        PoseStack poseStack = new PoseStack();
        this.flamethrower.setupAnim(Unit.INSTANCE);
        this.flamethrower.root().getExtentsForGui(poseStack, output);
    }
}