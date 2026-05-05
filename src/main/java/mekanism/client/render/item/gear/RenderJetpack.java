package mekanism.client.render.item.gear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.model.ModelArmoredJetpack;
import mekanism.client.model.ModelJetpack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import org.joml.Vector3fc;

@NothingNullByDefault
public class RenderJetpack implements NoDataSpecialModelRenderer {

    public static final Unbaked REGULAR = new Unbaked(GearArmorType.UNARMORED);
    public static final Unbaked ARMORED = new Unbaked(GearArmorType.ARMORED);

    private final ModelJetpack jetpack;

    private RenderJetpack(ModelJetpack jetpack) {
        this.jetpack = jetpack;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        jetpack.collect(poseStack, nodeCollector, lightCoords, overlayCoords, hasFoil);
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        PoseStack poseStack = new PoseStack();
        this.jetpack.setupAnim();
        this.jetpack.root().getExtentsForGui(poseStack, output);
    }

    public record Unbaked(GearArmorType armorType) implements NoDataSpecialModelRenderer.Unbaked {

        public static final MapCodec<Unbaked> MAP_CODEC = GearArmorType.CODEC
              .xmap(Unbaked::new, Unbaked::armorType)
              .fieldOf("armor_type");

        @Override
        public SpecialModelRenderer<Void> bake(BakingContext context) {
            ModelJetpack model = switch (armorType) {
                case UNARMORED -> new ModelJetpack(context.entityModelSet());
                case ARMORED -> new ModelArmoredJetpack(context.entityModelSet());
            };
            return new RenderJetpack(model);
        }

        @Override
        public MapCodec<? extends NoDataSpecialModelRenderer.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}