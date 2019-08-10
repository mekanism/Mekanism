package mekanism.client.gui;

import java.util.Collections;
import mekanism.client.gui.element.GuiHeatInfo;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.ContainerThermalEvaporationController;
import mekanism.common.tile.TileEntityThermalEvaporationController;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiThermalEvaporationController extends GuiMekanismTile<TileEntityThermalEvaporationController> {

    public GuiThermalEvaporationController(PlayerInventory inventory, TileEntityThermalEvaporationController tile) {
        super(tile, new ContainerThermalEvaporationController(inventory, tile));
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiFluidGauge(() -> tileEntity.inputTank, GuiGauge.Type.STANDARD, this, resource, 6, 13));
        addGuiElement(new GuiFluidGauge(() -> tileEntity.outputTank, GuiGauge.Type.STANDARD, this, resource, 152, 13));
        addGuiElement(new GuiHeatInfo(() -> {
            TemperatureUnit unit = TemperatureUnit.values()[MekanismConfig.current().general.tempUnit.val().ordinal()];
            String environment = UnitDisplayUtils.getDisplayShort(tileEntity.totalLoss * unit.intervalSize, false, unit);
            return Collections.singletonList(LangUtils.localize("gui.dissipated") + ": " + environment + "/t");
        }, this, resource));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        font.drawString(LangUtils.localize("container.inventory"), 8, (ySize - 96) + 4, 0x404040);
        font.drawString(tileEntity.getName(), (xSize / 2) - (font.getStringWidth(tileEntity.getName()) / 2), 4, 0x404040);
        font.drawString(getStruct(), 50, 21, 0x00CD00);
        font.drawString(LangUtils.localize("gui.height") + ": " + tileEntity.height, 50, 30, 0x00CD00);
        font.drawString(LangUtils.localize("gui.temp") + ": " + getTemp(), 50, 39, 0x00CD00);
        renderScaledText(LangUtils.localize("gui.production") + ": " + Math.round(tileEntity.lastGain * 100D) / 100D + " mB/t", 50, 48, 0x00CD00, 76);
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        if (xAxis >= 7 && xAxis <= 23 && yAxis >= 14 && yAxis <= 72) {
            FluidStack fluid = tileEntity.inputTank.getFluid();
            displayTooltip(fluid != null ? LangUtils.localizeFluidStack(fluid) + ": " + tileEntity.inputTank.getFluidAmount() : LangUtils.localize("gui.empty"), xAxis, yAxis);
        } else if (xAxis >= 153 && xAxis <= 169 && yAxis >= 14 && yAxis <= 72) {
            FluidStack fluid = tileEntity.outputTank.getFluid();
            displayTooltip(fluid != null ? LangUtils.localizeFluidStack(fluid) + ": " + tileEntity.outputTank.getFluidAmount() : LangUtils.localize("gui.empty"), xAxis, yAxis);
        } else if (xAxis >= 49 && xAxis <= 127 && yAxis >= 64 && yAxis <= 72) {
            displayTooltip(getTemp(), xAxis, yAxis);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    private String getStruct() {
        if (tileEntity.structured) {
            return LangUtils.localize("gui.formed");
        } else if (tileEntity.controllerConflict) {
            return LangUtils.localize("gui.conflict");
        }
        return LangUtils.localize("gui.incomplete");
    }

    private String getTemp() {
        return MekanismUtils.getTemperatureDisplay(tileEntity.getTemperature(), TemperatureUnit.AMBIENT);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        drawTexturedRect(guiLeft + 49, guiTop + 64, 176, 59, tileEntity.getScaledTempLevel(78), 8);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiThermalEvaporationController.png");
    }
}