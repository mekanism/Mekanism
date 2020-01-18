package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.Coord4D;
import mekanism.api.RelativeSide;
import mekanism.api.text.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.GuiSideConfiguration.GuiPos;
import mekanism.client.gui.button.ColorButton;
import mekanism.client.gui.button.MekanismButton.IHoverable;
import mekanism.client.gui.button.MekanismImageButton;
import mekanism.client.gui.button.SideDataButton;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.inventory.container.tile.TransporterConfigurationContainer;
import mekanism.common.network.PacketConfigurationUpdate;
import mekanism.common.network.PacketConfigurationUpdate.ConfigurationPacket;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;

public class GuiTransporterConfig extends GuiMekanismTile<TileEntityMekanism, TransporterConfigurationContainer> {

    private List<GuiPos> slotPosList = new ArrayList<>();

    public GuiTransporterConfig(TransporterConfigurationContainer container, PlayerInventory inv, ITextComponent title) {
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
        addButton(new MekanismImageButton(this, getGuiLeft() + 6, getGuiTop() + 6, 14, getButtonLocation("back"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.BACK_BUTTON, tile.getPos()))));
        addButton(new MekanismImageButton(this, getGuiLeft() + 156, getGuiTop() + 6, 14, getButtonLocation("strict_input"),
              () -> Mekanism.packetHandler.sendToServer(new PacketConfigurationUpdate(ConfigurationPacket.STRICT_INPUT, Coord4D.get(tile), 0, 0, null)),
              getOnHover(MekanismLang.STRICT_INPUT)));
        addButton(new ColorButton(this, getGuiLeft() + 122, getGuiTop() + 49, 16, 16, () -> getTile().getEjector().getOutputColor(),
              () -> Mekanism.packetHandler.sendToServer(new PacketConfigurationUpdate(ConfigurationPacket.EJECT_COLOR, Coord4D.get(tile),
                    InputMappings.isKeyDown(minecraft.mainWindow.getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) ? 2 : 0, 0, null)),
              () -> Mekanism.packetHandler.sendToServer(new PacketConfigurationUpdate(ConfigurationPacket.EJECT_COLOR, Coord4D.get(tile), 1, 0, null))));
        for (GuiPos guiPos : slotPosList) {
            addButton(new SideDataButton(this, getGuiLeft() + guiPos.xPos, getGuiTop() + guiPos.yPos, guiPos.relativeSide.ordinal(),
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
        drawCenteredText(MekanismLang.TRANSPORTER_CONFIG.translate(), 0, getXSize(), 5, 0x404040);
        renderScaledText(MekanismLang.STRICT_INPUT_ENABLED.translate(OnOff.of(getTile().getEjector().hasStrictInput())), 53, 17, 0x00CD00, 70);
        drawString(MekanismLang.INPUT.translate(), 48, 81, 0x787878);
        drawString(MekanismLang.OUTPUT.translate(), 114, 68, 0x787878);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "transporter_config.png");
    }
}