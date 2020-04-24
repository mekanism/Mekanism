package mekanism.client.gui;

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
import mekanism.common.tile.TileEntityFluidicPlenisher;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;

public class GuiFluidicPlenisher extends GuiMekanismTile<TileEntityFluidicPlenisher, MekanismTileContainer<TileEntityFluidicPlenisher>> {

    public GuiFluidicPlenisher(MekanismTileContainer<TileEntityFluidicPlenisher> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiInnerScreen(this, 48, 23, 80, 41));
        addButton(new GuiDownArrow(this, 32, 39));
        addButton(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 164, 15));
        addButton(new GuiFluidGauge(() -> tile.fluidTank, () -> tile.getFluidTanks(null), GaugeType.STANDARD, this, 6, 13));
        addButton(new GuiEnergyInfo(tile.getEnergyContainer(), this));
        addButton(new GuiSecurityTab<>(this, tile));
        addButton(new GuiRedstoneControl(this, tile));
        addButton(new GuiUpgradeTab(this, tile));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        renderTitleText();
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 94) + 2, titleTextColor());
        renderScaledText(EnergyDisplay.of(tile.getEnergyContainer().getEnergy(), tile.getEnergyContainer().getMaxEnergy()).getTextComponent(), 51, 26, screenTextColor(), 74);
        renderScaledText(MekanismLang.FINISHED.translate(YesNo.of(tile.finishedCalc)), 51, 35, screenTextColor(), 74);
        FluidStack fluid = tile.fluidTank.getFluid();
        if (fluid.isEmpty()) {
            renderScaledText(MekanismLang.NO_FLUID.translate(), 51, 44, screenTextColor(), 74);
        } else {
            renderScaledText(MekanismLang.GENERIC_STORED_MB.translate(fluid, fluid.getAmount()), 51, 44, screenTextColor(), 74);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}