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
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

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

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(getGuiLocation());
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        int xAxis = (mouseX - (width - xSize) / 2);
        int yAxis = (mouseY - (height - ySize) / 2);
        if (xAxis >= 6 && xAxis <= 20 && yAxis >= 6 && yAxis <= 20) {
            drawTexturedModalRect(guiWidth + 6, guiHeight + 6, 176 + 14, 0, 14, 14);
        } else {
            drawTexturedModalRect(guiWidth + 6, guiHeight + 6, 176 + 14, 14, 14, 14);
        }
        if (xAxis >= 156 && xAxis <= 170 && yAxis >= 6 && yAxis <= 20) {
            drawTexturedModalRect(guiWidth + 156, guiHeight + 6, 176 + 28, 0, 14, 14);
        } else {
            drawTexturedModalRect(guiWidth + 156, guiHeight + 6, 176 + 28, 14, 14, 14);
        }
        for (int i = 0; i < slotPosMap.size(); i++) {
            MekanismRenderer.resetColor();
            int x = slotPosMap.get(i).xPos;
            int y = slotPosMap.get(i).yPos;
            EnumColor color = configurable.getEjector().getInputColor(EnumFacing.byIndex(i));
            if (configurable.getConfig().getOutput(TransmissionType.ITEM, EnumFacing.byIndex(i))
                  != TileComponentConfig.EMPTY) {
                if (color != null) {
                    MekanismRenderer.color(color);
                }
                if (xAxis >= x && xAxis <= x + 14 && yAxis >= y && yAxis <= y + 14) {
                    drawTexturedModalRect(guiWidth + x, guiHeight + y, 176, 0, 14, 14);
                } else {
                    drawTexturedModalRect(guiWidth + x, guiHeight + y, 176, 14, 14, 14);
                }
            } else {
                drawTexturedModalRect(guiWidth + x, guiHeight + y, 176, 28, 14, 14);
            }
        }
        MekanismRenderer.resetColor();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String text = LangUtils.localize("gui.configuration.transporter");
        fontRenderer.drawString(text, (xSize / 2) - (fontRenderer.getStringWidth(text) / 2), 5, 0x404040);
        text = LangUtils.localize("gui.strictInput") + " (" + LangUtils
              .transOnOff(configurable.getEjector().hasStrictInput()) + ")";
        renderScaledText(text, 53, 17, 0x00CD00, 70);
        fontRenderer.drawString(LangUtils.localize("gui.input"), 48, 81, 0x787878);
        fontRenderer.drawString(LangUtils.localize("gui.output"), 114, 68, 0x787878);
        if (configurable.getEjector().getOutputColor() != null) {
            GlStateManager.pushMatrix();
            GL11.glColor4f(1, 1, 1, 1);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            mc.getTextureManager().bindTexture(MekanismRenderer.getBlocksTexture());
            drawTexturedRectFromIcon(122, 49, MekanismRenderer.getColorIcon(configurable.getEjector().getOutputColor()),
                  16, 16);
            GL11.glDisable(GL11.GL_LIGHTING);
            GlStateManager.popMatrix();
        }
        int xAxis = (mouseX - (width - xSize) / 2);
        int yAxis = (mouseY - (height - ySize) / 2);
        for (int i = 0; i < slotPosMap.size(); i++) {
            int x = slotPosMap.get(i).xPos;
            int y = slotPosMap.get(i).yPos;
            EnumColor color = configurable.getEjector().getInputColor(EnumFacing.byIndex(i));
            if (configurable.getConfig().getOutput(TransmissionType.ITEM, EnumFacing.byIndex(i))
                  != TileComponentConfig.EMPTY) {
                if (xAxis >= x && xAxis <= x + 14 && yAxis >= y && yAxis <= y + 14) {
                    drawHoveringText(color != null ? color.getColoredName() : LangUtils.localize("gui.none"), xAxis,
                          yAxis);
                }
            }
        }
        if (xAxis >= 122 && xAxis <= 138 && yAxis >= 49 && yAxis <= 65) {
            if (configurable.getEjector().getOutputColor() != null) {
                drawHoveringText(configurable.getEjector().getOutputColor().getColoredName(), xAxis, yAxis);
            } else {
                drawHoveringText(LangUtils.localize("gui.none"), xAxis, yAxis);
            }
        }
        if (xAxis >= 156 && xAxis <= 170 && yAxis >= 6 && yAxis <= 20) {
            drawHoveringText(LangUtils.localize("gui.configuration.strictInput"), xAxis, yAxis);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        int xAxis = (mouseX - (width - xSize) / 2);
        int yAxis = (mouseY - (height - ySize) / 2);
        TileEntity tile = (TileEntity) configurable;
        if (button == 0) {
            if (xAxis >= 6 && xAxis <= 20 && yAxis >= 6 && yAxis <= 20) {
                int guiId = Mekanism.proxy.getGuiId(tile.getBlockType(), tile.getBlockMetadata());
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                Mekanism.packetHandler.sendToServer(new SimpleGuiMessage(Coord4D.get(tile), 0, guiId));
            }
            if (xAxis >= 156 && xAxis <= 170 && yAxis >= 6 && yAxis <= 20) {
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                Mekanism.packetHandler.sendToServer(
                      new ConfigurationUpdateMessage(ConfigurationPacket.STRICT_INPUT, Coord4D.get(tile), 0, 0, null));
            }
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && button == 0) {
            button = 2;
        }
        if (xAxis >= 122 && xAxis <= 138 && yAxis >= 49 && yAxis <= 65) {
            SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
            Mekanism.packetHandler.sendToServer(
                  new ConfigurationUpdateMessage(ConfigurationPacket.EJECT_COLOR, Coord4D.get(tile), button, 0, null));
        }
        for (int i = 0; i < slotPosMap.size(); i++) {
            int x = slotPosMap.get(i).xPos;
            int y = slotPosMap.get(i).yPos;
            if (xAxis >= x && xAxis <= x + 14 && yAxis >= y && yAxis <= y + 14) {
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                Mekanism.packetHandler.sendToServer(
                      new ConfigurationUpdateMessage(ConfigurationPacket.INPUT_COLOR, Coord4D.get(tile), button, i,
                            null));
            }
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiTransporterConfig.png");
    }
}