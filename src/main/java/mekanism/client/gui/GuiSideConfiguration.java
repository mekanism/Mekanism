package mekanism.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mekanism.api.Coord4D;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.button.GuiButtonDisableableImage;
import mekanism.client.gui.button.GuiSideDataButton;
import mekanism.client.gui.element.tab.GuiConfigTypeTab;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.SideData;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.network.PacketConfigurationUpdate;
import mekanism.common.network.PacketConfigurationUpdate.ConfigurationPacket;
import mekanism.common.network.PacketSimpleGui;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.input.Keyboard;

@OnlyIn(Dist.CLIENT)
public class GuiSideConfiguration extends GuiMekanismTile<TileEntityMekanism> {

    private Map<Integer, GuiPos> slotPosMap = new HashMap<>();
    private ISideConfiguration configurable;
    private TransmissionType currentType;
    private List<GuiConfigTypeTab> configTabs = new ArrayList<>();
    private List<GuiSideDataButton> sideDataButtons = new ArrayList<>();
    private Button backButton;
    private Button autoEjectButton;
    private int buttonID = 0;

    public GuiSideConfiguration(PlayerEntity player, ISideConfiguration tile) {
        super((TileEntityMekanism) tile, new ContainerNull(player, (TileEntityMekanism) tile));
        ySize = 95;
        configurable = tile;
        ResourceLocation resource = getGuiLocation();
        for (TransmissionType type : configurable.getConfig().getTransmissions()) {
            GuiConfigTypeTab tab = new GuiConfigTypeTab(this, type, resource);
            addGuiElement(tab);
            configTabs.add(tab);
        }
        currentType = getTopTransmission();
        updateTabs();
        slotPosMap.put(0, new GuiPos(81, 64));
        slotPosMap.put(1, new GuiPos(81, 34));
        slotPosMap.put(2, new GuiPos(81, 49));
        slotPosMap.put(3, new GuiPos(66, 64));
        slotPosMap.put(4, new GuiPos(66, 49));
        slotPosMap.put(5, new GuiPos(96, 49));
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.clear();
        buttonList.add(backButton = new GuiButtonDisableableImage(buttonID++, guiLeft + 6, guiTop + 6, 14, 14, 204, 14, -14, getGuiLocation()));
        buttonList.add(autoEjectButton = new GuiButtonDisableableImage(buttonID++, guiLeft + 156, guiTop + 6, 14, 14, 190, 14, -14, getGuiLocation()));
        for (int i = 0; i < slotPosMap.size(); i++) {
            GuiPos guiPos = slotPosMap.get(i);
            Direction facing = Direction.byIndex(i);
            GuiSideDataButton button = new GuiSideDataButton(buttonID++, guiLeft + guiPos.xPos, guiTop + guiPos.yPos, getGuiLocation(), i,
                  () -> configurable.getConfig().getOutput(currentType, facing), () -> configurable.getConfig().getOutput(currentType, facing).color);
            buttonList.add(button);
            sideDataButtons.add(button);
        }
    }

    @Override
    protected void actionPerformed(Button guibutton) throws IOException {
        super.actionPerformed(guibutton);
        TileEntity tile = (TileEntity) configurable;
        if (guibutton.id == backButton.id) {
            int guiId = Mekanism.proxy.getGuiId(tile.getBlockType());
            Mekanism.packetHandler.sendToServer(new PacketSimpleGui(Coord4D.get(tile), 0, guiId));
        } else if (guibutton.id == autoEjectButton.id) {
            Mekanism.packetHandler.sendToServer(new PacketConfigurationUpdate(ConfigurationPacket.EJECT, Coord4D.get(tile), 0, 0, currentType));
        } else {
            for (GuiSideDataButton button : sideDataButtons) {
                if (guibutton.id == button.id) {
                    Mekanism.packetHandler.sendToServer(new PacketConfigurationUpdate(ConfigurationPacket.SIDE_DATA, Coord4D.get(tile),
                          Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) ? 2 : 0, button.getSlotPosMapIndex(), currentType));
                    break;
                }
            }
        }
    }

    public TransmissionType getTopTransmission() {
        return configurable.getConfig().getTransmissions().get(0);
    }

    public void setCurrentType(TransmissionType type) {
        currentType = type;
    }

    public void updateTabs() {
        int rendered = 0;
        for (GuiConfigTypeTab tab : configTabs) {
            tab.setVisible(currentType != tab.getTransmissionType());
            if (tab.isVisible()) {
                tab.setLeft(rendered >= 0 && rendered <= 2);
                tab.setY(2 + ((rendered % 3) * (26 + 2)));
            }
            rendered++;
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = currentType.localize() + " " + LangUtils.localize("gui.config");
        font.drawString(title, (xSize / 2) - (font.getStringWidth(title) / 2), 5, 0x404040);
        if (configurable.getConfig().canEject(currentType)) {
            font.drawString(LangUtils.localize("gui.eject") + ": " + (configurable.getConfig().isEjecting(currentType) ? "On" : "Off"), 53, 17, 0x00CD00);
        } else {
            font.drawString(LangUtils.localize("gui.noEject"), 53, 17, 0x00CD00);
        }
        font.drawString(LangUtils.localize("gui.slots"), 77, 81, 0x787878);
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        for (GuiSideDataButton button : sideDataButtons) {
            if (button.isMouseOver()) {
                SideData data = button.getSideData();
                if (data != TileComponentConfig.EMPTY) {
                    displayTooltip(data.color + data.localize() + " (" + data.color.getColoredName() + ")", xAxis, yAxis);
                }
                break;
            }
        }
        if (autoEjectButton.isMouseOver()) {
            displayTooltip(LangUtils.localize("gui.autoEject"), xAxis, yAxis);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        TileEntity tile = (TileEntity) configurable;
        if (tile == null || mc.world.getTileEntity(tile.getPos()) == null) {
            mc.displayGuiScreen(null);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        if (button == 1) {
            //Handle right clicking the side data buttons
            for (GuiSideDataButton sideDataButton : sideDataButtons) {
                if (sideDataButton.isMouseOver()) {
                    SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                    Mekanism.packetHandler.sendToServer(new PacketConfigurationUpdate(ConfigurationPacket.SIDE_DATA, Coord4D.get((TileEntity) configurable), 1, sideDataButton.getSlotPosMapIndex(), currentType));
                    break;
                }
            }
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiConfiguration.png");
    }

    public static class GuiPos {

        public final int xPos;
        public final int yPos;

        public GuiPos(int x, int y) {
            xPos = x;
            yPos = y;
        }
    }
}