package mekanism.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.button.GuiButtonImageMek;
import mekanism.client.gui.element.tab.GuiConfigTypeTab;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.SideData;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.network.PacketConfigurationUpdate.ConfigurationPacket;
import mekanism.common.network.PacketConfigurationUpdate.ConfigurationUpdateMessage;
import mekanism.common.network.PacketSimpleGui.SimpleGuiMessage;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
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
public class GuiSideConfiguration extends GuiMekanismTile<TileEntityContainerBlock> {

    private Map<Integer, GuiPos> slotPosMap = new HashMap<>();
    private ISideConfiguration configurable;
    private TransmissionType currentType;
    private List<GuiConfigTypeTab> configTabs = new ArrayList<>();
    private GuiButtonImageMek backButton;
    private GuiButtonImageMek autoEjectButton;

    public GuiSideConfiguration(EntityPlayer player, ISideConfiguration tile) {
        super((TileEntityContainerBlock) tile, new ContainerNull(player, (TileEntityContainerBlock) tile));
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
        backButton = new GuiButtonImageMek(0, guiLeft + 6, guiTop + 6, 14, 14, 204, 14, -14, getGuiLocation());
        autoEjectButton = new GuiButtonImageMek(1, guiLeft + 156, guiTop + 6, 14, 14, 190, 14, -14, getGuiLocation());
        buttonList.add(backButton);
        buttonList.add(autoEjectButton);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) throws IOException {
        super.actionPerformed(guibutton);
        TileEntity tile = (TileEntity) configurable;
        if (guibutton.id == backButton.id) {
            int guiId = Mekanism.proxy.getGuiId(tile.getBlockType(), tile.getBlockMetadata());
            Mekanism.packetHandler.sendToServer(new SimpleGuiMessage(Coord4D.get(tile), 0, guiId));
        } else if (guibutton.id == autoEjectButton.id) {
            Mekanism.packetHandler.sendToServer(new ConfigurationUpdateMessage(ConfigurationPacket.EJECT, Coord4D.get(tile), 0, 0, currentType));
        }
    }

    private boolean overSide(int xAxis, int yAxis, int x, int y) {
        return xAxis >= x && xAxis <= x + 14 && yAxis >= y && yAxis <= y + 14;
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
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        for (int i = 0; i < slotPosMap.size(); i++) {
            int x = slotPosMap.get(i).xPos;
            int y = slotPosMap.get(i).yPos;
            SideData data = configurable.getConfig().getOutput(currentType, EnumFacing.byIndex(i));
            if (data != TileComponentConfig.EMPTY) {
                boolean doColor = data.color != EnumColor.GREY;
                if (doColor) {
                    MekanismRenderer.color(data.color);
                }
                drawTexturedModalRect(guiLeft + x, guiTop + y, 176, 0, overSide(xAxis, yAxis, x, y), 14);
                if (doColor) {
                    MekanismRenderer.resetColor();
                }
            } else {
                drawTexturedModalRect(guiLeft + x, guiTop + y, 176, 28, 14, 14);
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = currentType.localize() + " " + LangUtils.localize("gui.config");
        fontRenderer.drawString(title, (xSize / 2) - (fontRenderer.getStringWidth(title) / 2), 5, 0x404040);
        if (configurable.getConfig().canEject(currentType)) {
            fontRenderer.drawString(LangUtils.localize("gui.eject") + ": " + (configurable.getConfig().isEjecting(currentType) ? "On" : "Off"), 53, 17, 0x00CD00);
        } else {
            fontRenderer.drawString(LangUtils.localize("gui.noEject"), 53, 17, 0x00CD00);
        }
        fontRenderer.drawString(LangUtils.localize("gui.slots"), 77, 81, 0x787878);
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        for (int i = 0; i < slotPosMap.size(); i++) {
            GuiPos slotPos = slotPosMap.get(i);
            SideData data = configurable.getConfig().getOutput(currentType, EnumFacing.byIndex(i));
            if (data != TileComponentConfig.EMPTY && overSide(xAxis, yAxis, slotPos.xPos, slotPos.yPos)) {
                drawHoveringText(data.color + data.localize() + " (" + data.color.getColoredName() + ")", xAxis, yAxis);
            }
        }
        if (autoEjectButton.isMouseOver()) {
            drawHoveringText(LangUtils.localize("gui.autoEject"), xAxis, yAxis);
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
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        TileEntity tile = (TileEntity) configurable;
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && button == 0) {
            button = 2;
        }
        for (int i = 0; i < slotPosMap.size(); i++) {
            GuiPos slotPos = slotPosMap.get(i);
            if (overSide(xAxis, yAxis, slotPos.xPos, slotPos.yPos)) {
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                Mekanism.packetHandler.sendToServer(new ConfigurationUpdateMessage(ConfigurationPacket.SIDE_DATA, Coord4D.get(tile), button, i, currentType));
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