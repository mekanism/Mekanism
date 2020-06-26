package mekanism.generators.client.gui;

import java.util.Arrays;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.generators.client.gui.element.GuiFusionReactorTab;
import mekanism.generators.client.gui.element.GuiFusionReactorTab.FusionReactorTab;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorController;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiFusionReactorController extends GuiMekanismTile<TileEntityFusionReactorController, MekanismTileContainer<TileEntityFusionReactorController>> {

    public GuiFusionReactorController(MekanismTileContainer<TileEntityFusionReactorController> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void func_231160_c_() {
        super.func_231160_c_();
        if (tile.getMultiblock().isFormed()) {
            func_230480_a_(new GuiEnergyTab(() -> Arrays.asList(MekanismLang.STORING.translate(
                  EnergyDisplay.of(tile.getMultiblock().energyContainer.getEnergy(), tile.getMultiblock().energyContainer.getMaxEnergy())),
                  GeneratorsLang.PRODUCING_AMOUNT.translate(EnergyDisplay.of(tile.getMultiblock().getPassiveGeneration(false, true)))), this));
            func_230480_a_(new GuiFusionReactorTab(this, tile, FusionReactorTab.HEAT));
            func_230480_a_(new GuiFusionReactorTab(this, tile, FusionReactorTab.FUEL));
            func_230480_a_(new GuiFusionReactorTab(this, tile, FusionReactorTab.STAT));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawTitleText(GeneratorsLang.FUSION_REACTOR.translate(), 5);
        drawString(MekanismLang.MULTIBLOCK_FORMED.translate(), 8, 16, titleTextColor());
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}