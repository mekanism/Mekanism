package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiDynamicHorizontalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.lib.Color;
import mekanism.common.lib.Color.ColorFunction;
import mekanism.common.tile.multiblock.TileEntitySPSCasing;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextUtils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiSPS extends GuiMekanismTile<TileEntitySPSCasing, MekanismTileContainer<TileEntitySPSCasing>> {

    public GuiSPS(MekanismTileContainer<TileEntitySPSCasing> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
        ySize += 16;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiGasGauge(() -> tile.getMultiblock().inputTank, () -> tile.getMultiblock().getGasTanks(null), GaugeType.STANDARD, this, 7, 17));
        addButton(new GuiGasGauge(() -> tile.getMultiblock().outputTank, () -> tile.getMultiblock().getGasTanks(null), GaugeType.STANDARD, this, 151, 17));
        addButton(new GuiInnerScreen(this, 27, 17, 122, 60, () -> {
            List<ITextComponent> list = new ArrayList<>();
            boolean active = tile.getMultiblock().lastProcessed > 0;
            list.add(MekanismLang.STATUS.translate(active ? MekanismLang.ACTIVE.translate() : MekanismLang.IDLE.translate()));
            if (active) {
                list.add(MekanismLang.SPS_ENERGY_INPUT.translate(EnergyDisplay.of(tile.getMultiblock().lastReceivedEnergy)));
                list.add(MekanismLang.PROCESS_RATE_MB.translate(tile.getMultiblock().getProcessRate()));
            }
            return list;
        }));
        addButton(new GuiDynamicHorizontalRateBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return MekanismLang.PROGRESS.translate(TextUtils.getPercent(tile.getMultiblock().getScaledProgress()));
            }

            @Override
            public double getLevel() {
                return tile.getMultiblock().getScaledProgress();
            }
        }, 7, 79, xSize - 16, ColorFunction.scale(Color.rgbi(60, 45, 74), Color.rgbi(100, 30, 170))));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawTitleText(MekanismLang.SPS.translate(), 6);
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 4, titleTextColor());
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}
