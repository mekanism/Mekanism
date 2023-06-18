package mekanism.client.render.item.gear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mekanism.client.model.ModelArmoredFreeRunners;
import mekanism.client.model.ModelFreeRunners;
import mekanism.client.render.item.MekanismISTER;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RenderFreeRunners extends MekanismISTER {

    public static final RenderFreeRunners RENDERER = new RenderFreeRunners(false);
    public static final RenderFreeRunners ARMORED_RENDERER = new RenderFreeRunners(true);

    private final boolean armored;
    private ModelFreeRunners freeRunners;

    private RenderFreeRunners(boolean armored) {
        this.armored = armored;
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        if (armored) {
            freeRunners = new ModelArmoredFreeRunners(getEntityModels());
        } else {
            freeRunners = new ModelFreeRunners(getEntityModels());
        }
    }

    @Override
    public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext displayContext, @NotNull PoseStack matrix, @NotNull MultiBufferSource renderer,
          int light, int overlayLight) {
        matrix.pushPose();
        matrix.translate(0.5, 0.5, 0.5);
        matrix.mulPose(Axis.ZP.rotationDegrees(180));
        matrix.translate(0, -1, 0);
        freeRunners.render(matrix, renderer, light, overlayLight, stack.hasFoil());
        matrix.popPose();
    }
}