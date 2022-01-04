package mekanism.generators.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.client.render.item.MekanismISTER;
import mekanism.generators.client.model.ModelWindGenerator;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;

public class RenderWindGeneratorItem extends MekanismISTER {

    public static final RenderWindGeneratorItem RENDERER = new RenderWindGeneratorItem();
    private static float lastTicksUpdated = 0;
    private static int angle = 0;
    private ModelWindGenerator windGenerator;

    @Override
    public void onResourceManagerReload(@Nonnull ResourceManager resourceManager) {
        windGenerator = new ModelWindGenerator(getEntityModels());
    }

    @Override
    public void renderByItem(@Nonnull ItemStack stack, @Nonnull TransformType transformType, @Nonnull PoseStack matrix, @Nonnull MultiBufferSource renderer, int light, int overlayLight) {
        float renderPartialTicks = Minecraft.getInstance().getFrameTime();
        if (lastTicksUpdated != renderPartialTicks) {
            //Only update the angle if we are in a world and that world is not blacklisted
            if (Minecraft.getInstance().level != null) {
                List<ResourceLocation> blacklistedDimensions = MekanismGeneratorsConfig.generators.windGenerationDimBlacklist.get();
                if (blacklistedDimensions.isEmpty() || !blacklistedDimensions.contains(Minecraft.getInstance().level.dimension().location())) {
                    angle = (angle + 2) % 360;
                }
            }
            lastTicksUpdated = renderPartialTicks;
        }
        matrix.pushPose();
        matrix.translate(0.5, 0.5, 0.5);
        matrix.mulPose(Vector3f.ZP.rotationDegrees(180));
        windGenerator.render(matrix, renderer, angle, light, overlayLight, stack.hasFoil());
        matrix.popPose();
    }
}