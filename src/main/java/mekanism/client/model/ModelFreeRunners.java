package mekanism.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import mekanism.client.model.ModelFreeRunners.FreeRunnerRenderState;
import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class ModelFreeRunners extends MekanismJavaModel<FreeRunnerRenderState> {

    public static final ModelLayerLocation FREE_RUNNER_LAYER = new ModelLayerLocation(Mekanism.rl("free_runners"), "main");
    private static final Identifier FREE_RUNNER_TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "free_runners.png");

    protected static final ModelPartData SPRING_L = new ModelPartData("SpringL", CubeListBuilder.create()
          .texOffs(8, 0)
          .addBox(1.5F, 18F, 0F, 1, 6, 1),
          PartPose.rotation(0.1047198F, 0F, 0F));
    protected static final ModelPartData SPRING_R = new ModelPartData("SpringR", CubeListBuilder.create()
          .texOffs(8, 0)
          .addBox(-2.5F, 18F, 0F, 1, 6, 1),
          PartPose.rotation(0.1047198F, 0F, 0F));
    protected static final ModelPartData BRACE_L = new ModelPartData("BraceL", CubeListBuilder.create()
          .texOffs(12, 0)
          .addBox(0.2F, 18F, -0.8F, 4, 2, 3));
    protected static final ModelPartData BRACE_R = new ModelPartData("BraceR", CubeListBuilder.create()
          .texOffs(12, 0)
          .addBox(-4.2F, 18F, -0.8F, 4, 2, 3));
    protected static final ModelPartData SUPPORT_L = new ModelPartData("SupportL", CubeListBuilder.create()
          .addBox(1F, 16.5F, -4.2F, 2, 4, 2),
          PartPose.rotation(0.296706F, 0F, 0F));
    protected static final ModelPartData SUPPORT_R = new ModelPartData("SupportR", CubeListBuilder.create()
          .addBox(-3F, 16.5F, -4.2F, 2, 4, 2),
          PartPose.rotation(0.296706F, 0F, 0F));

    public static LayerDefinition createLayerDefinition() {
        return createLayerDefinition(64, 32, SPRING_L, SPRING_R, BRACE_L, BRACE_R, SUPPORT_L, SUPPORT_R);
    }

    private final RenderType RENDER_TYPE = RenderTypes.entitySolid(FREE_RUNNER_TEXTURE);
    protected final List<ModelPart> leftParts;
    protected final List<ModelPart> rightParts;

    public ModelFreeRunners(EntityModelSet entityModelSet) {
        this(entityModelSet.bakeLayer(FREE_RUNNER_LAYER));
    }

    protected ModelFreeRunners(ModelPart root) {
        super(root);
        leftParts = getRenderableParts(root, SPRING_L, BRACE_L, SUPPORT_L);
        rightParts = getRenderableParts(root, SPRING_R, BRACE_R, SUPPORT_R);
    }

    public RenderType getRenderType() {
        return RENDER_TYPE;
    }

    @Override
    public void setupAnim(FreeRunnerRenderState state) {
        super.setupAnim(state);
        for (ModelPart leftPart : leftParts) {
            leftPart.visible = state.leftVisible();
        }
        for (ModelPart rightPart : rightParts) {
            rightPart.visible = state.rightVisible();
        }
    }

    @Override
    public void collect(FreeRunnerRenderState state, @NotNull PoseStack poseStack, @NotNull SubmitNodeCollector submitNodeCollector, int light, int overlayLight, boolean hasEffect) {
        if (state.leftVisible()) {
            collectParts(leftParts, poseStack, RENDER_TYPE, submitNodeCollector, light, overlayLight, 0xFFFFFFFF, null, hasEffect);
        }
        if (state.rightVisible) {
            collectParts(rightParts, poseStack, RENDER_TYPE, submitNodeCollector, light, overlayLight, 0xFFFFFFFF, null, hasEffect);
        }
    }

    //TODO - 1.21.11: Do we want a static field for the various states?
    public record FreeRunnerRenderState(boolean leftVisible, boolean rightVisible) {

        /// Don't call this with both false....
        public static FreeRunnerRenderState choose(boolean leftVisible, boolean rightVisible) {
            if (leftVisible && rightVisible) {
                return BOTH;
            } else if (leftVisible) {
                return LEFT_ONLY;
            }
            return RIGHT_ONLY;
        }

        public static final FreeRunnerRenderState BOTH = new FreeRunnerRenderState(true, true);
        public static final FreeRunnerRenderState LEFT_ONLY = new FreeRunnerRenderState(true, false);
        public static final FreeRunnerRenderState RIGHT_ONLY = new FreeRunnerRenderState(false, true);
    }
}