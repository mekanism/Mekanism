package mekanism.generators.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Arrays;
import javax.annotation.Nonnull;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.text.InputValidator;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.generators.client.gui.element.GuiFusionReactorTab;
import mekanism.generators.client.gui.element.GuiFusionReactorTab.FusionReactorTab;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.content.fusion.FusionReactorMultiblockData;
import mekanism.generators.common.network.PacketGeneratorsGuiInteract;
import mekanism.generators.common.network.PacketGeneratorsGuiInteract.GeneratorsGuiInteraction;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorController;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiFusionReactorFuel extends GuiFusionReactorInfo {

    private GuiTextField injectionRateField;

    public GuiFusionReactorFuel(EmptyTileContainer<TileEntityFusionReactorController> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiEnergyTab(() -> {
            FusionReactorMultiblockData multiblock = tile.getMultiblock();
            return Arrays.asList(MekanismLang.STORING.translate(EnergyDisplay.of(multiblock.energyContainer.getEnergy(), multiblock.energyContainer.getMaxEnergy())),
                  GeneratorsLang.PRODUCING_AMOUNT.translate(EnergyDisplay.of(multiblock.getPassiveGeneration(false, true))));
        }, this));
        addButton(new GuiGasGauge(() -> tile.getMultiblock().deuteriumTank, () -> tile.getMultiblock().getGasTanks(null), GaugeType.SMALL, this, 25, 64));
        addButton(new GuiGasGauge(() -> tile.getMultiblock().fuelTank, () -> tile.getMultiblock().getGasTanks(null), GaugeType.STANDARD, this, 79, 50));
        addButton(new GuiGasGauge(() -> tile.getMultiblock().tritiumTank, () -> tile.getMultiblock().getGasTanks(null), GaugeType.SMALL, this, 133, 64));
        addButton(new GuiProgress(() -> tile.getMultiblock().isBurning(), ProgressType.SMALL_RIGHT, this, 47, 76));
        addButton(new GuiProgress(() -> tile.getMultiblock().isBurning(), ProgressType.SMALL_LEFT, this, 101, 76));
        addButton(new GuiFusionReactorTab(this, tile, FusionReactorTab.HEAT));
        addButton(new GuiFusionReactorTab(this, tile, FusionReactorTab.STAT));
        addButton(injectionRateField = new GuiTextField(this, 98, 115, 26, 11));
        injectionRateField.changeFocus(true);
        injectionRateField.setInputValidator(InputValidator.DIGIT);
        injectionRateField.setEnterHandler(this::setInjection);
        injectionRateField.setMaxStringLength(2);
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        drawTitleText(matrix, GeneratorsLang.FUSION_REACTOR.translate(), titleY);
        drawCenteredText(matrix, GeneratorsLang.REACTOR_INJECTION_RATE.translate(tile.getMultiblock().getInjectionRate()), 0, xSize, 35, titleTextColor());
        drawString(matrix, GeneratorsLang.REACTOR_EDIT_RATE.translate(), 50, 117, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    private void setInjection() {
        if (!injectionRateField.getText().isEmpty()) {
            MekanismGenerators.packetHandler.sendToServer(new PacketGeneratorsGuiInteract(GeneratorsGuiInteraction.INJECTION_RATE, tile, Integer.parseInt(injectionRateField.getText())));
            injectionRateField.setText("");
        }
    }
}