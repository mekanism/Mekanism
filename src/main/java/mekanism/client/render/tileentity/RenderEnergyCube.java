package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.api.RelativeSide;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.MekanismClient;
import mekanism.client.model.ModelEnergyCube;
import mekanism.client.model.ModelEnergyCube.ModelEnergyCore;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class RenderEnergyCube extends TileEntityRenderer<TileEntityEnergyCube> {

    public static final Vector3f coreVec = new Vector3f(0.0F, MekanismUtils.ONE_OVER_ROOT_TWO, MekanismUtils.ONE_OVER_ROOT_TWO);
    private ModelEnergyCube model = new ModelEnergyCube();
    private ModelEnergyCore core = new ModelEnergyCore();

    public RenderEnergyCube(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    public void render(@Nonnull TileEntityEnergyCube tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light,
          int overlayLight) {
        matrix.push();
        matrix.translate(0.5, 1.5, 0.5);

        matrix.push();
        switch (tile.getDirection()) {
            case DOWN:
                matrix.rotate(Vector3f.field_229178_a_.func_229187_a_(90));
                matrix.translate(0, 1, -1);
                break;
            case UP:
                matrix.rotate(Vector3f.field_229179_b_.func_229187_a_(90));
                matrix.translate(0, 1, 1);
                break;
            default:
                //Otherwise use the helper method for handling different face options because it is one of them
                MekanismRenderer.rotate(matrix, tile.getDirection(), 0, 180, 90, 270);
                break;
        }

        matrix.rotate(Vector3f.field_229183_f_.func_229187_a_(180));
        model.render(matrix, renderer, light, overlayLight, tile.tier, false);

        ConfigInfo config = tile.configComponent.getConfig(TransmissionType.ENERGY);
        if (config != null) {
            for (RelativeSide side : EnumUtils.SIDES) {
                ISlotInfo slotInfo = config.getSlotInfo(side);
                //TODO: Re-evaluate
                boolean canInput = false;
                boolean canOutput = false;
                if (slotInfo != null) {
                    canInput = slotInfo.canInput();
                    canOutput = slotInfo.canOutput();
                }
                model.renderSide(matrix, renderer, light, overlayLight, side, canInput, canOutput);
            }
        }
        matrix.pop();

        double energyPercentage = tile.getEnergy() / tile.getMaxEnergy();
        if (energyPercentage > 0.1) {
            matrix.translate(0, -1, 0);
            float ticks = MekanismClient.ticksPassed + partialTick;
            matrix.scale(0.4F, 0.4F, 0.4F);
            matrix.translate(0, Math.sin(Math.toRadians(3 * ticks)) / 7, 0);
            matrix.rotate(Vector3f.field_229181_d_.func_229187_a_(4 * ticks));
            matrix.rotate(coreVec.func_229187_a_(36F + 4 * ticks));
            core.render(matrix, renderer, MekanismRenderer.FULL_LIGHT, overlayLight, tile.tier.getBaseTier().getColor(), (float) energyPercentage);
        }
        matrix.pop();
        MekanismRenderer.machineRenderer().render(tile, partialTick, matrix, renderer, light, overlayLight);
    }
}