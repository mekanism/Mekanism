package mekanism.client.gui.item;

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
import mekanism.common.attachments.containers.type.ContainerType;
import mekanism.common.content.teleporter.TeleporterFrequency;
import mekanism.common.inventory.container.item.PortableTeleporterContainer;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.FrequencyTypes;
import mekanism.common.tile.TileEntityTeleporter.TeleporterStatus;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import org.jetbrains.annotations.NotNull;

public class GuiPortableTeleporter extends GuiMekanism<PortableTeleporterContainer> implements IItemGuiFrequencySelector<TeleporterFrequency, PortableTeleporterContainer>,
      IGuiColorFrequencySelector<TeleporterFrequency> {

    private GuiTeleporterStatus status;
    private MekanismButton teleportButton;

    public GuiPortableTeleporter(PortableTeleporterContainer container, Inventory inv, Component title) {
        super(container, inv, title, DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT + 6);
        titleLabelY = 4;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        status = addRenderableWidget(new GuiTeleporterStatus(this, () -> getFrequency() != null, menu::getStatus));
        addRenderableWidget(new GuiVerticalPowerBar(this, new IBarInfoHandler() {
                  @Override
                  public Component getTooltip() {
                      EnergyHandler energyHandler = menu.getEnergyHandler();
                      return energyHandler == null ? EnergyDisplay.ZERO.getTextComponent() : EnergyDisplay.of(energyHandler).getTextComponent();
                  }

                  @Override
                  public double getLevel() {
                      EnergyHandler energyHandler = menu.getEnergyHandler();
                      return energyHandler == null ? 0 : ContainerType.ENERGY.divideToLevel(energyHandler);
                  }
              }, 158, 26)
        ).warning(WarningType.NOT_ENOUGH_ENERGY, () -> menu.getStatus() == TeleporterStatus.NOT_ENOUGH_ENERGY);
        teleportButton = addRenderableWidget(new TranslationButton(this, 42, 147, 92, 20, MekanismLang.BUTTON_TELEPORT, (element, event, isDoubleClick) -> {
            GuiPortableTeleporter gui = (GuiPortableTeleporter) element.gui();
            TeleporterFrequency frequency = gui.getFrequency();
            if (frequency != null && gui.menu.getStatus().isReady()) {
                //This should always be true if the teleport button is active, but validate it just in case
                Player player = Minecraft.getInstance().player;
                if (player == null) {
                    return false;
                }
                ClientTickHandler.portableTeleport(player, gui.menu.getHand(), frequency.getIdentity());
                player.closeContainer();
            } else {
                //If something did go wrong make the teleport button not able to be pressed
                element.active = false;
            }
            return true;
        }));
        //Teleporter button starts as deactivated until we have a frequency get synced
        teleportButton.active = false;
        addRenderableWidget(new GuiFrequencySelector<>(this, 14));
    }

    @Override
    public void buttonsUpdated() {
        teleportButton.active = menu.getStatus().isReady() && getFrequency() != null;
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        renderTitleTextWithOffset(guiGraphics, status.getRelativeRight());
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }

    @Override
    public FrequencyType<TeleporterFrequency> getFrequencyType() {
        return FrequencyTypes.TELEPORTER;
    }

    @Override
    public PortableTeleporterContainer getFrequencyContainer() {
        return menu;
    }
}