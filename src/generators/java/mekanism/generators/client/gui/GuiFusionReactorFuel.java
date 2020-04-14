package mekanism.generators.client.gui;

import java.util.Arrays;
import java.util.Collections;
import javax.annotation.Nonnull;
import org.lwjgl.glfw.GLFW;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.generators.client.gui.element.GuiReactorTab;
import mekanism.generators.client.gui.element.GuiReactorTab.ReactorTab;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.network.PacketGeneratorsGuiInteract;
import mekanism.generators.common.network.PacketGeneratorsGuiInteract.GeneratorsGuiInteraction;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiFusionReactorFuel extends GuiFusionReactorInfo {

    private TextFieldWidget injectionRateField;

    public GuiFusionReactorFuel(EmptyTileContainer<TileEntityFusionReactorController> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiEnergyInfo(() -> tile.isFormed() ? Arrays.asList(MekanismLang.STORING.translate(EnergyDisplay.of(tile.energyContainer.getEnergy(), tile.energyContainer.getMaxEnergy())),
              GeneratorsLang.PRODUCING_AMOUNT.translate(EnergyDisplay.of(tile.getReactor().getPassiveGeneration(false, true)))) : Collections.emptyList(),
              this));
        addButton(new GuiGasGauge(() -> tile.deuteriumTank, () -> tile.getGasTanks(null), GaugeType.SMALL, this, 25, 64));
        addButton(new GuiGasGauge(() -> tile.fuelTank, () -> tile.getGasTanks(null), GaugeType.STANDARD, this, 79, 50));
        addButton(new GuiGasGauge(() -> tile.tritiumTank, () -> tile.getGasTanks(null), GaugeType.SMALL, this, 133, 64));
        addButton(new GuiProgress(() -> tile.isBurning() ? 1 : 0, ProgressType.SMALL_RIGHT, this, 47, 76));
        addButton(new GuiProgress(() -> tile.isBurning() ? 1 : 0, ProgressType.SMALL_LEFT, this, 101, 76));
        addButton(new GuiReactorTab(this, tile, ReactorTab.HEAT));
        addButton(new GuiReactorTab(this, tile, ReactorTab.STAT));
        addButton(injectionRateField = new TextFieldWidget(font, getGuiLeft() + 98, getGuiTop() + 115, 26, 11, ""));
        injectionRateField.changeFocus(true);
        injectionRateField.setMaxStringLength(2);
    }

    @Override
    public void resize(@Nonnull Minecraft minecraft, int scaledWidth, int scaledHeight) {
        String s = injectionRateField.getText();
        super.resize(minecraft, scaledWidth, scaledHeight);
        injectionRateField.setText(s);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        drawString(tile.getName(), (getXSize() / 2) - (getStringWidth(tile.getName()) / 2), 5, 0x404040);
        drawCenteredText(GeneratorsLang.REACTOR_INJECTION_RATE.translate(tile.getReactor() == null ? MekanismLang.NONE : tile.getReactor().getInjectionRate()),
              0, getXSize(), 35, 0x404040);
        drawString(GeneratorsLang.REACTOR_EDIT_RATE.translate(), 50, 117, 0x404040);
    }

    @Override
    public void tick() {
        super.tick();
        injectionRateField.tick();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (injectionRateField.canWrite()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                //Manually handle hitting escape making the field lose focus
                injectionRateField.setFocused2(false);
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
                setInjection();
                return true;
            }
            return injectionRateField.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int keyCode) {
        if (injectionRateField.canWrite()) {
            if (Character.isDigit(c)) {
                //Only allow a subset of characters to be entered into the frequency text box
                return injectionRateField.charTyped(c, keyCode);
            }
            return false;
        }
        return super.charTyped(c, keyCode);
    }

    private void setInjection() {
        if (!injectionRateField.getText().isEmpty()) {
            MekanismGenerators.packetHandler.sendToServer(new PacketGeneratorsGuiInteract(GeneratorsGuiInteraction.INJECTION_RATE, tile, Integer.parseInt(injectionRateField.getText())));
            injectionRateField.setText("");
        }
    }
}