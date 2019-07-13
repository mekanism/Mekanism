package mekanism.client.gui;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.GuiSideConfiguration.GuiPos;
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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class GuiTransporterConfig extends GuiMekanismTile<TileEntityContainerBlock> {

    private Map<Integer, GuiPos> slotPosMap = new HashMap<>();
    private ISideConfiguration configurable;

    public GuiTransporterConfig(EntityPlayer player, ISideConfiguration tile) {
        super((TileEntityContainerBlock) tile, new ContainerNull(player, (TileEntityContainerBlock) tile));
        ySize = 95;
        configurable = tile;
        slotPosMap.put(0, new GuiPos(54, 64));
        slotPosMap.put(1, new GuiPos(54, 34));
        slotPosMap.put(2, new GuiPos(54, 49));
        slotPosMap.put(3, new GuiPos(39, 64));
        slotPosMap.put(4, new GuiPos(39, 49));
        slotPosMap.put(5, new GuiPos(69, 49));
    }

    private boolean overColor(int xAxis, int yAxis) {
        return xAxis >= 122 && xAxis <= 138 && yAxis >= 49 && yAxis <= 65;
    }

    private boolean overStrictInput(int xAxis, int yAxis) {
        return xAxis >= 156 && xAxis <= 170 && yAxis >= 6 && yAxis <= 20;
    }

    private boolean overBackButton(int xAxis, int yAxis) {
        return xAxis >= 6 && xAxis <= 20 && yAxis >= 6 && yAxis <= 20;
    }

    private boolean overSide(int xAxis, int yAxis, int x, int y) {
        return xAxis >= x && xAxis <= x + 14 && yAxis >= y && yAxis <= y + 14;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        drawTexturedModalRect(guiLeft + 6, guiTop + 6, 190, 0, overBackButton(xAxis, yAxis), 14);
        drawTexturedModalRect(guiLeft + 156, guiTop + 6, 204, 0, overStrictInput(xAxis, yAxis), 14);
        for (int i = 0; i < slotPosMap.size(); i++) {
            int x = slotPosMap.get(i).xPos;
            int y = slotPosMap.get(i).yPos;
            if (configurable.getConfig().getOutput(TransmissionType.ITEM, EnumFacing.byIndex(i)) != TileComponentConfig.EMPTY) {
                MekanismRenderer.color(configurable.getEjector().getInputColor(EnumFacing.byIndex(i)));
                drawTexturedModalRect(guiLeft + x, guiTop + y, 176, 0, overSide(xAxis, yAxis, x, y), 14);
                MekanismRenderer.resetColor();
            } else {
                drawTexturedModalRect(guiLeft + x, guiTop + y, 176, 28, 14, 14);
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
        drawColorIcon(122, 49, configurable.getEjector().getOutputColor(), 1);
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        for (int i = 0; i < slotPosMap.size(); i++) {
            GuiPos slotPos = slotPosMap.get(i);
            EnumColor color = configurable.getEjector().getInputColor(EnumFacing.byIndex(i));
            SideData data = configurable.getConfig().getOutput(TransmissionType.ITEM, EnumFacing.byIndex(i));
            if (data != TileComponentConfig.EMPTY && overSide(xAxis, yAxis, slotPos.xPos, slotPos.yPos)) {
                drawHoveringText(color != null ? color.getColoredName() : LangUtils.localize("gui.none"), xAxis, yAxis);
            }
        }
        if (overColor(xAxis, yAxis)) {
            if (configurable.getEjector().getOutputColor() != null) {
                drawHoveringText(configurable.getEjector().getOutputColor().getColoredName(), xAxis, yAxis);
            } else {
                drawHoveringText(LangUtils.localize("gui.none"), xAxis, yAxis);
            }
        } else if (overStrictInput(xAxis, yAxis)) {
            drawHoveringText(LangUtils.localize("gui.configuration.strictInput"), xAxis, yAxis);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        TileEntity tile = (TileEntity) configurable;
        if (button == 0) {
            if (overBackButton(xAxis, yAxis)) {
                int guiId = Mekanism.proxy.getGuiId(tile.getBlockType(), tile.getBlockMetadata());
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                Mekanism.packetHandler.sendToServer(new SimpleGuiMessage(Coord4D.get(tile), 0, guiId));
            } else if (overStrictInput(xAxis, yAxis)) {
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                Mekanism.packetHandler.sendToServer(new ConfigurationUpdateMessage(ConfigurationPacket.STRICT_INPUT, Coord4D.get(tile), 0, 0, null));
            }
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && button == 0) {
            button = 2;
        }
        if (overColor(xAxis, yAxis)) {
            SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
            Mekanism.packetHandler.sendToServer(new ConfigurationUpdateMessage(ConfigurationPacket.EJECT_COLOR, Coord4D.get(tile), button, 0, null));
        }
        for (int i = 0; i < slotPosMap.size(); i++) {
            GuiPos slotPos = slotPosMap.get(i);
            if (overSide(xAxis, yAxis, slotPos.xPos, slotPos.yPos)) {
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                Mekanism.packetHandler.sendToServer(new ConfigurationUpdateMessage(ConfigurationPacket.INPUT_COLOR, Coord4D.get(tile), button, i, null));
            }
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiTransporterConfig.png");
    }
}