package mekanism.generators.client.gui;

import java.util.Arrays;
import java.util.Collections;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import mekanism.generators.client.gui.element.GuiReactorTab;
import mekanism.generators.client.gui.element.GuiReactorTab.ReactorTab;
import mekanism.generators.common.inventory.container.reactor.ReactorControllerContainer;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiReactorController extends GuiMekanismTile<TileEntityReactorController, ReactorControllerContainer> {

    public GuiReactorController(ReactorControllerContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        if (tile.isFormed()) {
            ResourceLocation resource = getGuiLocation();
            addButton(new GuiEnergyInfo(() -> tile.isFormed() ? Arrays.asList(
                  TextComponentUtil.build(Translation.of("gui.mekanism.storing"), ": ", EnergyDisplay.of(tile.getEnergy(), tile.getMaxEnergy())),
                  TextComponentUtil.build(Translation.of("gui.mekanism.producing"), ": ",
                        EnergyDisplay.of(tile.getReactor().getPassiveGeneration(false, true)), "/t")) : Collections.emptyList(), this, resource));
            addButton(new GuiSlot(SlotType.NORMAL, this, resource, 79, 38));
            addButton(new GuiReactorTab(this, tile, ReactorTab.HEAT, resource));
            addButton(new GuiReactorTab(this, tile, ReactorTab.FUEL, resource));
            addButton(new GuiReactorTab(this, tile, ReactorTab.STAT, resource));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), 46, 6, 0x404040);
        drawString(TextComponentUtil.translate(tile.getActive() ? "gui.mekanism.formed" : "gui.mekanism.incomplete"), 8, 16, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "blank.png");
    }
}