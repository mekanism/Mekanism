package mekanism.client.gui.machine;

import java.util.List;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.tile.machine.TileEntitySeismicVibrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiSeismicVibrator extends GuiMekanismTile<TileEntitySeismicVibrator, MekanismTileContainer<TileEntitySeismicVibrator>> {

    public GuiSeismicVibrator(MekanismTileContainer<TileEntitySeismicVibrator> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiInnerScreen(this, 16, 23, 112, 40, () -> List.of(
              tile.getActive() ? MekanismLang.VIBRATING.translate() : MekanismLang.IDLE.translate(),
              MekanismLang.CHUNK.translate(SectionPos.blockToSectionCoord(tile.getBlockPos().getX()), SectionPos.blockToSectionCoord(tile.getBlockPos().getZ()))
        )));
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 164, 15))
              .warning(WarningType.NOT_ENOUGH_ENERGY, () -> {
                  MachineEnergyContainer<TileEntitySeismicVibrator> energyContainer = tile.getEnergyContainer();
                  return energyContainer.getEnergyPerTick() > energyContainer.getEnergy();
              });
        addRenderableWidget(new GuiEnergyTab(this, tile.getEnergyContainer(), tile::getActive));
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}