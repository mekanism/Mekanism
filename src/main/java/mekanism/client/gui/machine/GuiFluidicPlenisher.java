package mekanism.client.gui.machine;

import java.util.ArrayList;
import java.util.List;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiDownArrow;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiRedstoneControlTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.machine.TileEntityFluidicPlenisher;
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
    public void func_231160_c_() {
        super.func_231160_c_();
        func_230480_a_(new GuiInnerScreen(this, 54, 23, 80, 41, () -> {
            List<ITextComponent> list = new ArrayList<>();
            list.add(EnergyDisplay.of(tile.getEnergyContainer().getEnergy(), tile.getEnergyContainer().getMaxEnergy()).getTextComponent());
            list.add(MekanismLang.FINISHED.translate(YesNo.of(tile.finishedCalc)));
            FluidStack fluid = tile.fluidTank.getFluid();
            if (fluid.isEmpty()) {
                list.add(MekanismLang.NO_FLUID.translate());
            } else {
                list.add(MekanismLang.GENERIC_STORED_MB.translate(fluid, fluid.getAmount()));
            }
            return list;
        }));
        func_230480_a_(new GuiDownArrow(this, 32, 39));
        func_230480_a_(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 164, 15));
        func_230480_a_(new GuiFluidGauge(() -> tile.fluidTank, () -> tile.getFluidTanks(null), GaugeType.STANDARD, this, 6, 13));
        func_230480_a_(new GuiEnergyTab(tile.getEnergyContainer(), this));
        func_230480_a_(new GuiSecurityTab<>(this, tile));
        func_230480_a_(new GuiRedstoneControlTab(this, tile));
        func_230480_a_(new GuiUpgradeTab(this, tile));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        renderTitleText();
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 94) + 2, titleTextColor());
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}