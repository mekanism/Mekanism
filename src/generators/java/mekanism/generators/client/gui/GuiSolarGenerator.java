package mekanism.generators.client.gui;

import java.util.Arrays;
import java.util.Collections;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.generators.client.gui.element.GuiStateTexture;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.tile.TileEntityAdvancedSolarGenerator;
import mekanism.generators.common.tile.TileEntitySolarGenerator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiSolarGenerator<TILE extends TileEntitySolarGenerator> extends GuiMekanismTile<TILE, MekanismTileContainer<TILE>> {

    public GuiSolarGenerator(MekanismTileContainer<TILE> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiInnerScreen(this, 48, 23, 80, 40));
        addButton(new GuiRedstoneControl(this, tile));
        addButton(new GuiSecurityTab<>(this, tile));
        addButton(new GuiEnergyInfo(() -> Arrays.asList(
            GeneratorsLang.PRODUCING_AMOUNT.translate(EnergyDisplay.of(tile.getLastProductionAmount())),
            MekanismLang.MAX_OUTPUT.translate(EnergyDisplay.of(tile.getMaxOutput()))), this));
        addButton(new GuiStateTexture(this, 18, 35, tile::canSeeSun, MekanismGenerators.rl(ResourceType.GUI.getPrefix() + "sees_sun.png"),
              MekanismGenerators.rl(ResourceType.GUI.getPrefix() + "no_sun.png")));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), tile instanceof TileEntityAdvancedSolarGenerator ? 30 : 45, 6, 0x404040);
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 2, 0x404040);
        renderScaledText(EnergyDisplay.of(tile.getEnergy(), tile.getMaxEnergy()).getTextComponent(), 51, 26, 0x00CD00, 75);
        renderScaledText(GeneratorsLang.PRODUCING_AMOUNT.translate(EnergyDisplay.of(tile.getLastProductionAmount())), 51, 35, 0x00CD00, 75);
        renderScaledText(GeneratorsLang.OUTPUT_RATE_SHORT.translate(EnergyDisplay.of(tile.getMaxOutput())), 51, 44, 0x00CD00, 75);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}