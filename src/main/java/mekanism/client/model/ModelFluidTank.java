package mekanism.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.tier.FluidTankTier;
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

//TODO: Replace usage of this by using the json model and drawing fluid inside of it?
public class ModelFluidTank extends MekanismJavaModel {

    public static final ModelLayerLocation TANK_LAYER = new ModelLayerLocation(Mekanism.rl("fluid_tank"), "main");
    private static final ResourceLocation TANK_TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "fluid_tank.png");

    private static final ModelPartData BASE = new ModelPartData("Base", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 12, 1, 12),
          PartPose.offset(-6F, 23F, -6F));
    private static final ModelPartData POLE_F_L = new ModelPartData("PoleFL", CubeListBuilder.create()
          .texOffs(48, 0)
          .addBox(0F, 0F, 0F, 1, 14, 1),
          PartPose.offset(5F, 9F, -6F));
    private static final ModelPartData POLE_L_B = new ModelPartData("PoleLB", CubeListBuilder.create()
          .texOffs(48, 0)
          .addBox(0F, 0F, 0F, 1, 14, 1),
          PartPose.offset(5F, 9F, 5F));
    private static final ModelPartData POLE_B_R = new ModelPartData("PoleBR", CubeListBuilder.create()
          .texOffs(48, 0)
          .addBox(0F, 0F, 0F, 1, 14, 1),
          PartPose.offset(-6F, 9F, 5F));
    private static final ModelPartData POLE_R_F = new ModelPartData("PoleRF", CubeListBuilder.create()
          .texOffs(48, 0)
          .addBox(0F, 0F, 0F, 1, 14, 1),
          PartPose.offset(-6F, 9F, -6F));
    private static final ModelPartData TOP = new ModelPartData("Top", CubeListBuilder.create()
          .addBox(0F, 0F, 0F, 12, 1, 12),
          PartPose.offset(-6F, 8F, -6F));
    private static final ModelPartData FRONT_GLASS = new ModelPartData("FrontGlass", CubeListBuilder.create()
          .texOffs(0, 13)
          .addBox(0F, 0F, 0F, 10, 14, 1),
          PartPose.offset(-5F, 9F, -6F));
    private static final ModelPartData BACK_GLASS = new ModelPartData("BackGlass", CubeListBuilder.create()
          .texOffs(0, 28)
          .addBox(0F, 0F, 3F, 10, 14, 1),
          PartPose.offset(-5F, 9F, 2F));
    private static final ModelPartData RIGHT_GLASS = new ModelPartData("RightGlass", CubeListBuilder.create()
          .texOffs(22, 13)
          .addBox(0F, 0F, 0F, 1, 14, 10),
          PartPose.offset(-6F, 9F, -5F));
    private static final ModelPartData LEFT_GLASS = new ModelPartData("LeftGlass", CubeListBuilder.create()
          .texOffs(22, 37)
          .addBox(0F, 0F, 0F, 1, 14, 10),
          PartPose.offset(5F, 9F, -5F));

    public static LayerDefinition createLayerDefinition() {
        return createLayerDefinition(128, 128, BASE, POLE_F_L, POLE_L_B, POLE_B_R, POLE_R_F, TOP, FRONT_GLASS, BACK_GLASS, RIGHT_GLASS, LEFT_GLASS);
    }

    private final RenderType GLASS_RENDER_TYPE = RenderType.entityCutout(TANK_TEXTURE);
    private final RenderType RENDER_TYPE = renderType(TANK_TEXTURE);
    private final List<ModelPart> parts;
    private final List<ModelPart> glass;

    public ModelFluidTank(EntityModelSet entityModelSet) {
        super(RenderType::entitySolid);
        ModelPart root = entityModelSet.bakeLayer(TANK_LAYER);
        parts = getRenderableParts(root, BASE, POLE_F_L, POLE_L_B, POLE_B_R, POLE_R_F, TOP);
        glass = getRenderableParts(root, FRONT_GLASS, BACK_GLASS, RIGHT_GLASS, LEFT_GLASS);
    }

    public void render(@Nonnull PoseStack matrix, @Nonnull MultiBufferSource renderer, int light, int overlayLight, FluidTankTier tier, boolean hasEffect) {
        renderToBuffer(matrix, getVertexConsumer(renderer, RENDER_TYPE, hasEffect), light, overlayLight, 1, 1, 1, 1);
        EnumColor color = tier.getBaseTier().getColor();
        //TODO: Try to make it so the lines can still show up on the back walls of the tank in first person
        renderPartsToBuffer(glass, matrix, getVertexConsumer(renderer, GLASS_RENDER_TYPE, hasEffect), light, overlayLight, color.getColor(0), color.getColor(1), color.getColor(2), 1);
    }

    @Override
    public void renderToBuffer(@Nonnull PoseStack poseStack, @Nonnull VertexConsumer vertexConsumer, int light, int overlayLight, float red, float green, float blue, float alpha) {
        renderPartsToBuffer(parts, poseStack, vertexConsumer, light, overlayLight, red, green, blue, alpha);
    }
}