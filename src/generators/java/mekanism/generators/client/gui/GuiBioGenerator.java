package mekanism.generators.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Arrays;
import javax.annotation.Nonnull;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiFluidBar;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.tab.GuiEnergyTab;
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
    protected void addGuiElements() {
        super.addGuiElements();
        addButton(new GuiInnerScreen(this, 48, 23, 80, 40, () -> Arrays.asList(
              EnergyDisplay.of(tile.getEnergyContainer().getEnergy()).getTextComponent(),
              GeneratorsLang.STORED_BIO_FUEL.translate(TextUtils.format(tile.bioFuelTank.getFluidAmount())),
              GeneratorsLang.OUTPUT_RATE_SHORT.translate(EnergyDisplay.of(tile.getMaxOutput()))
        )));
        addButton(new GuiEnergyTab(this, () -> Arrays.asList(
              GeneratorsLang.PRODUCING_AMOUNT.translate(tile.getActive() ? EnergyDisplay.of(MekanismGeneratorsConfig.generators.bioGeneration.get()) : EnergyDisplay.ZERO),
              MekanismLang.MAX_OUTPUT.translate(EnergyDisplay.of(tile.getMaxOutput())))));
        addButton(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 164, 15));
        addButton(new GuiFluidBar(this, GuiFluidBar.getProvider(tile.bioFuelTank, tile.getFluidTanks(null)), 7, 15, 4, 52, false));
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, inventory.getDisplayName(), inventoryLabelX, inventoryLabelY, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}