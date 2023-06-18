package mekanism.client.render.item.gear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mekanism.client.model.ModelAtomicDisassembler;
import mekanism.client.render.item.MekanismISTER;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RenderAtomicDisassembler extends MekanismISTER {

    public static final RenderAtomicDisassembler RENDERER = new RenderAtomicDisassembler();
    private ModelAtomicDisassembler atomicDisassembler;

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        atomicDisassembler = new ModelAtomicDisassembler(getEntityModels());
    }

    @Override
    public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext displayContext, @NotNull PoseStack matrix, @NotNull MultiBufferSource renderer,
          int light, int overlayLight) {
        matrix.pushPose();
        matrix.translate(0.5, 0.5, 0.5);
        matrix.mulPose(Axis.ZP.rotationDegrees(180));
        atomicDisassembler.render(matrix, renderer, light, overlayLight, stack.hasFoil());
        matrix.popPose();
    }
}