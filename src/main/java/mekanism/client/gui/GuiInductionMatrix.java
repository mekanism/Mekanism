package mekanism.client.gui;

import java.util.Arrays;
import mekanism.api.math.FloatingLong;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.gauge.GuiEnergyGauge.IEnergyInfoHandler;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.tab.GuiMatrixTab;
import mekanism.client.gui.element.tab.GuiMatrixTab.MatrixTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.TileEntityInductionCasing;
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
        addButton(new GuiElementHolder(this, 141, 16, 26, 56));
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiSlot(SlotType.INNER_HOLDER_SLOT, this, 145, 20));
        addButton(new GuiSlot(SlotType.INNER_HOLDER_SLOT, this, 145, 50));
        addButton(new GuiInnerScreen(this, 51, 23, 80, 42, () -> Arrays.asList(
            MekanismLang.MATRIX_INPUT_AMOUNT.translate(MekanismLang.GENERIC_PER_TICK.translate(EnergyDisplay.of(tile.getLastInput()))),
            MekanismLang.MATRIX_OUTPUT_AMOUNT.translate(MekanismLang.GENERIC_PER_TICK.translate(EnergyDisplay.of(tile.getLastOutput()))),
            MekanismLang.CAPACITY.translate(EnergyDisplay.of(tile.getMaxEnergy()))
        )).defaultFormat());
        addButton(new GuiMatrixTab(this, tile, MatrixTab.STAT));
        addButton(new GuiEnergyGauge(new IEnergyInfoHandler() {
            @Override
            public FloatingLong getEnergy() {
                return tile.getEnergy();
            }

            @Override
            public FloatingLong getMaxEnergy() {
                return tile.getMaxEnergy();
            }
        }, GaugeType.MEDIUM, this, 7, 16, 34, 56));
        addButton(new GuiEnergyInfo(() -> Arrays.asList(MekanismLang.STORING.translate(EnergyDisplay.of(tile.getEnergy(), tile.getMaxEnergy())),
              MekanismLang.MATRIX_INPUT_RATE.translate(EnergyDisplay.of(tile.getLastInput())),
              MekanismLang.MATRIX_OUTPUT_RATE.translate(EnergyDisplay.of(tile.getLastOutput()))
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