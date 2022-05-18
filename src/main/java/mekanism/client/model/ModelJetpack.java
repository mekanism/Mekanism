package mekanism.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class ModelJetpack extends MekanismJavaModel {

    public static final ModelLayerLocation JETPACK_LAYER = new ModelLayerLocation(Mekanism.rl("jetpack"), "main");
    private static final ResourceLocation JETPACK_TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "jetpack.png");

    protected static final ModelPartData PACK_TOP = new ModelPartData("packTop", CubeListBuilder.create()
          .texOffs(92, 28)
          .addBox(-4, 0, 4, 8, 4, 1),
          PartPose.rotation(0.2094395F, 0, 0));
    protected static final ModelPartData PACK_BOTTOM = new ModelPartData("packBottom", CubeListBuilder.create()
          .texOffs(92, 42)
          .addBox(-4, 4.1F, 1.5F, 8, 4, 4),
          PartPose.rotation(-0.0872665F, 0, 0));
    protected static final ModelPartData PACK_MID = new ModelPartData("packMid", CubeListBuilder.create()
          .texOffs(92, 34)
          .addBox(-4, 3.3F, 1.5F, 8, 1, 4));
    protected static final ModelPartData PACK_CORE = new ModelPartData("packCore", CubeListBuilder.create()
          .texOffs(69, 2)
          .addBox(-3.5F, 3, 2, 7, 1, 3));
    protected static final ModelPartData WING_SUPPORT_L = new ModelPartData("wingSupportL", CubeListBuilder.create()
          .texOffs(71, 55)
          .addBox(3, -1, 2.2F, 7, 2, 2),
          PartPose.rotation(0, 0, 0.2792527F));
    protected static final ModelPartData WING_SUPPORT_R = new ModelPartData("wingSupportR", CubeListBuilder.create()
          .texOffs(71, 55)
          .addBox(-10, -1, 2.2F, 7, 2, 2),
          PartPose.rotation(0, 0, -0.2792527F));
    protected static final ModelPartData PACK_TOP_REAR = new ModelPartData("packTopRear", CubeListBuilder.create()
          .texOffs(106, 28)
          .addBox(-4, 1, 1, 8, 3, 3),
          PartPose.rotation(0.2094395F, 0, 0));
    protected static final ModelPartData EXTENDO_SUPPORT_L = new ModelPartData("extendoSupportL", CubeListBuilder.create()
          .texOffs(94, 16)
          .addBox(8, -0.2F, 2.5F, 9, 1, 1),
          PartPose.rotation(0, 0, 0.2792527F));
    protected static final ModelPartData EXTENDO_SUPPORT_R = new ModelPartData("extendoSupportR", CubeListBuilder.create()
          .texOffs(94, 16)
          .addBox(-17, -0.2F, 2.5F, 9, 1, 1),
          PartPose.rotation(0, 0, -0.2792527F));
    protected static final ModelPartData WING_BLADE_L = new ModelPartData("wingBladeL", CubeListBuilder.create()
          .texOffs(62, 5)
          .addBox(3.3F, 1.1F, 3, 14, 2, 0),
          PartPose.rotation(0, 0, 0.2094395F));
    protected static final ModelPartData WING_BLADE_R = new ModelPartData("wingBladeR", CubeListBuilder.create()
          .texOffs(62, 5)
          .addBox(-17.3F, 1.1F, 3, 14, 2, 0),
          PartPose.rotation(0, 0, -0.2094395F));
    protected static final ModelPartData PACK_DOODAD_2 = new ModelPartData("packDoodad2", CubeListBuilder.create()
          .texOffs(116, 0)
          .addBox(1, 0.5F, 4.2F, 2, 1, 1),
          PartPose.rotation(0.2094395F, 0, 0));
    protected static final ModelPartData PACK_DOODAD_3 = new ModelPartData("packDoodad3", CubeListBuilder.create()
          .texOffs(116, 0)
          .addBox(1, 2, 4.2F, 2, 1, 1),
          PartPose.rotation(0.2094395F, 0, 0));
    protected static final ModelPartData BOTTOM_THRUSTER = new ModelPartData("bottomThruster", CubeListBuilder.create()
          .texOffs(68, 26)
          .addBox(-3, 8, 2.333333F, 6, 1, 2));
    protected static final ModelPartData LIGHT_1 = new ModelPartData("light1", CubeListBuilder.create()
          .texOffs(55, 2)
          .addBox(2, 6.55F, 4, 1, 1, 1));
    protected static final ModelPartData LIGHT_2 = new ModelPartData("light2", CubeListBuilder.create()
          .texOffs(55, 2)
          .addBox(0, 6.55F, 4, 1, 1, 1));
    protected static final ModelPartData LIGHT_3 = new ModelPartData("light3", CubeListBuilder.create()
          .texOffs(55, 2)
          .addBox(-3, 6.55F, 4, 1, 1, 1));

    //Specific to the Jetpack, the armored jetpack uses different numbers
    private static final ModelPartData THRUSTER_LEFT = thrusterLeft(-3);
    private static final ModelPartData THRUSTER_RIGHT = thrusterRight(-3);
    private static final ModelPartData FUEL_TUBE_RIGHT = fuelTubeRight(-3);
    private static final ModelPartData FUEL_TUBE_LEFT = fuelTubeLeft(-3);

    public static LayerDefinition createLayerDefinition() {
        return createLayerDefinition(128, 64, PACK_TOP, PACK_BOTTOM, THRUSTER_LEFT, THRUSTER_RIGHT,
              FUEL_TUBE_RIGHT, FUEL_TUBE_LEFT, PACK_MID, PACK_CORE, WING_SUPPORT_L, WING_SUPPORT_R, PACK_TOP_REAR,
              EXTENDO_SUPPORT_L, EXTENDO_SUPPORT_R, WING_BLADE_L, WING_BLADE_R, PACK_DOODAD_2, PACK_DOODAD_3,
              BOTTOM_THRUSTER, LIGHT_1, LIGHT_2, LIGHT_3);
    }

    private final RenderType frameRenderType;
    private final RenderType wingRenderType;
    private final List<ModelPart> parts;
    private final List<ModelPart> litParts;
    private final List<ModelPart> wingParts;

    public ModelJetpack(EntityModelSet entityModelSet) {
        this(entityModelSet.bakeLayer(JETPACK_LAYER));
    }

    protected ModelJetpack(ModelPart root) {
        super(RenderType::entitySolid);
        this.frameRenderType = renderType(JETPACK_TEXTURE);
        this.wingRenderType = MekanismRenderType.standard(JETPACK_TEXTURE);
        parts = getRenderableParts(root, PACK_TOP, PACK_BOTTOM, THRUSTER_LEFT, THRUSTER_RIGHT, FUEL_TUBE_RIGHT, FUEL_TUBE_LEFT, PACK_MID,
              WING_SUPPORT_L, WING_SUPPORT_R, PACK_TOP_REAR, EXTENDO_SUPPORT_L, EXTENDO_SUPPORT_R, PACK_DOODAD_2, PACK_DOODAD_3, BOTTOM_THRUSTER);
        litParts = getRenderableParts(root, LIGHT_1, LIGHT_2, LIGHT_3, PACK_CORE);
        wingParts = getRenderableParts(root, WING_BLADE_L, WING_BLADE_R);
    }

    public void render(@Nonnull PoseStack poseStack, @Nonnull MultiBufferSource renderer, int light, int overlayLight, boolean hasEffect) {
        renderToBuffer(poseStack, getVertexConsumer(renderer, frameRenderType, hasEffect), light, overlayLight, 1, 1, 1, 1);
        renderPartsToBuffer(wingParts, poseStack, getVertexConsumer(renderer, wingRenderType, hasEffect), MekanismRenderer.FULL_LIGHT, overlayLight, 1, 1, 1, 0.2F);
    }

    @Override
    public void renderToBuffer(@Nonnull PoseStack poseStack, @Nonnull VertexConsumer vertexConsumer, int light, int overlayLight, float red, float green, float blue, float alpha) {
        renderPartsToBuffer(parts, poseStack, vertexConsumer, light, overlayLight, red, green, blue, alpha);
        renderPartsToBuffer(litParts, poseStack, vertexConsumer, MekanismRenderer.FULL_LIGHT, overlayLight, red, green, blue, alpha);
    }

    protected static ModelPartData thrusterLeft(float fuelZ) {
        return new ModelPartData("thrusterLeft", CubeListBuilder.create()
              .texOffs(69, 30)
              .addBox(7.8F, 1.5F, fuelZ - 0.5F, 3, 3, 3),
              PartPose.rotation(0.7853982F, -0.715585F, 0.3490659F));
    }

    protected static ModelPartData thrusterRight(float fuelZ) {
        return new ModelPartData("thrusterRight", CubeListBuilder.create()
              .texOffs(69, 30)
              .addBox(-10.8F, 1.5F, fuelZ - 0.5F, 3, 3, 3),
              PartPose.rotation(0.7853982F, 0.715585F, -0.3490659F));
    }

    protected static ModelPartData fuelTubeRight(float fuelZ) {
        return new ModelPartData("fuelTubeRight", CubeListBuilder.create()
              .texOffs(92, 23)
              .addBox(-11.2F, 2, fuelZ, 8, 2, 2),
              PartPose.rotation(0.7853982F, 0.715585F, -0.3490659F));
    }

    protected static ModelPartData fuelTubeLeft(float fuelZ) {
        return new ModelPartData("fuelTubeLeft", CubeListBuilder.create()
              .texOffs(92, 23)
              .addBox(3.2F, 2, fuelZ, 8, 2, 2),
              PartPose.rotation(0.7853982F, -0.715585F, 0.3490659F));
    }
}