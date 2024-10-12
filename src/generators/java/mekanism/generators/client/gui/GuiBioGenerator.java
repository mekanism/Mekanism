package mekanism.generators.client.gui;

import java.util.List;
import mekanism.client.SpecialColors;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.GuiSideHolder;
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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiBioGenerator extends GuiMekanismTile<TileEntityBioGenerator, MekanismTileContainer<TileEntityBioGenerator>> {

    public GuiBioGenerator(MekanismTileContainer<TileEntityBioGenerator> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        //Add the side holder before the slots, as it holds a couple of the slots
        addRenderableWidget(GuiSideHolder.create(this, -26, 6, 98, true, true, SpecialColors.TAB_ARMOR_SLOTS));
        super.addGuiElements();
        addRenderableWidget(new GuiInnerScreen(this, 48, 23, 80, 40, () -> List.of(
              EnergyDisplay.of(tile.getEnergyContainer().getEnergy()).getTextComponent(),
              GeneratorsLang.STORED_BIO_FUEL.translate(TextUtils.format(tile.bioFuelTank.getFluidAmount())),
              GeneratorsLang.OUTPUT_RATE_SHORT.translate(EnergyDisplay.of(tile.getMaxOutput()))
        )));
        addRenderableWidget(new GuiEnergyTab(this, () -> List.of(
              GeneratorsLang.PRODUCING_AMOUNT.translate(tile.getActive() ? EnergyDisplay.of(MekanismGeneratorsConfig.generators.bioGeneration.get()) : EnergyDisplay.ZERO),
              MekanismLang.MAX_OUTPUT.translate(EnergyDisplay.of(tile.getMaxOutput())))));
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 164, 15));
        addRenderableWidget(new GuiFluidBar(this, GuiFluidBar.getProvider(tile.bioFuelTank, tile.getFluidTanks(null)), 7, 15, 4, 52, false));
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}