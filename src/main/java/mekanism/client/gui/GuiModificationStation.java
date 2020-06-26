package mekanism.client.gui;

import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.scroll.GuiModuleScrollList;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiRedstoneControlTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.Module;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.PacketRemoveModule;
import mekanism.common.tile.TileEntityModificationStation;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiModificationStation extends GuiMekanismTile<TileEntityModificationStation, MekanismTileContainer<TileEntityModificationStation>> {

    private Module selectedModule;
    private TranslationButton removeButton;

    public GuiModificationStation(MekanismTileContainer<TileEntityModificationStation> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
        ySize += 64;
    }

    @Override
    public void func_231160_c_() {
        super.func_231160_c_();
        func_230480_a_(new GuiSecurityTab<>(this, tile));
        func_230480_a_(new GuiRedstoneControlTab(this, tile));
        func_230480_a_(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 154, 40));
        func_230480_a_(new GuiEnergyTab(tile.getEnergyContainer(), this));
        func_230480_a_(new GuiProgress(tile::getScaledProgress, ProgressType.LARGE_RIGHT, this, 65, 123));
        func_230480_a_(removeButton = new TranslationButton(this, getGuiLeft() + 34, getGuiTop() + 96, 108, 17, MekanismLang.BUTTON_REMOVE,
              () -> Mekanism.packetHandler.sendToServer(new PacketRemoveModule(tile.getPos(), selectedModule.getData()))));
        removeButton.field_230693_o_ = false;

        func_230480_a_(new GuiModuleScrollList(this, 34, 20, 108, 74, () -> tile.containerSlot.getStack().copy(), this::onModuleSelected));
    }

    public void onModuleSelected(Module module) {
        selectedModule = module;
        removeButton.field_230693_o_ = module != null;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        renderTitleText();
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 94) + 2, titleTextColor());
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}
