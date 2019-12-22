package mekanism.client.render.item.block;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
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
import net.minecraft.util.Direction;

public class RenderEnergyCubeItem extends MekanismItemStackRenderer {

    private static ModelEnergyCube energyCube = new ModelEnergyCube();
    private static ModelEnergyCore core = new ModelEnergyCore();
    public static ItemLayerWrapper model;

    @Override
    public void renderBlockSpecific(@Nonnull ItemStack stack, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int otherLight,
          TransformType transformType) {
        //TODO: 1.15
        EnergyCubeTier tier = ((ItemBlockEnergyCube) stack.getItem()).getTier(stack);
        if (tier == null) {
            return;
        }
        matrix.func_227860_a_();
        matrix.func_227863_a_(Vector3f.field_229183_f_.func_229187_a_(180));
        matrix.func_227860_a_();
        matrix.func_227861_a_(0, -1, 0);
        //TODO: Instead of having this be a thing, make it do it from model like the block does?
        energyCube.render(matrix, renderer, light, otherLight, tier, true);

        CompoundNBT configData = ItemDataUtils.getDataMapIfPresent(stack);
        if (configData != null && configData.getBoolean("sideDataStored")) {
            CompoundNBT sideConfig = configData.getCompound("config" + TransmissionType.ENERGY.ordinal());
            //TODO: Maybe improve on this, but for now this is a decent way of making it not have disabled sides show
            for (RelativeSide side : EnumUtils.SIDES) {
                DataType dataType = DataType.byIndexStatic(sideConfig.getInt("side" + side.ordinal()));
                //TODO: Improve on the check compared to just directly comparing the data type?
                energyCube.renderSide(matrix, renderer, light, otherLight, side.getDirection(Direction.NORTH), dataType.equals(DataType.INPUT), dataType.equals(DataType.OUTPUT));
            }
        } else {
            for (Direction side : EnumUtils.DIRECTIONS) {
                energyCube.renderSide(matrix, renderer, light, otherLight, side, true, true);
            }
        }

        matrix.func_227865_b_();
        double energyPercentage = ItemDataUtils.getDouble(stack, "energyStored") / tier.getMaxEnergy();
        if (energyPercentage > 0.1) {
            matrix.func_227862_a_(0.4F, 0.4F, 0.4F);
            matrix.func_227861_a_(0, Math.sin(Math.toRadians(3 * MekanismClient.ticksPassed)) / 7, 0);
            matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(4 * MekanismClient.ticksPassed));
            matrix.func_227863_a_(RenderEnergyCube.coreVec.func_229187_a_(36F + 4 * MekanismClient.ticksPassed));
            core.render(matrix, renderer, MekanismRenderer.FULL_LIGHT, otherLight, tier, (float) energyPercentage);
        }
        matrix.func_227865_b_();
    }

    @Override
    protected void renderItemSpecific(@Nonnull ItemStack stack, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int otherLight,
          TransformType transformType) {
    }

    @Nonnull
    @Override
    protected TransformType getTransform(@Nonnull ItemStack stack) {
        return model.getTransform();
    }
}