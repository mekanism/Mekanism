package mekanism.client.gui.machine;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiDownArrow;
import mekanism.client.gui.element.bar.GuiHorizontalPowerBar;
import mekanism.client.gui.element.button.ToggleButton;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.IProgressInfoHandler.IBooleanProgressInfoHandler;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.machine.TileEntityRotaryCondensentrator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiRotaryCondensentrator extends GuiConfigurableTile<TileEntityRotaryCondensentrator, MekanismTileContainer<TileEntityRotaryCondensentrator>> {

    private static final ResourceLocation condensentrating = Mekanism.rl("rotary_condensentrator_condensentrating");
    private static final ResourceLocation decondensentrating = Mekanism.rl("rotary_condensentrator_decondensentrating");

    public GuiRotaryCondensentrator(MekanismTileContainer<TileEntityRotaryCondensentrator> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
        titleLabelY = 4;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addButton(new GuiDownArrow(this, 159, 44));
        addButton(new GuiHorizontalPowerBar(this, tile.getEnergyContainer(), 115, 75));
        addButton(new GuiEnergyTab(this, tile.getEnergyContainer(), tile::getEnergyUsed));
        addButton(new GuiFluidGauge(() -> tile.fluidTank, () -> tile.getFluidTanks(null), GaugeType.STANDARD, this, 133, 13));
        addButton(new GuiGasGauge(() -> tile.gasTank, () -> tile.getGasTanks(null), GaugeType.STANDARD, this, 25, 13));
        addButton(new GuiProgress(new IBooleanProgressInfoHandler() {
            @Override
            public boolean fillProgressBar() {
                return tile.getActive();
            }

            @Override
            public boolean isActive() {
                return !tile.mode;
            }
        }, ProgressType.LARGE_RIGHT, this, 64, 39).jeiCategories(condensentrating));
        addButton(new GuiProgress(new IBooleanProgressInfoHandler() {
            @Override
            public boolean fillProgressBar() {
                return tile.getActive();
            }

            @Override
            public boolean isActive() {
                return tile.mode;
            }
        }, ProgressType.LARGE_LEFT, this, 64, 39).jeiCategories(decondensentrating));
        addButton(new ToggleButton(this, 4, 4, () -> tile.mode, () -> Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.NEXT_MODE, tile)),
              getOnHover(MekanismLang.CONDENSENTRATOR_TOGGLE)));
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, (tile.mode ? MekanismLang.DECONDENSENTRATING : MekanismLang.CONDENSENTRATING).translate(), 6, imageHeight - 92, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}