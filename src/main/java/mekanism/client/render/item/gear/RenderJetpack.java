package mekanism.client.render.item.gear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.model.ModelArmoredJetpack;
import mekanism.client.model.ModelJetpack;
import mekanism.client.render.item.MekanismISTER;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3fc;

@NothingNullByDefault
public class RenderJetpack extends MekanismISTER {

    public static final RenderJetpack RENDERER = new RenderJetpack(false);
    public static final RenderJetpack ARMORED_RENDERER = new RenderJetpack(true);

    private final ModelJetpack jetpack;

    private RenderJetpack(boolean armored) {
        if (armored) {
            jetpack = new ModelArmoredJetpack(getEntityModels());
        } else {
            jetpack = new ModelJetpack(getEntityModels());
        }
    }

    @Override
    public void submit(ItemDisplayContext type, PoseStack poseStack, SubmitNodeCollector nodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        jetpack.render(poseStack, renderer, lightCoords, overlayCoords, hasFoil);
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        PoseStack poseStack = new PoseStack();
        this.jetpack.setupAnim(Unit.INSTANCE);
        this.jetpack.root().getExtentsForGui(poseStack, output);
    }
}