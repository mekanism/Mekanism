package mekanism.client.gui.machine;

import java.util.ArrayList;
import java.util.List;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.tile.machine.TileEntityElectricPump;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

public class GuiElectricPump extends GuiMekanismTile<TileEntityElectricPump, MekanismTileContainer<TileEntityElectricPump>> {

    public GuiElectricPump(MekanismTileContainer<TileEntityElectricPump> container, Inventory inv, Component title) {
        super(container, inv, title);
        titleLabelY = 5;
        inventoryLabelY += 2;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiInnerScreen(this, 54, 23, 80, 42, () -> {
            List<Component> list = new ArrayList<>();
            list.add(EnergyDisplay.of(tile.energyContainer()).getTextComponent());
            if (tile.fluidTank.isEmpty()) {
                FluidResource fallBack = tile.getActiveType();
                if (fallBack.isEmpty()) {
                    list.add(MekanismLang.NO_FLUID.translate());
                } else {
                    list.add(fallBack.getHoverName());
                }
            } else {
                list.add(MekanismLang.GENERIC_STORED_MB.translate(tile.fluidTank.resource(), TextUtils.format(tile.fluidTank.amountAsLong())));
            }
            return list;
        }));
        addRenderableWidget(GuiTexturedElement.downArrow(this, 32, 39));
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.energyContainer(), 164, 15))
              .warning(WarningType.NOT_ENOUGH_ENERGY, () -> {
                  MachineEnergyContainer<TileEntityElectricPump> energyContainer = tile.energyContainer();
                  return energyContainer.getEnergyPerTick() > energyContainer.getAmountAsLong();
              });
        addRenderableWidget(new GuiFluidGauge(() -> tile.fluidTank, tile::getFluidTanks, GaugeType.STANDARD, this, 6, 13))
              .warning(WarningType.NO_SPACE_IN_OUTPUT, () -> tile.fluidTank.getNeededAsInt(FluidResource.EMPTY) < tile.estimateIncrementAmount());
        //TODO: Eventually we may want to consider showing a warning if the block under the pump is of the wrong type or there wasn't a valid spot to suck
        addRenderableWidget(new GuiEnergyTab(this, tile.energyContainer(), tile::usedEnergy));
    }

    @Override
    protected void drawForegroundText(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}