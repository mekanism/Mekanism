package mekanism.client.render.item.gear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelArmoredFreeRunners;
import mekanism.client.model.ModelFreeRunners;
import mekanism.client.render.item.MekanismISTER;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;

public class RenderFreeRunners extends MekanismISTER {

    public static final RenderFreeRunners RENDERER = new RenderFreeRunners(false);
    public static final RenderFreeRunners ARMORED_RENDERER = new RenderFreeRunners(true);

    private final boolean armored;
    private ModelFreeRunners freeRunners;

    private RenderFreeRunners(boolean armored) {
        this.armored = armored;
    }

    @Override
    public void onResourceManagerReload(@Nonnull ResourceManager resourceManager) {
        if (armored) {
            freeRunners = new ModelArmoredFreeRunners(getEntityModels());
        } else {
            freeRunners = new ModelFreeRunners(getEntityModels());
        }
    }

    @Override
    public void renderByItem(@Nonnull ItemStack stack, @Nonnull TransformType transformType, @Nonnull PoseStack matrix, @Nonnull MultiBufferSource renderer, int light, int overlayLight) {
        matrix.pushPose();
        matrix.translate(0.5, 0.5, 0.5);
        matrix.mulPose(Vector3f.ZP.rotationDegrees(180));
        matrix.translate(0, -1, 0);
        freeRunners.render(matrix, renderer, light, overlayLight, stack.hasFoil());
        matrix.popPose();
    }
}