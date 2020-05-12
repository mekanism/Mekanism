package mekanism.client.gui.element.custom;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.RelativeSide;
import mekanism.api.text.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.GuiWindow;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.SideDataButton;
import mekanism.client.gui.element.tab.GuiConfigTypeTab;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.network.PacketConfigurationUpdate;
import mekanism.common.network.PacketConfigurationUpdate.ConfigurationPacket;
import mekanism.common.network.PacketGuiInteract;
import mekanism.common.network.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;

public class GuiSideConfiguration extends GuiWindow {

    private List<GuiPos> slotPosList = new ArrayList<>();
    private TransmissionType currentType;
    private List<GuiConfigTypeTab> configTabs = new ArrayList<>();
    private TileEntityMekanism tile;

    public GuiSideConfiguration(IGuiWrapper gui, int x, int y, TileEntityMekanism tile) {
        super(gui, x, y, 156, 95);
        this.tile = tile;
        interactionStrategy = InteractionStrategy.ALL;
        currentType = getTopTransmission();
        slotPosList.add(new GuiPos(RelativeSide.BOTTOM, 71, 64));
        slotPosList.add(new GuiPos(RelativeSide.TOP, 71, 34));
        slotPosList.add(new GuiPos(RelativeSide.FRONT, 71, 49));
        slotPosList.add(new GuiPos(RelativeSide.BACK, 56, 64));
        slotPosList.add(new GuiPos(RelativeSide.LEFT, 56, 49));
        slotPosList.add(new GuiPos(RelativeSide.RIGHT, 86, 49));

        addChild(new GuiInnerScreen(gui, relativeX + 41, relativeY + 15, 74, 12));
        //Add the borders to the actual buttons
        //Note: We don't bother adding a border for the center one as it is covered by the side ones
        //Top
        addChild(new GuiInnerScreen(gui, relativeX + 70, relativeY + 33, 16, 16));
        //Left
        addChild(new GuiInnerScreen(gui, relativeX + 55, relativeY + 48, 16, 16));
        //Right
        addChild(new GuiInnerScreen(gui, relativeX + 85, relativeY + 48, 16, 16));
        //Bottom
        addChild(new GuiInnerScreen(gui, relativeX + 70, relativeY + 63, 16, 16));
        //Bottom left
        addChild(new GuiInnerScreen(gui, relativeX + 55, relativeY + 63, 16, 16));
        List<TransmissionType> transmissions = getTile().getConfig().getTransmissions();
        for (int i = 0; i < transmissions.size(); i++) {
            TransmissionType type = transmissions.get(i);
            GuiConfigTypeTab tab = new GuiConfigTypeTab(gui, type, relativeX + (i < 3 ? -26 : width), relativeY + (2 + 28 * (i % 3)), this);
            addChild(tab);
            configTabs.add(tab);
        }
        updateTabs();

        addChild(new MekanismImageButton(gui, gui.getLeft() + relativeX + 136, gui.getTop() + relativeY + 6, 14, getButtonLocation("auto_eject"),
              () -> Mekanism.packetHandler.sendToServer(new PacketConfigurationUpdate(tile.getPos(), currentType)), getOnHover(MekanismLang.AUTO_EJECT)));
        for (GuiPos guiPos : slotPosList) {
            addChild(new SideDataButton(gui, gui.getLeft() + relativeX + guiPos.xPos, gui.getTop() + relativeY + guiPos.yPos, guiPos.relativeSide,
                  () -> getTile().getConfig().getDataType(currentType, guiPos.relativeSide), () -> {
                DataType dataType = getTile().getConfig().getDataType(currentType, guiPos.relativeSide);
                return dataType == null ? EnumColor.GRAY : dataType.getColor();
            }, tile, () -> currentType, ConfigurationPacket.SIDE_DATA, getOnHover()));
        }
        Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.CONTAINER_TRACK_SIDE_CONFIG, tile, 1));
        ((MekanismContainer)((GuiMekanism<?>) guiObj).getContainer()).startTracking(1, ((ISideConfiguration) tile).getConfig());
    }

    @Override
    public void close() {
        super.close();
        Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.CONTAINER_STOP_TRACKING, tile, 1));
        ((MekanismContainer)((GuiMekanism<?>) guiObj).getContainer()).stopTracking(1);
    }

    public <TILE extends TileEntityMekanism & ISideConfiguration> TILE getTile() {
        return (TILE) tile;
    }

    private IHoverable getOnHover() {
        return (onHover, xAxis, yAxis) -> {
            if (onHover instanceof SideDataButton) {
                DataType dataType = ((SideDataButton) onHover).getDataType();
                if (dataType != null) {
                    displayTooltip(MekanismLang.GENERIC_WITH_PARENTHESIS.translateColored(dataType.getColor(), dataType, dataType.getColor().getName()), xAxis, yAxis);
                }
            }
        };
    }

    public TransmissionType getTopTransmission() {
        return getTile().getConfig().getTransmissions().get(0);
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
    public void renderForeground(int mouseX, int mouseY) {
        super.renderForeground(mouseX, mouseY);
        drawTitleText(MekanismLang.CONFIG_TYPE.translate(currentType), 5);
        ConfigInfo config = getTile().getConfig().getConfig(currentType);
        if (config == null || !config.canEject()) {
            drawString(MekanismLang.NO_EJECT.translate(), relativeX + 43, relativeY + 17, screenTextColor());
        } else {
            drawString(MekanismLang.EJECT.translate(OnOff.of(config.isEjecting())), relativeX + 43, relativeY + 17, screenTextColor());
        }
        drawString(MekanismLang.SLOTS.translate(), relativeX + 67, relativeY + 81, 0x787878);
    }

    public static class GuiPos {

        public final RelativeSide relativeSide;
        public final int xPos;
        public final int yPos;

        public GuiPos(RelativeSide side, int x, int y) {
            relativeSide = side;
            xPos = x;
            yPos = y;
        }
    }
}