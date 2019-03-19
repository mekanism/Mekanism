package mekanism.client.gui;

import java.util.Collections;
import mekanism.client.gui.element.GuiFluidGauge;
import mekanism.client.gui.element.GuiGauge;
import mekanism.client.gui.element.GuiHeatInfo;
import mekanism.common.config.MekanismConfig.general;
import mekanism.common.inventory.container.ContainerThermalEvaporationController;
import mekanism.common.tile.TileEntityThermalEvaporationController;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiThermalEvaporationController extends GuiMekanismTile<TileEntityThermalEvaporationController> {

    public GuiThermalEvaporationController(InventoryPlayer inventory, TileEntityThermalEvaporationController tile) {
        super(tile, new ContainerThermalEvaporationController(inventory, tile));
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiFluidGauge(() -> tileEntity.inputTank, GuiGauge.Type.STANDARD, this, resource, 6, 13));
        addGuiElement(new GuiFluidGauge(() ->
              tileEntity.outputTank, GuiGauge.Type.STANDARD, this, resource, 152, 13));
        addGuiElement(new GuiHeatInfo(() -> {
            TemperatureUnit unit = TemperatureUnit.values()[general.tempUnit.ordinal()];
            String environment = UnitDisplayUtils
                  .getDisplayShort(tileEntity.totalLoss * unit.intervalSize, false, unit);
            return Collections.singletonList(LangUtils.localize("gui.dissipated") + ": " + environment + "/t");
        }, this, resource));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(LangUtils.localize("container.inventory"), 8, (ySize - 96) + 4, 0x404040);
        fontRenderer
              .drawString(tileEntity.getName(), (xSize / 2) - (fontRenderer.getStringWidth(tileEntity.getName()) / 2),
                    4, 0x404040);
        fontRenderer.drawString(getStruct(), 50, 21, 0x00CD00);
        fontRenderer.drawString(LangUtils.localize("gui.height") + ": " + tileEntity.height, 50, 30, 0x00CD00);
        fontRenderer.drawString(LangUtils.localize("gui.temp") + ": " + getTemp(), 50, 39, 0x00CD00);
        renderScaledText(
              LangUtils.localize("gui.production") + ": " + Math.round(tileEntity.lastGain * 100D) / 100D + " mB/t", 50,
              48, 0x00CD00, 76);
        int xAxis = (mouseX - (width - xSize) / 2);
        int yAxis = (mouseY - (height - ySize) / 2);
        if (xAxis >= 7 && xAxis <= 23 && yAxis >= 14 && yAxis <= 72) {
            drawHoveringText(tileEntity.inputTank.getFluid() != null ?
                  LangUtils.localizeFluidStack(tileEntity.inputTank.getFluid()) + ": " + tileEntity.inputTank
                        .getFluidAmount() : LangUtils.localize("gui.empty"), xAxis, yAxis);
        }
        if (xAxis >= 153 && xAxis <= 169 && yAxis >= 14 && yAxis <= 72) {
            drawHoveringText(tileEntity.outputTank.getFluid() != null ?
                  LangUtils.localizeFluidStack(tileEntity.outputTank.getFluid()) + ": " + tileEntity.outputTank
                        .getFluidAmount() : LangUtils.localize("gui.empty"), xAxis, yAxis);
        }
        if (xAxis >= 49 && xAxis <= 127 && yAxis >= 64 && yAxis <= 72) {
            drawHoveringText(getTemp(), xAxis, yAxis);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    private String getStruct() {
        if (tileEntity.structured) {
            return LangUtils.localize("gui.formed");
        } else {
            if (tileEntity.controllerConflict) {
                return LangUtils.localize("gui.conflict");
            } else {
                return LangUtils.localize("gui.incomplete");
            }
        }
    }

    private String getTemp() {
        return MekanismUtils.getTemperatureDisplay(tileEntity.getTemperature(), TemperatureUnit.AMBIENT);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        mc.renderEngine.bindTexture(getGuiLocation());
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        int displayInt = tileEntity.getScaledTempLevel(78);
        drawTexturedModalRect(guiWidth + 49, guiHeight + 64, 176, 59, displayInt, 8);
        super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiThermalEvaporationController.png");
    }
}