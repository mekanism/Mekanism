package mekanism.client.gui;

import mekanism.api.gear.IModule;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.scroll.GuiModuleScrollList;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketRemoveModule;
import mekanism.common.tile.TileEntityModificationStation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiModificationStation extends GuiMekanismTile<TileEntityModificationStation, MekanismTileContainer<TileEntityModificationStation>> {

    private IModule<?> selectedModule;
    private MekanismButton removeButton;

    public GuiModificationStation(MekanismTileContainer<TileEntityModificationStation> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
        imageHeight += 64;
        inventoryLabelY = imageHeight - 92;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 156, 40))
              .warning(WarningType.NOT_ENOUGH_ENERGY, () -> {
                  MachineEnergyContainer<TileEntityModificationStation> energyContainer = tile.getEnergyContainer();
                  return energyContainer.getEnergyPerTick() > energyContainer.getEnergy();
              });
        addRenderableWidget(new GuiEnergyTab(this, tile.getEnergyContainer(), tile::usedEnergy));
        addRenderableWidget(new GuiProgress(tile::getScaledProgress, ProgressType.LARGE_RIGHT, this, 65, 123));
        removeButton = addRenderableWidget(new TranslationButton(this, 28, 96, 120, 17, MekanismLang.BUTTON_REMOVE, (element, mouseX, mouseY) -> {
            GuiModificationStation gui = (GuiModificationStation) element.gui();
            return PacketUtils.sendToServer(new PacketRemoveModule(gui.tile.getBlockPos(), gui.selectedModule.getDataHolder(), hasShiftDown()));
        })).setTooltip(MekanismLang.REMOVE_ALL_MODULES_TOOLTIP);
        removeButton.active = selectedModule != null;

        addRenderableWidget(new GuiModuleScrollList(this, 28, 20, 74, () -> tile.containerSlot.getStack().copy(), this::onModuleSelected));
    }

    private void onModuleSelected(@Nullable IModule<?> module) {
        selectedModule = module;
        removeButton.active = module != null;
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleTextWithOffset(guiGraphics, 24);
        renderInventoryText(guiGraphics);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}