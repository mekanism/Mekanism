package mekanism.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.model.ModelIndustrialAlarm.IndustrialAlarmRenderState;
import mekanism.client.render.MekanismRenderType;
import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;

public class ModelIndustrialAlarm extends MekanismJavaModel<IndustrialAlarmRenderState> {

    public static final ModelLayerLocation ALARM_LAYER = new ModelLayerLocation(Mekanism.rl("industrial_alarm"), "main");
    private static final Identifier TEXTURE_ACTIVE = MekanismUtils.getResource(ResourceType.RENDER, "industrial_alarm_active.png");

    private static final ModelPartData BULB = new ModelPartData("bulb", CubeListBuilder.create()
          .texOffs(16, 0)
          .addBox(-1F, 1F, -1F, 2, 3, 2));

    private static final ModelPartData AURA = new ModelPartData("aura", CubeListBuilder.create()
          .texOffs(0, 16)
          .addBox(-6F, 2F, -1F, 12, 1, 2, new CubeDeformation(0.01F)));

    public static LayerDefinition createLayerDefinition() {
        return createLayerDefinition(64, 64, BULB, AURA);
    }

    private final RenderType RENDER_TYPE = MekanismRenderType.ALARM.apply(TEXTURE_ACTIVE);

    public ModelIndustrialAlarm(EntityModelSet entityModelSet) {
        super(entityModelSet.bakeLayer(ALARM_LAYER));
    }

    public RenderType getRenderType() {
        return RENDER_TYPE;
    }

    @Override
    public void setupAnim(IndustrialAlarmRenderState state) {
        super.setupAnim(state);
        root.setRotation(0, state.rotation * Mth.DEG_TO_RAD, 0);
        //TODO - 26.1: Validate that we can just rotate the root and have it work for both the bulb and the aura
        /*float yRot = state.rotation * Mth.DEG_TO_RAD;
        aura.setRotation(0, yRot, 0);
        bulb.setRotation(0, yRot, 0);*/
    }

    @Override
    public void collect(IndustrialAlarmRenderState state, PoseStack poseStack, SubmitNodeCollector collector, int light, int overlayLight, boolean hasFoil) {
        setupAnim(state);
        collectParts(allParts, poseStack, RENDER_TYPE, collector, light, overlayLight, CommonColors.WHITE, null, hasFoil);
    }

    public static class IndustrialAlarmRenderState {

        private float rotation;
        private int tint = CommonColors.WHITE;

        public void setRotation(float rotation) {
            this.rotation = rotation;
            //Apply a changing alpha based on how far it is through the rotation
            //TODO - 26.1: See if there is a helper in Mth to do this calculation with
            this.tint = ARGB.white(0.3F + 0.7F * (Math.abs(((this.rotation * 2) % 360) - 180F) / 180F));
        }

        public int getTint() {
            return tint;
        }
    }
}
