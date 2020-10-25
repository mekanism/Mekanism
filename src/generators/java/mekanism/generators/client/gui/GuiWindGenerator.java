package mekanism.generators.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiRedstoneControlTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.MekanismLang;
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
        addButton(new GuiInnerScreen(this, 48, 23, 80, 40, () -> {
            List<ITextComponent> list = new ArrayList<>();
            list.add(EnergyDisplay.of(tile.getEnergyContainer().getEnergy(), tile.getEnergyContainer().getMaxEnergy()).getTextComponent());
            list.add(GeneratorsLang.POWER.translate(MekanismUtils.convertToDisplay(MekanismGeneratorsConfig.generators.windGenerationMin.get()
                  .multiply(tile.getCurrentMultiplier())).toString(2)));
            list.add(GeneratorsLang.OUTPUT_RATE_SHORT.translate(EnergyDisplay.of(tile.getMaxOutput())));
            if (!tile.getActive()) {
                ILangEntry reason = tile.isBlacklistDimension() ? GeneratorsLang.NO_WIND : GeneratorsLang.SKY_BLOCKED;
                list.add(reason.translateColored(EnumColor.DARK_RED));
            }
            return list;
        }));
        addButton(new GuiRedstoneControlTab(this, tile));
        addButton(new GuiSecurityTab(this, tile));
        addButton(new GuiEnergyTab(() -> Arrays.asList(GeneratorsLang.PRODUCING_AMOUNT.translate(
              tile.getActive() ? EnergyDisplay.of(MekanismGeneratorsConfig.generators.windGenerationMin.get().multiply(tile.getCurrentMultiplier())) : EnergyDisplay.ZERO),
              MekanismLang.MAX_OUTPUT.translate(EnergyDisplay.of(tile.getMaxOutput()))), this));
        addButton(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 164, 15));
        addButton(new GuiStateTexture(this, 18, 35, tile::getActive, MekanismGenerators.rl(ResourceType.GUI.getPrefix() + "wind_on.png"),
              MekanismGenerators.rl(ResourceType.GUI.getPrefix() + "wind_off.png")));
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, playerInventory.getDisplayName(), playerInventoryTitleX, playerInventoryTitleY, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}