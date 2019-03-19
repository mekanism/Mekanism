package mekanism.client.gui;

import java.util.Collections;
import mekanism.client.gui.element.GuiBoilerTab;
import mekanism.client.gui.element.GuiBoilerTab.BoilerTab;
import mekanism.client.gui.element.GuiHeatInfo;
import mekanism.client.gui.element.GuiRateBar;
import mekanism.client.gui.element.GuiRateBar.IRateInfoHandler;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.common.config.MekanismConfig.general;
import mekanism.common.content.boiler.SynchronizedBoilerData;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.tile.TileEntityBoilerCasing;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiThermoelectricBoiler extends GuiMekanismTile<TileEntityBoilerCasing> {

    public GuiThermoelectricBoiler(InventoryPlayer inventory, TileEntityBoilerCasing tile) {
        super(tile, new ContainerFilter(inventory, tile));
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiBoilerTab(this, tileEntity, BoilerTab.STAT, 6, resource));
        addGuiElement(new GuiRateBar(this, new IRateInfoHandler() {
            @Override
            public String getTooltip() {
                return LangUtils.localize("gui.boilRate") + ": " + tileEntity.structure.lastBoilRate + " mB/t";
            }

            @Override
            public double getLevel() {
                return (double) tileEntity.structure.lastBoilRate / (double) tileEntity.structure.lastMaxBoil;
            }
        }, resource, 24, 13));
        addGuiElement(new GuiRateBar(this, new IRateInfoHandler() {
            @Override
            public String getTooltip() {
                return LangUtils.localize("gui.maxBoil") + ": " + tileEntity.structure.lastMaxBoil + " mB/t";
            }

            @Override
            public double getLevel() {
                double cap = (tileEntity.structure.superheatingElements * general.superheatingHeatTransfer)
                      / SynchronizedBoilerData.getHeatEnthalpy();
                return (double) tileEntity.structure.lastMaxBoil / cap;
            }
        }, resource, 144, 13));
        addGuiElement(new GuiHeatInfo(() -> {
            TemperatureUnit unit = TemperatureUnit.values()[general.tempUnit.ordinal()];
            String environment = UnitDisplayUtils
                  .getDisplayShort(tileEntity.structure.lastEnvironmentLoss * unit.intervalSize, false, unit);
            return Collections.singletonList(LangUtils.localize("gui.dissipated") + ": " + environment + "/t");
        }, this, resource));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(LangUtils.localize("container.inventory"), 8, (ySize - 96) + 4, 0x404040);
        fontRenderer
              .drawString(tileEntity.getName(), (xSize / 2) - (fontRenderer.getStringWidth(tileEntity.getName()) / 2),
                    5, 0x404040);
        renderScaledText(LangUtils.localize("gui.temp") + ": " + MekanismUtils
              .getTemperatureDisplay(tileEntity.structure.temperature, TemperatureUnit.AMBIENT), 43, 30, 0x00CD00, 90);
        renderScaledText(LangUtils.localize("gui.boilRate") + ": " + tileEntity.structure.lastBoilRate + " mB/t", 43,
              39, 0x00CD00, 90);
        renderScaledText(LangUtils.localize("gui.maxBoil") + ": " + tileEntity.structure.lastMaxBoil + " mB/t", 43, 48,
              0x00CD00, 90);
        int xAxis = (mouseX - (width - xSize) / 2);
        int yAxis = (mouseY - (height - ySize) / 2);
        if (xAxis >= 7 && xAxis <= 23 && yAxis >= 14 && yAxis <= 72) {
            drawHoveringText(tileEntity.structure.waterStored != null ?
                        LangUtils.localizeFluidStack(tileEntity.structure.waterStored) + ": "
                              + tileEntity.structure.waterStored.amount + "mB" : LangUtils.localize("gui.empty"), xAxis,
                  yAxis);
        }
        if (xAxis >= 153 && xAxis <= 169 && yAxis >= 14 && yAxis <= 72) {
            drawHoveringText(tileEntity.structure.steamStored != null ?
                        LangUtils.localizeFluidStack(tileEntity.structure.steamStored) + ": "
                              + tileEntity.structure.steamStored.amount + "mB" : LangUtils.localize("gui.empty"), xAxis,
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
        if (tileEntity.getScaledWaterLevel(58) > 0) {
            displayGauge(7, 14, tileEntity.getScaledWaterLevel(58), tileEntity.structure.waterStored);
        }
        if (tileEntity.getScaledSteamLevel(58) > 0) {
            displayGauge(153, 14, tileEntity.getScaledSteamLevel(58), tileEntity.structure.steamStored);
        }
        super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiThermoelectricBoiler.png");
    }

    public void displayGauge(int xPos, int yPos, int scale, FluidStack fluid) {
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
        mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiIndustrialTurbine.png"));
        drawTexturedModalRect(guiWidth + xPos, guiHeight + yPos, 176, 0, 16, 54);
    }
}