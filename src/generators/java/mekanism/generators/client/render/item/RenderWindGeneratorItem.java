package mekanism.generators.client.render.item;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.MekanismItemStackRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.model.ModelWindGenerator;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;

public class RenderWindGeneratorItem extends MekanismItemStackRenderer {

    private static ModelWindGenerator windGenerator = new ModelWindGenerator();
    private static int angle = 0;
    private static float lastTicksUpdated = 0;
    public static ItemLayerWrapper model;

    @Override
    public void renderBlockSpecific(@Nonnull ItemStack stack, TransformType transformType) {
        RenderSystem.pushMatrix();
        RenderSystem.rotatef(180, 0, 0, 1);
        if (transformType == TransformType.THIRD_PERSON_RIGHT_HAND || transformType == TransformType.THIRD_PERSON_LEFT_HAND) {
            RenderSystem.rotatef(180, 0, 1, 0);
            RenderSystem.translatef(0, 0.4F, 0);
            if (transformType == TransformType.THIRD_PERSON_LEFT_HAND) {
                RenderSystem.rotatef(-45, 0, 1, 0);
            } else {
                RenderSystem.rotatef(45, 0, 1, 0);
            }
            RenderSystem.rotatef(50, 1, 0, 0);
            RenderSystem.scalef(2.0F, 2.0F, 2.0F);
            RenderSystem.translatef(0, -0.4F, 0);
        } else {
            if (transformType == TransformType.GUI) {
                RenderSystem.rotatef(90, 0, 1, 0);
            } else if (transformType == TransformType.FIRST_PERSON_RIGHT_HAND) {
                RenderSystem.rotatef(180, 0, 1, 0);
            }
            RenderSystem.translatef(0, 0.4F, 0);
        }

        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "wind_generator.png"));
        float renderPartialTicks = Minecraft.getInstance().getRenderPartialTicks();
        if (lastTicksUpdated != renderPartialTicks) {
            //Only update the angle if we are in a world and that world is not blacklisted
            if (Minecraft.getInstance().world != null) {
                //TODO: Should this check to see if this can be cached somehow
                List<? extends String> blacklistedDimensions = MekanismGeneratorsConfig.generators.windGenerationDimBlacklist.get();
                if (blacklistedDimensions.isEmpty() || !blacklistedDimensions.contains(Minecraft.getInstance().world.getDimension().getType().getRegistryName().toString())) {
                    angle = (angle + 2) % 360;
                }
            }
            lastTicksUpdated = renderPartialTicks;
        }
        windGenerator.render(0.016F, angle);
        RenderSystem.popMatrix();
    }

    @Override
    protected void renderItemSpecific(@Nonnull ItemStack stack, TransformType transformType) {
    }

    @Nonnull
    @Override
    protected TransformType getTransform(@Nonnull ItemStack stack) {
        return model.getTransform();
    }
}