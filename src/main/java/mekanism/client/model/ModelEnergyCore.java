package mekanism.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.api.tier.BaseTier;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.tileentity.RenderEnergyCube;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

//TODO - 26.1: Remove this in favor of just rendering a model part?
public class ModelEnergyCore extends MekanismJavaModel<Integer> {

    private static final Identifier CORE_TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "energy_core.png");

    public static final RenderType BATCHED_RENDER_TYPE = MekanismRenderType.STANDARD_TRANSLUCENT_TARGET.apply(CORE_TEXTURE);
    public static final RenderType RENDER_TYPE = MekanismRenderType.STANDARD.apply(CORE_TEXTURE);

    public ModelEnergyCore(EntityModelSet entityModelSet) {
        super(entityModelSet.bakeLayer(RenderEnergyCube.CORE_LAYER));
    }

    @Override
    public void collect(Integer color, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, int overlayLight) {
        setupAnim(color);
        collectParts(allParts, poseStack, RENDER_TYPE, submitNodeCollector, light, overlayLight, color, null);
    }

    public static Integer getState(BaseTier baseTier, float energyPercentage) {
        return baseTier.getPackedColor(ARGB.as8BitChannel(energyPercentage));
    }
}