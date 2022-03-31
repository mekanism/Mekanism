package mekanism.generators.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.tile.TileEntityGasGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiGasGenerator extends GuiMekanismTile<TileEntityGasGenerator, MekanismTileContainer<TileEntityGasGenerator>> {

    public GuiGasGenerator(MekanismTileContainer<TileEntityGasGenerator> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiEnergyTab(this, () -> List.of(
              GeneratorsLang.PRODUCING_AMOUNT.translate(EnergyDisplay.of(tile.getGenerationRate().multiply(tile.getUsed()).multiply(tile.getMaxBurnTicks()))),
              MekanismLang.MAX_OUTPUT.translate(EnergyDisplay.of(tile.getMaxOutput())))));
        addRenderableWidget(new GuiGasGauge(() -> tile.fuelTank, () -> tile.getGasTanks(null), GaugeType.WIDE, this, 55, 18));
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 164, 15));
    }

    @Override
    protected void drawForegroundText(@Nonnull PoseStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, playerInventoryTitle, inventoryLabelX, inventoryLabelY, titleTextColor());
        Component component = GeneratorsLang.GAS_BURN_RATE.translate(tile.getUsed());
        int left = inventoryLabelX + getStringWidth(playerInventoryTitle) + 4;
        int end = imageWidth - 8;
        left = Math.max(left, end - getStringWidth(component));
        drawTextScaledBound(matrix, component, left, inventoryLabelY, titleTextColor(), end - left);
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}