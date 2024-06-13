package mekanism.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
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
import org.jetbrains.annotations.NotNull;

public class ModelFlamethrower extends MekanismJavaModel {

    public static final ModelLayerLocation FLAMETHROWER_LAYER = new ModelLayerLocation(Mekanism.rl("flamethrower"), "main");
    private static final ResourceLocation FLAMETHROWER_TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "flamethrower.png");

    private static final ModelPartData RING_BOTTOM = new ModelPartData("RingBottom", CubeListBuilder.create()
          .texOffs(19, 14)
          .addBox(0F, 0F, 0F, 3, 1, 3),
          PartPose.offset(-2F, 19.5F, 1.5F));
    private static final ModelPartData RING_TOP = new ModelPartData("RingTop", CubeListBuilder.create()
          .texOffs(19, 14)
          .addBox(0F, 0F, 0F, 3, 1, 3),
          PartPose.offset(-2F, 13.5F, 1.466667F));
    private static final ModelPartData RING = new ModelPartData("Ring", CubeListBuilder.create()
          .texOffs(0, 14)
          .addBox(0F, 0F, 0F, 5, 6, 4),
          PartPose.offset(-3F, 14F, 1F));
    private static final ModelPartData AXLE = new ModelPartData("Axle", CubeListBuilder.create()
          .texOffs(32, 12)
          .addBox(0F, 0F, 0F, 4, 4, 7),
          PartPose.offset(-2.5F, 15F, -6.5F));
    private static final ModelPartData AXLE_B_LEFT = new ModelPartData("AxleBLeft", CubeListBuilder.create()
          .texOffs(0, 25)
          .addBox(-0.5F, -0.5F, 0F, 1, 1, 8),
          PartPose.offsetAndRotation(-2F, 19F, -7F, 0F, 0F, 0.2094395F));
    private static final ModelPartData AXLE_B_RIGHT = new ModelPartData("AxleBRight", CubeListBuilder.create()
          .texOffs(0, 25)
          .addBox(-0.5F, -0.5F, 0F, 1, 1, 8),
          PartPose.offsetAndRotation(1F, 19F, -7F, 0.0174533F, 0F, -0.2094395F));
    private static final ModelPartData AXLE_T_RIGHT = new ModelPartData("AxleTRight", CubeListBuilder.create()
          .texOffs(0, 25)
          .addBox(-0.5F, -0.5F, 0F, 1, 1, 8),
          PartPose.offsetAndRotation(1F, 15F, -7F, 0F, 0F, 0.2094395F));
    private static final ModelPartData AXLE_T_LEFT = new ModelPartData("AxleTLeft", CubeListBuilder.create()
          .texOffs(0, 25)
          .addBox(-0.5F, -0.5F, 0F, 1, 1, 8),
          PartPose.offsetAndRotation(-2F, 15F, -7F, 0F, 0F, -0.2094395F));
    private static final ModelPartData GRASP = new ModelPartData("Grasp", CubeListBuilder.create()
          .texOffs(24, 19)
          .addBox(0F, 0F, 0F, 2, 1, 1),
          PartPose.offsetAndRotation(-1.5F, 13F, -1.1F, 0.7807508F, 0F, 0F));
    private static final ModelPartData GRASP_ROD = new ModelPartData("GraspRod", CubeListBuilder.create()
          .texOffs(19, 19)
          .addBox(0F, 0F, 0F, 1, 3, 1),
          PartPose.offsetAndRotation(-1F, 13F, -1F, 0.2230717F, 0F, 0F));
    private static final ModelPartData SUPPORT_CENTER = new ModelPartData("SupportCenter", CubeListBuilder.create()
          .texOffs(0, 40)
          .addBox(0F, 0F, 0F, 2, 1, 6),
          PartPose.offsetAndRotation(-1.5F, 12.4F, 6.6F, -0.1115358F, 0F, 0F));
    private static final ModelPartData SUPPORT_FRONT = new ModelPartData("SupportFront", CubeListBuilder.create()
          .texOffs(19, 24)
          .addBox(0F, 0F, 0F, 1, 1, 4),
          PartPose.offsetAndRotation(-1F, 13.1F, 12.5F, -1.226894F, 0F, 0F));
    private static final ModelPartData SUPPORT_REAR = new ModelPartData("SupportRear", CubeListBuilder.create()
          .texOffs(0, 35)
          .addBox(0F, 0F, 0F, 3, 1, 3),
          PartPose.offsetAndRotation(-2F, 14F, 4F, 0.5424979F, 0F, 0F));
    private static final ModelPartData LARGE_BARREL = new ModelPartData("LargeBarrel", CubeListBuilder.create()
          .texOffs(19, 48)
          .addBox(0F, 0F, 0F, 2, 3, 7),
          PartPose.offset(-1.5F, 16F, 4F));
    private static final ModelPartData LARGE_BARREL_DECOR = new ModelPartData("LargeBarrelDecor", CubeListBuilder.create()
          .texOffs(0, 48)
          .addBox(0F, 0F, 0F, 3, 3, 6),
          PartPose.offsetAndRotation(-2F, 15F, 4F, -0.1115358F, 0F, 0F));
    private static final ModelPartData LARGE_BARREL_DECOR_2 = new ModelPartData("LargeBarrelDecor2", CubeListBuilder.create()
          .texOffs(17, 41)
          .addBox(0F, 0F, 0F, 4, 2, 4),
          PartPose.offset(-2.5F, 16F, 4F));
    private static final ModelPartData BARREL = new ModelPartData("Barrel", CubeListBuilder.create()
          .texOffs(19, 30)
          .addBox(0F, 0F, 0F, 2, 2, 8),
          PartPose.offset(-1.5F, 16.5F, 11F));
    private static final ModelPartData BARREL_RING = new ModelPartData("BarrelRing", CubeListBuilder.create()
          .texOffs(30, 25)
          .addBox(0F, 0F, 0F, 3, 3, 1),
          PartPose.offset(-2F, 16F, 13F));
    private static final ModelPartData BARREL_RING_2 = new ModelPartData("BarrelRing2", CubeListBuilder.create()
          .texOffs(30, 25)
          .addBox(0F, 0F, 0F, 3, 3, 1),
          PartPose.offset(-2F, 16F, 17F));
    private static final ModelPartData FLAME = new ModelPartData("Flame", CubeListBuilder.create()
          .texOffs(38, 0)
          .addBox(0F, 0F, 0F, 1, 1, 2),
          PartPose.offsetAndRotation(-1F, 19.5F, 19F, 0.7063936F, 0F, 0F));
    private static final ModelPartData FLAME_STRUT = new ModelPartData("FlameStrut", CubeListBuilder.create()
          .texOffs(27, 0)
          .addBox(0F, 0F, 0F, 2, 1, 3),
          PartPose.offsetAndRotation(-1.466667F, 18.5F, 17F, -0.2602503F, 0F, 0F));
    private static final ModelPartData HYDROGEN_DECOR = new ModelPartData("HydrogenDecor", CubeListBuilder.create()
          .texOffs(27, 5)
          .addBox(0F, 0F, 0F, 3, 1, 5),
          PartPose.offsetAndRotation(1.5F, 15.66667F, -4.933333F, 0F, 0F, 0.4438713F));
    private static final ModelPartData HYDROGEN = new ModelPartData("Hydrogen", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 3, 3, 10),
          PartPose.offsetAndRotation(1.5F, 16F, -5.5F, 0F, 0F, 0.4438713F));

    public static LayerDefinition createLayerDefinition() {
        return createLayerDefinition(64, 64, RING_BOTTOM, RING_TOP, RING, AXLE, AXLE_B_LEFT, AXLE_B_RIGHT, AXLE_T_RIGHT, AXLE_T_LEFT,
              GRASP, GRASP_ROD, SUPPORT_CENTER, SUPPORT_FRONT, SUPPORT_REAR, LARGE_BARREL, LARGE_BARREL_DECOR, LARGE_BARREL_DECOR_2, BARREL, BARREL_RING,
              BARREL_RING_2, FLAME, FLAME_STRUT, HYDROGEN_DECOR, HYDROGEN);
    }

    private final RenderType RENDER_TYPE = renderType(FLAMETHROWER_TEXTURE);
    private final List<ModelPart> parts;

    public ModelFlamethrower(EntityModelSet entityModelSet) {
        super(RenderType::entitySolid);
        ModelPart root = entityModelSet.bakeLayer(FLAMETHROWER_LAYER);
        parts = getRenderableParts(root, RING_BOTTOM, RING_TOP, RING, AXLE, AXLE_B_LEFT, AXLE_B_RIGHT, AXLE_T_RIGHT, AXLE_T_LEFT, GRASP, GRASP_ROD,
              SUPPORT_CENTER, SUPPORT_FRONT, SUPPORT_REAR, LARGE_BARREL, LARGE_BARREL_DECOR, LARGE_BARREL_DECOR_2, BARREL, BARREL_RING, BARREL_RING_2,
              FLAME, FLAME_STRUT, HYDROGEN_DECOR, HYDROGEN);
    }

    public void render(@NotNull PoseStack matrix, @NotNull MultiBufferSource renderer, int light, int overlayLight, boolean hasEffect) {
        renderToBuffer(matrix, getVertexConsumer(renderer, RENDER_TYPE, hasEffect), light, overlayLight, 0xFFFFFFFF);
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int light, int overlayLight, int color) {
        renderPartsToBuffer(parts, poseStack, vertexConsumer, light, overlayLight, color);
    }
}