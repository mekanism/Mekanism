package mekanism.client.gui.element.window;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.RelativeSide;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.SideDataButton;
import mekanism.client.gui.element.tab.GuiConfigTypeTab;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.network.PacketConfigurationUpdate;
import mekanism.common.network.PacketConfigurationUpdate.ConfigurationPacket;
import mekanism.common.network.PacketGuiInteract;
import mekanism.common.network.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;

public class GuiSideConfiguration<TILE extends TileEntityMekanism & ISideConfiguration> extends GuiWindow {

    private final List<GuiConfigTypeTab> configTabs = new ArrayList<>();
    private final TILE tile;
    private TransmissionType currentType;

    public GuiSideConfiguration(IGuiWrapper gui, int x, int y, TILE tile) {
        super(gui, x, y, 156, 115);
        this.tile = tile;
        interactionStrategy = InteractionStrategy.ALL;
        currentType = getTopTransmission();
        //TODO: Try to make the GUI look a bit better as it still seems a bit off with the scales and such
        // Maybe we want to eventually add some sort of "in world preview" type thing
        addChild(new GuiInnerScreen(gui, relativeX + 41, relativeY + 25, 74, 12));
        List<TransmissionType> transmissions = this.tile.getConfig().getTransmissions();
        for (int i = 0; i < transmissions.size(); i++) {
            GuiConfigTypeTab tab = new GuiConfigTypeTab(gui, transmissions.get(i), relativeX + (i < 4 ? -26 : width), relativeY + (2 + 28 * (i % 4)), this, i < 4);
            addChild(tab);
            configTabs.add(tab);
        }
        updateTabs();
        addChild(new MekanismImageButton(gui, gui.getLeft() + relativeX + 136, gui.getTop() + relativeY + 6, 14, getButtonLocation("auto_eject"),
              () -> Mekanism.packetHandler.sendToServer(new PacketConfigurationUpdate(this.tile.getPos(), currentType)), getOnHover(MekanismLang.AUTO_EJECT)));
        addSideDataButton(RelativeSide.BOTTOM, 71, 74);
        addSideDataButton(RelativeSide.TOP, 71, 44);
        addSideDataButton(RelativeSide.FRONT, 71, 59);
        addSideDataButton(RelativeSide.BACK, 56, 74);
        addSideDataButton(RelativeSide.LEFT, 56, 59);
        addSideDataButton(RelativeSide.RIGHT, 86, 59);
        Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.CONTAINER_TRACK_SIDE_CONFIG, tile, 1));
        ((MekanismContainer) ((GuiMekanism<?>) guiObj).getContainer()).startTracking(1, this.tile.getConfig());
    }

    private void addSideDataButton(RelativeSide side, int xPos, int yPos) {
        addChild(new SideDataButton(guiObj, guiObj.getLeft() + relativeX + xPos, guiObj.getTop() + relativeY + yPos, side,
              () -> tile.getConfig().getDataType(currentType, side), () -> {
            DataType dataType = tile.getConfig().getDataType(currentType, side);
            return dataType == null ? EnumColor.GRAY : dataType.getColor();
        }, tile, () -> currentType, ConfigurationPacket.SIDE_DATA, getOnHover(side)));
    }

    @Override
    public void close() {
        super.close();
        Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.CONTAINER_STOP_TRACKING, tile, 1));
        ((MekanismContainer) ((GuiMekanism<?>) guiObj).getContainer()).stopTracking(1);
    }

    private IHoverable getOnHover(RelativeSide side) {
        return (onHover, matrix, xAxis, yAxis) -> {
            if (onHover instanceof SideDataButton) {
                DataType dataType = ((SideDataButton) onHover).getDataType();
                if (dataType != null) {
                    displayTooltip(matrix, MekanismLang.GENERIC_WITH_TWO_PARENTHESIS.translateColored(dataType.getColor(), dataType,
                          dataType.getColor().getName(), side), xAxis, yAxis);
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
        for (GuiConfigTypeTab tab : configTabs) {
            tab.visible = currentType != tab.getTransmissionType();
        }
    }

    @Override
    public void renderForeground(MatrixStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        drawTitleText(matrix, MekanismLang.CONFIG_TYPE.translate(currentType), 5);
        ConfigInfo config = tile.getConfig().getConfig(currentType);
        if (config == null || !config.canEject()) {
            drawString(matrix, MekanismLang.NO_EJECT.translate(), relativeX + 43, relativeY + 27, screenTextColor());
        } else {
            drawString(matrix, MekanismLang.EJECT.translate(OnOff.of(config.isEjecting())), relativeX + 43, relativeY + 27, screenTextColor());
        }
        drawString(matrix, MekanismLang.SLOTS.translate(), relativeX + 67, relativeY + 96, subheadingTextColor());
    }
}