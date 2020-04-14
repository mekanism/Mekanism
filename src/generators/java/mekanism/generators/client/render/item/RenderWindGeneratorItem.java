package mekanism.generators.client.render.item;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.MekanismItemStackRenderer;
import mekanism.generators.client.model.ModelWindGenerator;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RenderWindGeneratorItem extends MekanismItemStackRenderer {

    private static ModelWindGenerator windGenerator = new ModelWindGenerator();
    private static int angle = 0;
    private static float lastTicksUpdated = 0;
    public static ItemLayerWrapper model;

    @Override
    public void renderBlockSpecific(@Nonnull ItemStack stack, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight,
          TransformType transformType) {
        matrix.push();
        matrix.rotate(Vector3f.ZP.rotationDegrees(180));
        if (transformType == TransformType.THIRD_PERSON_RIGHT_HAND || transformType == TransformType.THIRD_PERSON_LEFT_HAND) {
            matrix.rotate(Vector3f.YP.rotationDegrees(180));
            matrix.translate(0, 0.4, 0);
            if (transformType == TransformType.THIRD_PERSON_LEFT_HAND) {
                matrix.rotate(Vector3f.YP.rotationDegrees(-45));
            } else {
                matrix.rotate(Vector3f.YP.rotationDegrees(45));
            }
            matrix.rotate(Vector3f.XP.rotationDegrees(50));
            matrix.scale(2, 2, 2);
            matrix.translate(0, -0.4, 0);
        } else {
            matrix.translate(0, 0.4, 0);
        }

        float renderPartialTicks = Minecraft.getInstance().getRenderPartialTicks();
        if (lastTicksUpdated != renderPartialTicks) {
            //Only update the angle if we are in a world and that world is not blacklisted
            if (Minecraft.getInstance().world != null) {
                List<ResourceLocation> blacklistedDimensions = MekanismGeneratorsConfig.generators.windGenerationDimBlacklist.get();
                if (blacklistedDimensions.isEmpty() || !blacklistedDimensions.contains(Minecraft.getInstance().world.getDimension().getType().getRegistryName())) {
                    angle = (angle + 2) % 360;
                }
            }
            lastTicksUpdated = renderPartialTicks;
        }
        //Scale the model to the correct size
        matrix.scale(0.256F, 0.256F, 0.256F);
        windGenerator.render(matrix, renderer, angle, light, overlayLight);
        matrix.pop();
    }

    @Override
    protected void renderItemSpecific(@Nonnull ItemStack stack, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight,
          TransformType transformType) {
    }

    @Nonnull
    @Override
    protected TransformType getTransform(@Nonnull ItemStack stack) {
        return model.getTransform();
    }
}