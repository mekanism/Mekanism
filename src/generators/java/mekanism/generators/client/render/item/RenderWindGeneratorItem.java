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

public class RenderWindGeneratorItem extends MekanismItemStackRenderer {

    private static ModelWindGenerator windGenerator = new ModelWindGenerator();
    private static int angle = 0;
    private static float lastTicksUpdated = 0;
    public static ItemLayerWrapper model;

    @Override
    public void renderBlockSpecific(@Nonnull ItemStack stack, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight,
          TransformType transformType) {
        matrix.func_227860_a_();
        matrix.func_227863_a_(Vector3f.field_229183_f_.func_229187_a_(180));
        if (transformType == TransformType.THIRD_PERSON_RIGHT_HAND || transformType == TransformType.THIRD_PERSON_LEFT_HAND) {
            matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(180));
            matrix.func_227861_a_(0, 0.4, 0);
            if (transformType == TransformType.THIRD_PERSON_LEFT_HAND) {
                matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(-45));
            } else {
                matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(45));
            }
            matrix.func_227863_a_(Vector3f.field_229179_b_.func_229187_a_(50));
            matrix.func_227862_a_(2, 2, 2);
            matrix.func_227861_a_(0, -0.4, 0);
        } else {
            if (transformType == TransformType.GUI) {
                matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(90));
            } else if (transformType == TransformType.FIRST_PERSON_RIGHT_HAND) {
                matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(180));
            }
            matrix.func_227861_a_(0, 0.4, 0);
        }

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
        //Scale the model to the correct size
        matrix.func_227862_a_(0.256F, 0.256F, 0.256F);
        windGenerator.render(matrix, renderer, angle, light, overlayLight);
        matrix.func_227865_b_();
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