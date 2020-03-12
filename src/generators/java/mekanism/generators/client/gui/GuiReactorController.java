package mekanism.generators.client.gui;

import java.util.Arrays;
import java.util.Collections;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.generators.client.gui.element.GuiReactorTab;
import mekanism.generators.client.gui.element.GuiReactorTab.ReactorTab;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiReactorController extends GuiMekanismTile<TileEntityReactorController, MekanismTileContainer<TileEntityReactorController>> {

    public GuiReactorController(MekanismTileContainer<TileEntityReactorController> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        if (tile.isFormed()) {
            addButton(new GuiEnergyInfo(() -> tile.isFormed() ? Arrays.asList(MekanismLang.STORING.translate(EnergyDisplay.of(tile.getEnergy(), tile.getMaxEnergy())),
                  GeneratorsLang.PRODUCING_AMOUNT.translate(EnergyDisplay.of(tile.getReactor().getPassiveGeneration(false, true)))) : Collections.emptyList(),
                  this));
            addButton(new GuiReactorTab(this, tile, ReactorTab.HEAT));
            addButton(new GuiReactorTab(this, tile, ReactorTab.FUEL));
            addButton(new GuiReactorTab(this, tile, ReactorTab.STAT));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), 46, 6, 0x404040);
        drawString(tile.isFormed() ? MekanismLang.MULTIBLOCK_FORMED.translate() : MekanismLang.MULTIBLOCK_INCOMPLETE.translate(), 8, 16, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}