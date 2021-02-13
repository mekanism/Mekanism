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
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextUtils;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.tile.TileEntityBioGenerator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiBioGenerator extends GuiMekanismTile<TileEntityBioGenerator, MekanismTileContainer<TileEntityBioGenerator>> {

    public GuiBioGenerator(MekanismTileContainer<TileEntityBioGenerator> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiInnerScreen(this, 48, 23, 80, 40, () -> Arrays.asList(
              EnergyDisplay.of(tile.getEnergyContainer().getEnergy()).getTextComponent(),
              GeneratorsLang.STORED_BIO_FUEL.translate(TextUtils.format(tile.bioFuelTank.getFluidAmount())),
              GeneratorsLang.OUTPUT_RATE_SHORT.translate(EnergyDisplay.of(tile.getMaxOutput()))
        )).defaultFormat());
        addButton(new GuiRedstoneControlTab(this, tile));
        addButton(new GuiSecurityTab(this, tile));
        addButton(new GuiEnergyTab(() -> Arrays.asList(
              GeneratorsLang.PRODUCING_AMOUNT.translate(tile.getActive() ? EnergyDisplay.of(MekanismGeneratorsConfig.generators.bioGeneration.get()) : EnergyDisplay.ZERO),
              MekanismLang.MAX_OUTPUT.translate(EnergyDisplay.of(tile.getMaxOutput()))), this));
        addButton(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 164, 15));
        addButton(new GuiVerticalPowerBar(this, () -> tile.bioFuelTank.getFluidAmount() / (double) tile.bioFuelTank.getCapacity(), 7, 15));
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, playerInventory.getDisplayName(), playerInventoryTitleX, playerInventoryTitleY, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}