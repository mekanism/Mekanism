package mekanism.client.render.item.block;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelIndustrialAlarm;
import mekanism.client.render.item.MekanismISTER;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;

public class RenderIndustrialAlarmItem extends MekanismISTER {

    public static final RenderIndustrialAlarmItem RENDERER = new RenderIndustrialAlarmItem();
    private ModelIndustrialAlarm industrialAlarm;

    @Override
    public void onResourceManagerReload(@Nonnull ResourceManager resourceManager) {
        industrialAlarm = new ModelIndustrialAlarm(getEntityModels());
    }

    @Override
    public void renderByItem(@Nonnull ItemStack stack, @Nonnull TransformType transformType, @Nonnull PoseStack matrix, @Nonnull MultiBufferSource renderer, int light, int overlayLight) {
        matrix.pushPose();
        matrix.translate(0.5, 0.3, 0.5);
        industrialAlarm.render(matrix, renderer, light, overlayLight, false, 0, true, stack.hasFoil());
        matrix.popPose();
    }
}