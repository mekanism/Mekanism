package mekanism.client.gui;

import java.util.Arrays;
import mekanism.api.math.FloatingLong;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.gauge.GuiEnergyGauge.IEnergyInfoHandler;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiMatrixTab;
import mekanism.client.gui.element.tab.GuiMatrixTab.MatrixTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.multiblock.TileEntityInductionCasing;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiInductionMatrix extends GuiMekanismTile<TileEntityInductionCasing, MekanismTileContainer<TileEntityInductionCasing>> {

    public GuiInductionMatrix(MekanismTileContainer<TileEntityInductionCasing> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    protected void initPreSlots() {
        func_230480_a_(new GuiElementHolder(this, 141, 16, 26, 56));
    }

    @Override
    public void init() {
        super.init();
        func_230480_a_(new GuiSlot(SlotType.INNER_HOLDER_SLOT, this, 145, 20));
        func_230480_a_(new GuiSlot(SlotType.INNER_HOLDER_SLOT, this, 145, 50));
        func_230480_a_(new GuiInnerScreen(this, 49, 21, 84, 46, () -> Arrays.asList(
              MekanismLang.ENERGY.translate(EnergyDisplay.of(tile.getMultiblock().getEnergy())),
              MekanismLang.CAPACITY.translate(EnergyDisplay.of(tile.getMultiblock().getStorageCap())),
              MekanismLang.MATRIX_INPUT_AMOUNT.translate(MekanismLang.GENERIC_PER_TICK.translate(EnergyDisplay.of(tile.getMultiblock().getLastInput()))),
              MekanismLang.MATRIX_OUTPUT_AMOUNT.translate(MekanismLang.GENERIC_PER_TICK.translate(EnergyDisplay.of(tile.getMultiblock().getLastOutput())))
        )).spacing(2));
        func_230480_a_(new GuiMatrixTab(this, tile, MatrixTab.STAT));
        func_230480_a_(new GuiEnergyGauge(new IEnergyInfoHandler() {
            @Override
            public FloatingLong getEnergy() {
                return tile.getMultiblock().getEnergy();
            }

            @Override
            public FloatingLong getMaxEnergy() {
                return tile.getMultiblock().getStorageCap();
            }
        }, GaugeType.MEDIUM, this, 7, 16, 34, 56));
        func_230480_a_(new GuiEnergyTab(() -> Arrays.asList(MekanismLang.STORING.translate(EnergyDisplay.of(tile.getMultiblock().getEnergy(), tile.getMultiblock().getStorageCap())),
              MekanismLang.MATRIX_INPUT_RATE.translate(EnergyDisplay.of(tile.getMultiblock().getLastInput())),
              MekanismLang.MATRIX_OUTPUT_RATE.translate(EnergyDisplay.of(tile.getMultiblock().getLastOutput()))
        ), this));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        ITextComponent name = MekanismLang.MATRIX.translate();
        drawTitleText(name, 6);
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 94) + 2, titleTextColor());
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}