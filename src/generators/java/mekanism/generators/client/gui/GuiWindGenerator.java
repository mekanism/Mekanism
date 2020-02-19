package mekanism.generators.client.gui;

import java.text.DecimalFormat;
import java.util.Arrays;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.generators.client.gui.element.GuiStateTexture;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiWindGenerator extends GuiMekanismTile<TileEntityWindGenerator, MekanismTileContainer<TileEntityWindGenerator>> {

    private final DecimalFormat powerFormat = new DecimalFormat("0.##");

    public GuiWindGenerator(MekanismTileContainer<TileEntityWindGenerator> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiInnerScreen(this, 48, 23, 80, 49));
        addButton(new GuiRedstoneControl(this, tile));
        addButton(new GuiSecurityTab<>(this, tile));
        addButton(new GuiEnergyInfo(() -> Arrays.asList(
              GeneratorsLang.PRODUCING_AMOUNT.translate(EnergyDisplay.of(tile.getActive() ? MekanismGeneratorsConfig.generators.windGenerationMin.get() * tile.getCurrentMultiplier() : 0)),
              MekanismLang.MAX_OUTPUT.translate(EnergyDisplay.of(tile.getMaxOutput()))), this));
        addButton(new GuiVerticalPowerBar(this, tile, 164, 15));
        addButton(new GuiStateTexture(this, 18, 35, tile::getActive, MekanismGenerators.rl("gui/elements/wind_on.png"), MekanismGenerators.rl("gui/elements/wind_off.png")));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), 45, 6, 0x404040);
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 2, 0x404040);
        drawString(EnergyDisplay.of(tile.getEnergy(), tile.getMaxEnergy()).getTextComponent(), 51, 26, 0x00CD00);
        //TODO: Why is this different from how all the other ones do it
        drawString(GeneratorsLang.POWER.translate(powerFormat.format(MekanismUtils.convertToDisplay(
              MekanismGeneratorsConfig.generators.windGenerationMin.get() * tile.getCurrentMultiplier()))), 51, 35, 0x00CD00);
        drawString(GeneratorsLang.OUTPUT_RATE_SHORT.translate(EnergyDisplay.of(tile.getMaxOutput())), 51, 44, 0x00CD00);
        int size = 44;
        if (!tile.getActive()) {
            size += 9;
            ILangEntry reason = GeneratorsLang.SKY_BLOCKED;
            if (tile.isBlacklistDimension()) {
                reason = GeneratorsLang.NO_WIND;
            }
            drawString(reason.translateColored(EnumColor.DARK_RED), 51, size, 0x00CD00);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}