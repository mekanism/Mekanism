package mekanism.client.render.tileentity;

import java.util.EnumMap;
import java.util.Map;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.MekanismClient;
import mekanism.client.model.ModelEnergyCube;
import mekanism.client.model.ModelEnergyCube.ModelEnergyCore;
import mekanism.client.render.MekanismRenderHelper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderEnergyCube extends TileEntitySpecialRenderer<TileEntityEnergyCube> {

    public static Map<EnergyCubeTier, ResourceLocation> resources = new EnumMap<>(EnergyCubeTier.class);
    public static ResourceLocation baseTexture = MekanismUtils.getResource(ResourceType.RENDER, "EnergyCube.png");
    public static ResourceLocation coreTexture = MekanismUtils.getResource(ResourceType.RENDER, "EnergyCore.png");

    static {
        if (resources.isEmpty()) {
            for (EnergyCubeTier tier : EnergyCubeTier.values()) {
                resources.put(tier, MekanismUtils.getResource(ResourceType.RENDER, "EnergyCube" + tier.getBaseTier().getSimpleName() + ".png"));
            }
        }
    }

    private ModelEnergyCube model = new ModelEnergyCube();
    private ModelEnergyCore core = new ModelEnergyCore();

    @Override
    public void render(TileEntityEnergyCube tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        MekanismRenderHelper renderHelper = new MekanismRenderHelper(true).enableBlendPreset().translate(x + 0.5, y + 1.5, z + 0.5);
        switch (tileEntity.facing) {
            case DOWN:
                renderHelper.rotateX(90, -1).translateYZ(1.0F, -1.0F);
                break;
            case UP:
                renderHelper.rotateX(90, 1).translateYZ(1.0F, 1.0F);
                break;
            case NORTH:
                renderHelper.rotateY(0, 1);
                break;
            case SOUTH:
                renderHelper.rotateY(180, 1);
                break;
            case WEST:
                renderHelper.rotateY(90, 1);
                break;
            case EAST:
                renderHelper.rotateY(270, 1);
                break;
        }

        renderHelper.rotateZ(180, 1);
        model.render(0.0625F, tileEntity.tier, rendererDispatcher.renderEngine, false);

        for (EnumFacing side : EnumFacing.values()) {
            bindTexture(baseTexture);
            model.renderSide(0.0625F, side, tileEntity.configComponent.getOutput(TransmissionType.ENERGY, side).ioState, tileEntity.tier, rendererDispatcher.renderEngine);
        }

        renderHelper.cleanup();

        if (tileEntity.getEnergy() / tileEntity.getMaxEnergy() > 0.1) {
            MekanismRenderHelper coreRenderHelper = new MekanismRenderHelper(true).translate(x + 0.5, y + 0.5, z + 0.5);
            bindTexture(coreTexture);
            coreRenderHelper.enableBlendPreset().enableGlow();

            float ticks = MekanismClient.ticksPassed + partialTick;
            MekanismRenderHelper coreColorRenderHelper = new MekanismRenderHelper(true).scale(0.4F).color(tileEntity.tier.getBaseTier())
                  .translateY(Math.sin(Math.toRadians(3 * ticks)) / 7).rotateY(4 * ticks, 1).rotateYZ(36F + 4 * ticks, 1, 1);
            core.render(0.0625F);
            coreColorRenderHelper.cleanup();
            coreRenderHelper.cleanup();
        }

        MekanismRenderer.machineRenderer().render(tileEntity, x, y, z, partialTick, destroyStage, alpha);
    }
}