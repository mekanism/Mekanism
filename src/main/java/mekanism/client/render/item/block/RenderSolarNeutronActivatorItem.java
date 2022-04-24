package mekanism.client.render.item.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelSolarNeutronActivator;
import mekanism.client.render.item.MekanismISTER;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;

public class RenderSolarNeutronActivatorItem extends MekanismISTER {

    public static final RenderSolarNeutronActivatorItem RENDERER = new RenderSolarNeutronActivatorItem();
    private ModelSolarNeutronActivator solarNeutronActivator;

    @Override
    public void onResourceManagerReload(@Nonnull ResourceManager resourceManager) {
        solarNeutronActivator = new ModelSolarNeutronActivator(getEntityModels());
    }

    @Override
    public void renderByItem(@Nonnull ItemStack stack, @Nonnull TransformType transformType, @Nonnull PoseStack matrix, @Nonnull MultiBufferSource renderer, int light, int overlayLight) {
        matrix.pushPose();
        matrix.translate(0.5, 0.5, 0.5);
        matrix.mulPose(Vector3f.ZP.rotationDegrees(180));
        matrix.translate(0, -0.55, 0);
        solarNeutronActivator.render(matrix, renderer, light, overlayLight, stack.hasFoil());
        matrix.popPose();
    }
}