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
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class ModelIndustrialAlarm extends MekanismJavaModel {

    public static final ModelLayerLocation ALARM_LAYER = new ModelLayerLocation(Mekanism.rl("industrial_alarm"), "main");
    private static final ResourceLocation TEXTURE_ACTIVE = MekanismUtils.getResource(ResourceType.RENDER, "industrial_alarm_active.png");

    private static final ModelPartData BULB = new ModelPartData("bulb", CubeListBuilder.create()
          .texOffs(16, 0)
          .addBox(-1F, 1F, -1F, 2, 3, 2));
    private static final ModelPartData LIGHT_BOX = new ModelPartData("light_box", CubeListBuilder.create()
          .addBox(-2F, 1F, -2F, 4, 4, 4, new CubeDeformation(0.01F)));
    private static final ModelPartData AURA = new ModelPartData("aura", CubeListBuilder.create()
          .texOffs(0, 16)
          .addBox(-6F, 2F, -1F, 12, 1, 2, new CubeDeformation(0.01F)));

    public static LayerDefinition createLayerDefinition() {
        return createLayerDefinition(64, 64, BULB, LIGHT_BOX, AURA);
    }

    private final RenderType RENDER_TYPE = renderType(TEXTURE_ACTIVE);
    private final ModelPart bulb;
    private final ModelPart lightBox;
    private final ModelPart aura;

    public ModelIndustrialAlarm(EntityModelSet entityModelSet) {
        super(MekanismRenderType.ALARM);
        ModelPart root = entityModelSet.bakeLayer(ALARM_LAYER);
        bulb = BULB.getFromRoot(root);
        lightBox = LIGHT_BOX.getFromRoot(root);
        aura = AURA.getFromRoot(root);
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack matrix, @NotNull VertexConsumer vertexBuilder, int light, int overlayLight, int color) {
        render(matrix, vertexBuilder, LightTexture.FULL_BRIGHT, overlayLight, color, 0);
    }

    public RenderType getRenderType() {
        return RENDER_TYPE;
    }

    public void render(@NotNull PoseStack matrix, @NotNull VertexConsumer vertexBuilder, int light, int overlayLight, int color, float rotation) {
        float yRot = rotation * Mth.DEG_TO_RAD;
        setRotation(aura, 0, yRot, 0);
        setRotation(bulb, 0, yRot, 0);
        float bulbAlpha = 0.3F + (Math.abs(((rotation * 2) % 360) - 180F) / 180F) * 0.7F;
        int bulbColor = FastColor.ARGB32.color(FastColor.as8BitChannel(bulbAlpha), color);
        bulb.render(matrix, vertexBuilder, light, overlayLight, bulbColor);
        lightBox.render(matrix, vertexBuilder, light, overlayLight, color);
        aura.render(matrix, vertexBuilder, light, overlayLight, bulbColor);
    }
}
