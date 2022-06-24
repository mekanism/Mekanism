package mekanism.client.render.item.block;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.model.ModelIndustrialAlarm;
import mekanism.client.render.item.MekanismISTER;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RenderIndustrialAlarmItem extends MekanismISTER {

    public static final RenderIndustrialAlarmItem RENDERER = new RenderIndustrialAlarmItem();
    private ModelIndustrialAlarm industrialAlarm;

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        industrialAlarm = new ModelIndustrialAlarm(getEntityModels());
    }

    @Override
    public void renderByItem(@NotNull ItemStack stack, @NotNull TransformType transformType, @NotNull PoseStack matrix, @NotNull MultiBufferSource renderer, int light, int overlayLight) {
        matrix.pushPose();
        matrix.translate(0.5, 0.3, 0.5);
        industrialAlarm.render(matrix, renderer, light, overlayLight, false, 0, true, stack.hasFoil());
        matrix.popPose();
    }
}