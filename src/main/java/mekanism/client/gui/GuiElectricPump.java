package mekanism.client.gui;

import java.util.Arrays;
import mekanism.client.gui.element.GuiDownArrow;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.TileEntityElectricPump;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;

public class GuiElectricPump extends GuiMekanismTile<TileEntityElectricPump, MekanismTileContainer<TileEntityElectricPump>> {


    public GuiElectricPump(MekanismTileContainer<TileEntityElectricPump> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiInnerScreen(this, 48, 23, 80, 41));
        addButton(new GuiDownArrow(this, 32, 39));
        addButton(new GuiVerticalPowerBar(this, tile, 164, 15));
        addButton(new GuiFluidGauge(() -> tile.fluidTank, GaugeType.STANDARD, this, 6, 13));
        addButton(new GuiEnergyInfo(() -> Arrays.asList(MekanismLang.USING.translate(EnergyDisplay.of(tile.getEnergyPerTick())),
              MekanismLang.NEEDED.translate(EnergyDisplay.of(tile.getNeededEnergy()))), this));
        addButton(new GuiSecurityTab<>(this, tile));
        addButton(new GuiRedstoneControl(this, tile));
        addButton(new GuiUpgradeTab(this, tile));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), (getXSize() / 2) - (getStringWidth(tile.getName()) / 2), 6, 0x404040);
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 94) + 2, 0x404040);
        renderScaledText(EnergyDisplay.of(tile.getEnergy(), tile.getMaxEnergy()).getTextComponent(), 51, 26, 0x00CD00, 74);
        FluidStack fluidStack = tile.fluidTank.getFluid();
        if (fluidStack.isEmpty()) {
            renderScaledText(MekanismLang.NO_FLUID.translate(), 51, 35, 0x00CD00, 74);
        } else {
            renderScaledText(MekanismLang.GENERIC_STORED_MB.translate(fluidStack, fluidStack.getAmount()), 51, 35, 0x00CD00, 74);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}