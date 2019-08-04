package mekanism.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.GuiSideConfiguration.GuiPos;
import mekanism.client.gui.button.GuiButtonDisableableImage;
import mekanism.client.gui.button.GuiColorButton;
import mekanism.client.gui.button.GuiSideDataButton;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.SideData;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.network.PacketConfigurationUpdate.ConfigurationPacket;
import mekanism.common.network.PacketConfigurationUpdate.ConfigurationUpdateMessage;
import mekanism.common.network.PacketSimpleGui.SimpleGuiMessage;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class GuiTransporterConfig extends GuiMekanismTile<TileEntityMekanism> {

    private Map<Integer, GuiPos> slotPosMap = new HashMap<>();
    private ISideConfiguration configurable;
    private List<GuiSideDataButton> sideDataButtons = new ArrayList<>();
    private GuiButton backButton;
    private GuiButton strictInputButton;
    private GuiButton colorButton;
    private int buttonID = 0;

    public GuiTransporterConfig(EntityPlayer player, ISideConfiguration tile) {
        super((TileEntityMekanism) tile, new ContainerNull(player, (TileEntityMekanism) tile));
        ySize = 95;
        configurable = tile;
        slotPosMap.put(0, new GuiPos(54, 64));
        slotPosMap.put(1, new GuiPos(54, 34));
        slotPosMap.put(2, new GuiPos(54, 49));
        slotPosMap.put(3, new GuiPos(39, 64));
        slotPosMap.put(4, new GuiPos(39, 49));
        slotPosMap.put(5, new GuiPos(69, 49));
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.clear();
        buttonList.add(backButton = new GuiButtonDisableableImage(buttonID++, guiLeft + 6, guiTop + 6, 14, 14, 190, 14, -14, getGuiLocation()));
        buttonList.add(strictInputButton = new GuiButtonDisableableImage(buttonID++, guiLeft + 156, guiTop + 6, 14, 14, 204, 14, -14, getGuiLocation()));
        buttonList.add(colorButton = new GuiColorButton(buttonID++, guiLeft + 122, guiTop + 49, 16, 16, () -> configurable.getEjector().getOutputColor()));
        for (int i = 0; i < slotPosMap.size(); i++) {
            GuiPos guiPos = slotPosMap.get(i);
            EnumFacing facing = EnumFacing.byIndex(i);
            GuiSideDataButton button = new GuiSideDataButton(buttonID++, guiLeft + guiPos.xPos, guiTop + guiPos.yPos, getGuiLocation(), i,
                  () -> configurable.getConfig().getOutput(TransmissionType.ITEM, facing), () -> configurable.getEjector().getInputColor(facing));
            buttonList.add(button);
            sideDataButtons.add(button);
        }
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) throws IOException {
        super.actionPerformed(guibutton);
        TileEntity tile = (TileEntity) configurable;
        if (guibutton.id == backButton.id) {
            int guiId = Mekanism.proxy.getGuiId(tile.getBlockType());
            Mekanism.packetHandler.sendToServer(new SimpleGuiMessage(Coord4D.get(tile), 0, guiId));
        } else if (guibutton.id == strictInputButton.id) {
            Mekanism.packetHandler.sendToServer(new ConfigurationUpdateMessage(ConfigurationPacket.STRICT_INPUT, Coord4D.get(tile), 0, 0, null));
        } else if (guibutton.id == colorButton.id) {
            Mekanism.packetHandler.sendToServer(new ConfigurationUpdateMessage(ConfigurationPacket.EJECT_COLOR, Coord4D.get(tile), Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? 2 : 0, 0, null));
        } else {
            for (GuiSideDataButton button : sideDataButtons) {
                if (guibutton.id == button.id) {
                    Mekanism.packetHandler.sendToServer(new ConfigurationUpdateMessage(ConfigurationPacket.INPUT_COLOR, Coord4D.get(tile),
                          Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? 2 : 0, button.getSlotPosMapIndex(), null));
                    break;
                }
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String text = LangUtils.localize("gui.configuration.transporter");
        fontRenderer.drawString(text, (xSize / 2) - (fontRenderer.getStringWidth(text) / 2), 5, 0x404040);
        text = LangUtils.localize("gui.strictInput") + " (" + LangUtils.transOnOff(configurable.getEjector().hasStrictInput()) + ")";
        renderScaledText(text, 53, 17, 0x00CD00, 70);
        fontRenderer.drawString(LangUtils.localize("gui.input"), 48, 81, 0x787878);
        fontRenderer.drawString(LangUtils.localize("gui.output"), 114, 68, 0x787878);
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        for (GuiSideDataButton button : sideDataButtons) {
            if (button.isMouseOver()) {
                SideData data = button.getSideData();
                if (data != TileComponentConfig.EMPTY) {
                    EnumColor color = button.getColor();
                    displayTooltip(color != null ? color.getColoredName() : LangUtils.localize("gui.none"), xAxis, yAxis);
                }
                break;
            }
        }
        if (strictInputButton.isMouseOver()) {
            displayTooltip(LangUtils.localize("gui.configuration.strictInput"), xAxis, yAxis);
        } else if (colorButton.isMouseOver()) {
            if (configurable.getEjector().getOutputColor() != null) {
                displayTooltip(configurable.getEjector().getOutputColor().getColoredName(), xAxis, yAxis);
            } else {
                displayTooltip(LangUtils.localize("gui.none"), xAxis, yAxis);
            }
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        if (button == 1) {
            TileEntity tile = (TileEntity) configurable;
            if (colorButton.isMouseOver()) {
                //Allow going backwards
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                Mekanism.packetHandler.sendToServer(new ConfigurationUpdateMessage(ConfigurationPacket.EJECT_COLOR, Coord4D.get(tile), 1, 0, null));
            } else {
                //Handle right clicking the side data buttons
                for (GuiSideDataButton sideDataButton : sideDataButtons) {
                    if (sideDataButton.isMouseOver()) {
                        SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                        Mekanism.packetHandler.sendToServer(new ConfigurationUpdateMessage(ConfigurationPacket.INPUT_COLOR, Coord4D.get(tile), 1, sideDataButton.getSlotPosMapIndex(), null));
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiTransporterConfig.png");
    }
}