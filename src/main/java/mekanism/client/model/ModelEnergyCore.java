package mekanism.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.api.tier.BaseTier;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.tileentity.RenderEnergyCube;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.NotNull;

//TODO - 1.21.11: Remove this in favor of just rendering a model part?
public class ModelEnergyCore extends MekanismJavaModel<Unit> {

    private static final Identifier CORE_TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "energy_core.png");

    public static final RenderType BATCHED_RENDER_TYPE = MekanismRenderType.STANDARD_TRANSLUCENT_TARGET.apply(CORE_TEXTURE);
    public static final RenderType RENDER_TYPE = MekanismRenderType.STANDARD.apply(CORE_TEXTURE);

    public ModelEnergyCore(EntityModelSet entityModelSet) {
        super(entityModelSet.bakeLayer(RenderEnergyCube.CORE_LAYER), MekanismRenderType.STANDARD);
    }

    public void render(@NotNull PoseStack matrix, @NotNull MultiBufferSource renderer, int light, int overlayLight, BaseTier baseTier, float energyPercentage) {
        renderToBuffer(matrix, renderer.getBuffer(RENDER_TYPE), light, overlayLight, baseTier.getPackedColor(ARGB.as8BitChannel(energyPercentage)));
    }
}