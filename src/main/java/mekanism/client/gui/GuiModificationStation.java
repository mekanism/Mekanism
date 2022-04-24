package mekanism.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nonnull;
import mekanism.api.gear.IModule;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.scroll.GuiModuleScrollList;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.to_server.PacketRemoveModule;
import mekanism.common.tile.TileEntityModificationStation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiModificationStation extends GuiMekanismTile<TileEntityModificationStation, MekanismTileContainer<TileEntityModificationStation>> {

    private IModule<?> selectedModule;
    private TranslationButton removeButton;

    public GuiModificationStation(MekanismTileContainer<TileEntityModificationStation> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
        imageHeight += 64;
        inventoryLabelY = imageHeight - 92;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 154, 40));
        addRenderableWidget(new GuiEnergyTab(this, tile.getEnergyContainer()));
        addRenderableWidget(new GuiProgress(tile::getScaledProgress, ProgressType.LARGE_RIGHT, this, 65, 123));
        removeButton = addRenderableWidget(new TranslationButton(this, 34, 96, 108, 17, MekanismLang.BUTTON_REMOVE,
              () -> Mekanism.packetHandler().sendToServer(new PacketRemoveModule(tile.getBlockPos(), selectedModule.getData()))));
        removeButton.active = false;

        addRenderableWidget(new GuiModuleScrollList(this, 34, 20, 108, 74, () -> tile.containerSlot.getStack().copy(), this::onModuleSelected));
    }

    public void onModuleSelected(IModule<?> module) {
        selectedModule = module;
        removeButton.active = module != null;
    }

    @Override
    protected void drawForegroundText(@Nonnull PoseStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, playerInventoryTitle, inventoryLabelX, inventoryLabelY, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}