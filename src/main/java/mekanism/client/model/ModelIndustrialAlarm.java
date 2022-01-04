package mekanism.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class ModelIndustrialAlarm extends MekanismJavaModel {

    public static final ModelLayerLocation ALARM_LAYER = new ModelLayerLocation(Mekanism.rl("industrial_alarm"), "main");
    private static final ResourceLocation TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "industrial_alarm.png");
    private static final ResourceLocation TEXTURE_ACTIVE = MekanismUtils.getResource(ResourceType.RENDER, "industrial_alarm_active.png");

    private static final ModelPartData BASE = new ModelPartData("base", CubeListBuilder.create()
          .texOffs(0, 9)
          .addBox(-3F, 0F, -3F, 6, 1, 6));
    private static final ModelPartData BULB = new ModelPartData("bulb", CubeListBuilder.create()
          .texOffs(16, 0)
          .addBox(-1F, 1F, -1F, 2, 3, 2));
    private static final ModelPartData LIGHT_BOX = new ModelPartData("light_box", CubeListBuilder.create()
          .addBox(-2F, 1F, -2F, 4, 4, 4));
    private static final ModelPartData AURA = new ModelPartData("aura", CubeListBuilder.create()
          .texOffs(0, 16)
          .addBox(-6F, 2F, -1F, 12, 1, 2));

    public static LayerDefinition createLayerDefinition() {
        return createLayerDefinition(64, 64, BASE, BULB, LIGHT_BOX, AURA);
    }

    private final RenderType RENDER_TYPE = MekanismRenderType.standard(TEXTURE);
    private final RenderType RENDER_TYPE_ACTIVE = MekanismRenderType.standard(TEXTURE_ACTIVE);
    private final ModelPart base;
    private final ModelPart bulb;
    private final ModelPart lightBox;
    private final ModelPart aura;

    public ModelIndustrialAlarm(EntityModelSet entityModelSet) {
        super(RenderType::entitySolid);
        ModelPart root = entityModelSet.bakeLayer(ALARM_LAYER);
        base = BASE.getFromRoot(root);
        bulb = BULB.getFromRoot(root);
        lightBox = LIGHT_BOX.getFromRoot(root);
        aura = AURA.getFromRoot(root);
    }

    public void render(@Nonnull PoseStack matrix, @Nonnull MultiBufferSource renderer, int light, int overlayLight, boolean active, float rotation, boolean renderBase,
          boolean hasEffect) {
        render(matrix, getVertexConsumer(renderer, active ? RENDER_TYPE_ACTIVE : RENDER_TYPE, hasEffect), light, overlayLight, 1, 1, 1, 1,
              active, rotation, renderBase);
    }

    @Override
    public void renderToBuffer(@Nonnull PoseStack matrix, @Nonnull VertexConsumer vertexBuilder, int light, int overlayLight, float red, float green, float blue,
          float alpha) {
        render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha, false, 0, false);
    }

    private void render(@Nonnull PoseStack matrix, @Nonnull VertexConsumer vertexBuilder, int light, int overlayLight, float red, float green, float blue, float alpha,
          boolean active, float rotation, boolean renderBase) {
        if (renderBase) {
            base.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        }
        if (active) {
            setRotation(aura, 0, (float) Math.toRadians(rotation), 0);
            setRotation(bulb, 0, (float) Math.toRadians(rotation), 0);
        } else {
            setRotation(aura, 0, 0, 0);
            setRotation(bulb, 0, 0, 0);
        }
        float bulbAlpha = 0.3F + (Math.abs(((rotation * 2) % 360) - 180F) / 180F) * 0.7F;
        bulb.render(matrix, vertexBuilder, active ? MekanismRenderer.FULL_LIGHT : light, overlayLight, red, green, blue, bulbAlpha);
        lightBox.render(matrix, vertexBuilder, active ? MekanismRenderer.FULL_LIGHT : light, overlayLight, red, green, blue, alpha);
        if (!renderBase) {
            aura.render(matrix, vertexBuilder, MekanismRenderer.FULL_LIGHT, overlayLight, red, green, blue, bulbAlpha);
        }
    }
}
