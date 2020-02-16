package mekanism.client.gui;

import java.util.Arrays;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.TileEntityFluidicPlenisher;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;

public class GuiFluidicPlenisher extends GuiMekanismTile<TileEntityFluidicPlenisher, MekanismTileContainer<TileEntityFluidicPlenisher>> {

    public GuiFluidicPlenisher(MekanismTileContainer<TileEntityFluidicPlenisher> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiSlot(SlotType.NORMAL, this, 27, 19));
        addButton(new GuiSlot(SlotType.NORMAL, this, 27, 50));
        addButton(new GuiSlot(SlotType.POWER, this, 142, 34).with(SlotOverlay.POWER));
        addButton(new GuiVerticalPowerBar(this, tile, 164, 15));
        addButton(new GuiFluidGauge(() -> tile.fluidTank, GuiGauge.Type.STANDARD, this, 6, 13));
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
        drawString(EnergyDisplay.of(tile.getEnergy(), tile.getMaxEnergy()).getTextComponent(), 51, 26, 0x00CD00);
        drawString(MekanismLang.FINISHED.translate(YesNo.of(tile.finishedCalc)), 51, 35, 0x00CD00);
        //TODO: 1.14 Convert to GuiElement
        FluidStack fluid = tile.fluidTank.getFluid();
        if (fluid.isEmpty()) {
            drawString(MekanismLang.NO_FLUID.translate(), 51, 44, 0x00CD00);
        } else {
            drawString(MekanismLang.GENERIC_STORED_MB.translate(fluid, fluid.getAmount()), 51, 44, 0x00CD00);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "electric_pump.png");
    }
}