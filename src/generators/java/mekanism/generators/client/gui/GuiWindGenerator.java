package mekanism.generators.client.gui;

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
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.generators.client.gui.element.GuiStateTexture;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiWindGenerator extends GuiMekanismTile<TileEntityWindGenerator, MekanismTileContainer<TileEntityWindGenerator>> {

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
        addButton(new GuiEnergyInfo(() -> Arrays.asList(GeneratorsLang.PRODUCING_AMOUNT.translate(
              tile.getActive() ? EnergyDisplay.of(MekanismGeneratorsConfig.generators.windGenerationMin.get().multiply(tile.getCurrentMultiplier())) : EnergyDisplay.ZERO),
              MekanismLang.MAX_OUTPUT.translate(EnergyDisplay.of(tile.getMaxOutput()))), this));
        addButton(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 164, 15));
        addButton(new GuiStateTexture(this, 18, 35, tile::getActive, MekanismGenerators.rl(ResourceType.GUI.getPrefix() + "wind_on.png"),
              MekanismGenerators.rl(ResourceType.GUI.getPrefix() + "wind_off.png")));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), 45, 6, titleTextColor());
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 2, titleTextColor());
        renderScaledText(EnergyDisplay.of(tile.getEnergyContainer().getEnergy(), tile.getEnergyContainer().getMaxEnergy()).getTextComponent(), 51, 26, screenTextColor(), 75);
        renderScaledText(GeneratorsLang.POWER.translate(MekanismUtils.convertToDisplay(MekanismGeneratorsConfig.generators.windGenerationMin.get()
              .multiply(tile.getCurrentMultiplier())).toString(2)), 51, 35, screenTextColor(), 75);
        renderScaledText(GeneratorsLang.OUTPUT_RATE_SHORT.translate(EnergyDisplay.of(tile.getMaxOutput())), 51, 44, screenTextColor(), 75);
        int size = 44;
        if (!tile.getActive()) {
            size += 9;
            ILangEntry reason = GeneratorsLang.SKY_BLOCKED;
            if (tile.isBlacklistDimension()) {
                reason = GeneratorsLang.NO_WIND;
            }
            drawString(reason.translateColored(EnumColor.DARK_RED), 51, size, screenTextColor());
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}