package mekanism.client.gui.robit;

import java.io.IOException;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.entity.EntityRobit;
import mekanism.common.network.PacketRobit;
import mekanism.common.network.PacketRobit.RobitMessage;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public abstract class GuiRobit extends GuiMekanism {

    protected final EntityRobit robit;

    protected GuiRobit(EntityRobit robit, Container container) {
        super(container);
        this.robit = robit;
        xSize += 25;
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, getBackgroundImage());
    }

    protected abstract String getBackgroundImage();

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
        boolean correctX = xAxis >= 179 && xAxis <= 197;
        drawRect(correctX, yAxis, guiWidth, guiHeight, 10, 0);
        drawRect(correctX, yAxis, guiWidth, guiHeight, 30, 36);
        drawRect(correctX, yAxis, guiWidth, guiHeight, 50, 72);
        drawRect(correctX, yAxis, guiWidth, guiHeight, 70, 108);
        drawRect(correctX, yAxis, guiWidth, guiHeight, 90, 144);
    }

    private void drawRect(boolean correctX, int yAxis, int guiWidth, int guiHeight, int heightBonus, int textureY) {
        int yBonus = correctX && yAxis >= heightBonus && yAxis <= heightBonus + 18 ? 0 : 18;
        drawTexturedModalRect(guiWidth + 179, guiHeight + heightBonus, 201, textureY + yBonus, 18, 18);
    }

    private void buttonClicked(int id) {
        SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
        if (openGui(id)) {
            Mekanism.packetHandler
                  .sendToServer(new RobitMessage(PacketRobit.RobitPacketType.GUI, id, robit.getEntityId(), null));
            mc.player.openGui(Mekanism.instance, 21 + id, mc.world, robit.getEntityId(), 0, 0);
        }
    }

    protected abstract boolean openGui(int id);

    protected void extraClickListeners(int mouseX, int mouseY, int button) {
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        extraClickListeners(mouseX, mouseY, button);
        int xAxis = (mouseX - (width - xSize) / 2);
        if (button == 0 && xAxis >= 179 && xAxis <= 197) {
            int yAxis = (mouseY - (height - ySize) / 2);
            if (yAxis >= 10 && yAxis <= 28) {
                buttonClicked(0);
            } else if (yAxis >= 30 && yAxis <= 48) {
                buttonClicked(1);
            } else if (yAxis >= 50 && yAxis <= 68) {
                buttonClicked(2);
            } else if (yAxis >= 70 && yAxis <= 88) {
                buttonClicked(3);
            } else if (yAxis >= 90 && yAxis <= 108) {
                buttonClicked(4);
            }
        }
    }
}