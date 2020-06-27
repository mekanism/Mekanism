package mekanism.generators.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Arrays;
import javax.annotation.Nonnull;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiRedstoneControlTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.generators.client.gui.element.GuiStateTexture;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.tile.TileEntitySolarGenerator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiSolarGenerator<TILE extends TileEntitySolarGenerator> extends GuiMekanismTile<TILE, MekanismTileContainer<TILE>> {

    public GuiSolarGenerator(MekanismTileContainer<TILE> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void func_231160_c_() {
        super.func_231160_c_();
        func_230480_a_(new GuiInnerScreen(this, 48, 23, 80, 40, () -> Arrays.asList(
              EnergyDisplay.of(tile.getEnergyContainer().getEnergy(), tile.getEnergyContainer().getMaxEnergy()).getTextComponent(),
              GeneratorsLang.PRODUCING_AMOUNT.translate(EnergyDisplay.of(tile.getLastProductionAmount())),
              GeneratorsLang.OUTPUT_RATE_SHORT.translate(EnergyDisplay.of(tile.getMaxOutput()))
        )));
        func_230480_a_(new GuiRedstoneControlTab(this, tile));
        func_230480_a_(new GuiSecurityTab<>(this, tile));
        func_230480_a_(new GuiEnergyTab(() -> Arrays.asList(
              GeneratorsLang.PRODUCING_AMOUNT.translate(EnergyDisplay.of(tile.getLastProductionAmount())),
              MekanismLang.MAX_OUTPUT.translate(EnergyDisplay.of(tile.getMaxOutput()))), this));
        func_230480_a_(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 164, 15));
        func_230480_a_(new GuiStateTexture(this, 18, 35, tile::canSeeSun, MekanismGenerators.rl(ResourceType.GUI.getPrefix() + "sees_sun.png"),
              MekanismGenerators.rl(ResourceType.GUI.getPrefix() + "no_sun.png")));
    }

    @Override
    protected void func_230451_b_(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 2, titleTextColor());
        super.func_230451_b_(matrix, mouseX, mouseY);
    }
}