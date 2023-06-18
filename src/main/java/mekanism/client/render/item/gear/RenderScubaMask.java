package mekanism.client.render.item.gear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mekanism.client.model.ModelScubaMask;
import mekanism.client.render.item.MekanismISTER;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RenderScubaMask extends MekanismISTER {

    public static final RenderScubaMask RENDERER = new RenderScubaMask();
    private ModelScubaMask scubaMask;

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        scubaMask = new ModelScubaMask(getEntityModels());
    }

    @Override
    public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext displayContext, @NotNull PoseStack matrix, @NotNull MultiBufferSource renderer,
          int light, int overlayLight) {
        matrix.pushPose();
        matrix.translate(0.5, 0.5, 0.5);
        matrix.mulPose(Axis.ZP.rotationDegrees(180));
        scubaMask.render(matrix, renderer, light, overlayLight, stack.hasFoil());
        matrix.popPose();
    }
}