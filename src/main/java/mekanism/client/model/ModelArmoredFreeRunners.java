package mekanism.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.common.Mekanism;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.CommonColors;
import net.minecraft.util.LightCoordsUtil;

public class ModelArmoredFreeRunners extends ModelFreeRunners {

    public static final ModelLayerLocation ARMORED_FREE_RUNNER_LAYER = new ModelLayerLocation(Mekanism.rl("armored_free_runners"), "main");
    public static final ModelLayerLocation ARMORED_FREE_RUNNER_BABY_LAYER = new ModelLayerLocation(Mekanism.rl("armored_free_runners_baby"), "main");

    public static LayerDefinition createLayerDefinition() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        createDefaultMesh(root);
        PartDefinition leftParts = root.getChild("left");
        PartDefinition rightParts = root.getChild("right");

        leftParts.addOrReplaceChild("plate", CubeListBuilder.create()
                    .mirror()
                    .texOffs(0, 11)
                    .addBox(0.5F, 21, -3, 3, 2, 1)
                    .texOffs(0, 7)
                    .addBox(0.5F, 17, -3, 3, 1, 1),
              PartPose.ZERO
        );
        leftParts.addOrReplaceChild("top_plate", CubeListBuilder.create()
                    .mirror()
                    .texOffs(12, 7)
                    .addBox(0, 0, -0.25F, 2, 2, 1),
              PartPose.offsetAndRotation(1, 16, -2, -0.7854F, 0, 0)
        );
        leftParts.addOrReplaceChild("connection", CubeListBuilder.create()
                    .mirror()
                    .texOffs(8, 7)
                    .addBox(2.5F, 18, -3, 1, 3, 1)
                    .texOffs(8, 7)
                    .addBox(0.5F, 18, -3, 1, 3, 1),
              PartPose.ZERO
        );
        leftParts.addOrReplaceChild("armored_brace", CubeListBuilder.create()
                    .texOffs(10, 12)
                    .addBox(0.2F, 17, -2.3F, 4, 1, 1)
                    .texOffs(8, 10)
                    .addBox(0.2F, 21, -2.3F, 4, 1, 3),
              PartPose.ZERO
        );


        rightParts.addOrReplaceChild("plate", CubeListBuilder.create()
                    .texOffs(0, 11)
                    .addBox(-3.5F, 21, -3, 3, 2, 1)
                    .texOffs(0, 7)
                    .addBox(-3.5F, 17, -3, 3, 1, 1),
              PartPose.ZERO
        );
        rightParts.addOrReplaceChild("top_plate", CubeListBuilder.create()
                    .texOffs(12, 7)
                    .addBox(-2, 0, -0.25F, 2, 2, 1),
              PartPose.offsetAndRotation(-1, 16, -2, -0.7854F, 0, 0)
        );
        rightParts.addOrReplaceChild("connection", CubeListBuilder.create()
                    .texOffs(8, 7)
                    .addBox(-1.5F, 18, -3, 1, 3, 1)
                    .texOffs(8, 7)
                    .addBox(-3.5F, 18, -3, 1, 3, 1),
              PartPose.ZERO
        );
        rightParts.addOrReplaceChild("armored_brace", CubeListBuilder.create()
                    .mirror()
                    .texOffs(10, 12)
                    .addBox(-4.2F, 17, -2.3F, 4, 1, 1)
                    .texOffs(8, 10)
                    .addBox(-4.2F, 21, -2.3F, 4, 1, 3),
              PartPose.ZERO
        );

        PartDefinition litLeftParts = root.addOrReplaceChild("left_lit", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition litRightParts = root.addOrReplaceChild("right_lit", CubeListBuilder.create(), PartPose.ZERO);

        litLeftParts.addOrReplaceChild("battery", CubeListBuilder.create()
                    .texOffs(22, 11)
                    .addBox(1.5F, 18, -3, 1, 2, 1),
              PartPose.ZERO
        );

        litRightParts.addOrReplaceChild("battery", CubeListBuilder.create()
                    .texOffs(22, 11)
                    .addBox(-2.5F, 18, -3, 1, 2, 1),
              PartPose.ZERO
        );

        return LayerDefinition.create(mesh, 64, 32);
    }

    private final ModelPart litLeftParts;
    private final ModelPart litRightParts;

    public ModelArmoredFreeRunners(EntityModelSet entityModelSet) {
        this(entityModelSet.bakeLayer(ARMORED_FREE_RUNNER_LAYER));
    }

    public ModelArmoredFreeRunners(ModelPart root) {
        super(root);
        litLeftParts = root.getChild("left_lit");
        litRightParts = root.getChild("right_lit");
    }

    @Override
    public int collect(FreeRunnerRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, int overlayLight, FoilRendering foil, int outlineColor, int order) {
        int nextOrder = super.collect(state, poseStack, submitNodeCollector, light, overlayLight, foil, outlineColor, order);
        if (state.leftVisible()) {
            nextOrder = collectParts(litLeftParts, poseStack, getRenderType(), submitNodeCollector, LightCoordsUtil.FULL_BRIGHT, overlayLight, CommonColors.WHITE, null, foil, outlineColor, nextOrder);
        }
        if (state.rightVisible()) {
            nextOrder = collectParts(litRightParts, poseStack, getRenderType(), submitNodeCollector, LightCoordsUtil.FULL_BRIGHT, overlayLight, CommonColors.WHITE, null, foil, outlineColor, nextOrder);
        }
        return nextOrder;
    }

    @Override
    public void setupAnim(FreeRunnerRenderState state) {
        super.setupAnim(state);
        litLeftParts.visible = state.leftVisible();
        litRightParts.visible = state.rightVisible();
    }
}