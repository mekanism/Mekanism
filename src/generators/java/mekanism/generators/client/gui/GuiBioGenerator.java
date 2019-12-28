package mekanism.generators.client.gui;

import java.util.Arrays;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.MekanismLang;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.inventory.container.BioGeneratorContainer;
import mekanism.generators.common.tile.TileEntityBioGenerator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiBioGenerator extends GuiMekanismTile<TileEntityBioGenerator, BioGeneratorContainer> {

    public GuiBioGenerator(BioGeneratorContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        ResourceLocation resource = getGuiLocation();
        addButton(new GuiRedstoneControl(this, tile, resource));
        addButton(new GuiSecurityTab<>(this, tile, resource));
        addButton(new GuiEnergyInfo(() -> Arrays.asList(
              GeneratorsLang.PRODUCING_AMOUNT.translate(EnergyDisplay.of(tile.getActive() ? MekanismGeneratorsConfig.generators.bioGeneration.get() : 0)),
              MekanismLang.MAX_OUTPUT.translate(EnergyDisplay.of(tile.getMaxOutput()))), this, resource));
        addButton(new GuiVerticalPowerBar(this, tile, resource, 164, 15));
        addButton(new GuiSlot(SlotType.NORMAL, this, resource, 16, 34));
        addButton(new GuiSlot(SlotType.NORMAL, this, resource, 142, 34).with(SlotOverlay.POWER));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), 45, 6, 0x404040);
        drawString(MekanismLang.INVENTORY.translate(), 8, (ySize - 96) + 2, 0x404040);
        drawString(EnergyDisplay.of(tile.getEnergy()).getTextComponent(), 51, 26, 0x00CD00);
        drawString(GeneratorsLang.STORED_BIO_FUEL.translate(tile.bioFuelSlot.fluidStored), 51, 35, 0x00CD00);
        drawString(GeneratorsLang.OUTPUT_RATE_SHORT.translate(EnergyDisplay.of(tile.getMaxOutput())), 51, 44, 0x00CD00);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        int displayInt = tile.getScaledFuelLevel(52);
        drawTexturedRect(guiLeft + 7, guiTop + 17 + 52 - displayInt, 176, 52 + 52 - displayInt, 4, displayInt);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismGenerators.rl("gui/bio_generator.png");
    }
}