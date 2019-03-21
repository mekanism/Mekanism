package mekanism.generators.client.gui;

import java.io.IOException;
import java.util.Arrays;
import mekanism.api.Coord4D;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiRateBar;
import mekanism.client.gui.element.GuiRateBar.IRateInfoHandler;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.api.TileNetworkList;
import mekanism.common.config.MekanismConfig.general;
import mekanism.common.config.MekanismConfig.generators;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityGasTank;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.gui.element.GuiTurbineTab;
import mekanism.generators.client.gui.element.GuiTurbineTab.TurbineTab;
import mekanism.generators.common.content.turbine.TurbineUpdateProtocol;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiIndustrialTurbine extends GuiMekanismTile<TileEntityTurbineCasing> {

    public GuiIndustrialTurbine(InventoryPlayer inventory, TileEntityTurbineCasing tile) {
        super(tile, new ContainerFilter(inventory, tile));
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiTurbineTab(this, tileEntity, TurbineTab.STAT, 6, resource));
        addGuiElement(new GuiPowerBar(this, tileEntity, resource, 164, 16));
        addGuiElement(new GuiRateBar(this, new IRateInfoHandler() {
            @Override
            public String getTooltip() {
                return LangUtils.localize("gui.steamInput") + ": " + tileEntity.structure.lastSteamInput + " mB/t";
            }

            @Override
            public double getLevel() {
                double rate = tileEntity.structure.lowerVolume * (tileEntity.structure.clientDispersers
                      * generators.turbineDisperserGasFlow);
                rate = Math.min(rate, tileEntity.structure.vents * generators.turbineVentGasFlow);

                if (rate == 0) {
                    return 0;
                }

                return (double) tileEntity.structure.lastSteamInput / rate;
            }
        }, resource, 40, 13));
        addGuiElement(new GuiEnergyInfo(() -> {
            double energyMultiplier = (general.maxEnergyPerSteam / TurbineUpdateProtocol.MAX_BLADES) * Math
                  .min(tileEntity.structure.blades, tileEntity.structure.coils * generators.turbineBladesPerCoil);
            return Arrays.asList(
                  LangUtils.localize("gui.storing") + ": " + MekanismUtils
                        .getEnergyDisplay(tileEntity.getEnergy(), tileEntity.getMaxEnergy()),
                  LangUtils.localize("gui.producing") + ": " + MekanismUtils
                        .getEnergyDisplay(tileEntity.structure.clientFlow * energyMultiplier) + "/t");
        }, this, resource));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(LangUtils.localize("container.inventory"), 8, (ySize - 96) + 4, 0x404040);
        fontRenderer
              .drawString(tileEntity.getName(), (xSize / 2) - (fontRenderer.getStringWidth(tileEntity.getName()) / 2),
                    5, 0x404040);
        double energyMultiplier = (general.maxEnergyPerSteam / TurbineUpdateProtocol.MAX_BLADES) * Math
              .min(tileEntity.structure.blades, tileEntity.structure.coils * generators.turbineBladesPerCoil);
        double rate = tileEntity.structure.lowerVolume * (tileEntity.structure.clientDispersers
              * generators.turbineDisperserGasFlow);
        rate = Math.min(rate, tileEntity.structure.vents * generators.turbineVentGasFlow);
        renderScaledText(LangUtils.localize("gui.production") + ": " + MekanismUtils
              .getEnergyDisplay(tileEntity.structure.clientFlow * energyMultiplier), 53, 26, 0x00CD00, 106);
        renderScaledText(LangUtils.localize("gui.flowRate") + ": " + tileEntity.structure.clientFlow + " mB/t", 53, 35,
              0x00CD00, 106);
        renderScaledText(LangUtils.localize("gui.capacity") + ": " + tileEntity.structure.getFluidCapacity() + " mB",
              53, 44, 0x00CD00, 106);
        renderScaledText(LangUtils.localize("gui.maxFlow") + ": " + rate + " mB/t", 53, 53, 0x00CD00, 106);
        String name = chooseByMode(tileEntity.structure.dumpMode, LangUtils.localize("gui.idle"),
              LangUtils.localize("gui.dumping"), LangUtils.localize("gui.dumping_excess"));
        renderScaledText(name, 156 - (int) (fontRenderer.getStringWidth(name) * getNeededScale(name, 66)), 73, 0x404040,
              66);
        int xAxis = (mouseX - (width - xSize) / 2);
        int yAxis = (mouseY - (height - ySize) / 2);
        if (xAxis >= 7 && xAxis <= 39 && yAxis >= 14 && yAxis <= 72) {
            drawHoveringText(tileEntity.structure.fluidStored != null ?
                        LangUtils.localizeFluidStack(tileEntity.structure.fluidStored) + ": "
                              + tileEntity.structure.fluidStored.amount + "mB" : LangUtils.localize("gui.empty"), xAxis,
                  yAxis);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        mc.renderEngine.bindTexture(getGuiLocation());
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        int displayInt = chooseByMode(tileEntity.structure.dumpMode, 142, 150, 158);
        drawTexturedModalRect(guiWidth + 160, guiHeight + 73, 176, displayInt, 8, 8);
        if (tileEntity.getScaledFluidLevel(58) > 0) {
            displayGauge(7, 14, tileEntity.getScaledFluidLevel(58), tileEntity.structure.fluidStored, 0);
            displayGauge(23, 14, tileEntity.getScaledFluidLevel(58), tileEntity.structure.fluidStored, 1);
        }
        super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
    }

    public void displayGauge(int xPos, int yPos, int scale, FluidStack fluid, int side /*0-left, 1-right*/) {
        if (fluid == null) {
            return;
        }
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        int start = 0;
        while (true) {
            int renderRemaining;
            if (scale > 16) {
                renderRemaining = 16;
                scale -= 16;
            } else {
                renderRemaining = scale;
                scale = 0;
            }
            mc.renderEngine.bindTexture(MekanismRenderer.getBlocksTexture());
            drawTexturedModalRect(guiWidth + xPos, guiHeight + yPos + 58 - renderRemaining - start,
                  MekanismRenderer.getFluidTexture(fluid.getFluid(), FluidType.STILL), 16, 16 - (16 - renderRemaining));
            start += 16;
            if (renderRemaining == 0 || scale == 0) {
                break;
            }
        }
        mc.renderEngine.bindTexture(getGuiLocation());
        drawTexturedModalRect(guiWidth + xPos, guiHeight + yPos, 176, side == 0 ? 0 : 54, 16, 54);
    }

    @Override
    protected void mouseClicked(int x, int y, int button) throws IOException {
        super.mouseClicked(x, y, button);
        int xAxis = (x - (width - xSize) / 2);
        int yAxis = (y - (height - ySize) / 2);
        if (xAxis > 160 && xAxis < 169 && yAxis > 73 && yAxis < 82) {
            TileNetworkList data = TileNetworkList.withContents((byte) 0);
            Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
            SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiIndustrialTurbine.png");
    }

    private <T> T chooseByMode(TileEntityGasTank.GasMode dumping, T idleOption, T dumpingOption,
          T dumpingExcessOption) {
        if (dumping.equals(TileEntityGasTank.GasMode.IDLE)) {
            return idleOption;
        } else if (dumping.equals(TileEntityGasTank.GasMode.DUMPING)) {
            return dumpingOption;
        } else if (dumping.equals(TileEntityGasTank.GasMode.DUMPING_EXCESS)) {
            return dumpingExcessOption;
        }
        return idleOption; //should not happen;
    }
}