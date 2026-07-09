package mekanism.client.render.item.gear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import mekanism.client.model.MekanismJavaModel.FoilRendering;
import mekanism.client.model.ModelArmoredFreeRunners;
import mekanism.client.model.ModelFreeRunners;
import mekanism.client.model.ModelFreeRunners.FreeRunnerRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.util.Mth;
import org.joml.Vector3fc;

public class RenderFreeRunners implements NoDataSpecialModelRenderer {

    public static final Unbaked REGULAR = new Unbaked(GearArmorType.UNARMORED);
    public static final Unbaked ARMORED = new Unbaked(GearArmorType.ARMORED);

    private final ModelFreeRunners freeRunners;

    private RenderFreeRunners(ModelFreeRunners freeRunners) {
        this.freeRunners = freeRunners;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.ZP.rotation(Mth.PI));
        poseStack.translate(0, -1, 0);
        this.freeRunners.collect(FreeRunnerRenderState.BOTH, poseStack, nodeCollector, lightCoords, overlayCoords, FoilRendering.ITEM.foil(hasFoil), outlineColor);
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        PoseStack poseStack = new PoseStack();
        this.freeRunners.setupAnim(FreeRunnerRenderState.BOTH);
        this.freeRunners.root().getExtentsForGui(poseStack, output);
    }

    public record Unbaked(GearArmorType armorType) implements NoDataSpecialModelRenderer.Unbaked {

        public static final MapCodec<Unbaked> MAP_CODEC = GearArmorType.CODEC.xmap(Unbaked::new, Unbaked::armorType).fieldOf("armor_type");

        @Override
        public SpecialModelRenderer<Void> bake(BakingContext context) {
            ModelFreeRunners model = switch (armorType) {
                case UNARMORED -> new ModelFreeRunners(context.entityModelSet());
                case ARMORED -> new ModelArmoredFreeRunners(context.entityModelSet());
            };
            return new RenderFreeRunners(model);
        }

        @Override
        public MapCodec<? extends NoDataSpecialModelRenderer.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}