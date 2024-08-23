package mekanism.generators.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mekanism.api.MekanismAPITags;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.item.MekanismISTER;
import mekanism.generators.client.model.ModelWindGenerator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RenderWindGeneratorItem extends MekanismISTER {

    public static final RenderWindGeneratorItem RENDERER = new RenderWindGeneratorItem();
    private static final int SPEED = 16;
    private static int lastTicksUpdated = 0;
    private static int angle = 0;
    private ModelWindGenerator windGenerator;

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        windGenerator = new ModelWindGenerator(getEntityModels());
    }

    @Override
    public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext displayContext, @NotNull PoseStack matrix, @NotNull MultiBufferSource renderer,
          int light, int overlayLight) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean tickingNormally = MekanismRenderer.isRunningNormally();
        if (tickingNormally && minecraft.level != null) {
            //Only update the angle if we are in a world and that world is not blacklisted
            if (minecraft.level.dimensionTypeRegistration().is(MekanismAPITags.DimensionTypes.NO_WIND)) {
                //If the dimension is blacklisted, don't try to tick it at all
                tickingNormally = false;
            } else {
                int ticks = Minecraft.getInstance().levelRenderer.getTicks();
                if (lastTicksUpdated != ticks) {
                    angle = (angle + SPEED) % 360;
                    lastTicksUpdated = ticks;
                }
            }
        }
        float renderAngle = angle;
        if (tickingNormally) {
            renderAngle = (renderAngle + SPEED * MekanismRenderer.getPartialTick()) % 360;
        }
        matrix.pushPose();
        matrix.translate(0.5, 0.5, 0.5);
        matrix.mulPose(Axis.ZP.rotationDegrees(180));
        windGenerator.render(matrix, renderer, renderAngle, light, overlayLight, stack.hasFoil());
        matrix.popPose();
    }
}