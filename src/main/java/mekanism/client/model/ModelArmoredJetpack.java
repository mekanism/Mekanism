package mekanism.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;

public class ModelArmoredJetpack extends ModelJetpack {

    public static final ModelLayerLocation ARMORED_JETPACK_LAYER = new ModelLayerLocation(Mekanism.rl("armored_jetpack"), "main");

    private static final ModelPartData THRUSTER_LEFT = thrusterLeft(-1.9F);
    private static final ModelPartData THRUSTER_RIGHT = thrusterRight(-1.9F);
    private static final ModelPartData FUEL_TUBE_RIGHT = fuelTubeRight(-1.9F);
    private static final ModelPartData FUEL_TUBE_LEFT = fuelTubeLeft(-1.9F);

    private static final ModelPartData CHESTPLATE = new ModelPartData("chestplate", CubeListBuilder.create()
          .texOffs(104, 22)
          .addBox(-4, 1.333333F, -3, 8, 4, 3),
          PartPose.rotation(-0.3665191F, 0, 0));
    private static final ModelPartData LEFT_GUARD_TOP = new ModelPartData("leftGuardTop", CubeListBuilder.create()
          .texOffs(87, 31)
          .addBox(0.95F, 3, -5, 3, 4, 2),
          PartPose.rotation(0.2094395F, 0, 0));
    private static final ModelPartData RIGHT_GUARD_TOP = new ModelPartData("rightGuardTop", CubeListBuilder.create()
          .texOffs(87, 31)
          .addBox(-3.95F, 3, -5, 3, 4, 2),
          PartPose.rotation(0.2094395F, 0, 0));
    private static final ModelPartData MIDDLE_PLATE = new ModelPartData("middlePlate", CubeListBuilder.create()
          .texOffs(93, 20)
          .addBox(-1.5F, 3, -6.2F, 3, 5, 3),
          PartPose.rotation(0.2094395F, 0, 0));
    private static final ModelPartData RIGHT_GUARD_BOT = new ModelPartData("rightGuardBot", CubeListBuilder.create()
          .texOffs(84, 30)
          .addBox(-3.5F, 5.5F, -6.5F, 2, 2, 2),
          PartPose.rotation(0.4712389F, 0, 0));
    private static final ModelPartData LEFT_GUARD_BOT = new ModelPartData("leftGuardBot", CubeListBuilder.create()
          .texOffs(84, 30)
          .addBox(1.5F, 5.5F, -6.5F, 2, 2, 2),
          PartPose.rotation(0.4712389F, 0, 0));
    private static final ModelPartData RIGHT_LIGHT = new ModelPartData("rightLight", CubeListBuilder.create()
          .texOffs(81, 0)
          .addBox(-3, 4, -4.5F, 1, 3, 1));
    private static final ModelPartData LEFT_LIGHT = new ModelPartData("leftLight", CubeListBuilder.create()
          .texOffs(81, 0)
          .addBox(2, 4, -4.5F, 1, 3, 1));

    public static LayerDefinition createLayerDefinition() {
        return createLayerDefinition(128, 64, PACK_TOP, PACK_BOTTOM, THRUSTER_LEFT, THRUSTER_RIGHT,
              FUEL_TUBE_RIGHT, FUEL_TUBE_LEFT, PACK_MID, PACK_CORE, WING_SUPPORT_L, WING_SUPPORT_R, PACK_TOP_REAR,
              EXTENDO_SUPPORT_L, EXTENDO_SUPPORT_R, WING_BLADE_L, WING_BLADE_R, PACK_DOODAD_2, PACK_DOODAD_3,
              BOTTOM_THRUSTER, LIGHT_1, LIGHT_2, LIGHT_3, CHESTPLATE, LEFT_GUARD_TOP, RIGHT_GUARD_TOP, MIDDLE_PLATE,
              RIGHT_GUARD_BOT, LEFT_GUARD_BOT, RIGHT_LIGHT, LEFT_LIGHT);
    }

    private final List<ModelPart> armoredParts;
    private final List<ModelPart> armoredLights;

    public ModelArmoredJetpack(EntityModelSet entityModelSet) {
        this(entityModelSet.bakeLayer(ARMORED_JETPACK_LAYER));
    }

    private ModelArmoredJetpack(ModelPart root) {
        super(root);
        //Note: Parts are gotten by name and given our parts we override for super have the same name, we don't have to inject them elsewhere
        armoredParts = getRenderableParts(root, CHESTPLATE, LEFT_GUARD_TOP, RIGHT_GUARD_TOP, MIDDLE_PLATE, RIGHT_GUARD_BOT, LEFT_GUARD_BOT);
        armoredLights = getRenderableParts(root, RIGHT_LIGHT, LEFT_LIGHT);
    }

    @Override
    public void renderToBuffer(@Nonnull PoseStack poseStack, @Nonnull VertexConsumer vertexConsumer, int light, int overlayLight, float red, float green, float blue, float alpha) {
        super.renderToBuffer(poseStack, vertexConsumer, light, overlayLight, red, green, blue, alpha);
        poseStack.pushPose();
        poseStack.translate(0, 0, -0.0625);
        renderPartsToBuffer(armoredParts, poseStack, vertexConsumer, light, overlayLight, red, green, blue, alpha);
        renderPartsToBuffer(armoredLights, poseStack, vertexConsumer, MekanismRenderer.FULL_LIGHT, overlayLight, red, green, blue, alpha);
        poseStack.popPose();
    }
}