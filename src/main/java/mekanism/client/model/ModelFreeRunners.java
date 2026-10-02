package mekanism.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.model.ModelFreeRunners.FreeRunnerRenderState;
import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import org.jetbrains.annotations.UnknownNullability;

public class ModelFreeRunners extends MekanismJavaModel<FreeRunnerRenderState> {

    public static final ModelLayerLocation FREE_RUNNER_LAYER = new ModelLayerLocation(Mekanism.rl("free_runners"), "main");
    public static final ModelLayerLocation FREE_RUNNER_BABY_LAYER = new ModelLayerLocation(Mekanism.rl("free_runners_baby"), "main");
    private static final Identifier FREE_RUNNER_TEXTURE = MekanismUtils.getRenderResource("free_runners.png");

    public static LayerDefinition createLayerDefinition() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        createDefaultMesh(root);

        return LayerDefinition.create(mesh, 64, 32);
    }

    protected static void createDefaultMesh(PartDefinition root) {
        PartDefinition leftParts = root.addOrReplaceChild("left", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition rightParts = root.addOrReplaceChild("right", CubeListBuilder.create(), PartPose.ZERO);

        leftParts.addOrReplaceChild("spring", CubeListBuilder.create()
                    .texOffs(8, 0)
                    .addBox(1.5F, 18F, 0F, 1, 6, 1),
              PartPose.rotation(0.1047198F, 0F, 0F)
        );
        leftParts.addOrReplaceChild("brace", CubeListBuilder.create()
                    .texOffs(12, 0)
                    .addBox(0.2F, 18F, -0.8F, 4, 2, 3),
              PartPose.ZERO
        );
        leftParts.addOrReplaceChild("support", CubeListBuilder.create()
                    .addBox(1F, 16.5F, -4.2F, 2, 4, 2),
              PartPose.rotation(0.296706F, 0F, 0F)
        );

        rightParts.addOrReplaceChild("spring", CubeListBuilder.create()
                    .texOffs(8, 0)
                    .addBox(-2.5F, 18F, 0F, 1, 6, 1),
              PartPose.rotation(0.1047198F, 0F, 0F)
        );
        rightParts.addOrReplaceChild("brace", CubeListBuilder.create()
                    .texOffs(12, 0)
                    .addBox(-4.2F, 18F, -0.8F, 4, 2, 3),
              PartPose.ZERO
        );
        rightParts.addOrReplaceChild("support", CubeListBuilder.create()
                    .addBox(-3F, 16.5F, -4.2F, 2, 4, 2),
              PartPose.rotation(0.296706F, 0F, 0F)
        );
    }

    private final RenderType RENDER_TYPE = RenderTypes.entitySolid(FREE_RUNNER_TEXTURE);
    private final ModelPart leftParts;
    private final ModelPart rightParts;

    public ModelFreeRunners(EntityModelSet entityModelSet) {
        this(entityModelSet.bakeLayer(FREE_RUNNER_LAYER));
    }

    public ModelFreeRunners(ModelPart root) {
        super(root);
        leftParts = root.getChild("left");
        rightParts = root.getChild("right");
    }

    public RenderType getRenderType() {
        return RENDER_TYPE;
    }

    @Override
    public void setupAnim(FreeRunnerRenderState state) {
        super.setupAnim(state);
        leftParts.visible = state.leftVisible();
        rightParts.visible = state.rightVisible();
    }

    @Override
    public void collect(FreeRunnerRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, int overlayLight, @UnknownNullability FoilRendering foil, int outlineColor) {
        setupAnim(state);
        collect(state, poseStack, submitNodeCollector, light, overlayLight, foil, outlineColor, 1);
    }

    protected int collect(FreeRunnerRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, int overlayLight, FoilRendering foil, int outlineColor, int order) {
        int nextOrder = order;
        if (state.leftVisible()) {
            nextOrder = collectParts(leftParts, poseStack, RENDER_TYPE, submitNodeCollector, light, overlayLight, CommonColors.WHITE, null, foil, outlineColor, nextOrder);
        }
        if (state.rightVisible()) {
            nextOrder = collectParts(rightParts, poseStack, RENDER_TYPE, submitNodeCollector, light, overlayLight, CommonColors.WHITE, null, foil, outlineColor, nextOrder);
        }
        return nextOrder;
    }

    public record FreeRunnerRenderState(boolean leftVisible, boolean rightVisible) {

        public static final FreeRunnerRenderState BOTH = new FreeRunnerRenderState(true, true);
        public static final FreeRunnerRenderState LEFT_ONLY = new FreeRunnerRenderState(true, false);
        public static final FreeRunnerRenderState RIGHT_ONLY = new FreeRunnerRenderState(false, true);
    }
}