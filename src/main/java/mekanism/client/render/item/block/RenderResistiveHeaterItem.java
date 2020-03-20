package mekanism.client.render.item.block;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Optional;
import javax.annotation.Nonnull;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.client.model.ModelResistiveHeater;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.MekanismItemStackRenderer;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;

public class RenderResistiveHeaterItem extends MekanismItemStackRenderer {

    private static ModelResistiveHeater resistiveHeater = new ModelResistiveHeater();
    public static ItemLayerWrapper model;

    @Override
    public void renderBlockSpecific(@Nonnull ItemStack stack, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight,
          TransformType transformType) {
        matrix.rotate(Vector3f.ZP.rotationDegrees(180));
        matrix.translate(0.05, -0.96, 0.05);
        boolean hasEnergy = false;
        Optional<IStrictEnergyHandler> capability = MekanismUtils.toOptional(stack.getCapability(Capabilities.STRICT_ENERGY_CAPABILITY));
        if (capability.isPresent()) {
            IStrictEnergyHandler energyHandlerItem = capability.get();
            if (energyHandlerItem.getEnergyContainerCount() > 0) {
                hasEnergy = energyHandlerItem.getEnergy(0) > 0;
            }
        }
        resistiveHeater.render(matrix, renderer, light, overlayLight, hasEnergy);
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