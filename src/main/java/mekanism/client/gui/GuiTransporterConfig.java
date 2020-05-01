package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.RelativeSide;
import mekanism.api.text.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.GuiSideConfiguration.GuiPos;
import mekanism.client.gui.element.GuiElement.IHoverable;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.ColorButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.SideDataButton;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.network.PacketConfigurationUpdate;
import mekanism.common.network.PacketConfigurationUpdate.ConfigurationPacket;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiTransporterConfig extends GuiMekanismTile<TileEntityMekanism, EmptyTileContainer<TileEntityMekanism>> {

    private List<GuiPos> slotPosList = new ArrayList<>();

    public GuiTransporterConfig(EmptyTileContainer<TileEntityMekanism> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        ySize = 95;
        slotPosList.add(new GuiPos(RelativeSide.BOTTOM, 54, 64));
        slotPosList.add(new GuiPos(RelativeSide.TOP, 54, 34));
        slotPosList.add(new GuiPos(RelativeSide.FRONT, 54, 49));
        slotPosList.add(new GuiPos(RelativeSide.BACK, 39, 64));
        slotPosList.add(new GuiPos(RelativeSide.LEFT, 39, 49));
        slotPosList.add(new GuiPos(RelativeSide.RIGHT, 69, 49));
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiInnerScreen(this, 51, 15, 74, 12));
        //Add the borders to the actual buttons
        //Note: We don't bother adding a border for the center one as it is covered by the side ones
        //Top
        addButton(new GuiInnerScreen(this, 53, 33, 16, 16));
        //Left
        addButton(new GuiInnerScreen(this, 38, 48, 16, 16));
        //Right
        addButton(new GuiInnerScreen(this, 68, 48, 16, 16));
        //Bottom
        addButton(new GuiInnerScreen(this, 53, 63, 16, 16));
        //Bottom left
        addButton(new GuiInnerScreen(this, 38, 63, 16, 16));
        addButton(new GuiSlot(SlotType.NORMAL, this, 121, 48));
        addButton(new MekanismImageButton(this, getGuiLeft() + 6, getGuiTop() + 6, 14, getButtonLocation("back"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.BACK_BUTTON, tile))));
        addButton(new MekanismImageButton(this, getGuiLeft() + 156, getGuiTop() + 6, 14, getButtonLocation("strict_input"),
              () -> Mekanism.packetHandler.sendToServer(new PacketConfigurationUpdate(tile.getPos())), getOnHover(MekanismLang.STRICT_INPUT)));
        addButton(new ColorButton(this, getGuiLeft() + 122, getGuiTop() + 49, 16, 16, () -> getTile().getEjector().getOutputColor(),
              () -> Mekanism.packetHandler.sendToServer(new PacketConfigurationUpdate(tile.getPos(), hasShiftDown() ? 2 : 0)),
              () -> Mekanism.packetHandler.sendToServer(new PacketConfigurationUpdate(tile.getPos(), 1))));
        for (GuiPos guiPos : slotPosList) {
            addButton(new SideDataButton(this, getGuiLeft() + guiPos.xPos, getGuiTop() + guiPos.yPos, guiPos.relativeSide,
                  () -> getTile().getConfig().getDataType(TransmissionType.ITEM, guiPos.relativeSide), () -> getTile().getEjector().getInputColor(guiPos.relativeSide),
                  tile, () -> null, ConfigurationPacket.INPUT_COLOR, getOnHover()));
        }
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
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawTitleText(MekanismLang.TRANSPORTER_CONFIG.translate(), 5);
        drawScaledText(MekanismLang.STRICT_INPUT_ENABLED.translate(OnOff.of(getTile().getEjector().hasStrictInput())), 53, 17, screenTextColor(), 70);
        drawString(MekanismLang.INPUT.translate(), 48, 81, 0x787878);
        drawString(MekanismLang.OUTPUT.translate(), 114, 68, 0x787878);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}