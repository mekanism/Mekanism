package mekanism.client.gui.item;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.api.energy.IEnergyContainer;
import mekanism.client.ClientTickHandler;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.custom.GuiFrequencySelector;
import mekanism.client.gui.element.custom.GuiFrequencySelector.IGuiColorFrequencySelector;
import mekanism.client.gui.element.custom.GuiFrequencySelector.IItemGuiFrequencySelector;
import mekanism.client.gui.element.custom.GuiTeleporterStatus;
import mekanism.common.MekanismLang;
import mekanism.common.content.teleporter.TeleporterFrequency;
import mekanism.common.inventory.container.item.PortableTeleporterContainer;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiPortableTeleporter extends GuiMekanism<PortableTeleporterContainer> implements IItemGuiFrequencySelector<TeleporterFrequency, PortableTeleporterContainer>,
      IGuiColorFrequencySelector<TeleporterFrequency> {

    private MekanismButton teleportButton;

    public GuiPortableTeleporter(PortableTeleporterContainer container, Inventory inv, Component title) {
        super(container, inv, title);
        imageHeight = 172;
        titleLabelY = 4;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiTeleporterStatus(this, () -> getFrequency() != null, menu::getStatus));
        addRenderableWidget(new GuiVerticalPowerBar(this, new IBarInfoHandler() {
            @Override
            public Component getTooltip() {
                IEnergyContainer container = StorageUtils.getEnergyContainer(menu.getStack(), 0);
                return container == null ? EnergyDisplay.ZERO.getTextComponent() : EnergyDisplay.of(container).getTextComponent();
            }

            @Override
            public double getLevel() {
                IEnergyContainer container = StorageUtils.getEnergyContainer(menu.getStack(), 0);
                return container == null ? 0 : container.getEnergy().divideToLevel(container.getMaxEnergy());
            }
        }, 158, 26));
        teleportButton = addRenderableWidget(new TranslationButton(this, 42, 147, 92, 20, MekanismLang.BUTTON_TELEPORT, () -> {
            TeleporterFrequency frequency = getFrequency();
            if (frequency != null && menu.getStatus() == 1) {
                //This should always be true if the teleport button is active, but validate it just in case
                ClientTickHandler.portableTeleport(getMinecraft().player, menu.getHand(), frequency.getIdentity());
                getMinecraft().player.closeContainer();
            } else {
                //If something did go wrong make the teleport button not able to be pressed
                teleportButton.active = false;
            }
        }));
        //Teleporter button starts as deactivated until we have a frequency get synced
        teleportButton.active = false;
        addRenderableWidget(new GuiFrequencySelector<>(this, 14));
    }

    @Override
    public void buttonsUpdated() {
        teleportButton.active = menu.getStatus() == 1 && getFrequency() != null;
    }

    @Override
    protected void drawForegroundText(@NotNull PoseStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    @Override
    public FrequencyType<TeleporterFrequency> getFrequencyType() {
        return FrequencyType.TELEPORTER;
    }

    @Override
    public PortableTeleporterContainer getFrequencyContainer() {
        return menu;
    }
}