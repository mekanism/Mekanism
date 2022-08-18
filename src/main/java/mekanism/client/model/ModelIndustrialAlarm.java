package mekanism.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mekanism.client.render.MekanismRenderType;
import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class ModelIndustrialAlarm extends MekanismJavaModel {

    public static final ModelLayerLocation ALARM_LAYER = new ModelLayerLocation(Mekanism.rl("industrial_alarm"), "main");
    private static final ResourceLocation TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "industrial_alarm.png");
    private static final ResourceLocation TEXTURE_ACTIVE = MekanismUtils.getResource(ResourceType.RENDER, "industrial_alarm_active.png");
    private static final RenderType BASE_RENDER_TYPE = RenderType.entitySolid(TEXTURE);

    private static final ModelPartData BASE = new ModelPartData("base", CubeListBuilder.create()
          .texOffs(0, 9)
          .addBox(-3F, 0F, -3F, 6, 1, 6));
    private static final ModelPartData BULB = new ModelPartData("bulb", CubeListBuilder.create()
          .texOffs(16, 0)
          .addBox(-1F, 1F, -1F, 2, 3, 2));
    private static final ModelPartData LIGHT_BOX = new ModelPartData("light_box", CubeListBuilder.create()
          .addBox(-2F, 1F, -2F, 4, 4, 4, new CubeDeformation(0.01F)));
    private static final ModelPartData AURA = new ModelPartData("aura", CubeListBuilder.create()
          .texOffs(0, 16)
          .addBox(-6F, 2F, -1F, 12, 1, 2, new CubeDeformation(0.01F)));

    public static LayerDefinition createLayerDefinition() {
        return createLayerDefinition(64, 64, BASE, BULB, LIGHT_BOX, AURA);
    }

    private final RenderType RENDER_TYPE = renderType(TEXTURE);
    private final RenderType RENDER_TYPE_ACTIVE = renderType(TEXTURE_ACTIVE);
    private final ModelPart base;
    private final ModelPart bulb;
    private final ModelPart lightBox;
    private final ModelPart aura;

    public ModelIndustrialAlarm(EntityModelSet entityModelSet, boolean item) {
        super(item ? MekanismRenderType.ALARM : MekanismRenderType.ALARM_TRANSLUCENT_TARGET);
        ModelPart root = entityModelSet.bakeLayer(ALARM_LAYER);
        base = BASE.getFromRoot(root);
        bulb = BULB.getFromRoot(root);
        lightBox = LIGHT_BOX.getFromRoot(root);
        aura = AURA.getFromRoot(root);
    }

    public void renderItem(@NotNull PoseStack matrix, @NotNull MultiBufferSource renderer, int light, int overlayLight, boolean hasEffect) {
        base.render(matrix, getVertexConsumer(renderer, BASE_RENDER_TYPE, hasEffect), light, overlayLight, 1, 1, 1, 1);
        renderToBuffer(matrix, getVertexConsumer(renderer, getRenderType(false), hasEffect), light, overlayLight, 1, 1, 1, 1);
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack matrix, @NotNull VertexConsumer vertexBuilder, int light, int overlayLight, float red, float green, float blue,
          float alpha) {
        render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha, false, 0);
    }

    public RenderType getRenderType(boolean active) {
        return active ? RENDER_TYPE_ACTIVE : RENDER_TYPE;
    }

    public void render(@NotNull PoseStack matrix, @NotNull VertexConsumer vertexBuilder, int light, int overlayLight, float red, float green, float blue, float alpha,
          boolean active, float rotation) {
        if (active) {
            light = LightTexture.FULL_BRIGHT;
            float yRot = rotation * Mth.DEG_TO_RAD;
            setRotation(aura, 0, yRot, 0);
            setRotation(bulb, 0, yRot, 0);
        } else {
            setRotation(aura, 0, 0, 0);
            setRotation(bulb, 0, 0, 0);
        }
        float bulbAlpha = 0.3F + (Math.abs(((rotation * 2) % 360) - 180F) / 180F) * 0.7F;
        bulb.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, bulbAlpha);
        lightBox.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        if (active) {
            aura.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, bulbAlpha);
        }
    }
}
