package mekanism.client.gui;

import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.button.ToggleButton;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.TileEntityDimensionalStabilizer;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiDimensionalStabilizer extends GuiMekanismTile<TileEntityDimensionalStabilizer, MekanismTileContainer<TileEntityDimensionalStabilizer>> {


    public GuiDimensionalStabilizer(MekanismTileContainer<TileEntityDimensionalStabilizer> container, Inventory inv, Component title) {
        super(container, inv, title);
        inventoryLabelY += 2;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 164, 15))
              .warning(WarningType.NOT_ENOUGH_ENERGY, () -> {
                  MachineEnergyContainer<TileEntityDimensionalStabilizer> energyContainer = tile.getEnergyContainer();
                  return energyContainer.getEnergyPerTick().greaterThan(energyContainer.getEnergy());
              });
        addRenderableWidget(new GuiEnergyTab(this, tile.getEnergyContainer()));
        for (int z = 0; z < TileEntityDimensionalStabilizer.MAX_LOAD_DIAMETER; ++z) {
            for (int x = 0; x < TileEntityDimensionalStabilizer.MAX_LOAD_DIAMETER; ++x) {
                int finalX = x;
                int finalZ = z;
                addRenderableWidget(new ToggleButton(
                      this,
                      63 + 10 * x,
                      19 + 10 * z,
                      10,
                      () -> tile.isChunkloadingAt(finalX, finalZ),
                      () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteraction.TOGGLE_CHUNKLOAD, tile, finalZ * TileEntityDimensionalStabilizer.MAX_LOAD_DIAMETER + finalX)),
                      null));
            }
        }
    }
}
