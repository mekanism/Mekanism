package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.Coord4D;
import mekanism.api.RelativeSide;
import mekanism.api.text.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.GuiElement.IHoverable;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.SideDataButton;
import mekanism.client.gui.element.tab.GuiConfigTypeTab;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.network.PacketConfigurationUpdate;
import mekanism.common.network.PacketConfigurationUpdate.ConfigurationPacket;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiSideConfiguration extends GuiMekanismTile<TileEntityMekanism, EmptyTileContainer<TileEntityMekanism>> {

    private List<GuiPos> slotPosList = new ArrayList<>();
    private TransmissionType currentType;
    private List<GuiConfigTypeTab> configTabs = new ArrayList<>();

    public GuiSideConfiguration(EmptyTileContainer<TileEntityMekanism> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        ySize = 95;
        currentType = getTopTransmission();
        slotPosList.add(new GuiPos(RelativeSide.BOTTOM, 81, 64));
        slotPosList.add(new GuiPos(RelativeSide.TOP, 81, 34));
        slotPosList.add(new GuiPos(RelativeSide.FRONT, 81, 49));
        slotPosList.add(new GuiPos(RelativeSide.BACK, 66, 64));
        slotPosList.add(new GuiPos(RelativeSide.LEFT, 66, 49));
        slotPosList.add(new GuiPos(RelativeSide.RIGHT, 96, 49));
    }

    public <TILE extends TileEntityMekanism & ISideConfiguration> TILE getTile() {
        return (TILE) tile;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiInnerScreen(this, 51, 15, 74, 12));
        //Add the borders to the actual buttons
        //Note: We don't bother adding a border for the center one as it is covered by the side ones
        //Top
        addButton(new GuiInnerScreen(this, 80, 33, 16, 16));
        //Left
        addButton(new GuiInnerScreen(this, 65, 48, 16, 16));
        //Right
        addButton(new GuiInnerScreen(this, 95, 48, 16, 16));
        //Bottom
        addButton(new GuiInnerScreen(this, 80, 63, 16, 16));
        //Bottom left
        addButton(new GuiInnerScreen(this, 65, 63, 16, 16));
        List<TransmissionType> transmissions = getTile().getConfig().getTransmissions();
        for (int i = 0; i < transmissions.size(); i++) {
            TransmissionType type = transmissions.get(i);
            //TODO: Figure out if there is a simpler way to do y
            GuiConfigTypeTab tab = new GuiConfigTypeTab(this, type, i < 3 ? -26 : 176, 2 + 28 * (i % 3));
            addButton(tab);
            configTabs.add(tab);
        }
        updateTabs();

        addButton(new MekanismImageButton(this, getGuiLeft() + 6, getGuiTop() + 6, 14, getButtonLocation("back"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.BACK_BUTTON, tile.getPos()))));
        addButton(new MekanismImageButton(this, getGuiLeft() + 156, getGuiTop() + 6, 14, getButtonLocation("auto_eject"),
              () -> Mekanism.packetHandler.sendToServer(new PacketConfigurationUpdate(ConfigurationPacket.EJECT, Coord4D.get(tile), 0, 0, currentType)),
              getOnHover(MekanismLang.AUTO_EJECT)));
        for (GuiPos guiPos : slotPosList) {
            addButton(new SideDataButton(this, getGuiLeft() + guiPos.xPos, getGuiTop() + guiPos.yPos, guiPos.relativeSide.ordinal(),
                  () -> getTile().getConfig().getDataType(currentType, guiPos.relativeSide), () -> {
                DataType dataType = getTile().getConfig().getDataType(currentType, guiPos.relativeSide);
                return dataType == null ? EnumColor.GRAY : dataType.getColor();
            }, tile, () -> currentType, ConfigurationPacket.SIDE_DATA, getOnHover()));
        }
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
        //TODO: Is there a reason to not have them just always all be visible?
        for (GuiConfigTypeTab tab : configTabs) {
            tab.visible = currentType != tab.getTransmissionType();
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawCenteredText(MekanismLang.CONFIG_TYPE.translate(currentType), 0, getXSize(), 5, 0x404040);
        //TODO: Convert to GuiElement
        ConfigInfo config = getTile().getConfig().getConfig(currentType);
        if (config != null && config.canEject()) {
            drawString(MekanismLang.EJECT.translate(OnOff.of(config.isEjecting())), 53, 17, 0x00CD00);
        } else {
            drawString(MekanismLang.NO_EJECT.translate(), 53, 17, 0x00CD00);
        }
        drawString(MekanismLang.SLOTS.translate(), 77, 81, 0x787878);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    public void tick() {
        super.tick();
        if (tile == null || MekanismUtils.getTileEntity(minecraft.world, tile.getPos()) == null) {
            minecraft.displayGuiScreen(null);
        }
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