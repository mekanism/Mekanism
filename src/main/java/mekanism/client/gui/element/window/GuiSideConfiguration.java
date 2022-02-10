package mekanism.client.gui.element.window;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import mekanism.api.RelativeSide;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.SideDataButton;
import mekanism.client.gui.element.tab.GuiConfigTypeTab;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.network.to_server.PacketConfigurationUpdate;
import mekanism.common.network.to_server.PacketConfigurationUpdate.ConfigurationPacket;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;

public class GuiSideConfiguration<TILE extends TileEntityMekanism & ISideConfiguration> extends GuiWindow {

    private final List<GuiConfigTypeTab> configTabs = new ArrayList<>();
    private final Map<RelativeSide, SideDataButton> sideConfigButtons = new EnumMap<>(RelativeSide.class);
    private final MekanismButton ejectButton;
    private final TILE tile;
    private TransmissionType currentType;

    public GuiSideConfiguration(IGuiWrapper gui, int x, int y, TILE tile) {
        super(gui, x, y, 156, 115, WindowType.SIDE_CONFIG);
        this.tile = tile;
        interactionStrategy = InteractionStrategy.ALL;
        currentType = getTopTransmission();
        //TODO: Try to make the GUI look a bit better as it still seems a bit off with the scales and such
        // Maybe we want to eventually add some sort of "in world preview" type thing
        addChild(new GuiInnerScreen(gui, relativeX + 41, relativeY + 25, 74, 12, () -> {
            ConfigInfo config = this.tile.getConfig().getConfig(currentType);
            if (config == null || !config.canEject()) {
                return Collections.singletonList(MekanismLang.NO_EJECT.translate());
            }
            return Collections.singletonList(MekanismLang.EJECT.translate(OnOff.of(config.isEjecting())));
        }).tooltip(() -> {
            ConfigInfo config = this.tile.getConfig().getConfig(currentType);
            if (config == null || !config.canEject()) {
                return Collections.singletonList(MekanismLang.CANT_EJECT_TOOLTIP.translate());
            }
            return Collections.emptyList();
        }));
        List<TransmissionType> transmissions = this.tile.getConfig().getTransmissions();
        for (int i = 0; i < transmissions.size(); i++) {
            GuiConfigTypeTab tab = new GuiConfigTypeTab(gui, transmissions.get(i), relativeX + (i < 4 ? -26 : width), relativeY + (2 + 28 * (i % 4)), this, i < 4);
            addChild(tab);
            configTabs.add(tab);
        }
        ejectButton = addChild(new MekanismImageButton(gui, relativeX + 136, relativeY + 6, 14, getButtonLocation("auto_eject"),
              () -> Mekanism.packetHandler().sendToServer(new PacketConfigurationUpdate(ConfigurationPacket.EJECT, this.tile.getBlockPos(), currentType)),
              getOnHover(MekanismLang.AUTO_EJECT)));
        addChild(new MekanismImageButton(gui, relativeX + 136, relativeY + 95, 14, getButtonLocation("clear_sides"),
              () -> Mekanism.packetHandler().sendToServer(new PacketConfigurationUpdate(ConfigurationPacket.CLEAR_ALL, this.tile.getBlockPos(), currentType)),
              getOnHover(MekanismLang.SIDE_CONFIG_CLEAR)));
        addSideDataButton(RelativeSide.BOTTOM, 71, 74);
        addSideDataButton(RelativeSide.TOP, 71, 44);
        addSideDataButton(RelativeSide.FRONT, 71, 59);
        addSideDataButton(RelativeSide.BACK, 56, 74);
        addSideDataButton(RelativeSide.LEFT, 56, 59);
        addSideDataButton(RelativeSide.RIGHT, 86, 59);
        updateTabs();
        Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteraction.CONTAINER_TRACK_SIDE_CONFIG, tile, MekanismContainer.SIDE_CONFIG_WINDOW));
        ((MekanismContainer) ((GuiMekanism<?>) gui()).getMenu()).startTracking(MekanismContainer.SIDE_CONFIG_WINDOW, this.tile.getConfig());
    }

    private void addSideDataButton(RelativeSide side, int xPos, int yPos) {
        sideConfigButtons.put(side, addChild(new SideDataButton(gui(), relativeX + xPos, relativeY + yPos, side,
              () -> tile.getConfig().getDataType(currentType, side), () -> {
            DataType dataType = tile.getConfig().getDataType(currentType, side);
            return dataType == null ? EnumColor.GRAY : dataType.getColor();
        }, tile, () -> currentType, ConfigurationPacket.SIDE_DATA, getOnHover(side))));
    }

    @Override
    public void close() {
        super.close();
        Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteraction.CONTAINER_STOP_TRACKING, tile, MekanismContainer.SIDE_CONFIG_WINDOW));
        ((MekanismContainer) ((GuiMekanism<?>) gui()).getMenu()).stopTracking(MekanismContainer.SIDE_CONFIG_WINDOW);
    }

    private IHoverable getOnHover(RelativeSide side) {
        return (onHover, matrix, mouseX, mouseY) -> {
            if (onHover instanceof SideDataButton button) {
                DataType dataType = button.getDataType();
                if (dataType != null) {
                    displayTooltips(matrix, mouseX, mouseY, MekanismLang.GENERIC_WITH_TWO_PARENTHESIS.translateColored(dataType.getColor(), dataType,
                          dataType.getColor().getName(), side));
                }
            }
        };
    }

    private TransmissionType getTopTransmission() {
        return tile.getConfig().getTransmissions().get(0);
    }

    public void setCurrentType(TransmissionType type) {
        currentType = type;
    }

    public void updateTabs() {
        //Hide the current tab and make the others visible
        for (GuiConfigTypeTab tab : configTabs) {
            tab.visible = currentType != tab.getTransmissionType();
        }
        //Disable or enable the eject button based on if the transmission type supports ejecting
        ConfigInfo config = this.tile.getConfig().getConfig(currentType);
        if (config == null) {//Should never actually be null but just in case handle it
            ejectButton.active = false;
            for (SideDataButton button : sideConfigButtons.values()) {
                button.active = false;
            }
        } else {
            ejectButton.active = config.canEject();
            for (Map.Entry<RelativeSide, SideDataButton> entry : sideConfigButtons.entrySet()) {
                entry.getValue().active = config.isSideEnabled(entry.getKey());
            }
        }
    }

    @Override
    public void renderForeground(PoseStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        drawTitleText(matrix, MekanismLang.CONFIG_TYPE.translate(currentType), 5);
        drawCenteredText(matrix, MekanismLang.SLOTS.translate(), relativeX + 80, relativeY + 96, subheadingTextColor());
    }

    @Override
    protected int getTitlePadEnd() {
        return super.getTitlePadEnd() + 15;
    }
}