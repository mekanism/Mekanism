package mekanism.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiDynamicHorizontalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.MekanismLang;
import mekanism.common.content.sps.SPSMultiblockData;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.lib.Color;
import mekanism.common.lib.Color.ColorFunction;
import mekanism.common.tile.multiblock.TileEntitySPSCasing;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiSPS extends GuiMekanismTile<TileEntitySPSCasing, MekanismTileContainer<TileEntitySPSCasing>> {

    public GuiSPS(MekanismTileContainer<TileEntitySPSCasing> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
        imageHeight += 16;
        inventoryLabelY = imageHeight - 92;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiGasGauge(() -> tile.getMultiblock().inputTank, () -> tile.getMultiblock().getGasTanks(null), GaugeType.STANDARD, this, 7, 17));
        addRenderableWidget(new GuiGasGauge(() -> tile.getMultiblock().outputTank, () -> tile.getMultiblock().getGasTanks(null), GaugeType.STANDARD, this, 151, 17));
        addRenderableWidget(new GuiInnerScreen(this, 27, 17, 122, 60, () -> {
            List<Component> list = new ArrayList<>();
            SPSMultiblockData multiblock = tile.getMultiblock();
            boolean active = multiblock.lastProcessed > 0;
            list.add(MekanismLang.STATUS.translate(active ? MekanismLang.ACTIVE : MekanismLang.IDLE));
            if (active) {
                list.add(MekanismLang.SPS_ENERGY_INPUT.translate(EnergyDisplay.of(multiblock.lastReceivedEnergy)));
                list.add(MekanismLang.PROCESS_RATE_MB.translate(multiblock.getProcessRate()));
            }
            return list;
        }).jeiCategories(MekanismJEIRecipeType.SPS));
        addRenderableWidget(new GuiDynamicHorizontalRateBar(this, new IBarInfoHandler() {
            @Override
            public Component getTooltip() {
                return MekanismLang.PROGRESS.translate(TextUtils.getPercent(tile.getMultiblock().getScaledProgress()));
            }

            @Override
            public double getLevel() {
                return Math.min(1, tile.getMultiblock().getScaledProgress());
            }
        }, 7, 79, 160, ColorFunction.scale(Color.rgbi(60, 45, 74), Color.rgbi(100, 30, 170))));
    }

    @Override
    protected void drawForegroundText(@NotNull PoseStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, playerInventoryTitle, inventoryLabelX, inventoryLabelY, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}
