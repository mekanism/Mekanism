package mekanism.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
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
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiModificationStation extends GuiMekanismTile<TileEntityModificationStation, MekanismTileContainer<TileEntityModificationStation>> {

    private IModule<?> selectedModule;
    private TranslationButton removeButton;

    public GuiModificationStation(MekanismTileContainer<TileEntityModificationStation> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
        imageHeight += 64;
        inventoryLabelY = imageHeight - 92;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addButton(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 154, 40));
        addButton(new GuiEnergyTab(this, tile.getEnergyContainer()));
        addButton(new GuiProgress(tile::getScaledProgress, ProgressType.LARGE_RIGHT, this, 65, 123));
        removeButton = addButton(new TranslationButton(this, 34, 96, 108, 17, MekanismLang.BUTTON_REMOVE,
              () -> Mekanism.packetHandler.sendToServer(new PacketRemoveModule(tile.getBlockPos(), selectedModule.getData()))));
        removeButton.active = false;

        addButton(new GuiModuleScrollList(this, 34, 20, 108, 74, () -> tile.containerSlot.getStack().copy(), this::onModuleSelected));
    }

    public void onModuleSelected(IModule<?> module) {
        selectedModule = module;
        removeButton.active = module != null;
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, inventory.getDisplayName(), inventoryLabelX, inventoryLabelY, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}