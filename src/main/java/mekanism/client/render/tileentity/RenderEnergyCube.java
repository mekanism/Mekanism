package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.MekanismClient;
import mekanism.client.model.ModelEnergyCube;
import mekanism.client.model.ModelEnergyCube.ModelEnergyCore;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.util.Direction;

public class RenderEnergyCube extends MekanismTileEntityRenderer<TileEntityEnergyCube> {

    public static final Vector3f coreVec = new Vector3f(0.0F, MekanismUtils.ONE_OVER_ROOT_TWO, MekanismUtils.ONE_OVER_ROOT_TWO);
    private ModelEnergyCube model = new ModelEnergyCube();
    private ModelEnergyCore core = new ModelEnergyCore();

    @Override
    public void func_225616_a_(@Nonnull TileEntityEnergyCube tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int otherLight) {
        matrix.func_227860_a_();
        matrix.func_227861_a_(0.5, 1.5, 0.5);

        matrix.func_227860_a_();
        switch (tile.getDirection()) {
            case DOWN:
                matrix.func_227863_a_(Vector3f.field_229178_a_.func_229187_a_(90));
                matrix.func_227861_a_(0, 1, -1);
                break;
            case UP:
                matrix.func_227863_a_(Vector3f.field_229179_b_.func_229187_a_(90));
                matrix.func_227861_a_(0, 1, 1);
                break;
            default:
                //Otherwise use the helper method for handling different face options because it is one of them
                MekanismRenderer.rotate(matrix, tile.getDirection(), 0, 180, 90, 270);
                break;
        }

        matrix.func_227863_a_(Vector3f.field_229183_f_.func_229187_a_(180));
        model.render(matrix, renderer, light, otherLight, tile.tier, false);

        for (Direction side : EnumUtils.DIRECTIONS) {
            ISlotInfo slotInfo = tile.configComponent.getSlotInfo(TransmissionType.ENERGY, side);
            //TODO: Re-evaluate
            boolean canInput = false;
            boolean canOutput = false;
            if (slotInfo != null) {
                canInput = slotInfo.canInput();
                canOutput = slotInfo.canOutput();
            }
            model.renderSide(matrix, renderer, light, otherLight, side, canInput, canOutput);
        }
        matrix.func_227865_b_();

        double energyPercentage = tile.getEnergy() / tile.getMaxEnergy();
        if (energyPercentage > 0.1) {
            matrix.func_227861_a_(0, -1, 0);
            float ticks = MekanismClient.ticksPassed + partialTick;
            matrix.func_227862_a_(0.4F, 0.4F, 0.4F);
            matrix.func_227861_a_(0, Math.sin(Math.toRadians(3 * ticks)) / 7, 0);
            matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(4 * ticks));
            matrix.func_227863_a_(coreVec.func_229187_a_(36F + 4 * ticks));
            core.render(matrix, renderer, MekanismRenderer.FULL_LIGHT, otherLight, tile.tier, (float) energyPercentage);
        }
        matrix.func_227865_b_();
        MekanismRenderer.machineRenderer().func_225616_a_(tile, partialTick, matrix, renderer, light, otherLight);
    }
}