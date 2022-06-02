package mekanism.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiUpArrow;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.button.BasicColorButton;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiVisualsTab;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.machine.TileEntityDimensionalStabilizer;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.core.SectionPos;
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
        addRenderableWidget(new GuiVisualsTab(this, tile));
        addRenderableWidget(new GuiEnergyTab(this, tile.getEnergyContainer()));
        int tileChunkX = SectionPos.blockToSectionCoord(tile.getBlockPos().getX());
        int tileChunkZ = SectionPos.blockToSectionCoord(tile.getBlockPos().getZ());
        for (int x = -TileEntityDimensionalStabilizer.MAX_LOAD_RADIUS; x <= TileEntityDimensionalStabilizer.MAX_LOAD_RADIUS; x++) {
            int shiftedX = x + TileEntityDimensionalStabilizer.MAX_LOAD_RADIUS;
            int chunkX = tileChunkX + x;
            for (int z = -TileEntityDimensionalStabilizer.MAX_LOAD_RADIUS; z <= TileEntityDimensionalStabilizer.MAX_LOAD_RADIUS; z++) {
                int shiftedZ = z + TileEntityDimensionalStabilizer.MAX_LOAD_RADIUS;
                int chunkZ = tileChunkZ + z;
                if (x == 0 && z == 0) {
                    addRenderableWidget(BasicColorButton.renderActive(this, 63 + 10 * shiftedX, 19 + 10 * shiftedZ, 10, EnumColor.DARK_BLUE,
                          getOnHover(() -> MekanismLang.STABILIZER_CENTER.translate(chunkX, chunkZ))));
                } else {
                    int packetTarget = shiftedX * TileEntityDimensionalStabilizer.MAX_LOAD_DIAMETER + shiftedZ;
                    addRenderableWidget(BasicColorButton.toggle(this, 63 + 10 * shiftedX, 19 + 10 * shiftedZ, 10, EnumColor.DARK_BLUE,
                          () -> tile.isChunkLoadingAt(shiftedX, shiftedZ),
                          () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteraction.TOGGLE_CHUNKLOAD, tile, packetTarget)),
                          getOnHover(() -> MekanismLang.STABILIZER_TOGGLE_LOADING.translate(OnOff.of(tile.isChunkLoadingAt(shiftedX, shiftedZ), true), chunkX, chunkZ))));
                }
            }
        }
        addRenderableWidget(new GuiUpArrow(this, 52, 28));
    }

    @Override
    protected void drawForegroundText(@Nonnull PoseStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, playerInventoryTitle, inventoryLabelX, inventoryLabelY, titleTextColor());
        drawTextExact(matrix, MekanismLang.NORTH_SHORT.translate(), 53.5F, 41F, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}
