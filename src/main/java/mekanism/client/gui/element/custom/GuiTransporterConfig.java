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
import mekanism.client.gui.element.button.ColorButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.SideDataButton;
import mekanism.client.gui.element.custom.GuiSideConfiguration.GuiPos;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.network.PacketConfigurationUpdate;
import mekanism.common.network.PacketConfigurationUpdate.ConfigurationPacket;
import mekanism.common.network.PacketGuiInteract;
import mekanism.common.network.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.client.gui.screen.Screen;

public class GuiTransporterConfig extends GuiWindow {

    private List<GuiPos> slotPosList = new ArrayList<>();
    private TileEntityMekanism tile;

    public GuiTransporterConfig(IGuiWrapper gui, int x, int y, TileEntityMekanism tile) {
        super(gui, x, y, 156, 95);
        this.tile = tile;
        interactionStrategy = InteractionStrategy.ALL;
        slotPosList.add(new GuiPos(RelativeSide.BOTTOM, 44, 64));
        slotPosList.add(new GuiPos(RelativeSide.TOP, 44, 34));
        slotPosList.add(new GuiPos(RelativeSide.FRONT, 44, 49));
        slotPosList.add(new GuiPos(RelativeSide.BACK, 29, 64));
        slotPosList.add(new GuiPos(RelativeSide.LEFT, 29, 49));
        slotPosList.add(new GuiPos(RelativeSide.RIGHT, 59, 49));

        addChild(new GuiInnerScreen(gui, relativeX + 41, relativeY + 15, 74, 12));
        //Add the borders to the actual buttons
        //Note: We don't bother adding a border for the center one as it is covered by the side ones
        //Top
        addChild(new GuiInnerScreen(gui, relativeX + 43, relativeY + 33, 16, 16));
        //Left
        addChild(new GuiInnerScreen(gui, relativeX + 28, relativeY + 48, 16, 16));
        //Right
        addChild(new GuiInnerScreen(gui, relativeX + 58, relativeY + 48, 16, 16));
        //Bottom
        addChild(new GuiInnerScreen(gui, relativeX + 43, relativeY + 63, 16, 16));
        //Bottom left
        addChild(new GuiInnerScreen(gui, relativeX + 28, relativeY + 63, 16, 16));
        addChild(new GuiSlot(SlotType.NORMAL, gui, relativeX + 111, relativeY + 48));
        addChild(new MekanismImageButton(gui, gui.getLeft() + relativeX + 136, gui.getTop() + relativeY + 6, 14, getButtonLocation("strict_input"),
              () -> Mekanism.packetHandler.sendToServer(new PacketConfigurationUpdate(tile.getPos())), getOnHover(MekanismLang.STRICT_INPUT)));
        addChild(new ColorButton(gui, gui.getLeft() + relativeX + 112, gui.getTop() + relativeY + 49, 16, 16, () -> getTile().getEjector().getOutputColor(),
              () -> Mekanism.packetHandler.sendToServer(new PacketConfigurationUpdate(tile.getPos(), Screen.hasShiftDown() ? 2 : 0)),
              () -> Mekanism.packetHandler.sendToServer(new PacketConfigurationUpdate(tile.getPos(), 1))));
        for (GuiPos guiPos : slotPosList) {
            addChild(new SideDataButton(gui, gui.getLeft() + relativeX + guiPos.xPos, gui.getTop() + relativeY + guiPos.yPos, guiPos.relativeSide,
                  () -> getTile().getConfig().getDataType(TransmissionType.ITEM, guiPos.relativeSide), () -> getTile().getEjector().getInputColor(guiPos.relativeSide),
                  tile, () -> null, ConfigurationPacket.INPUT_COLOR, getOnHover()));
        }
        Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.CONTAINER_TRACK_EJECTOR, tile, 0));
        ((MekanismContainer)((GuiMekanism<?>) guiObj).getContainer()).startTracking(0, ((ISideConfiguration) tile).getEjector());
    }

    @Override
    public void close() {
        super.close();
        Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.CONTAINER_STOP_TRACKING, tile, 0));
        ((MekanismContainer)((GuiMekanism<?>) guiObj).getContainer()).stopTracking(0);
    }

    public <TILE extends TileEntityMekanism & ISideConfiguration> TILE getTile() {
        return (TILE) tile;
    }

    private IHoverable getOnHover() {
        return (onHover, xAxis, yAxis) -> {
            if (onHover instanceof SideDataButton) {
                SideDataButton button = (SideDataButton) onHover;
                DataType dataType = button.getDataType();
                if (dataType != null) {
                    EnumColor color = button.getColor();
                    if (color != null) {
                        displayTooltip(color.getColoredName(), xAxis, yAxis);
                    } else {
                        displayTooltip(MekanismLang.NONE.translate(), xAxis, yAxis);
                    }
                }
            }
        };
    }

    @Override
    public void renderForeground(int mouseX, int mouseY) {
        super.renderForeground(mouseX, mouseY);
        drawTitleText(MekanismLang.TRANSPORTER_CONFIG.translate(), 5);
        drawTextScaledBound(MekanismLang.STRICT_INPUT_ENABLED.translate(OnOff.of(getTile().getEjector().hasStrictInput())), relativeX + 43, relativeY + 17, screenTextColor(), 70);
        drawString(MekanismLang.INPUT.translate(), relativeX + 38, relativeY + 81, 0x787878);
        drawString(MekanismLang.OUTPUT.translate(), relativeX + 104, relativeY + 68, 0x787878);
    }
}
