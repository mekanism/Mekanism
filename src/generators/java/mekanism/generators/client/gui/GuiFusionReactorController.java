package mekanism.generators.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Arrays;
import javax.annotation.Nonnull;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.generators.client.gui.element.GuiFusionReactorTab;
import mekanism.generators.client.gui.element.GuiFusionReactorTab.FusionReactorTab;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.content.fusion.FusionReactorMultiblockData;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorController;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiFusionReactorController extends GuiMekanismTile<TileEntityFusionReactorController, MekanismTileContainer<TileEntityFusionReactorController>> {

    public GuiFusionReactorController(MekanismTileContainer<TileEntityFusionReactorController> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
        titleY = 5;
    }

    @Override
    public void init() {
        super.init();
        if (tile.getMultiblock().isFormed()) {
            addButton(new GuiEnergyTab(() -> {
                FusionReactorMultiblockData multiblock = tile.getMultiblock();
                return Arrays.asList(MekanismLang.STORING.translate(EnergyDisplay.of(multiblock.energyContainer.getEnergy(), multiblock.energyContainer.getMaxEnergy())),
                      GeneratorsLang.PRODUCING_AMOUNT.translate(EnergyDisplay.of(multiblock.getPassiveGeneration(false, true))));
            }, this));
            addButton(new GuiFusionReactorTab(this, tile, FusionReactorTab.HEAT));
            addButton(new GuiFusionReactorTab(this, tile, FusionReactorTab.FUEL));
            addButton(new GuiFusionReactorTab(this, tile, FusionReactorTab.STAT));
        }
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        drawTitleText(matrix, GeneratorsLang.FUSION_REACTOR.translate(), titleY);
        drawString(matrix, MekanismLang.MULTIBLOCK_FORMED.translate(), 8, 16, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}