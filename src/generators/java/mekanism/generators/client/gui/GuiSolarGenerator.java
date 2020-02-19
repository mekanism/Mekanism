package mekanism.generators.client.gui;

import java.util.Collections;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.generators.client.gui.element.GuiStateTexture;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.tile.TileEntityAdvancedSolarGenerator;
import mekanism.generators.common.tile.TileEntitySolarGenerator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiSolarGenerator extends GuiMekanismTile<TileEntitySolarGenerator, MekanismTileContainer<TileEntitySolarGenerator>> {

    public GuiSolarGenerator(MekanismTileContainer<TileEntitySolarGenerator> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiInnerScreen(this, 48, 23, 80, 40));
        addButton(new GuiRedstoneControl(this, tile));
        addButton(new GuiSecurityTab<>(this, tile));
        addButton(new GuiEnergyInfo(Collections::emptyList, this));
        addButton(new GuiStateTexture(this, 18, 35, tile::canSeeSun, MekanismGenerators.rl("gui/elements/sees_sun.png"), MekanismGenerators.rl("gui/elements/no_sun.png")));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), tile instanceof TileEntityAdvancedSolarGenerator ? 30 : 45, 6, 0x404040);
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 2, 0x404040);
        //TODO: Make this look better, it previously was split on two lines, but now the lang string includes the stuff for per tick
        drawCenteredText(GeneratorsLang.PRODUCING_AMOUNT.translate(EnergyDisplay.of(tile.getLastProductionAmount())), 48, 80, 28, 0x00CD00);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}