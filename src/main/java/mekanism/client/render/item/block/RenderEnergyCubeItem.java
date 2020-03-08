package mekanism.client.render.item.block;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.MekanismClient;
import mekanism.client.model.ModelEnergyCube;
import mekanism.client.model.ModelEnergyCube.ModelEnergyCore;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.MekanismItemStackRenderer;
import mekanism.client.render.tileentity.RenderEnergyCube;
import mekanism.common.item.block.ItemBlockEnergyCube;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants.NBT;

public class RenderEnergyCubeItem extends MekanismItemStackRenderer {

    private static ModelEnergyCube energyCube = new ModelEnergyCube();
    private static ModelEnergyCore core = new ModelEnergyCore();
    public static ItemLayerWrapper model;

    @Override
    public void renderBlockSpecific(@Nonnull ItemStack stack, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight,
          TransformType transformType) {
        EnergyCubeTier tier = ((ItemBlockEnergyCube) stack.getItem()).getTier(stack);
        if (tier == null) {
            return;
        }
        matrix.push();
        matrix.rotate(Vector3f.ZP.rotationDegrees(180));
        matrix.push();
        matrix.translate(0, -1, 0);
        //TODO: Instead of having this be a thing, make it do it from model like the block does?
        energyCube.render(matrix, renderer, light, overlayLight, tier, true);

        CompoundNBT configData = ItemDataUtils.getDataMapIfPresent(stack);
        if (configData != null && configData.contains(NBTConstants.COMPONENT_CONFIG, NBT.TAG_COMPOUND)) {
            CompoundNBT sideConfig = configData.getCompound(NBTConstants.COMPONENT_CONFIG).getCompound(NBTConstants.CONFIG + TransmissionType.ENERGY.ordinal());
            //TODO: Maybe improve on this, but for now this is a decent way of making it not have disabled sides show
            for (RelativeSide side : EnumUtils.SIDES) {
                DataType dataType = DataType.byIndexStatic(sideConfig.getInt(NBTConstants.SIDE + side.ordinal()));
                //TODO: Improve on the check compared to just directly comparing the data type?
                energyCube.renderSide(matrix, renderer, light, overlayLight, side, dataType.equals(DataType.INPUT), dataType.equals(DataType.OUTPUT));
            }
        } else {
            for (RelativeSide side : EnumUtils.SIDES) {
                energyCube.renderSide(matrix, renderer, light, overlayLight, side, true, true);
            }
        }

        matrix.pop();
        double energyPercentage = ItemDataUtils.getDouble(stack, NBTConstants.ENERGY_STORED) / tier.getMaxEnergy();
        if (energyPercentage > 0.1) {
            matrix.scale(0.4F, 0.4F, 0.4F);
            matrix.translate(0, Math.sin(Math.toRadians(3 * MekanismClient.ticksPassed)) / 7, 0);
            matrix.rotate(Vector3f.YP.rotationDegrees(4 * MekanismClient.ticksPassed));
            matrix.rotate(RenderEnergyCube.coreVec.rotationDegrees(36F + 4 * MekanismClient.ticksPassed));
            core.render(matrix, renderer, MekanismRenderer.FULL_LIGHT, overlayLight, tier.getBaseTier().getColor(), (float) energyPercentage);
        }
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